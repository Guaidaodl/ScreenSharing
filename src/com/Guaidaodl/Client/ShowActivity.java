package com.Guaidaodl.Client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

/**
 * Created by Zoro_x on 14-4-25.
 */
public class ShowActivity extends Activity implements View.OnTouchListener{
    public static final String MESSAGE_KEY = "BYTES";

    public static final int MODE_DRAG = 1;
    public static final int MODE_NORMAL = 2;

    private byte[] mImageBytes;
    private int mMode;
    private long mPressStartTime;
    private long mPressEndTime;
    private float mPreX;
    private float mPreY;
    private final long LONG_PRESS_TIME = 1500;
    //与接受线程交互用的handler
    ClientRunnable mClientRunnable;
    private Thread mSenderThread;
    private Handler mHandler;

    private MyService mService;
    private boolean mBound = false;

    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMode = MODE_NORMAL;
		/*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show);

        mImageView = (ImageView) findViewById(R.id.show);
        mImageView.setOnTouchListener(this);
        //与接受线程交互用的handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x1234) {
                    mImageBytes = msg.getData().getByteArray(MESSAGE_KEY);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(mImageBytes, 0, mImageBytes.length);
                    mImageView.setImageBitmap(bitmap);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //绑定服务
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mClientRunnable == null)
            return false;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mPreX - motionEvent.getX()) > 30 && Math.abs(mPreY - motionEvent.getY()) > 30)
                    mMode = MODE_DRAG;
                break;
            case MotionEvent.ACTION_DOWN:
                mPressStartTime = System.currentTimeMillis();
                mPreX = motionEvent.getX();
                mPreY = motionEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                mPressEndTime = System.currentTimeMillis();
                if (mMode == MODE_DRAG)
                    mMode = MODE_NORMAL;
                    //点击，模拟左键点击
                else{
                    final double xP = motionEvent.getX() / view.getWidth();
                    final double yP = motionEvent.getY() / view.getHeight();
                    mClientRunnable.send(3);
                    mClientRunnable.send(xP);
                    mClientRunnable.send(yP);
                    Log.i("CLIENT", (mPressEndTime - mPressStartTime)+ " asdf");
                    if (mPressEndTime - mPressStartTime <= LONG_PRESS_TIME) {
                        mClientRunnable.send(1);
                    }
                    else {
                        mClientRunnable.send(2);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        mSenderThread.interrupt();
        //断开服务
        if (mBound)
            unbindService(mConnection);
        setResult(0);
        super.onStop();

    }

    /**
     * 启动新的线程用来接收图片
     *
     * @param h         handler
     */
    public void startThread(Handler h) {
        String userName = getIntent().getStringExtra(StartActivity.USER_NAME);
        mClientRunnable = new ClientRunnable(h, mService.getSocket(), userName);
        mSenderThread = new Thread(mClientRunnable);
        mClientRunnable.setDealer(new ExceptionDealer() {
            @Override
            public void deal() {
                finish();
            }
        });
        mSenderThread.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mClientRunnable.send(2);
                mClientRunnable.send(mImageView.getWidth());
                mClientRunnable.send(mImageView.getHeight());
                Log.i("CLIENT", "Send " + mImageView.getWidth() + " " + mImageView.getHeight());
            }
        }, 1000);
    }

    //为SocketService定义回调连接
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.SocketBinder binder = (MyService.SocketBinder) iBinder;
            mService = binder.getService();
            mBound = true;

            startThread(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
}
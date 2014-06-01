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

import java.nio.*;

/**
 * Created by Zoro_x on 14-4-25.
 */
public class ShowActivity extends Activity implements View.OnTouchListener{

    public static final String MESSAGE_KEY = "BYTES";
    public static final String USER_NAME = "user_name";
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

    private ConnectService mService;
    private boolean mBound = false;

    private ImageView mImageView;

    public static Intent getShowIntent(Context context, String userName) {
        Intent intent = new Intent(context, ShowActivity.class);
        intent.putExtra(USER_NAME, userName);

        return intent;
    }
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
                if (msg.what == 0x250) {
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //绑定服务
        Intent intent = new Intent(this, ConnectService.class);
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
                    Log.i("CLIENT", (mPressEndTime - mPressStartTime)+ " asdf");
                    if (mPressEndTime - mPressStartTime <= LONG_PRESS_TIME) {
                        sendLeftClick(xP, yP);
                    }
                    else {
                        sendRightClick(xP, yP);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }
    public void sendLeftClick(double xP, double yP) {
        byte []commandBuffer = new byte[24];  //int+double+double+int  = 4 + 8 + 8 + 4= 24
        ByteBuffer.wrap(commandBuffer, 0, 4).putInt(3);
        ByteBuffer.wrap(commandBuffer, 4, 8).putDouble(xP);
        ByteBuffer.wrap(commandBuffer, 12, 8).putDouble(yP);
        ByteBuffer.wrap(commandBuffer, 20, 4).putInt(1);
        mClientRunnable.send(commandBuffer);
    }
    public void sendRightClick(double xP, double yP) {
        byte []commandBuffer = new byte[24];  //int+double+double+int  = 4 + 8 + 8 + 4= 24
        ByteBuffer.wrap(commandBuffer, 0, 4).putInt(3);
        ByteBuffer.wrap(commandBuffer, 4, 8).putDouble(xP);
        ByteBuffer.wrap(commandBuffer, 12, 8).putDouble(yP);
        ByteBuffer.wrap(commandBuffer, 20, 4).putInt(2);
        mClientRunnable.send(commandBuffer);
    }
    @Override
    protected void onStop() {
        mSenderThread.interrupt();
        //断开服务
        if (mBound) {
            unbindService(mConnection);
        }
        setResult(0);
        super.onStop();
        finish();
    }

    /**
     * 启动新的线程用来接收图片
     */
    public void startThread(Handler h) {
        String userName = getIntent().getStringExtra(USER_NAME);
        mClientRunnable = new ClientRunnable(h, mService.getSocket(), userName);
        mSenderThread = new Thread(mClientRunnable);
        mSenderThread.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                byte []buffer = new byte[4+4+4];
                ByteBuffer.wrap(buffer, 0, 4).putInt(2);
                ByteBuffer.wrap(buffer, 4, 4).putInt(mImageView.getWidth());
                ByteBuffer.wrap(buffer, 8, 4).putInt(mImageView.getHeight());
                mClientRunnable.send(buffer);
                Log.i("CLIENT", "Send " + mImageView.getWidth() + " " + mImageView.getHeight());
            }
        }, 1000);
    }

    //为SocketService定义回调连接
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ConnectService.SocketBinder binder = (ConnectService.SocketBinder) iBinder;
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
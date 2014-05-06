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
import android.view.Window;
import android.widget.ImageView;

/**
 * Created by Zoro_x on 14-4-25.
 */
public class ShowActivity extends Activity {
    public static String MESSAGE_KEY = "BYTES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show);

        final ImageView imageView = (ImageView) findViewById(R.id.show);
        //与接受线程交互用的handler
        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x1234) {
                    imageBytes = msg.getData().getByteArray(MESSAGE_KEY);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imageView.setImageBitmap(bitmap);
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
    protected void onStop() {
        t.interrupt();
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
        r = new ClientRunnable(h, mService.getSocket(), getIntent().getStringExtra(StartActivity.USER_NAME));
        t = new Thread(r);
        r.setDealer(new ExceptionDealer() {
            @Override
            public void deal() {
                finish();
            }
        });
        t.start();
    }


    //与接受线程交互用的handler
    ClientRunnable r;
    private Thread t;
    private Handler h;

    private MyService mService;
    private boolean mBound = false;
    //为SocketService定义回调连接
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.SocketBinder binder = (MyService.SocketBinder) iBinder;
            mService = binder.getService();
            mBound = true;

            startThread(h);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
    private byte[] imageBytes;
}
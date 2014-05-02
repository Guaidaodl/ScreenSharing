package com.Guaidaodl.Client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Zoro_x on 14-4-28.
 */
public class MyService extends Service {
    public static final int MESSAGE_CONNECT_ERROR = 0x1001;
    public static final int MESSAGE_CONNECT_SUCCESS = 0x1002;
    public static final int MESSAGE_CONNECT_TIMEOUT = 0x1003;

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("CLIENT","创建服务");
    }

    public class SocketBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    /**
     * 连接到服务器
     * @param ipAddress 服务器的ip地址
     * @param PORT 端口
     */
    public void connectServer(String ipAddress, int PORT) {
        if(s != null && s.isConnected()) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        s = new Socket();
        Thread t = new Thread(new ConnectRunnable(handler, ipAddress, PORT, s));
        t.start();
    }

    public Socket getSocket() {
        return s;
    }
    /**
     * 设置跟StartActivity交互的Handler
     *
     * @param h Handler
     */
    public void setStartHandler(Handler h) {
        startHandler = h;
    }

    /**
     * 用于跟连接线程进行交互
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_CONNECT_ERROR)
                finishConnect(false);
            else if (msg.what == MESSAGE_CONNECT_SUCCESS)
                finishConnect(true);
            else if (msg.what == MESSAGE_CONNECT_TIMEOUT)
                finishConnect(false);
        }
    };

    /**
     * 结束连接后给 StartActivity 发送信息
     * @param isSuccessful
     */
    private void finishConnect(boolean isSuccessful) {
        if (isSuccessful)
            startHandler.sendEmptyMessage(StartActivity.MESSAGE_SUCCESS);
        else
            startHandler.sendEmptyMessage(StartActivity.MESSAGE_FAIL);

    }

    // 用于跟StartActivity进行交互
    private Handler startHandler;
    private Socket s;
    private final IBinder mBinder = new SocketBinder();
}
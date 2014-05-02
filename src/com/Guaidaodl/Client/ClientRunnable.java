package com.Guaidaodl.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ClientRunnable implements Runnable {
    public static final String CLIENT_TAG = "CLIENT";
    public static final int PORT = 8198;

    public ClientRunnable(Handler h, Socket s) {
        handler = h;
        socket = s;
    }

    public void setDealer(ExceptionDealer dealer) {
        this.dealer = dealer;
    }

    @Override
    public void run() {
        try {
            in = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();
            out = new PrintWriter(outStream, true);

            //获取图片
            getImage();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            for (int i = 0; i < 10; i++)
                Log.i(CLIENT_TAG, "连接超时");
            dealer.deal();
        } catch (IOException e) {
            Log.i(CLIENT_TAG, "连接失败");
            dealer.deal();
        }

    }

    public void getImage() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int size = getSize(in);
                buffer = new byte[size + 200];
                int count = 0;
                while (count < size) {
                    int n = in.read(buffer, count, size - count);
                    if (n == -1)
                        break;
                    count += n;
                }

                sendMessage();
                Log.i(CLIENT_TAG, "size of picture is " + size);
                out.println("aaa");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(CLIENT_TAG, "发生异常");
            dealer.deal();
        }
    }

    //获取图片大小
    public int getSize(InputStream in) {
        byte[] buffer = new byte[4];
        try {
            int hasRead = 0;
            final int byteSize = 4;
            while (hasRead < byteSize) {
                hasRead += in.read(buffer, hasRead, byteSize - hasRead);
            }
        } catch (Exception e) {
            dealer.deal();
        }

        return b2i(buffer);

    }

    //从字节到整数
    public int b2i(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * 通过handler发送数据
     */
    public void sendMessage() {
        Bundle bundle = new Bundle();
        bundle.putByteArray(ShowActivity.MESSAGE_KEY, buffer);

        Message msg = new Message();
        msg.what = 0x1234;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private InputStream in;
    private PrintWriter out;
    private Socket socket;
    private Handler handler;
    private byte[] buffer;

    private ExceptionDealer dealer;
}
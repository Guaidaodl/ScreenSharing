package com.Guaidaodl.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ClientRunnable implements Runnable {
    public static final String CLIENT_TAG = "CLIENT";
    public ClientRunnable(Handler h, Socket s, String n) {
        handler = h;
        socket = s;
        userName = n;
    }

    @Override
    public void run() {
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            //发送用户名
            sendUserName();
            send(0);
            //获取图片
            getImage();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            for (int i = 0; i < 10; i++)
                Log.i(CLIENT_TAG, "连接超时");
           sendMessage(0x250);
        } catch (IOException e) {
            Log.i(CLIENT_TAG, "连接失败");
           sendMessage(0x250);
        }
    }

    public void getImage() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            int size = getSize(in);
            bufferImage = new byte[size + 200];
            int count = 0;
            while (count < size) {
                int n = in.read(bufferImage, count, size - count);
                if (n == -1)
                    break;
                count += n;
            }
            sendMessage(0x1234);
            //发送ACK
            send(0);
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
           sendMessage(0x250);
        }

        return TypeConvert.b2i(buffer);
    }

    public void sendUserName() throws IOException{
        byte[] n = userName.getBytes();
        int length = n.length + 8;
        byte[] buffer = new byte[length];
        ByteBuffer.wrap(buffer, 0, 4).putInt(1);
        ByteBuffer.wrap(buffer, 4, 4).putInt(n.length);
        ByteBuffer.wrap(buffer, 8, n.length).put(n);

        send(buffer);
    }

    /**
     * 通过handler发送数据
     */
    public void sendMessage(int messageWhat) {
        Message msg = new Message();
        msg.what = messageWhat;
        if (messageWhat == 0x1234) {
            Bundle bundle = new Bundle();
            bundle.putByteArray(ShowActivity.MESSAGE_KEY, bufferImage);
            msg.setData(bundle);
        }        
        handler.sendMessage(msg);
    }

    /**发送byte[] 数组，线程安全。
     */
    private synchronized boolean sendBytes(byte[] b) {
        if (out == null)
            return false;
        boolean send = false;
        try {
            out.write(b);
            send = true;
        } catch (IOException e) {
           sendMessage(0x250);
        } catch (Exception e) {
           sendMessage(0x250);
        } finally {
            return send;
        }
    }
    public boolean send(double d) {
        return sendBytes(TypeConvert.d2b(d));
    }
    public boolean send(int i) {
        return sendBytes(TypeConvert.i2b(i));
    }
    public boolean send(byte b) {
        return sendBytes(new byte[] {b});
    }
    public boolean send(byte[] b) {
        return sendBytes(b);
    }

    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private Handler handler;

    private byte[] bufferImage;

    private String userName;
}
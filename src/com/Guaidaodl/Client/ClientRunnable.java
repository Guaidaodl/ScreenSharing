package com.Guaidaodl.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ClientRunnable implements Runnable {
    public static final String CLIENT_TAG = "CLIENT";
    public static final int SOCKET_NO_ACK = 0;
    public static final int SOCKET_NO_USER_NAME = 1;
    public static final int PORT = 8198;

    public ClientRunnable(Handler h, Socket s, String n) {
        handler = h;
        socket = s;
        userName = n;
    }

    public void setDealer(ExceptionDealer dealer) {
        this.dealer = dealer;
    }

    @Override
    public void run() {
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            //发送用户名
            sendUserName();
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
            sendMessage();
            out.write(i2b(SOCKET_NO_ACK));
        }
    }

    public void sendUserName() throws IOException{
        out.write(i2b(1));

        byte[] n = userName.getBytes();

        out.write(i2b(n.length));
        out.write(n);
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
     * 将整数转换成对应的二进制串
     *
     * @param i 要转换的整数
     * @return 返回转换完成的byte数组
     */
    public static byte[] i2b(int i) {
        return new byte[]{
                (byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }

    /**
     * 通过handler发送数据
     */
    public void sendMessage() {
        Bundle bundle = new Bundle();
        bundle.putByteArray(ShowActivity.MESSAGE_KEY, bufferImage);

        Message msg = new Message();
        msg.what = 0x1234;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private Handler handler;

    private byte[] bufferImage;

    private String userName;
    private ExceptionDealer dealer;
}
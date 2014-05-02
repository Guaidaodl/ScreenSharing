package com.Guaidaodl.Client;

import android.os.Handler;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by Zoro_x on 14-4-28.
 */
public class ConnectRunnable implements Runnable {
    public ConnectRunnable(Handler h, String ipAddress, int port, Socket s) {
        this.h = h;
        this.ipAddress = ipAddress;
        this.port = port;
        this.s = s;
    }

    @Override
    public void run() {
        try {
            s.connect(new InetSocketAddress(ipAddress, port), 6000);
            h.sendEmptyMessage(MyService.MESSAGE_CONNECT_SUCCESS);
        } catch (SocketTimeoutException e) {
            h.sendEmptyMessage(MyService.MESSAGE_CONNECT_ERROR);
        } catch (Exception e) {
            h.sendEmptyMessage(MyService.MESSAGE_CONNECT_ERROR);
        }
    }

    private Handler h;
    private String ipAddress;
    private int port;

    private Socket s;
}

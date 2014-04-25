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

public class ClientRunnable implements Runnable
{
	public static final String CLIENT_TAG = "CLIENT";
	public ClientRunnable(Handler h, String ip)
	{
		handler = h;
		ipAddress = ip;
	}

	@Override
	public void run()
	{
		try
		{
			s = new Socket(ipAddress, 8198);
			s.setSoTimeout(6000);
			in = s.getInputStream();
			OutputStream outStream = s.getOutputStream();
			out = new PrintWriter(outStream, true);

			//获取图片
			getIamge();
		}
		catch (SocketTimeoutException e)
		{
			e.printStackTrace();
			for(int i = 0; i < 10; i++)
				Log.i(CLIENT_TAG, "连接超时");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void getIamge()
	{
		try
		{
			while (s.isConnected())
			{
				int size = getSize(in);
				buffer = new byte[size+200];
				int count = 0;
				while(count < size)
				{
					int n = in.read(buffer, count, size - count);
					if (n == -1)
						break;
					count+=n;
				}

				sendMessage();
				Log.i(CLIENT_TAG, "size of picture is " + size);
				out.println("aaa");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	//获取图片大小
	public int getSize(InputStream in)
	{
		byte []buffer = new byte[4];
		try
		{
			int hasRead = 0;
			final int byteSize = 4;
			while(hasRead < byteSize)
			{
				hasRead += in.read(buffer, hasRead, byteSize - hasRead);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
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
	public void sendMessage()
	{
		Bundle bundle = new Bundle();
		bundle.putByteArray(ShowActivity.MESSAGE_KEY, buffer);
		
		Message msg = new Message();
		msg.what = 0x1234;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}
	
	private String ipAddress;
	private InputStream in;
	private PrintWriter out;
	private Socket s;
	private Handler handler;
	private byte[] buffer;
}
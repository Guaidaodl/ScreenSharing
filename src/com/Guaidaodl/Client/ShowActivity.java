package com.Guaidaodl.Client;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * Created by Zoro_x on 14-4-25.
 */
public class ShowActivity extends Activity
{
	public static String MESSAGE_KEY = "BYTES";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);


		final ImageView imageView = (ImageView) findViewById(R.id.show);
		final Handler h = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if (msg.what == 0x1234)
				{
					imageBytes = msg.getData().getByteArray(MESSAGE_KEY);
					Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
					imageView.setImageBitmap(bitmap);
				}
			}
		};

		//获取IP地址
		Intent intent = getIntent();
		String ipAdress = intent.getStringExtra(StartActivity.MESSAGE);
		Runnable r = new ClientRunnable(h, ipAdress);
		Thread t = new Thread(r);
		t.start();
	}

	private byte[] imageBytes;
}
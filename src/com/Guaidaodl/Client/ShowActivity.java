package com.Guaidaodl.Client;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
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

		/*set it to be no title*/
		requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		String ipAddress = intent.getStringExtra(StartActivity.MESSAGE);
		startThread(h, ipAddress);
	}

	@Override
	protected void onStop()
	{
		t.interrupt();

		super.onStop();

	}

	/**
	 * 启动新的线程用来接收图片
	 * @param h handler
	 * @param ipAddress 服务器ip地址
	 */
	public void startThread(Handler h, String ipAddress)
	{
		r = new ClientRunnable(h, ipAddress);
		t = new Thread(r);
		r.setDealer(new ExceptionDealer()
		{
			@Override
			public void deal()
			{
				finish();
			}
		});
		t.start();
	}

	ClientRunnable r;
	private Thread t;
	private byte[] imageBytes;
}
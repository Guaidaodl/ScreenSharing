package com.Guaidaodl.Client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartActivity extends Activity
{
	public static final String MESSAGE = "IPADRESS";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final EditText t = (EditText)findViewById(R.id.ipText);
		final Button bn = (Button)findViewById(R.id.connect);

		bn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Pattern p = Pattern.compile(IPRegEx);
				String ipAdress = t.getText().toString();
				Matcher m = p.matcher(ipAdress);
				if(m.find()) {
					//启动界面进行通信
					Intent intent = new Intent(StartActivity.this, ShowActivity.class);
					intent.putExtra(MESSAGE, ipAdress);

					startActivity(intent);
				}
				else {
					Log.i("xxx", "格式错误");
				}
			}
		});
	}

	//ip地址的正则表达式
	private final String IPRegEx = "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$";
}

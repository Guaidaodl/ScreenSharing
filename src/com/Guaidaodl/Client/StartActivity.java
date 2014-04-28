package com.Guaidaodl.Client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartActivity extends Activity
{
	public static final String MESSAGE = "SOCKET";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*set it to be no title*/
		requestWindowFeature(Window.FEATURE_NO_TITLE);


		setContentView(R.layout.main);

		final EditText t = (EditText)findViewById(R.id.ipText);
		final Button bn = (Button)findViewById(R.id.connect);

		bn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Pattern p = Pattern.compile(IPRegEx);
				String ipAddress = t.getText().toString();
				Matcher m = p.matcher(ipAddress);
				if(m.find()) {
					//启动界面进行通信
					Intent intent = new Intent(StartActivity.this, ShowActivity.class);

					intent.putExtra(MESSAGE, ipAddress);
					startActivity(intent);
				}
				else {
					ShowMessage.displayMessage(StartActivity.this, "请输入正确的 IP 地址");
				}
			}
		});
	}

	//ip地址的正则表达式
	private final String IPRegEx = "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$";
}

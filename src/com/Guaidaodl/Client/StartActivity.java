package com.Guaidaodl.Client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartActivity extends Activity {
    //常量
    public static final String USER_NAME = "user_name";
    public static final int MESSAGE_SUCCESS = 0x1000;
    public static final int MESSAGE_FAIL = 0x1001;
    public static final int PORT = 8198;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        ipEditText = (EditText) findViewById(R.id.ipText);
        userNameEditText = (EditText) findViewById(R.id.userName);
        pd = new ProgressDialog(this);
        pd.setTitle("请耐心等待");
        pd.setMessage("连接中");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        final Button bn = (Button) findViewById(R.id.connect);

        bn.setOnClickListener(listener);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //绑定服务
        serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            mBound = false;
            unbindService(mConnection);
        }

    }
    /**
     * 结束播放时
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ShowMessage.displayMessage(getApplicationContext(), "传输结束");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        mService.disconnect();
    }

    /**
     * 连接按钮的监听者，负责验证输入格式与尝试连接网络
     */
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            if (!mBound) {
                ShowMessage.displayMessage(StartActivity.this, "未启动相关服务，无法连接网络");
                return;
            }
            //验证ip格式是否正确
            Pattern p = Pattern.compile(IPRegEx);
            final String ipAddress = ipEditText.getText().toString();
            Matcher m = p.matcher(ipAddress);
            String userName = userNameEditText.getText().toString();
            if (m.find() && !userName.isEmpty()) {
                pd.show();
                mService.connectServer(ipAddress, PORT);
            } else if (userName.isEmpty()) {
                ShowMessage.displayMessage(StartActivity.this, "用户名为空");
            } else {
                ShowMessage.displayMessage(StartActivity.this, "请输入正确的 IP 地址");
            }
        }
    };


    //为SocketService定义回调连接
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.SocketBinder binder = (MyService.SocketBinder) iBinder;
            mService = binder.getService();
            mService.setStartHandler(handler);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    //定义Handle，用于跟Service交互
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
            //连接成功，启动新的显示界面
            if (msg.what == MESSAGE_SUCCESS) {
                Intent intent = new Intent(StartActivity.this, ShowActivity.class);
                intent.putExtra(USER_NAME, userNameEditText.getText().toString());
                startActivityForResult(intent, 0);
            } else if (msg.what == MESSAGE_FAIL) {
                ShowMessage.displayMessage(getApplicationContext(), "连接失败");
            }
        }
    };
    //组件
    private EditText ipEditText;
    private EditText userNameEditText;

    private ProgressDialog pd = null;
    //是否和SocketService绑定
    private boolean mBound = false;
    private MyService mService;
    private Intent serviceIntent;

    //ip地址的正则表达式
    private final String IPRegEx = "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$";
}

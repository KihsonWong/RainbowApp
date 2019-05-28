package com.rainbow.user;

import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import java.net.UnknownHostException;

import static java.lang.System.currentTimeMillis;


public class MainActivity extends Activity {

    public static EditText edtGateId = null;
    private long mExitTime;

    private Button btnTest;

    //private String ip = "176.122.178.169";                         //IP地址
    private String ip = "192.168.1.107";
    private String port = "6000";

    public static ConnThread connCloudThread = null;
    //public static ConnThread connGatewayThread = null;
    public final static int SHOW_INFO_MAINACTIVITY = 0;
    public final static int START_REC_THREAD = 1;
    public final static int START_SEND_THREAD = 2;
    public final static int START_NEW_ACTIVITY =3;
    public final static int CLIENT_STATE_CORRECT_READ = 7;
    public final static int CLIENT_STATE_CORRECT_WRITE = 8;               //正常通信信息
    public final static int CLIENT_STATE_ERROR = 9;                 //发生错误异常信息
    public final static int CLIENT_STATE_IOFO = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtGateId = findViewById(R.id.id_gateway);
        btnTest = findViewById(R.id.id_btn_test);

        Button btn_con_gateway = findViewById(R.id.id_btn_con_gateway);
        btn_con_gateway.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtGateId.getText().toString().replaceAll(" ", "").equals("")) {
                    showInfo("请先输入网关ID");
                    return;
                }

                if (connCloudThread == null) {
                    connCloudThread = new ConnThread(cli_handler);

                    connCloudThread.setSeverIpAddress(ip);
                    connCloudThread.setServerPort(port);

                    connCloudThread.start();
                }
            }
        });

        Button btn_gateway_in = findViewById(R.id.id_btn_gateway_in);
        btn_gateway_in.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        GatewayInActivity.class);
                startActivityForResult(intent, 1000);
                //startActivity(intent);
            }
        });

        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connCloudThread != null) {
                    if (connCloudThread.isAlive())
                        showInfo("Alive");
                    else
                        showInfo("No alive");
                } else {
                    showInfo("Thread disconnected");
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == 1000 && resultCode == 1001){

            String returnId = data.getStringExtra("returnId");

            edtGateId.setText(returnId);
        }
    }

    @Override
    public void onBackPressed(){
        if ((currentTimeMillis() - mExitTime) > 2000) {
            mExitTime = currentTimeMillis();
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
        } else {
            this.finish();
        }
    }

    //客户端通信模式下
    private Handler cli_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_INFO_MAINACTIVITY:
                    switch (msg.arg1) {
                        case 0:
                            showInfo("连接服务器失败");
                            connCloudThread = null;
                            break;
                        case 1:
                            showInfo("连接服务器成功");
                            break;
                        case 2:
                            break;
                        default:
                            break;
                    }
                    break;
                case START_REC_THREAD:
                    connCloudThread.receiverData();
                    break;
                case START_SEND_THREAD:
                    connCloudThread.sendData();
                    break;
                case START_NEW_ACTIVITY:
                    Intent intent = new Intent(MainActivity.this,
                            ConnCloudActivity.class);
                    startActivity(intent);
                    break;
                case CLIENT_STATE_ERROR:
                    Toast.makeText(MainActivity.this, "连接异常"
                            , Toast.LENGTH_SHORT).show();
                    break;
                case CLIENT_STATE_IOFO:

                    break;
                //接收数据
                case CLIENT_STATE_CORRECT_READ:
                    //Handler_receive(msg);
                    break;
                //发送数据
                case CLIENT_STATE_CORRECT_WRITE:

                    break;
            }
        }
    };

    private void showInfo(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}

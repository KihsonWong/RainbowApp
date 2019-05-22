package com.rainbow.user;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class GatewayInActivity extends Activity implements View.OnClickListener {

    private String tag = "GatewayInActivity";

    private Thread recThread;
    private Thread sendThread;

    private EditText edtSSID = null;
    private EditText edtPsd = null;
    private EditText edtConnStatus = null;

    private Button btnConfirm;
    private Button btnCancel;
    private Button btnConnGate;

    private Context mContext = null;
    private MyHandler myHandler = null;

    private String rainbowIP = "192.168.1.101";
    private String rainbowPort = "6000";
    private Socket mSocket = null;
    private OutputStream outputStream = null;
    private InputStream inputDatas = null;
    private PrintWriter printWriter = null;

    private boolean isConnected = false;
    private int tempVal = 0;

    private static final int STARTRECEIVETHREAD = 1;
    private static final int STARTSENDTHREAD = 2;
    private static final int RETURNMESSAGETOMAINACTIVITY = 3;
    private static final int SENDFAINMESSAGE = 4;
    private static final int CONNECTGATEWAYSUCCESS = 5;
    private static final int CONNECTSERVERFAIL = 6;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_wifi);
        init();

        mContext = this;
    }

    private void init() {
        edtSSID = findViewById(R.id.wifiId);
        edtPsd = findViewById(R.id.passwordId);
        edtConnStatus = findViewById(R.id.hintStatusId);
        edtConnStatus.setSelection(edtConnStatus.getText().toString().length());

        btnConfirm = findViewById(R.id.confirmId);
        btnCancel = findViewById(R.id.cancelId);
        btnConnGate = findViewById(R.id.connectGatewayId);

        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnConnGate.setOnClickListener(this);

        myHandler = new MyHandler();
    }

    @Override
    public void onBackPressed() {
        returnMainActivity();
    }

    private void returnMainActivity() {
        isConnected = false;

        try {
            mSocket.shutdownInput();
            mSocket.shutdownOutput();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        finish();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.confirmId:
                Log.v(tag, "confirm key");
                if (isConnected) {
                    tempVal = 1;
                } else {
                    showInfo("请先连接网关");
                }
                break;
            case R.id.cancelId:
                Log.v(tag, "cancel key");
                onBackPressed();
                break;
            case R.id.connectGatewayId:
                Log.v(tag, "connect gateway key");
                connectGateway();
                break;
            default:
                Log.v(tag, "invalid key");
                break;
        }
    }

    private void connectGateway() {
        if (!isConnected) {
            isConnected = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Log.i(tag, "---->> connect rainbow gateway!");

                    connectRainbowGateway(rainbowIP, rainbowPort);
                }
            }).start();
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STARTRECEIVETHREAD:
                    Log.v(tag, "receiveData Thread");
                    receiverData();
                    break;
                case STARTSENDTHREAD:
                    Log.v(tag, "sendData Thread");
                    sendData();
                    break;
                case RETURNMESSAGETOMAINACTIVITY:
                    String result = null;
                    byte[] buffer = (byte[]) msg.obj;
                    try {
                        result = new String(buffer, 0, msg.arg1, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent();
                    intent.putExtra("returnId",result);
                    setResult(1001,intent);

                    returnMainActivity();
                    break;
                case SENDFAINMESSAGE:
                    Log.v(tag, "send fail");
                    showInfo("请正确输入");
                    break;
                case CONNECTGATEWAYSUCCESS:
                    Log.v(tag, "connect gateway success");
                    edtConnStatus.setText("已连接");
                    break;
                case CONNECTSERVERFAIL:
                    Log.v(tag, "connect gateway fail");
                    showInfo("连接服务器失败");
                    break;
                default:
                    break;
            }
        }
    }

    // 与服务器连接
    private void connectRainbowGateway(String ip, String port) {
        try {
            Log.v(tag, "--->>start connect  server !" + ip + "," + port);
            mSocket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ip,  Integer.parseInt(port));
            mSocket.connect(socketAddress, 100);
            Log.v(tag, "--->>end connect  server!");

            // 获取对应流通道
            outputStream = mSocket.getOutputStream();
            inputDatas = mSocket.getInputStream();

            printWriter = new PrintWriter(new BufferedWriter(   //转成UTF-8编码输出
                    new OutputStreamWriter(outputStream,
                            Charset.forName("UTF-8"))));

            myHandler.sendEmptyMessage(CONNECTGATEWAYSUCCESS);
            myHandler.sendEmptyMessage(STARTRECEIVETHREAD);
            myHandler.sendEmptyMessage(STARTSENDTHREAD);
        } catch (IOException e) {
            myHandler.sendEmptyMessage(CONNECTSERVERFAIL);
            Log.e(tag, "exception:" + e.toString());
        }
    }

    private void showInfo(String msg) {
        Toast.makeText(GatewayInActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
    // 接收数据线程
    private void receiverData() {

        recThread = new Thread(new Runnable() {
            public void run() {
                while (isConnected) {
                    if (mSocket != null && mSocket.isConnected()) {
                        try {
                            while (inputDatas.available() == 0) {
                            }
                            try {
                                Thread.sleep(0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            final byte[] buffer = new byte[1024];//创建接收缓冲区
                            Log.i(tag, "---->>client receive....");
                            final int len = inputDatas.read(buffer);//数据读出来，并且数据的长度
                            myHandler.sendMessage(myHandler.
                                    obtainMessage(RETURNMESSAGETOMAINACTIVITY, len, -1, buffer));
                        } catch (Exception e) {
                            Log.e(tag, "--->>read failure!" + e.toString());
                        }
                    }
                    try {
                        Thread.sleep(100L); // 线程休眠
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        recThread.start();

    }
    // 数据发送
    private void sendData() {
        sendThread = new Thread(new Runnable() {
            public void run() {
                while (isConnected) {
                    if (mSocket != null && mSocket.isConnected()) {
                        try {
                            while (tempVal == 0) ;
                            Log.v(tag, "send data...");
                            tempVal = 0;
                            String ssid = edtSSID.getText().toString().replaceAll(" ", "");
                            String password = edtPsd.getText().toString().replaceAll(" ", "");

                            if (ssid.equals("") || password.equals("")) {
                                myHandler.sendMessage(myHandler.
                                        obtainMessage(SENDFAINMESSAGE, -1, -1, -1));
                            } else {
                                String context = "ssid: " + ssid + " password: " + password;
                                printWriter.print(context);
                                printWriter.flush();
                                Log.i(tag, "--->> client send data!");
                            }
                        } catch (Exception e) {
                            Log.e(tag, "--->> send failure!" + e.toString());
                        }
                    }
                    try {
                        Thread.sleep(100L); // 线程休眠
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendThread.start();
    }
}

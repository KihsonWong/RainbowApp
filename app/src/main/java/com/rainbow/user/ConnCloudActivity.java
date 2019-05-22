package com.rainbow.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
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
import java.nio.charset.Charset;

public class ConnCloudActivity extends Activity implements View.OnClickListener {

    private String tag = "GatewayInActivity";
    private Thread recThread;
    private Thread sendThread;

    private String rainbowIP = "192.168.1.101";
    private String rainbowPort = "6000";
    private Socket mSocket = null;
    private OutputStream outputStream = null;
    private InputStream inputDatas = null;
    private PrintWriter printWriter = null;

    private Context mContext = null;
    private MyHandler myHandler = null;

    private boolean isConnected = false;
    private int tempVal = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_cloud);

        myHandler = new MyHandler();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectGatewayId:
                Log.v(tag, "connect gateway key");
                connectGateway();
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

//            myHandler.sendEmptyMessage(CONNECTGATEWAYSUCCESS);
//            myHandler.sendEmptyMessage(STARTRECEIVETHREAD);
//            myHandler.sendEmptyMessage(STARTSENDTHREAD);
        } catch (IOException e) {
//            myHandler.sendEmptyMessage(CONNECTSERVERFAIL);
            Log.e(tag, "exception:" + e.toString());
        }
    }

    private void showInfo(String msg) {
        Toast.makeText(ConnCloudActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                                    obtainMessage(1, len, -1, buffer));
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
//                            String ssid = edtSSID.getText().toString().replaceAll(" ", "");
//                            String password = edtPsd.getText().toString().replaceAll(" ", "");

//                            if (ssid.equals("") || password.equals("")) {
//                                myHandler.sendMessage(myHandler.
//                                        obtainMessage(SENDFAINMESSAGE, -1, -1, -1));
//                            } else {
//                                String context = "ssid: " + ssid + " password: " + password;
//                                printWriter.print(context);
//                                printWriter.flush();
//                                Log.i(tag, "--->> client send data!");
//                            }
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

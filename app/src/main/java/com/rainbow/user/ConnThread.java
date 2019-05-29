package com.rainbow.user;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.util.Arrays;

public class ConnThread extends Thread {

    private static final String tag = "MainActivity";
    private Handler mhandler;
    public static Socket mSocket;
    public static boolean isruning;

    public final String recMsg = "Who are you?";
    private String sendGatewayId = "I'm client, I want to connect rainbow ";

    Thread recThread;
    Thread sendThread;

    public static String sendContext = null;
    public InputStream inputStream;
    public OutputStream outputStream;
    private PrintWriter printWriter;
    private String ip;                             //IP地址
    private String port;                           //端口号

    private int tempVal;

    public ConnThread(Handler mhandler) {
        this.mhandler = mhandler;
        isruning = true;
    }

    @Override
    public void run() {
        if (mSocket == null) {
            try {
                Log.e(tag, "启动连接线程");
                mSocket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(ip,  Integer.parseInt(port));
                mSocket.connect(socketAddress, 1000);

                outputStream = mSocket.getOutputStream();
                inputStream = mSocket.getInputStream();

                printWriter = new PrintWriter(new BufferedWriter(   //转成UTF-8编码输出
                        new OutputStreamWriter(outputStream,
                                Charset.forName("UTF-8"))));

                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.START_REC_THREAD, -1, -1, -1));
                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.START_SEND_THREAD, -1, -1, -1));
                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.START_NEW_ACTIVITY, -1, -1,-1));
                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.SHOW_INFO_MAINACTIVITY, 1, -1,-1));
            } catch (IOException e) {
                e.printStackTrace();
                MainActivity.connCloudThread = null;
                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.SHOW_INFO_MAINACTIVITY, 0, -1, -1));
            }
        }
    }

    public void setSeverIpAddress(String ipAddress) {
        this.ip = ipAddress;
    }

    public void setServerPort(String port) {
        this.port = port;
    }

    // 接收数据线程
    public void receiverData() {

        recThread = new Thread(new Runnable() {
            public void run() {
                while (isruning) {
                    if (mSocket != null && mSocket.isConnected()) {
                        try {
                            while (inputStream.available() == 0) {
                            }
                            String recString = null;
                            final byte[] buffer = new byte[1024];//创建接收缓冲区

                            Log.i(tag, "---->>client receive....");
                            final int len = inputStream.read(buffer);//数据读出来，并且数据的长度

                            try {
                                recString = new String(buffer, 0, len, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Log.e(tag, recString);

                            if (recString.equals(recMsg)) {
                                Log.e(tag, "111");
                                sendToGateway(sendGatewayId + MainActivity.edtGateId.getText().toString());
                            }
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
    public void sendData() {
        sendThread = new Thread(new Runnable() {
            public void run() {
                while (isruning) {
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
                            //String context = "ssid: " + ssid + " password: " + password;
                            printWriter.print(sendContext);
                            printWriter.flush();
//                           }
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

    private void sendToGateway(String msg) {
        sendContext = msg;
        tempVal = 1;
    }
}

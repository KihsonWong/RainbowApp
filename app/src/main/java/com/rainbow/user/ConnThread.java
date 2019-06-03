package com.rainbow.user;

import android.app.Person;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
    private Socket mSocket;
    private boolean isruning;

    private final String recMsg = "Who are you?";
    private String sendGatewayId = "I'm client, I want to connect rainbow ";

    private String sendContext = null;
    private InputStream inputStream;
    //private OutputStream outputStream;
    private PrintWriter printWriter;
    private String ip;                             //IP地址
    private String port;                           //端口号

    private static int sendBit;

    ConnThread(Handler mhandler) {
        this.mhandler = mhandler;
        isruning = true;
    }

    Socket getSocket() {
        return mSocket;
    }

    void clearSocket() throws IOException {
        mSocket.close();
        mSocket = null;
    }

    void clearIsruning() {
        isruning = false;
    }

    @Override
    public void run() {
        if (mSocket == null) {
            try {
                OutputStream outputStream;
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
                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.SHOW_INFO_MAINACTIVITY, 0, -1, -1));
            }
        }
    }

    void setSeverIpAddress(String ipAddress) {
        this.ip = ipAddress;
    }

    void setServerPort(String port) {
        this.port = port;
    }

    // 接收数据线程
    void receiverThread() {

        Thread recThread = new Thread(new Runnable() {
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
                                recString = new String(buffer, 0, len, "utf-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Log.e(tag, recString);

                            parseMsgAndPerform(recString);
//                            //第一步，生成Json字符串格式的JSON对象
//                            JSONObject jsonObject = new JSONObject(recString);
//                            //第二步，从JSON对象中取值如果JSON 对象较多，可以用json数组
//                            String name="姓名："+jsonObject.getString("name");
//                            String age="年龄："+jsonObject.getString("age");
//                            String sex="性别："+jsonObject.getString("sex");

                            if (recString != null) {
                                if (recString.equals(recMsg)) {
                                    sendMsg(sendGatewayId + MainActivity.edtGateId.getText().toString());
                                }
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
    void sendThread() {
        Thread sendThread = new Thread(new Runnable() {
            public void run() {
                while (isruning) {
                    if (mSocket != null && mSocket.isConnected()) {
                        try {
                            while (sendBit == 0) ;
                            Log.v(tag, "send data...");
                            sendBit = 0;
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

    void sendMsg(String msg) {
        sendContext = msg;
        sendBit = 1;
    }

    enum Search {
        OPTIMAL, LONGEST, APPOINT, CLOSE
    }

    void pckSearchMsg(Search  type) throws JSONException {
        JSONObject jsonObject=new JSONObject();

        switch (type) {
            case OPTIMAL:
                jsonObject.put("class", "search");
                jsonObject.put("way", "optimal");
                break;
            case LONGEST:
                jsonObject.put("class", "search");
                jsonObject.put("way", "longest");
                break;
            case APPOINT:
                jsonObject.put("class", "search");
                jsonObject.put("way", "appoint");
                jsonObject.put("parent", "???");
                break;
            case CLOSE:
                jsonObject.put("class", "search");
                jsonObject.put("way", "close");
                break;
            default:
                break;
        }

        final String result = jsonObject.toString();
        Log.i("jSON字符串", result);
        sendMsg(result);
    }

    void parseMsgAndPerform(String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        String reply = jsonObject.getString("reply");
        Log.e(tag, "parse message");
        if (reply.equals("OK")) {
            //
        } else if (reply.equals("nodeinfo")) {
            //
            for (int i=0;i<ConnCloudActivity.nodeInfo.length;i++);
            ConnCloudActivity.nodeInfo[0].setNode(jsonObject.getInt("node"));
            ConnCloudActivity.nodeInfo[0].setType(jsonObject.getInt("type"));
            ConnCloudActivity.nodeInfo[0].setShownum(jsonObject.getInt("shownum"));
            ConnCloudActivity.nodeInfo[0].setControlnum(jsonObject.getInt("controlnum"));
            ConnCloudActivity.nodeInfo[0].setIdcode(jsonObject.getString("idcode"));
            Log.e(tag, "add new node");
            ConnCloudActivity.updtListFlag = true;
        }
    }
}

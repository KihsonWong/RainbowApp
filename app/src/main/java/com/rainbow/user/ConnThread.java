package com.rainbow.user;


import android.os.Handler;
import android.util.Log;

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

public class ConnThread extends Thread {

    private final String tag = "ConnThread";
    private Handler mhandler;
    private Socket mSocket;
    private boolean isruning;
    private String id;

    private final String recMsg = "Who are you?";
    private final String recMsg1 = "device doesn't find the rainbow id, close the device!";
    private final String recMsg2 = "client device connected rainbow success!";
    private final String sendGatewayId = "I'm client, I want to connect ";
    private final String HEART_PACKET = "HOLD ON CONNECTING HEART";

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

    public void setId(String id) {
        this.id = id;
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

    private void holdConnServer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(isruning) {
                    sendMsg(HEART_PACKET);
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void run() {
        if (mSocket == null) {
            try {
                OutputStream outputStream;
                Log.v(tag, "启动连接线程");
                mSocket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(ip,  Integer.parseInt(port));
                mSocket.connect(socketAddress, 1000);

                outputStream = mSocket.getOutputStream();
                inputStream = mSocket.getInputStream();

                printWriter = new PrintWriter(new BufferedWriter(   //转成UTF-8编码输出
                        new OutputStreamWriter(outputStream,
                                Charset.forName("gb2312"))));

                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.START_REC_THREAD, -1, -1, -1));
                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.START_SEND_THREAD, -1, -1, -1));
            } catch (IOException e) {
                e.printStackTrace();
                mhandler.sendMessage(mhandler.
                        obtainMessage(MainActivity.SHOW_INFO_MAIN_ACTIVITY, 0, -1, -1));
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
                            String []tempString;
                            final byte[] buffer = new byte[1024 * 10];//创建接收缓冲区

                            Log.i(tag, "client receive....");
                            final int len = inputStream.read(buffer);//数据读出来，并且数据的长度

                            try {
                                recString = new String(buffer, 0, len, "gb2312");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            assert recString != null;

                            switch (recString) {
                                case recMsg:
                                    sendMsg(sendGatewayId + id);
                                    Log.v(tag, "msg: " + recString);
                                    continue;
                                case recMsg1:
                                    mhandler.sendMessage(mhandler.
                                            obtainMessage(MainActivity.SHOW_INFO_MAIN_ACTIVITY, 0, -1, -1));
                                    Log.v(tag, "msg: " + recString);
                                    //to stop the thread
                                    clearSocket();
                                    clearIsruning();
                                    continue;
                                case recMsg2:
                                    mhandler.sendMessage(mhandler.
                                            obtainMessage(MainActivity.SHOW_INFO_MAIN_ACTIVITY, 1, -1, -1));
                                    mhandler.sendMessage(mhandler.
                                            obtainMessage(MainActivity.START_NEW_ACTIVITY, -1, -1, -1));
                                    holdConnServer();//send heart packet to server
                                    Log.v(tag, "msg: " + recString);
                                    continue;
                                default:
                                    Log.v(tag, "Json parse...");
                                    break;
                            }

                            tempString = recString.split("\r\n");
                            for (String s : tempString) {
                                Log.v(tag, s);
                                parseMsgAndPerform(s);
                                while (ConnCloudActivity.tempNewNode.getIsusing());
                                if (ControlNodeActivity.tempCtlRemark != null) {
                                    while (ControlNodeActivity.tempCtlRemark.getIsusing());
                                }
                            }

//                            //第一步，生成Json字符串格式的JSON对象
//                            JSONObject jsonObject = new JSONObject(recString);
//                            //第二步，从JSON对象中取值如果JSON 对象较多，可以用json数组
//                            String name="姓名："+jsonObject.getString("name");
//                            String age="年龄："+jsonObject.getString("age");
//                            String sex="性别："+jsonObject.getString("sex");

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
                            if (sendBit != 0) {
                                Log.v(tag, "send data: " + sendContext);
                                sendBit = 0;
                                printWriter.print(sendContext);
                                printWriter.flush();
                            }
                        } catch (Exception e) {
                            Log.e(tag, "send failure: " + e.toString());
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

    private void sendMsg(String msg) {
        sendContext = msg;
        sendBit = 1;
    }

    enum Search {
        OPTIMAL, LONGEST, APPOINT, CLOSE
    }
    //JSon打包及发送消息
    void pckSearchMsg(Search  type) throws JSONException {
        JSONObject jsonObject = new JSONObject();

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
        Log.i("jSON字符串：", result);
        sendMsg(result);
    }

    void pckDeleteMsg(int node) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("class", "delete");
        jsonObject.put("node", node);

        final String result = jsonObject.toString();
        Log.i("jSON字符串：", result);
        sendMsg(result);
    }

    void pckCommandMsg(int node, int window, int value, String obj) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("class", "control");
        jsonObject.put("node", node);
        jsonObject.put("window", window);
        jsonObject.put("value", value);
        jsonObject.put("object", obj);

        final String result = jsonObject.toString();
        Log.i("jSON字符串：", result);
        sendMsg(result);
    }

    void  pckCommandNodeInfo(int node) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("class", "nodeinfo");
        if (node == -1)
            jsonObject.put("node", "all");
        else
            jsonObject.put("node", node);
        jsonObject.put("object", "inquire");

        final String result = jsonObject.toString();
        Log.i("jSON字符串：", result);
        sendMsg(result);
    }
    //接受消息并解析执行
    private void parseMsgAndPerform(String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        //String reply = jsonObject.getString("reply");
        //String rev = jsonObject.getString("class");

        Log.v(tag, "parse message");

        if (jsonObject.has("reply")) {
            String reply = jsonObject.getString("reply");
            if (reply.equals("OK")) {
                Log.v(tag, "reply OK");
            } else if (reply.equals("nodeinfo")) {
                //
                if (ConnCloudActivity.activity_run_flag) {
                    if (!ConnCloudActivity.tempNewNode.getIsusing()) {
                        ConnCloudActivity.tempNewNode.setIsusing(true);
                        ConnCloudActivity.tempNewNode.setNode(jsonObject.getInt("node"));
                        ConnCloudActivity.tempNewNode.setType(jsonObject.getInt("type"));
                        ConnCloudActivity.tempNewNode.setShownum(jsonObject.getInt("shownum"));
                        ConnCloudActivity.tempNewNode.setControlnum(jsonObject.getInt("controlnum"));
                        ConnCloudActivity.tempNewNode.setIdcode(jsonObject.getString("idcode"));

                        Log.i(tag, "lookup nodes info");
                        ConnCloudActivity.updtListFlag = true;
                    } else {
                        Log.e(tag, "add new node fail");
                    }
                } else {
                    Log.e(tag, "ConnCloudActivity is not active!");
                }
            }
        } else if (jsonObject.has("class")) {
            String rev = jsonObject.getString("class");
            String object = jsonObject.getString("object");
            if (rev.equals("newnode") && object.equals("netin") && ConnCloudActivity.searchKey) {
                if (ConnCloudActivity.activity_run_flag) {
                    if (!ConnCloudActivity.tempNewNode.getIsusing()) {
                        ConnCloudActivity.tempNewNode.setIsusing(true);
                        ConnCloudActivity.tempNewNode.setNode(jsonObject.getInt("node"));
                        ConnCloudActivity.tempNewNode.setType(jsonObject.getInt("type"));
                        ConnCloudActivity.tempNewNode.setShownum(jsonObject.getInt("shownum"));
                        ConnCloudActivity.tempNewNode.setControlnum(jsonObject.getInt("controlnum"));
                        ConnCloudActivity.tempNewNode.setIdcode(jsonObject.getString("idcode"));

                        Log.i(tag, "add new node");
                        ConnCloudActivity.updtListFlag = true;
                    } else {
                        Log.e(tag, "add new node fail");
                    }
                } else {
                    Log.e(tag, "ConnCloudActivity is not active!");
                }

            } else if (rev.equals("remark")  && object.equals("control")) {
                if (ControlNodeActivity.activity_run_flag) {
                    if (ControlNodeActivity.tempCtlRemark != null && (ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index] != null))
                        if (!ControlNodeActivity.tempCtlRemark.getIsusing()
                                && (jsonObject.getInt("node") ==  ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getNode())) {
                            ControlNodeActivity.tempCtlRemark.setIsusing(true);
                            ControlNodeActivity.tempCtlRemark.setNode(jsonObject.getInt("node"));
                            ControlNodeActivity.tempCtlRemark.setWindow(jsonObject.getInt("window"));
                            ControlNodeActivity.tempCtlRemark.setContent(jsonObject.getString("content"));
                            Log.i(tag, "update cmd sublistview");

                            ControlNodeActivity.updtSubListCmdFlag = true;
                        } else {
                            Log.v(tag, "get button cmd info fail");
                        }
                    else {
                        Log.e(tag, "no need update control view");
                    }
                } else if (ControlNodeActivity.tempCtlRemark != null){
                    if (ControlNodeActivity.tempCtlRemark.getIsusing()) {//avoid cannot receive network data
                        ControlNodeActivity.tempCtlRemark.setIsusing(false);
                        Log.e(tag, "tempCtlRemark is true, but ControlNodeActivity is not active!");
                    }
                }

                for (int i=0;i<ConnCloudActivity.NODENUM;i++) {
                    if (ConnCloudActivity.nodeInfo[i] != null) {
                        if (ConnCloudActivity.nodeInfo[i].getNode() == jsonObject.getInt("node")) {
                            ConnCloudActivity.nodeInfo[i].setControl(jsonObject.getInt("window"), jsonObject.getString("content"));
                            Log.v(tag, "store node control info");
                            break;
                        }
                    }
                }
            } else if (rev.equals("show")  && object.equals("string")) {
                if (ControlNodeActivity.activity_run_flag) {
                    if (ControlNodeActivity.tempCtlRemark != null && (ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index] != null)) {
                        if (!ControlNodeActivity.tempCtlRemark.getIsusing()
                                && (jsonObject.getInt("node") ==  ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getNode())) {
                            ControlNodeActivity.tempCtlRemark.setIsusing(true);
                            ControlNodeActivity.tempCtlRemark.setNode(jsonObject.getInt("node"));
                            ControlNodeActivity.tempCtlRemark.setWindow(jsonObject.getInt("window"));
                            ControlNodeActivity.tempCtlRemark.setContent(jsonObject.getString("content"));
                            Log.i(tag, "update show sublistview");

                            ControlNodeActivity.updtSubListShowFlag = true;
                        } else {
                            Log.v(tag, "get show info fail");
                        }
                    } else {
                        Log.v(tag, "no need update show view");
                    }

                } else if (ControlNodeActivity.tempCtlRemark != null){
                    if (ControlNodeActivity.tempCtlRemark.getIsusing()) {//avoid cannot receive network data
                        ControlNodeActivity.tempCtlRemark.setIsusing(false);
                        Log.e(tag, "tempCtlRemark = true, butControlNodeActivity is not active!");
                    }
                }

                for (int i=0;i<ConnCloudActivity.NODENUM;i++) {
                    if (ConnCloudActivity.nodeInfo[i] != null) {
                        if (ConnCloudActivity.nodeInfo[i].getNode() == jsonObject.getInt("node")) {
                            ConnCloudActivity.nodeInfo[i].setShow(jsonObject.getInt("window"), jsonObject.getString("content"));
                            Log.v(tag, "store node show info");
                            break;
                        }
                    }
                }
            }
        }
    }
}

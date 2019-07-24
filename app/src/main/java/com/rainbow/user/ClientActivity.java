package com.rainbow.user;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream; //此抽象类表示字节输入流的所有类的超类
import java.io.OutputStream;
import java.io.OutputStreamWriter; //要输出的东西通过输出流输出到目的
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset; //声明字符集包
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//引用安卓的类
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle; //用来传递键值参数的类
import android.os.Handler; //处理子线程
import android.os.Looper; //循环
import android.os.Message;
import android.util.Log; //调试颜色 log.v为黑色，log.d为蓝色，log.i为绿色，log.w为橙色，log.e为红色

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast; //弹出框，提示信息�?

import com.example.bean.IP;
import com.example.util.CheckSum;
import com.example.util.ConvertDataFormat;
import com.example.util.Time;

import java.io.UnsupportedEncodingException;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
//弹出框，提示信息

//创建MainActivity类，该类继承了TabActivity类，并实现了按钮监听事件
public class ClientActivity extends Activity implements OnClickListener {

    private String tag = "MainActivity";
    private String selectSocketItem = "";   //下拉框显示的IP和端口
    private String selectIpItem = "";   //下拉框中显示的IP
    private String selectPortItem = "";    //下拉框显示的端口
    private EditText edtIP = null;
    private EditText edtPort = null;
    private EditText edtReceiver = null;
    private EditText edtSend = null;
    private InputStream inputDatas = null;
    private PrintWriter printWriter = null;
    private Socket mSocket = null;
    private MyHandler myHandler = null;
    private Context mContext = null;
    private OutputStream outputStream = null;
    private ArrayAdapter<String> ipListAdapter = null;

    private Button btnConn;
    private Button btnSend;
    private Button btnClean;
    private Button btncomm1;
    private Button btncomm2;
    private Button btncomm3;
    private Button btncomm4;
    private Button btncomm5;
    private Button btncomm6;
    private Button btncomm7;
    private Button btncomm8;
    private Button btnAddIP;
    private Button btnDeleteIP;
    private static Button btnDI1;
    private static Button btnDI2;
    private static Button btnDI3;
    private static Button btnDI4;
    private static Button btnHDI1;
    private static Button btnHDI2;
    private static Button btnHDI3;
    private static Button btnHDI4;
    private Spinner ipSpinner;

    int count_DI1 = 0;
    int count_DI2 = 0;
    int count_DI3 = 0;
    int count_DI4 = 0;
    int count_HDI1 = 0;
    int count_HDI2 = 0;
    int count_HDI3 = 0;
    int count_HDI4 = 0;
    final int DIALOG_REQ_CODE = 1;

    private int tempVal = 0;

    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_main);
        init();
        mContext = this;
    }

    private void init() {

        edtIP = (EditText) findViewById(R.id.id_edt_inputIP);
        edtPort = (EditText) findViewById(R.id.id_edt_inputport);
        edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea);
        edtSend.setOnClickListener(inputEditTextListener);
        edtReceiver = (EditText) findViewById(R.id.id_edt_recshow);
        edtReceiver.setSelection(edtReceiver.getText().toString().length());
        btnSend = (Button) findViewById(R.id.id_btn_send);
        btnSend.setOnClickListener(this);
        btnConn = (Button) findViewById(R.id.id_btn_connClose);
        btnConn.setOnClickListener(this);
        btnClean = (Button) findViewById(R.id.id_btn_clean);
        btnClean.setOnClickListener(this);
        btncomm1 = (Button) findViewById(R.id.id_btn_comm1);
        btncomm1.setOnClickListener(btncommlistener1);
        btncomm2 = (Button) findViewById(R.id.id_btn_comm2);
        btncomm2.setOnClickListener(btncommlistener2);
        btncomm3 = (Button) findViewById(R.id.id_btn_comm3);
        btncomm3.setOnClickListener(btncommlistener3);
        btncomm4 = (Button) findViewById(R.id.id_btn_comm4);
        btncomm4.setOnClickListener(btncommlistener4);
        btncomm5 = (Button) findViewById(R.id.id_btn_comm5);
        btncomm5.setOnClickListener(btncommlistener5);
        btncomm6 = (Button) findViewById(R.id.id_btn_comm6);
        btncomm6.setOnClickListener(btncommlistener6);
        btncomm7 = (Button) findViewById(R.id.id_btn_comm7);
        btncomm7.setOnClickListener(btncommlistener7);
        btncomm8 = (Button) findViewById(R.id.id_btn_comm8);
        btncomm8.setOnClickListener(btncommlistener8);
        btnAddIP = (Button) findViewById(R.id.id_btn_addIP);
        btnAddIP.setOnClickListener(btnAddIPListener);
        ipSpinner = (Spinner) findViewById(R.id.id_slecIP);
        ipSpinner.setOnItemSelectedListener(btnOnItemSelectedListener);
        btnDeleteIP = (Button) findViewById(R.id.id_btn_ip_deleteIP);
        btnDeleteIP.setOnClickListener(btnDeleteIpListener);
        btnDI1 = (Button) findViewById(R.id.id_btn_HDI_1);
        btnDI2 = (Button) findViewById(R.id.id_btn_HDI_2);
        btnDI3 = (Button) findViewById(R.id.id_btn_HDI_3);
        btnDI4 = (Button) findViewById(R.id.id_btn_HDI_4);
        btnHDI1 = (Button) findViewById(R.id.id_btn_HDI_1);
        btnHDI2 = (Button) findViewById(R.id.id_btn_HDI_2);
        btnHDI3 = (Button) findViewById(R.id.id_btn_HDI_3);
        btnHDI4 = (Button) findViewById(R.id.id_btn_HDI_4);

        //适配器
        ipListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        //设置样式
        ipListAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        initShowSocketItem();

        myHandler = new MyHandler();
    }

    //下拉框初始化显示
    private void initShowSocketItem() {
        ipListAdapter.clear();
        ipSpinner.setAdapter(ipListAdapter);    //清空下拉列表
        ArrayList<String> templist = new ArrayList<String>();
        List<IP> ips = DataSupport.findAll(IP.class);
        for (IP ipValue : ips) {
            templist.add(ipValue.getIp() + ":" + ipValue.getPort());
        }
        int length = templist.size();
        for (int i = length - 1; i >= 0; i--) {
            ipListAdapter.add(templist.get(i));
        }
        ipSpinner.setAdapter(ipListAdapter);

    }

    //点击添加输入框中ip和端口到下拉框中
    private OnClickListener btnAddIPListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {

            try {
                /*获取输入框ip值和port值并保存于IP数据库*/
                LitePal.getDatabase();
                String inputIpText = edtIP.getText().toString();
                String inputPortText = edtPort.getText().toString();
                Log.v("ip值", "inputIpText" + ":" + "inPutPortText");
                if ("".equals(inputIpText)) {
                    Toast.makeText(ClientActivity.this, "ip不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if ("".equals(inputPortText)) {
                    Toast.makeText(ClientActivity.this, "端口不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (LitePal.isExist(IP.class, "ip = ? and port = ?", inputIpText, inputPortText)) {
                    Toast.makeText(ClientActivity.this, "此地址已存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                IP ip = new IP();
                ip.setIp(inputIpText);
                ip.setPort(inputPortText);
                ip.save();
                initShowSocketItem();
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("tag", "Loading ip Error");
                Toast.makeText(mContext, "添加异常" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    //获取当前Spinner值
    private AdapterView.OnItemSelectedListener btnOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectSocketItem = ipSpinner.getSelectedItem().toString();
            if (selectSocketItem != null && !"".equals(selectSocketItem)) {
                String[] IpAndPort = selectSocketItem.split(":");
                edtIP.setText(IpAndPort[0]);
                edtPort.setText(IpAndPort[1]);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    //删除Spinner对应socket
    private OnClickListener btnDeleteIpListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                //删除
                String[] IpAndPort = selectSocketItem.split(":");
                selectIpItem = IpAndPort[0];
                selectPortItem = IpAndPort[1];
                DataSupport.deleteAll(IP.class, "ip = ? and port = ?", selectIpItem, selectPortItem);
                initShowSocketItem();
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener btncommlistener1 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x01, btncomm1);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener btncommlistener2 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x02, btncomm2);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener btncommlistener3 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x03, btncomm3);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener btncommlistener4 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x04, btncomm4);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
    private OnClickListener btncommlistener5 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x05, btncomm5);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener btncommlistener6 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x06, btncomm6);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener btncommlistener7 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x07, btncomm7);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
    private OnClickListener btncommlistener8 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                CheckSum.sendCmdBtnMes((byte) 0x08, btncomm8);
                outputStream.write(CheckSum.SendCmdBuf);
            } catch (Exception e) {
                Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    //点击输入框弹出输入对话框
    private OnClickListener inputEditTextListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ClientActivity.this, InputDialogActivity.class);
            intent.putExtra("edtSend", edtSend.getText().toString());
            if ("".equals(edtSend.getText().toString())) {
                Log.w("tag", "初始内容为空");
            }
            startActivityForResult(intent, DIALOG_REQ_CODE);
        }
    };

    //取回对话框传回的输入数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case DIALOG_REQ_CODE:
                    if (resultCode == RESULT_OK) {
                        String returnedData = data.getStringExtra("data_return");
                        if (returnedData != null) {
                            Log.w("return_data", returnedData);
                        } else
                            Log.w("return_data", "returnedData 为空");
                        edtSend.setText(returnedData);
                        edtSend.setSelection(edtSend.getText().toString().length());    //光标移动到最后
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 监听按钮事件，判断是那个按钮按下，随后执行相应的线程
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.id_btn_connClose:
                connectThread();
                break;
            case R.id.id_btn_send:
                Log.i(tag, "herer??");
                tempVal = 1;
                //sendData();
                break;
            case R.id.id_btn_clean:
                edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea);
                edtSend.setText("");
                break;
        }
    }

    // 建立连接按钮的线程
    private void connectThread() {
        if (!isConnected) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Log.i(tag, "---->> connect/close server!");

                    connectServer(edtIP.getText().toString(), edtPort.getText()
                            .toString());
                }
            }).start();
        } else {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                    Log.i(tag, "--->>重新连server.");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnConn.setText("重新连接");
            isConnected = false;
        }
    }

    // 与服务器连接
    private void connectServer(String ip, String port) {
        try {
            Log.e(tag, "--->>start connect  server !" + ip + "," + port);
            mSocket = new Socket(ip, Integer.parseInt(port));
            Log.e(tag, "--->>end connect  server!");

            // 获取对应流通道
            outputStream = mSocket.getOutputStream();
            inputDatas = mSocket.getInputStream();

            printWriter = new PrintWriter(new BufferedWriter(   //转成GB231编码输出
                    new OutputStreamWriter(outputStream,
                            Charset.forName("gb2312"))));

            myHandler.sendEmptyMessage(2);
            myHandler.sendEmptyMessage(3);
        } catch (Exception e) {
            isConnected = false;
            showInfo("连接服务器失败");
            Log.e(tag, "exception:" + e.toString());
        }
    }

    // 接收数据线程
    private void receiverData() {

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (isConnected) {
                        if (mSocket != null && mSocket.isConnected()) {
                            try {
                                // 通过从输入流读取数据，返回给字符串result
//                                byte[] byteArr = readFromInputStream(inputDatas);
//                                if (byteArr.length > 0) {
//                                    Message msg = new Message();
//                                    msg.what = 1;
//                                    Bundle data = new Bundle();
//                                    data.putByteArray("msg", byteArr);
//                                    msg.setData(data);
//                                    myHandler.sendMessage(msg); // 发送消息到队列
//
//                                }
                                inputDatas = mSocket.getInputStream();
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
                    }
                    try {
                        Thread.sleep(100L); // 线程休眠
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        Log.i(tag, "--->>socket 可以通信!");
        btnConn.setText("已连接");
        showInfo("连接服务器成功");
        isConnected = true;
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //byte[] byteArr = (byte[]) msg.getData().get("msg");
                    // String result = ConvertDataFormat.bytesToHexString(byteArr);    //byte数组转换成字符串输出
                    String result = null;
                    byte[] buffer = (byte[]) msg.obj;
                    try {
                        result = new String(buffer, 0, msg.arg1, "GB2312");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    edtReceiver.append(result);
                    edtReceiver.append("\n");
//                    if (byteArr[0] == (byte) 0xAA) {    //先校验首位，再校验其他
//                        CheckReturnStatus(byteArr);
//                        Log.v("接收到的数组:", Arrays.toString(byteArr));
//                    }
                    break;
                case 2:         //接收数据线程
                    Log.v(tag, "prepare to start to receive data");
                    receiverData();
                    break;
                case 3:
                    Log.v(tag, "prepare to start to send data");
                    sendData();
                    break;
                case 4:
                    Log.v(tag, "send fail");
                    showInfo("发送内容为空");
                    break;
                default:
                    break;
            }
        }

    }

    // 获得输入内容转换为字符串输出
    public byte[] readFromInputStream(InputStream in) {
        int count = 0;
        byte[] inDatas = null;
        try {
            while (count == 0) {
                count = in.available();
            }
            inDatas = new byte[count];
            in.read(inDatas);
            return inDatas;// new String(inDatas, "gb2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 数据发送
    private void sendData() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (isConnected) {
                        if (mSocket != null && mSocket.isConnected()) {
                            try {
                                while (tempVal == 0) ;
                                tempVal = 0;
                                String context = edtSend.getText().toString().replaceAll(" ", "");
//                                if (printWriter == null || context.equals("")) {
//                                    if (printWriter == null) {
//                                        showInfo("发送错误");
//                                        //return;
//                                    }
//                                }
                                if (context.equals("")) {
                                    myHandler.sendMessage(myHandler.
                                            obtainMessage(4, -1, -1, -1));
                                }
                                printWriter.print(context);
                                printWriter.flush();
                                Log.i(tag, "--->> client send data!");
                            } catch (Exception e) {
                                Log.e(tag, "--->> send failure!" + e.toString());
                            }
                        }
                    }
                    try {
                        Thread.sleep(100L); // 线程休眠
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void showInfo(String msg) {
        Toast.makeText(ClientActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    //返回数据校验
    private void CheckReturnStatus(byte[] arr) {
        try {
            byte checkSumValue = CheckSum.getCheckSum(arr);
            if (checkSumValue != arr[arr.length - 1]) {
                Log.i(tag, String.valueOf(checkSumValue));
                Toast.makeText(ClientActivity.this, "校验错误", Toast.LENGTH_SHORT).show();
                return;
            }

            switch (arr[3]) {
                case 0x43:      //对DI/DO的控制操作
                    switch (arr[4]) {
                        case 0x0A:      //返回DI状态计数
                            selectDIx(arr);
                            break;
                        case 0x05:
                            Toast.makeText(ClientActivity.this, "指令不正确", Toast.LENGTH_SHORT).show();
                            break;
                        case 0x06:
                            selectHDIx(arr);
                            break;
                        default:
                            Toast.makeText(ClientActivity.this, "指令不正确", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0x41:
                    switch (arr[5]) {
                        case (byte) 0xF0:
                            Toast toast = Toast.makeText(ClientActivity.this, "成功", Toast.LENGTH_SHORT);
                            Time.showToastLength(toast, 500);
                            break;
                        case (byte) 0xF1:
                            Toast.makeText(ClientActivity.this, "命令参数错", Toast.LENGTH_SHORT).show();
                            break;
                        case (byte) 0xF2:
                            Toast.makeText(ClientActivity.this, "校验错", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                default:
                    Toast.makeText(ClientActivity.this, "命令字不正确", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 四个DI按钮计数，计的是每个命令发送的次数
    private void selectDIx(byte[] arrDI) {
        int length = arrDI.length;
        if (length == 10) {
            if (arrDI[5] == 0x01) {
                count_DI1++;
                btnDI1.setText("DI1: " + count_DI1);
            }
            if (arrDI[6] == 0x01) {
                count_DI2++;
                btnDI2.setText("DI2: " + count_DI2);
            }
            if (arrDI[7] == 0x01) {
                count_DI3++;
                btnDI3.setText("DI3: " + count_DI3);
            }
            if (arrDI[8] == 0x01) {
                count_DI4++;
                btnDI4.setText("DI4: " + count_DI4);
            }
        } else {
            Toast toast = Toast.makeText(ClientActivity.this, "返回DI状态指令长度不正确", Toast.LENGTH_SHORT);
            Time.showToastLength(toast, 500);
        }
    }

    // 四个DI高电平按钮计数，计的是每个命令发送的次数
    private void selectHDIx(byte[] arrDI) {
        int length = arrDI.length;
        if (length == 10) {
            if (arrDI[5] == 0x01) {
                count_HDI1++;
                btnHDI1.setText("DI1高: " + count_HDI1);
            }
            if (arrDI[6] == 0x01) {
                count_HDI2++;
                btnHDI2.setText("DI2高: " + count_HDI2);
            }
            if (arrDI[7] == 0x01) {
                count_HDI3++;
                btnHDI3.setText("DI3高: " + count_HDI3);
            }
            if (arrDI[8] == 0x01) {
                count_HDI4++;
                btnHDI4.setText("DI4高: " + count_HDI4);
            }
        } else {
            Toast toast = Toast.makeText(ClientActivity.this, "返回DI状态指令长度不正确", Toast.LENGTH_SHORT);
            Time.showToastLength(toast, 500);
        }
    }
}
package com.rainbow.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ServerActivity extends Activity {

    private String recvMessageServer = "";

    private Button btncomm1;
    private Button btnsetserver;
    private Button btnsend2;
    private Button btnclean2;
    private EditText edtReceiver;
    private Context mContext;
    private Socket mSocketServer = null;
    private Thread mThreadServer = null;
    private ServerSocket serverSocket = null;
    private boolean serverRuning = false;

    static BufferedReader mBufferedReaderServer = null;
    static PrintWriter mPrintWriterServer = null;
    static OutputStream output = null;


    EditText edtSend;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_main);
        mContext = this;

        edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea2);
        edtReceiver = (EditText) findViewById(R.id.id_edt_jieshou2);

        btnsend2 = (Button) findViewById(R.id.id_btn_send2);
        btnsend2.setOnClickListener(btnsendlistener);
        btnsetserver = (Button) findViewById(R.id.id_btn_setServer);
        btnsetserver.setOnClickListener(btnsetserverlistener);
        btnclean2 = (Button) findViewById(R.id.id_btn_clean2);
        btnclean2.setOnClickListener(btncleanlistener);
//        btncomm1 = (Button) findViewById(R.id.id_btn_comm1);
//        btncomm1.setOnClickListener(btncommListener1);
    }



    private OnClickListener btncommListener1 = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (serverRuning && mSocketServer != null) {
                byte[] SendCmdBuf = {(byte)0xAA,0x00,0x01,0x43,0x01,0x01,0x00};
                mPrintWriterServer.flush();
                if(btncomm1.getText().toString().equals("DO1常开")){
                    SendCmdBuf[5] = 0x01;
                    btncomm1.setText("DO1常闭");
                }else if(btncomm1.getText().toString().equals("DO1常闭")){
                    SendCmdBuf[5] = 0x02;
                    btncomm1.setText("DO1常闪");
                }else if(btncomm1.getText().toString().equals("DO1常闪")){
                    SendCmdBuf[5] = 0x03;
                    btncomm1.setText("DO1常开");
                }
                try {
                    SendCmdBuf[6] = getCheckSum(SendCmdBuf);
                    // 发送给服务器
                    output.write(SendCmdBuf);
//						for (byte i : SendCmdBuf) {
//							mPrintWriterServer.write(i);
//						}

                } catch (Exception e) {
                    // TODO: handle exception
                    Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener btncleanlistener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            edtSend.setText("");
        }

    };

    private OnClickListener btnsendlistener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (serverRuning && mSocketServer != null) {
                String msgText = edtSend.getText().toString();// 取得编辑框中我们输入的内容
                if (msgText.length() <= 0) {
                    Toast.makeText(mContext, "发送内容不能为空！", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    try {
                        mPrintWriterServer.print(msgText);// 发送给服务器
                        mPrintWriterServer.flush();
                    } catch (Exception e) {
                        // TODO: handle exception
                        Toast.makeText(mContext, "发送异常：" + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
            }
        }
    };

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                edtReceiver.append("信息: " + recvMessageServer); // 刷新
            } else if (msg.what == 1) {
                edtReceiver.append("Client: "); // 刷新
            }
        }
    };

    private OnClickListener btnsetserverlistener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (serverRuning) {
                serverRuning = false;

                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                    if (mSocketServer != null) {
                        mSocketServer.close();
                        mSocketServer = null;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mThreadServer.interrupt();
                btnsetserver.setText("启动服务器功能");
                edtReceiver.setText("服务器信息:\n");
            } else {
                serverRuning = true;
                mThreadServer = new Thread(mcreateRunnable);
                mThreadServer.start();
                btnsetserver.setText("关闭服务器");
            }
        }
    };

    private Runnable mcreateRunnable = new Runnable() {
        public void run() {
            try {
                serverSocket = new ServerSocket(0);

                SocketAddress address = null;
                if (!serverSocket.isBound()) {
                    serverSocket.bind(address, 0);
                }

                getLocalIpAddress();

                // 等待客服连接
                mSocketServer = serverSocket.accept();

                // 接受客服端数据BufferedReader对象
                mBufferedReaderServer = new BufferedReader(
                        new InputStreamReader(mSocketServer.getInputStream()));
                // 给客服端发送数据
                output = mSocketServer.getOutputStream();
                mPrintWriterServer = new PrintWriter(
                        mSocketServer.getOutputStream(), true);
                // mPrintWriter.println("服务端已经收到数据！");

                Message msg = new Message();
                msg.what = 0;
                recvMessageServer = "client已经连接上！\n";
                mHandler.sendMessage(msg);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                Message msg = new Message();
                msg.what = 0;
                recvMessageServer = "出现错误" + e.getMessage() + e.toString()
                        + "\n";// 消息换行
                mHandler.sendMessage(msg);
                return;
            }
            char[] buffer = new char[256];
            int count = 0;
            while (serverRuning) {
                try {

                    if ((count = mBufferedReaderServer.read(buffer)) > 0)
                        ;
                    {
                        recvMessageServer = getInfoBuff(buffer, count) + "\n";// 消息换行
                        Message msg = new Message();
                        msg.what = 0;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    recvMessageServer = "接收异常:" + e.getMessage() + "\n";// 消息换行
                    Message msg = new Message();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                    return;
                }
            }
        }
    };

    public String getLocalIpAddress() {
        try {
            // for (Enumeration<NetworkInterface> en = NetworkInterface
            // .getNetworkInterfaces(); en.hasMoreElements();) {
            // NetworkInterface netWorkInterface = en.nextElement();
            // for (Enumeration<InetAddress> enumIpAddress = netWorkInterface
            // .getInetAddresses(); enumIpAddress.hasMoreElements();) {
            // InetAddress inetAddress = enumIpAddress.nextElement();
            //
            // {
            //
            // {
            // recvMessageServer = "本地IP地址："
            // + inetAddress.getHostAddress() + "端口号:"
            // + serverSocket.getLocalPort() + "\n";
            //
            // }
            // }
            // }
            // }

            /* 备用 */
            Enumeration<NetworkInterface> nifs = NetworkInterface
                    .getNetworkInterfaces();

            while (nifs.hasMoreElements()) {
                NetworkInterface nif = nifs.nextElement();

                // 获得与该网络接口绑定的 IP 地址，一般只有一个
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address) { // 只关心 IPv4 地址
                        // System.out.println("网卡接口名称：" + nif.getName());
                        // System.out.println("网卡接口地址：" + addr.getHostAddress());
                        // System.out.println();
                        //recvMessageServer = "";
                        if (!nif.getName().equals("lo")) {
                            recvMessageServer += "网卡接口名称:" + nif.getName() + " "
                                    + "本地IP地址：" + addr.getHostAddress() + "端口号:"
                                    + serverSocket.getLocalPort() + "\n";
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            recvMessageServer = "获取IP地址异常:" + ex.getMessage() + "\n";// 消息换行
            Message msg = new Message();
            msg.what = 0;
            mHandler.sendMessage(msg);
        }
        Message msg = new Message();
        msg.what = 0;
        mHandler.sendMessage(msg);
        return null;
    }

    private String getInfoBuff(char[] buff, int count) {
        char[] temp = new char[count];
        for (int i = 0; i < count; i++) {
            temp[i] = buff[i];
        }
        return new String(temp);
    }

    //	public int getCheckSum(int[] sendCmdBuf)
//    {
//        //从第二位开始进行校验和校验
//        int checkSumValue = sendCmdBuf[1];
//        for (int i = 2; i < sendCmdBuf.length - 1; i++){
//        	checkSumValue = ((int) (sendCmdBuf[i] ^ checkSumValue)) & 0xff;
//      }
//        return  checkSumValue;
//   }
    public byte getCheckSum(byte[] sendCmdBuf)
    {
        //从第二位开始进行校验和校验
        byte checkSumValue = sendCmdBuf[1];
        for (int i = 2; i < sendCmdBuf.length - 1; i++){
            checkSumValue =  (byte) (((sendCmdBuf[i] ^ checkSumValue)) & 0xff);
        }
        return checkSumValue;
    }
}

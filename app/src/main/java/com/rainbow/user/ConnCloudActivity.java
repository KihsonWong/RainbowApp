package com.rainbow.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;



public class ConnCloudActivity extends Activity implements View.OnClickListener {

    private String tag = "ConnCloudActivity";
    private int i = 0;

    private final int UPDATE_LISTVIEW = 0;
    private ListView lv;// 适配器控件------->V视图
    private static ArrayAdapter<String> adapter;// 适配器------>C控制器
    private static ArrayList<String> data;// 数据源-->M

    private final int DELETE_ITEM = 0;
    private final int ADD_ITEM = 1;
    public static final int NODENUM = 20;

    public static boolean updtListFlag = false;
    private boolean isrunning = false;

    public static NodeInfo tempNewNode;
    public static NodeInfo[] nodeInfo;

    private Button btnSearch;
    private Button btnNodeInfo;
    private Button btnDeletNode;

    public static boolean searchKey = false;

    private String msgSearchOn = "SEARCH: Optimal;";
    private String msgSearchOff= "SEARCH: Close;";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_cloud);
        init();
        adapterInit();

        UpdateListThread updateListThread = new UpdateListThread(mHandler);
        updateListThread.start();
    }

    private void init() {

        btnSearch = findViewById(R.id.searchId);
        btnNodeInfo = findViewById(R.id.id_btn_nodeInfo);
        btnDeletNode = findViewById(R.id.id_btn_del_node);

        btnSearch.setOnClickListener(this);
        btnNodeInfo.setOnClickListener(this);
        btnDeletNode.setOnClickListener(this);
    }

    public void adapterInit() {

        tempNewNode  = new NodeInfo(false);
        nodeInfo = new NodeInfo[NODENUM];
        data = new ArrayList<>();
        //找到ListView
        lv = findViewById(R.id.myList);
        // 实现适配器，利用系统定义的样式，加载数据源
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, data);
        // R.layout.cell 自己定义视图
        // android.R.layout.simple_list_item_1 系统定义视图样式
        // 绑定适配器到适配器控件上
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

//                Toast.makeText(ConnCloudActivity.this,
//                        "第" + (position + 1) + "项被单击按下", Toast.LENGTH_LONG)
//                        .show();
                String da = data.get(position);
                Log.e(tag, da);
                Log.e(tag, "id: " + id + "  position: " + position);
                Log.e(tag, "current list size: " + data.size());
                //setContentView(R.layout.input_wifi);
                //setContentView(R.layout.connect_cloud);
//                Intent intent = new Intent(ConnCloudActivity.this,
//                        GatewayInActivity.class);
//                startActivity(intent);
            }
        });
        //处理长时间按下事件：列表项被长时间按下时给出提示信息
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                showInfo("第" + (position + 1) + "项被长时间按下");
                //1.给服务器发送删除节点命令
                String temp = data.get(position);
                for (int i=0;i<NODENUM;i++) {
                    if (nodeInfo[i] != null && temp.equals(nodeInfo[i].getIdcode())) {
                        try {
                            MainActivity.connCloudThread.pckDeleteMsg(nodeInfo[i].getNode());
                            break;
                        } catch (JSONException e) {
                            Log.e(tag, "发送删除节点信息失败");
                            showInfo("删除节点失败");
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
                //2.删除视图列表
                //updateListView(DELETE_ITEM, position);
                mHandler.sendMessage(mHandler. //由于添加是通过消息队列，因此删除也必须在通过这种方式，否则size会出错。
                        obtainMessage(UPDATE_LISTVIEW, DELETE_ITEM, position, -1));
                //3.删除已存储的节点信息
                nodeInfoHandler(DELETE_ITEM, position);
                return true;
            }
        });
    }

    class UpdateListThread extends Thread {

        private Handler mHandler;

        public UpdateListThread(Handler mHandler) {
            this.mHandler = mHandler;
            isrunning = true;
        }

        @Override
        public void run() {
                while (isrunning) {
                    while (!updtListFlag);
                    updtListFlag = false;
                    Log.e(tag, "send message: update list");
                    //1.视图添加节点
                    mHandler.sendMessage(mHandler.
                            obtainMessage(UPDATE_LISTVIEW, ADD_ITEM, -1, -1));
                    //2.存储新的节点信息
                    nodeInfoHandler(ADD_ITEM, -1);
                    try {
                        Thread.sleep(100L); // 线程休眠
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
    //存储或删除该节点信息
    public void nodeInfoHandler(int handler, int position) {
        switch (handler) {
            case ADD_ITEM:
                for (int i=0;i<NODENUM;i++) {
                    if (nodeInfo[i] == null && tempNewNode.isusing) {
                        nodeInfo[i] = new NodeInfo(true);
                        nodeInfo[i].setNode(tempNewNode.getNode());
                        nodeInfo[i].setType(tempNewNode.getType());
                        nodeInfo[i].setControlnum(tempNewNode.getControlnum());
                        nodeInfo[i].setShownum(tempNewNode.getShownum());
                        nodeInfo[i].setIdcode(tempNewNode.getIdcode());
                        Log.i(tag, "添加节点信息成功");

                        tempNewNode.setIsusing(false);
                        break;
                    }
                }
                if (i == NODENUM) {
                    showInfo("节点添加失败");
                    Log.e(tag, "超出最大节点数量限制");
                }
                break;
            case DELETE_ITEM:
                String item = data.get(position);
                for (int i=0;i<NODENUM;i++) {
                    if (nodeInfo[i] != null && item.equals(nodeInfo[i].getIdcode())) {
                        nodeInfo[i] = null;
                        Log.i(tag, "删除节点信息成功");
                        break;
                    }
                }
                if (i == NODENUM) {
                    showInfo("节点删除失败");
                    Log.e(tag, "删除节点信息失败");
                }
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LISTVIEW:
                    Log.i(tag, "update list view: " + msg.arg1);
                    updateListView(msg.arg1, msg.arg2);
                    break;
            }
        }
    };


    public void updateListView(int handler, int position)  {
        switch (handler) {
            case DELETE_ITEM:
//                if (position == data.size() - 1){}
//
//                else
                Log.e(tag, "pos: " + position + " :" + i++ + " data.size: " + data.size());
                data.remove(position);
                break;
            case ADD_ITEM:
                data.add(tempNewNode.getIdcode());
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchId:
                searchKey = !searchKey;

                if (searchKey) {
                    try {
                        MainActivity.connCloudThread.pckSearchMsg(ConnThread.Search.OPTIMAL);
                        btnSearch.setText("搜索中");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        MainActivity.connCloudThread.pckSearchMsg(ConnThread.Search.CLOSE);
                        btnSearch.setText("搜索");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Log.v(tag, "搜索节点按键");
                break;
            case R.id.id_btn_nodeInfo:
                Log.v(tag, "得到节点信息" + data.size());

//                data.add("Item" + i++);
//                adapter.notifyDataSetChanged();
                break;
            case R.id.id_btn_del_node:
                Log.v(tag, "删除节点");
                Intent intent = new Intent(ConnCloudActivity.this,
                        ControlNodeActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        returnMainActivity();
    }

    private void returnMainActivity() {
        MainActivity.connCloudThread.clearIsruning();
        isrunning = false;

        try {
            MainActivity.connCloudThread.getSocket().shutdownInput();
            MainActivity.connCloudThread.getSocket().shutdownOutput();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (MainActivity.connCloudThread.getSocket() != null) {
                    MainActivity.connCloudThread.clearSocket();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MainActivity.connCloudThread = null;
        finish();
    }
//    /**
//     * 显示提示消息的对话框
//     * @author codingblock 2015-8-11
//     * @param  context     上下文
//     * @param  title       对话框标题
//     * @param  message     对话框提示内容
//     * @return
//     */
//    public AlertDialog.Builder simpleDialog(final Context context, String title, String message){
//        AlertDialog.Builder builder = new AlertDialog.Builder(context)
//                .setTitle(title)
//                .setIcon(R.drawable.ic_launcher_foreground)
//                .setMessage(message)
//                .setPositiveButton("完成", null)
//                .setNegativeButton("取消", null);
//        return builder;
//    }

    private void showInfo(String msg) {
        Toast.makeText(ConnCloudActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    public class NodeInfo {

        private boolean isusing;
        private int node;
        private int type;
        private int shownum;
        private int controlnum;
        private String idcode;

        public NodeInfo(boolean isusing) {
            this.isusing = isusing;
        }

        public void setIsusing(boolean isusing) {
            this.isusing = isusing;
        }

        public void setNode(int node) {
            this.node = node;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setShownum(int shownum) {
            this.shownum = shownum;
        }

        public void setControlnum(int controlnum) {
            this.controlnum = controlnum;
        }

        public void setIdcode(String idcode) {
            this.idcode = idcode;
        }

        public boolean getIsusing() {
            return isusing;
        }

        public int getNode() {
            return node;
        }

        public int getType() {
            return type;
        }

        public int getShownum() {
            return shownum;
        }

        public int getControlnum() {
            return controlnum;
        }

        public String getIdcode() {
            return idcode;
        }
    }


}

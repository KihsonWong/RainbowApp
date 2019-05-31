package com.rainbow.user;

import android.app.Activity;
import android.os.Bundle;
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

    private ListView lv;// 适配器控件------->V视图
    private static ArrayAdapter<String> adapter;// 适配器------>C控制器
    private static ArrayList<String> data;// 数据源-->M

    public static NodeInfo nodeInfo;

    private Button btnSearch;
    private Button btnNodeInfo;
    private Button btnDeletNode;

    private boolean searchKey = false;

    private String msgSearchOn = "SEARCH: Optimal;";
    private String msgSearchOff= "SEARCH: Close;";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_cloud);
        init();
        adapterInit();

        MainActivity.connCloudThread.receiverThread();
        MainActivity.connCloudThread.sendThread();
    }

    private void init() {

        btnSearch = findViewById(R.id.searchId);
        btnNodeInfo = findViewById(R.id.id_btn_nodeInfo);
        btnDeletNode = findViewById(R.id.id_btn_del_node);

        btnSearch.setOnClickListener(this);
        btnNodeInfo.setOnClickListener(this);
        btnDeletNode.setOnClickListener(this);
    }

    private void adapterInit() {

        nodeInfo = new NodeInfo();
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
//                Log.e(tag, "" + id);
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
                Toast.makeText(ConnCloudActivity.this,
                        "第" + (position + 1) + "项被长时间按下", Toast.LENGTH_LONG)
                        .show();
                return true;
            }
        });
    }

    public static void updateListView()  {
        data.add(nodeInfo.getIdcode());
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
                Log.v(tag, "得到节点信息");
                data.add("Item" + i++);
                adapter.notifyDataSetChanged();
                break;
            case R.id.id_btn_del_node:
                Log.v(tag, "删除节点");
                if (data.size() > 2)
                data.remove(2);
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    public boolean getSearch() {
        return searchKey;
    }

    @Override
    public void onBackPressed() {
        returnMainActivity();
    }

    private void returnMainActivity() {
        MainActivity.connCloudThread.clearIsruning();

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




    public class NodeInfo {
        private int node;
        private int type;
        private int shownum;
        private int controlnum;
        private String idcode;

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

package com.rainbow.user;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class ConnCloudActivity extends Activity implements View.OnClickListener {

    private String tag = "ConnCloudActivity";

    private ListView lv;// 适配器控件------->V视图
    private ArrayAdapter<String> adapter;// 适配器------>C控制器
    private String[] data = { "我是第1个列表项", "我是第2个列表项", "我是第3个列表项", "我是第4个列表项",
            "我是第5个列表项", "我是第6个列表项", "我是第7个列表项", "我是第8个列表项", "我是第9个列表项",
            "我是第10个列表项", "我是第11个列表项"};// 数据源-->M

    private Button btnSearch;
    private Button btnNodeInfo;
    private Button btnDeletNode;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_cloud);

        init();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(ConnCloudActivity.this,
                        "第" + (position + 1) + "项被单击按下", Toast.LENGTH_LONG)
                        .show();
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

    private void init() {
        //找到ListView
        lv = findViewById(R.id.myList);
        // 实现适配器，利用系统定义的样式，加载数据源
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, data);
        // R.layout.cell 自己定义视图
        // android.R.layout.simple_list_item_1 系统定义视图样式
        // 绑定适配器到适配器控件上
        lv.setAdapter(adapter);

        btnSearch = findViewById(R.id.searchId);
        btnNodeInfo = findViewById(R.id.id_btn_nodeInfo);
        btnDeletNode = findViewById(R.id.id_btn_del_node);

        btnSearch.setOnClickListener(this);
        btnNodeInfo.setOnClickListener(this);
        btnDeletNode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchId:
                Log.v(tag, "搜索节点...");
                break;
            case R.id.id_btn_nodeInfo:
                Log.v(tag, "得到节点信息");
                break;
            case R.id.id_btn_del_node:
                Log.v(tag, "删除节点");
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
        ConnCloudThread.isruning = false;

        try {
            ConnCloudThread.mSocket.shutdownInput();
            ConnCloudThread.mSocket.shutdownOutput();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (ConnCloudThread.mSocket != null) {
                    ConnCloudThread.mSocket.close();
                    ConnCloudThread.mSocket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MainActivity.connCloudThread = null;
        finish();
    }
    /**
     * 显示提示消息的对话框
     * @author codingblock 2015-8-11
     * @param  context     上下文
     * @param  title       对话框标题
     * @param  message     对话框提示内容
     * @return
     */
    public AlertDialog.Builder simpleDialog(final Context context, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setMessage(message)
                .setPositiveButton("完成", null)
                .setNegativeButton("取消", null);
        return builder;
    }






}

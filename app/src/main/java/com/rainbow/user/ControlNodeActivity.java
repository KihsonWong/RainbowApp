package com.rainbow.user;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlNodeActivity extends Activity {
    private String tag = "ControlNodeActivity";

    private ListView listView;
    private List<Map<String, Object>> list;

    private Button btn_cmd;
    public static CtlRemark tempCtlRemark;
    private final int  UPDATE_SUB_LISTVIEW = 0;
    public static boolean updtSubListCmdFlag = false;
    public static boolean updtSubListShowFlag = false;
    private boolean isrunning = false;
    MySimpleAdapter  adapter;
    //private TextView text_node;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lv);

        Intent intent = getIntent();
        int i = intent.getIntExtra("index", 0);
        Log.e(tag, "string: " + i);

        listView = findViewById(R.id.list);

        tempCtlRemark = new CtlRemark();

        btn_cmd = findViewById(R.id.id_btn_command);
        //text_node = findViewById(R.id.id_txt_node);

        adapter = new MySimpleAdapter(this, getData(), R.layout.node_control, new String[] { "button",  "string" }, new int[] { R.id.id_btn_command, R.id.id_btn_show });
        listView.setAdapter(adapter);
        //setListAdapter(adapter);

        UpdateSubListThread updateSubListThread = new UpdateSubListThread(mHandler);
        updateSubListThread.start();
    }

    public class CtlRemark {

        private boolean isusing;
        private int node;
        private int window;
        private String content;

        public int getNode() {
            return node;
        }

        public int getWindow() {
            return window;
        }

        public String getContent() {
            return content;
        }

        public void setNode(int node) {
            this.node = node;
        }


        public void setWindow(int window) {
            this.window = window;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    class UpdateSubListThread extends Thread {

        private Handler mHandler;

        public UpdateSubListThread(Handler mHandler) {
            this.mHandler = mHandler;
            isrunning = true;
        }

        @Override
        public void run() {
            while (isrunning) {
                if (updtSubListCmdFlag)  {
                    updtSubListCmdFlag = false;
                    Log.e(tag, "send message: update sub list" + updtSubListCmdFlag + " " + updtSubListShowFlag);
                    //1.视图添加节点
                    mHandler.sendMessage(mHandler.
                            obtainMessage(UPDATE_SUB_LISTVIEW, 0, -1, -1));
                }
                else if (updtSubListShowFlag){
                    updtSubListShowFlag = false;
                    Log.e(tag, "send message: update sub list" + updtSubListCmdFlag + " " + updtSubListShowFlag);
                    //1.视图添加节点
                    mHandler.sendMessage(mHandler.
                            obtainMessage(UPDATE_SUB_LISTVIEW, 1, -1, -1));
                }
                try {
                    Thread.sleep(200L); // 线程休眠
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_SUB_LISTVIEW:
                    Log.i(tag, "update list view: " + msg.arg1);
                    Map<String, Object> map = list.get(tempCtlRemark.getWindow());
                    if (msg.arg1 == 0) {
                        map.put("button", tempCtlRemark.getContent());
                    } else if (msg.arg1 == 1) {
                        Log.i(tag, "tem: " + ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getNode() + " sdf" + tempCtlRemark.getNode());
                        if (ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getNode() == tempCtlRemark.getNode())
                            map.put("string", tempCtlRemark.getContent());
                    }
                    break;
            }
            adapter.notifyDataSetChanged();
        }
    };

    public class MySimpleAdapter extends SimpleAdapter {
        Context context ;
        public MySimpleAdapter(Context context,
                               List<? extends Map<String, ?>> data, int resource, String[] from,
                               int[] to) {
            super(context, data, resource, from, to);
            this.context = context ;
            // TODO Auto-generated constructor stub
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int temp_pos = position;
            View view= super.getView(position, convertView, parent);
            Button btn_cmd = view.findViewById(R.id.id_btn_command);
            btn_cmd.setTag(position);
            btn_cmd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showInfo("短按Button"+v.getTag());
                try {
                    MainActivity.connCloudThread.pckCommandMsg(
                            ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getNode(),
                            temp_pos,
                            0, "value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            });

            btn_cmd.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method stub
                    showInfo("长按Button"+v.getTag());
                    try {
                        MainActivity.connCloudThread.pckCommandMsg(
                                ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getNode(),
                                2,
                                temp_pos, "inquire");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });
            return view;
        }
    }


    private List<Map<String, Object>> getData() {
        //map.put(参数名字,参数值)
        //text_node.setText(ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getIdcode());
        list = new ArrayList<Map<String, Object>>();

        for (int i=0;i<ConnCloudActivity.nodeInfo[ConnCloudActivity.temp_index].getControlnum();i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("button", "commad" + i);
            map.put("string", "text" + i);
            list.add(map);
        }

        return list;
    }

    private void showInfo(String msg) {
        Toast.makeText(ControlNodeActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


}

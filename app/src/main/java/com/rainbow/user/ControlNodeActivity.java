package com.rainbow.user;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlNodeActivity extends ListActivity {

    //private Button btn_cmd;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // btn_cmd = findViewById(R.id.id_btn_command);

        SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.node_control, new String[] { "button",  "string" }, new int[] { R.id.id_btn_command, R.id.id_btn_show });
        setListAdapter(adapter);
    }

    private List<Map<String, Object>> getData() {
        //map.put(参数名字,参数值)
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("button", "commad");
        map.put("string", "text");
        list.add(map);

        return list;
    }
}

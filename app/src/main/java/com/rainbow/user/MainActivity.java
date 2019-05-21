package com.rainbow.user;

import android.widget.Button;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import static java.lang.System.currentTimeMillis;


public class MainActivity extends Activity {

    private EditText edtGateId = null;
    private long mExitTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtGateId = findViewById(R.id.id_gateway);

        Button btn_con_gateway = findViewById(R.id.id_btn_con_gateway);
        btn_con_gateway.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        ConnCloudActivity.class);
                startActivity(intent);
            }
        });

        Button btn_gateway_in = findViewById(R.id.id_btn_gateway_in);
        btn_gateway_in.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        GatewayInActivity.class);
                startActivityForResult(intent, 1000);
                //startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == 1000 && resultCode == 1001){

            String returnId = data.getStringExtra("returnId");

            edtGateId.setText(returnId);
        }
    }

    @Override
    public void onBackPressed(){
        if ((currentTimeMillis() - mExitTime) > 2000) {
            mExitTime = currentTimeMillis();
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
        } else {
            this.finish();
        }
    }
}

package com.rainbow.user;

import android.widget.Button;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnclient = findViewById(R.id.id_btn_client);
        btnclient.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        ClientActivity.class);
                startActivity(intent);

            }
        });

        Button btnserver = findViewById(R.id.id_btn_server);
        btnserver.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        ServerActivity.class);
                startActivity(intent);

            }
        });
    }
}

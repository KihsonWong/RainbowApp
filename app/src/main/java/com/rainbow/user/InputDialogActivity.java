package com.rainbow.user;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InputDialogActivity extends AppCompatActivity {

    private EditText edtSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_dialog);

        //获得输入框原本的数据
        Intent edtSendIntent = getIntent();
        String sendContext = edtSendIntent.getStringExtra("edtSend");
        Log.v("Dialog_copyData:", sendContext);
        edtSend =  findViewById(R.id.id_edt_dialog_sendArea);
        if (!"".equals(sendContext)) edtSend.setText(sendContext + "\n");
        edtSend.setSelection(edtSend.getText().toString().length());

        //返回对话框输入的数据
        Button btnReturnSendMes = findViewById(R.id.id_btn_dialog_returnsendmes);
        btnReturnSendMes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("data_return", edtSend.getText().toString());
                if (edtSend.getText().toString() != null) {
                    Log.v("Dialog_mes:", edtSend.getText().toString());
                }
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}

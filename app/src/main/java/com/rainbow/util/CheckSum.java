package com.example.util;

import android.widget.Button;

public class CheckSum {
    public static byte[] SendCmdBuf = { (byte) 0xAA, 0x00, 0x01, 0x43, 0x00,
            0x00, 0x00 };

    public static byte getCheckSum(byte[] bytes) {

        try {
            if (bytes.length > 2) {
                // 从第二位开始进行校验和校验
                byte checkSumValue = bytes[1];
                for (int i = 2; i < bytes.length - 1; i++) {
                    checkSumValue = (byte) (((bytes[i] ^ checkSumValue)) & 0xff);
                }
                return checkSumValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;

    }

    public static void sendCmdBtnMes(byte DOport, Button btncommx) {
        SendCmdBuf[5] = DOport;
        if (btncommx.getText().toString().contains("常开")) {
            SendCmdBuf[4] = 0x01;
            btncommx.setText("DO" + DOport + "常闭");
        } else if (btncommx.getText().toString().contains("常闭")) {
            SendCmdBuf[4] = 0x02;
            btncommx.setText("DO" + DOport + "常闪");
        } else if (btncommx.getText().toString().contains("常闪")) {
            SendCmdBuf[4] = 0x03;
            btncommx.setText("DO" + DOport + "常开");
        }
        SendCmdBuf[6] = getCheckSum(SendCmdBuf);
    }
}

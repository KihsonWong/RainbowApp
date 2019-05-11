package com.example.util;

public class ConvertDataFormat {
    /**
     * Convert hex string to byte[]
     *
     * @param hexString
     *            the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /*
     * public static void printHexString( byte[] b) { for (int i = 0; i <
     * b.length; i++) { String hex = Integer.toHexString(b[i] & 0xFF); if
     * (hex.length() == 1) { hex = '0' + hex; }
     * System.out.print(hex.toUpperCase() ); }
     *
     * }
     */

    public static int[] changeToIntArr(String result) {
        String[] sDI = result.split(" ");
        int j = sDI.length;
        int[] a = new int[j];
        for (int i = 0; i < j; i++) {
            a[i] = Integer.parseInt(sDI[i], 16);
        }
        return a;
    }

    /**
     * Convert char to byte
     *
     * @param c
     *            char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }
}

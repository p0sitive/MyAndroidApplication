package com.lesports.geneliveman.ble;

import android.text.TextUtils;

/**
 * Created by raysun on 15/11/19.
 */
public class BleCmdFactory {
    public static String getCommand(String cmd, String param) {
        StringBuilder sb = new StringBuilder();
        sb.append(Command.CMD_PREFIX);
        sb.append(Command.CMD_VERSION);
        sb.append("$$");
        sb.append(cmd);
        if(!TextUtils.isEmpty(param)){
            sb.append(bytesToHexString(param.getBytes()));
        }
        sb.append(Command.CMD_SUFFIX);
        String result = sb.toString();
        return result.replace("$$", intToHexString(result.length()/2));
    }

    public static String intToHexString(int value) {
        String result = Integer.toHexString(value).toUpperCase();

        if (result.length() == 1) {
            return "0" + result;
        }

        if (result.length() > 2) {
            return "00";
        }

        return result;
    }

    private static String bytesToHexString(byte[] src) {
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
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}

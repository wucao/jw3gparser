package com.xxg.jw3gparser;

/**
 * Created by wucao on 17/3/6.
 */
public class HexUtil {

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String encodeHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}

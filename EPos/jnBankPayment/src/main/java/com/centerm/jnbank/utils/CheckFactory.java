package com.centerm.jnbank.utils;

/**
 * Created by yuhc on 2016/12/8.
 * 业务数据校验工作类
 */

public class CheckFactory {


    /**
     * 判断业务数据是否接收完成
     * @param dataBytes     接收到的数据
     * @return  true 已经接收完了，false 未接收完
     */
    public static boolean isTradeDataReceivedComplete(byte[] dataBytes){
        if (dataBytes == null)
            return false;
        long receivedLen = dataBytes.length;
        if (receivedLen < 3)
            return false;
        long len = (dataBytes[0] << 8) | (dataBytes[1] & 0x00ff);
        if (receivedLen >= len+2)
            return true;

        return false;
    }
}

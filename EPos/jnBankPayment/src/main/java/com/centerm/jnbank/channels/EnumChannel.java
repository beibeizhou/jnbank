package com.centerm.jnbank.channels;

/**
 * 渠道定义
 * author:wanliang527</br>
 * date:2016/10/17</br>
 */

public enum EnumChannel {
    QIANBAO(2), ZJRC(1), CPAY(1),SHENGPAY(3);

    EnumChannel(int msgFormat) {
        this.msgFormat = msgFormat;
    }

    private int msgFormat;//报文格式，1-Json，2-8583

    public int getMsgFormat() {
        return msgFormat;
    }

}

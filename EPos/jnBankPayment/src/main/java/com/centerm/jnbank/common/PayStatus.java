package com.centerm.jnbank.common;

import com.centerm.jnbank.R;

/**
 * Created by ysd on 2017/4/6.
 * 支付组件响应码定义
 */

public enum PayStatus {
    SUCCESS("00", R.string.success),
    E01("E01", R.string.e01_tip),
    E02("E02", R.string.e02_tip),
    E03("E03", R.string.e03_tip),
    E04("E04", R.string.e04_tip),
    E05("E05", R.string.e05_tip),
    E06("E06", R.string.e06_tip),
    E07("E07", R.string.e07_tip),
    E08("E08", R.string.e08_tip),
    E09("E09", R.string.e09_tip),
    E10("E10", R.string.e10_tip),
    E11("E11", R.string.e11_tip),
    E12("E12", R.string.e12_tip);
    private String statusCode;
    private int msgId;

    PayStatus(String statusCode, int msgId) {
        this.statusCode = statusCode;
        this.msgId = msgId;
    }
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }
}

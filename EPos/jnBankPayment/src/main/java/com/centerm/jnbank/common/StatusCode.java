package com.centerm.jnbank.common;

import com.centerm.jnbank.R;

import org.apache.log4j.Logger;

/**
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public enum StatusCode {
    SUCCESS("666", R.string.success),
    UNKNOWN_HOST("N001", R.string.error_unknown_host),
    SOCKET_TIMEOUT("N002", R.string.error_socket_timeout),
    CONNECTION_EXCEPTION("N002", R.string.error_connection_exception),
    KEY_VERIFY_FAILED("S001", R.string.error_key_verify_failed),
    INVALID_TERMINAL_SN("E001", R.string.tip_invalid_sn),
    MAC_INVALID("E002", R.string.tip_mac_verify_failed),
    PIN_TIMEOUT("E003", R.string.tip_pin_timeout),
    UNKNOWN_REASON("E900", R.string.error_unknown_reason),
    TRADING_TERMINATES("K001", R.string.error_trading_terminate),
    TRADING_REFUSED("K002", R.string.error_trading_refused),
    TRADING_FALLBACK("K003", R.string.error_trading_fallback),
    TRADING_CHANGE_OTHER_FACE("K004", R.string.error_change_other_face),
    EMV_KERNEL_EXCEPTION("K003", R.string.error_kernel_exception),
    RESIGIN_IN("C001", R.string.tip_sign_in_again),
    DOWNLOAD_TMK("C002", R.string.tip_download_tmk),
    PACKAGE_ERROR("M001", R.string.tip_pakcage_msg_error),
    UNPACKAGE_ERROR("M002", R.string.tip_unpakcage_msg_error),
    FLOW_NUM_ERROR("R001", R.string.tip_flow_num_error),
    QR_TIME_OUT("Q006", R.string.tip_qr_time_out),//支付超时
    QR_CANCEL("Q007", R.string.tip_qr_canceled),//用户主动取消交易
    QR_CLOSE_ORDER("Q010", R.string.tip_qr_close),//用户主动取消交易
    QR_SHOW_TIMEOUT("Q009", R.string.tip_qr_timeout),//显示二维码超时
    QR_NO_NEED_SETTLEMENT("Q008", R.string.tip_qr_canceled);//无需结算;
    private String statusCode;
    private int msgId;

    StatusCode(String statusCode, int msgId) {
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

    public static StatusCode codeMap(String statusCode) {
        StatusCode[] values = StatusCode.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].statusCode.equals(statusCode)) {
                return values[i];
            }
        }
        Logger logger = Logger.getLogger(ISORespCode.class);
        logger.warn("错误码" + statusCode + "未定义");
        return null;
    }
}

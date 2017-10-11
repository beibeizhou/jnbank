package com.centerm.jnbank.net;

import org.apache.log4j.Logger;

/**
 * 队列式网络请求的回调接收器
 * author:wanliang527</br>
 * date:2016/11/20</br>
 */

public abstract class SequenceHandler {

    private Logger logger = Logger.getLogger(SequenceHandler.class);
    private SocketClient client;
    private ResponseHandler handler;
    private String currentReqTag;
    private boolean isSync;

    void bindClient(SocketClient client, boolean isSync) {
        this.client = client;
        this.isSync = isSync;
        handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                _return(currentReqTag, data, statusCode, msg);
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                _return(currentReqTag, null, code, msg);
            }
        };
    }

    private void _return(String reqTag, byte[] respData, String code, String msg) {
        logger.info("队列式网络请求==>[" + reqTag + "]结果返回==>" + (respData != null));
        onReturn(reqTag, respData, code, msg);
    }

    protected abstract void onReturn(String reqTag, byte[] respData, String code, String msg);

    public void sendNext(String reqTag, byte[] data) {
        logger.info("队列式网络请求==>开始发送[" + reqTag + "]");
        if (data == null) {
            logger.warn("队列式网络请求==>发送数据为空==>结束");
            finish();
            return;
        }
        currentReqTag = reqTag;
        if (isSync) {
            client.syncSendData(data, handler);
        } else {
            client.sendData(data, handler);
        }
    }

    public void finish() {
        logger.info("队列式网络请求==>结束");
    }
}

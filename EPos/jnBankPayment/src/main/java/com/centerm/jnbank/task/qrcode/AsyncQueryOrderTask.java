package com.centerm.jnbank.task.qrcode;

import android.content.Context;
import android.os.ConditionVariable;

import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.task.AsyncMultiRequestTask;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;

/**
 * 微信支付宝的订单查询任务
 * 策略：
 * 1、微信刷卡支付或支付宝条码支付时，只查询一次，如果结果返回USERPAYING，则在1分钟时间内每隔5秒重复查询，直到超时或返回SUCCESS
 * 2、微信扫码支付或支付宝扫码支付时，1分钟时间内每隔5秒重复查询，直到超时或返回SUCCESS
 * author:wanliang527</br>
 * date:2017/1/2</br>
 */

public class AsyncQueryOrderTask extends AsyncMultiRequestTask {
    private final static long QUERY_PERIOD = 5 * 1000;//轮询周期  8秒一次
    public final static long QUERY_DURATION = 90 * 1000;//轮询持续时间
    protected Logger logger = Logger.getLogger(this.getClass());
    private final static int MAX_TIMEOUT_RETRY_TIMES = 3;
    private int retryTimes;
    private ResponseHandler handler;
    private Map<String, String> tempMap;
    private long startTime;
    private long lastQueryTime;
    private Timer timer = new Timer();
    private ConditionVariable cv = new ConditionVariable();
    private Map<String, String> queryMap = new HashMap<>();
    private String transCode;
    public AsyncQueryOrderTask(Context context, Map<String, String> dataMap, Map<String, String> tempMap) {
        super(context, dataMap);
        this.tempMap = tempMap;
    }

    @Override
    protected String[] doInBackground(final String... params) {
        logger.info("开始进行订单查询==>交易码：" + params[0]);
        sleep(MEDIUM_SLEEP);
        transCode = params[0];
        startTime = lastQueryTime = System.currentTimeMillis();
        queryMap.clear();
        handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> resultMap = factory.unpack(transCode, data);
                String result = resultMap.get(iso_f39);
                if ("00".equals(result)) {
                    String msgStr = null;
                    if (null != resultMap.get(iso_f44)) {
                        msgStr = resultMap.get(iso_f44).split(" ")[1];
                    }
                    taskResult[0] = result;
                    taskResult[1] = msgStr;
                    tempMap.putAll(resultMap);
                    cv.open();
                } else if ("AC".equals(result)) {
                    String msgStr = null;
                    if (null != resultMap.get(iso_f44)) {
                        msgStr = resultMap.get(iso_f44).split(" ")[1];
                    }
                    taskResult[0] = result;
                    taskResult[1] = msgStr;
                    tempMap.putAll(resultMap);
                    cv.open();
                } else {
                    long next = nextActionDelay();
                    if (next >= 0) {
                        //继续发起查询
                        if (timer != null) {
                            timer.schedule(new PollingTask(), next);
                        }
                    } else {
                        taskResult[0] = StatusCode.QR_TIME_OUT.getStatusCode();
                        taskResult[1] = context.getString(StatusCode.QR_TIME_OUT.getMsgId());
                        cv.open();
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                long next = nextActionDelay();
                if (next >= 0) {
                    //继续发起查询
                    if (timer != null) {
                        timer.schedule(new PollingTask(), next);
                    }
                } else {
                    taskResult[0] = StatusCode.QR_TIME_OUT.getStatusCode();
                    taskResult[1] = context.getString(StatusCode.QR_TIME_OUT.getMsgId());
                    cv.open();
                }
            }
        };
        Object msgPkg = factory.pack(transCode, dataMap);
        client.syncSendData((byte[])msgPkg, handler);
        cv.block();
        return super.doInBackground(params);
    }


    public void specialCancel() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
        cv.open();
        if (!isCancelled()) {
            cancel(true);
        }
    }

    private long nextActionDelay() {
        long now = System.currentTimeMillis();
        if (now - startTime < QUERY_DURATION) {
            if (now - lastQueryTime < QUERY_PERIOD) {
                //延迟继续查询
                return QUERY_PERIOD - (now - lastQueryTime);
            } else {
                //马上继续查询
                return 0;
            }
        } else {
            //不再继续查询
            return -1;
        }
    }

    private class PollingTask extends TimerTask {
        @Override
        public void run() {
            if (queryMap != null && handler != null) {
                lastQueryTime = System.currentTimeMillis();
                Object msgPkg = factory.pack(transCode, dataMap);
                client.syncSendData((byte[])msgPkg, handler);
            }
        }
    }

}

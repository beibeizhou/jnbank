package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.net.ResponseHandler;

import java.util.Map;

import static com.centerm.jnbank.common.TransDataKey.iso_f39;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncBatchUploadDown extends AsyncMultiRequestTask {
    private String transCode = TransCode.SETTLEMENT_DONE;
    public AsyncBatchUploadDown(Context context, Map<String, String> dataMap) {
        super(context, dataMap);
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(LONG_SLEEP);
        Object msgPkg = factory.pack(transCode, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> mapData = factory.unpack(transCode, data);
                if (null == mapData) {
                    taskResult[0] = "99";
                    taskResult[1] = "请求失败";
                    logger.error("自动签退解包异常");
                } else {
                    String respCode = mapData.get(iso_f39);
                    if ("00".equals(respCode)) {
                        taskResult[0]="00";
                        taskResult[1]="请求成功";

                    } else {
                        taskResult[0]="99";
                        taskResult[1]="请求失败";
                        logger.error("上送完成请求返失败（有收到平台返回值）");
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0]="99";
                taskResult[1]="请求失败";
                logger.error("上送完成请求返失败（有收到平台返回值）");
            }
        };
        client.syncSendData((byte[]) msgPkg, handler);
        return taskResult;
    }
}

package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.net.ResponseHandler;

import java.util.HashMap;
import java.util.Map;

import static com.centerm.jnbank.common.TransDataKey.iso_f39;

/**
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public class AsyncScanRearchTask extends AsyncMultiRequestTask {
    private String transCode = TransCode.SCAN_SERCH;
    public Map<String, String> returnMap;
    public AsyncScanRearchTask(Context context, Map<String, String> dataMap, Map<String, String> returnMap) {
        super(context, dataMap);
        this.returnMap = returnMap;
        if (this.returnMap == null) {
            this.returnMap = new HashMap<>();
        }
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(LONG_SLEEP);
        Object msgPkg = factory.pack(transCode, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> mapData = factory.unpack(transCode, data);
                if (null != mapData) {
                    String respCode = mapData.get(iso_f39);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    taskResult[0] = isoCode.getCode();
                    taskResult[1] = context.getString(isoCode.getResId());
                    returnMap.clear();
                    returnMap.putAll(mapData);
                } else {
                    ISORespCode isoCode = ISORespCode.codeMap("E111");
                    taskResult[0] = isoCode.getCode();
                    taskResult[1] = context.getString(isoCode.getResId());
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0] = code;
                taskResult[1] = msg;
            }
        };
        client.syncSendData((byte[]) msgPkg, handler,transCode);
        return taskResult;
    }
}

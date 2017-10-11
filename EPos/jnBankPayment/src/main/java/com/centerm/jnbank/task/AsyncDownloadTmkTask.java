package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.iso8583.util.SecurityUtil;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;

/**
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public class AsyncDownloadTmkTask extends AsyncMultiRequestTask {
    private Map<String, String> returnMap;

    public AsyncDownloadTmkTask(Context context, Map<String, String> dataMap, Map<String, String> returnMap) {
        super(context, dataMap);
        this.returnMap = returnMap;
        if (this.returnMap == null) {
            this.returnMap = new HashMap<>();
        }
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(LONG_SLEEP);
        Object msgPkg = factory.pack(TransCode.OBTAIN_TMK, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> mapData = factory.unpack(TransCode.OBTAIN_TMK, data);
                if (null != mapData) {
                    returnMap.putAll(mapData);
                    String respCode = mapData.get(iso_f39);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    taskResult[0] = isoCode.getCode();
                    taskResult[1] = context.getString(isoCode.getResId());
                    if ("00".equals(respCode)) {
                        /*String merchantName = mapData.get(iso_f43);
                        String merchantCode = mapData.get(iso_f42);
                        String terminalCode = mapData.get(iso_f41);
                        BusinessConfig config = BusinessConfig.getInstance();
                        if (null != merchantName && !"".equals(merchantName)) {
                            config.setValue(context, BusinessConfig.Key.PRESET_MERCHANT_NAME, merchantName);
                        }
                        if (null != merchantCode && !"".equals(merchantCode)) {
                            config.setValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD, merchantCode);
                        }
                        if (null != terminalCode && !"".equals(terminalCode)) {
                            config.setValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD, terminalCode);
                        }*/
                        String tmk = mapData.get(iso_f62);
                        String value = tmk.substring(0, 32);
                        String checkValue = tmk.substring(32, 40);
                        taskRetryTimes = 0;
                        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
                        boolean result = false;
                        logger.info("[开始下发主密钥]kek:"+value+" checkValue:"+checkValue);
                        if (pinPadDev != null) {
                            //// TODO: 2016/12/1 sdk的问题，目前还无法通过sdk下发kek，也无法下发TEK
                            String KEK = Settings.getParam(context,Settings.KEY.KEK);
                            logger.debug("KEK为："+KEK);
                            String realTmk = SecurityUtil.decrypt3DES(KEK, value);
                            result = pinPadDev.loadTMK(realTmk,checkValue);
                        }
                        if (result) {
                            logger.info("[下发主密钥成功]");
                            returnMap.put(iso_f39, "00");//下载主密钥并发散成功
                            Settings.setTmkExist(context);//设置主密钥存在的标识
                            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
                        } else {
                            logger.info("[下发主密钥失败]");
                            StatusCode code = StatusCode.KEY_VERIFY_FAILED;
                            taskResult[0] = code.getStatusCode();
                            taskResult[1] = context.getString(code.getMsgId());
                        }
                    }
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
        client.syncSendData((byte[]) msgPkg, handler);
        return taskResult;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

}

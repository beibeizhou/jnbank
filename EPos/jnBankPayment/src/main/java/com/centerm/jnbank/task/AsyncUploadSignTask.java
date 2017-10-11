package com.centerm.jnbank.task;

import android.content.Context;
import android.graphics.Bitmap;

import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.net.SequenceHandler;
import com.centerm.jnbank.utils.ImageUtils;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;

/**
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public class AsyncUploadSignTask extends AsyncMultiRequestTask {
    private String transCode = TransCode.ELC_SIGNATURE;
    private Bitmap bitmap;
    private int uploadSignCount = 0;
    private int indexCount = 0;
    private SequenceHandler handler;
    public AsyncUploadSignTask(Context context, Bitmap bitmap,Map<String, String> dataMap) {
        super(context, dataMap);
        this.bitmap = bitmap;
        String countStr = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.PARAM_SIGN_UPLOAD_TIME);
        uploadSignCount = Integer.parseInt(countStr);
    }

    @Override
    protected String[] doInBackground(String... params) {
        logger.debug("进入【电子签名】上送任务");
            //保存签购单信息：
        if (bitmap != null && !bitmap.isRecycled()) {
            Map<String, String> tempData = new HashMap<>();
            tempData.put(iso_f11, dataMap.get(iso_f11));
            tempData.put(iso_f37, dataMap.get(iso_f37));
            tempData.put(iso_f44, dataMap.get(iso_f44));
            tempData.put(iso_f60, "07" + BusinessConfig.getInstance().getBatchNo(context) + "800");
            String big = ImageUtils.bitmaptoJBJGString(context, bitmap);
            logger.debug("压缩后数据长度："+big.length());
            tempData.put(iso_f62, big);
            final Object msgPkg = factory.pack(transCode, tempData);
            if (handler == null) {
                handler = new SequenceHandler() {

                    @Override
                    protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                        Map<String, String> mapData = factory.unpack(transCode, respData);
                        if (null != mapData) {
                            String respCode = mapData.get(iso_f39);
                            if ("00".equals(respCode)) {
                                logger.debug("报文返回【电子签名】上送成功");
                            } else {
                                beginUpload(msgPkg);
                            }
                        } else {
                            //beginUpload(msgPkg);
                            logger.debug("【电子签名】无报文返回");
                        }
                    }
                };
            }
           beginUpload(msgPkg);
        } else {
            logger.debug("没有电子签名图片");
        }
        return taskResult;
    }

    private void beginUpload(Object msgPkg) {
        if (indexCount++ <= uploadSignCount) {
            logger.debug("第"+indexCount+"次上送电子签名");
            client.syncSendSequenceData(transCode, (byte[]) msgPkg, handler);
        } else {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            logger.error("电子签名上送次数已经超限"+indexCount);
        }
    }
}

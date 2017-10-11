package com.centerm.jnbank.task.qrcode;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.task.AsyncMultiRequestTask;

import org.apache.log4j.Logger;

import java.util.Map;

import static com.centerm.jnbank.common.TransCode.NEED_INSERT_TABLE_SETS;
import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f64;
import static com.centerm.jnbank.common.TransDataKey.key_resp_code;
import static com.centerm.jnbank.common.TransDataKey.key_resp_msg;

/**
 * 微信支付宝的扫码支付任务（被扫）
 * author:wanliang527</br>
 * date:2017/1/1</br>
 */

public class AsyncGetQrCodeTask extends AsyncMultiRequestTask {
    protected Logger logger = Logger.getLogger(this.getClass());
    private Map<String, String> tempMap;
    protected Map<String, String> dataMap;
    private CommonDao<TradeInfo> tradeDao;
    public AsyncGetQrCodeTask(Context context, Map<String, String> dataMap, Map<String, String> tempMap) {
        super(context, dataMap);
        this.tempMap = tempMap;
        this.dataMap = dataMap;
        tradeDao = new CommonDao<>(TradeInfo.class, new DbHelper(context));
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (params == null || params.length < 1) {
            throw new IllegalArgumentException(getClass().getSimpleName() + "==>请传入交易码！");
        }
        logger.info("开始获取二维码==>交易码：" + params[0]);
        final String transCode = params[0];
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> resultMap = factory.unpack(transCode, data);
                tempMap.putAll(resultMap);
                String msgStr = null;
                if (null != resultMap.get(iso_f44)) {
                    msgStr = resultMap.get(iso_f44).split(" ")[1];
                }
                taskResult[0] = tempMap.get(iso_f39);
                taskResult[1] = msgStr;
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0] = code;
                taskResult[1] = msg;
                tempMap.put(key_resp_code, code);
                tempMap.put(key_resp_msg, msg);
            }
        };
        Object msgPkg = factory.pack(transCode, dataMap);
        if (msgPkg instanceof byte[]) {
            String iso11 = dataMap.get(iso_f11);
            //保存交易信息到数据库
            if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
                TradeInfo info = new TradeInfo(transCode, dataMap);
                info.setIso_f64(dataMap.get(iso_f64));
                boolean r = tradeDao.save(info);
                logger.info(iso11 + "==>" + transCode + "==>插入交易流水表中==>" + r);
            }
            logger.info("结束获取二维码==>交易码：" + params[0]);
            client.syncSendData((byte[]) msgPkg, handler,"SHOWCODE");
        }
        return super.doInBackground(params);
    }

    public void cancelRequest() {
        client.cancelShowCodeSocket();
    }

}

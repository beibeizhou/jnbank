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
import static com.centerm.jnbank.common.TransDataKey.iso_f36;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f55;
import static com.centerm.jnbank.common.TransDataKey.iso_f64;
import static com.centerm.jnbank.common.TransDataKey.key_resp_code;
import static com.centerm.jnbank.common.TransDataKey.key_resp_msg;

/**
 * author:wanliang527</br>
 * date:2017/1/5</br>
 */

public class AsyncSendQrTask extends AsyncMultiRequestTask {
    protected Logger logger = Logger.getLogger(this.getClass());
    private Map<String, String> tempMap;
    private CommonDao<TradeInfo> dao;
    private TradeInfo curTradeInfo;
    private String transCode;
    public AsyncSendQrTask(Context context, Map<String, String> dataMap, Map<String, String> tempMap) {
        super(context, dataMap);
        this.tempMap = tempMap;
        dao = new CommonDao<>(TradeInfo.class, new DbHelper(context));
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (params == null || params.length < 1) {
            throw new IllegalArgumentException(getClass().getSimpleName() + "==>请传入交易码！");
        } else {
            transCode = params[0];
        }
        logger.info("开始二维码消费=>交易码：" + transCode);
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
            if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
                TradeInfo info = new TradeInfo(transCode, dataMap);
                info.setIso_f55_send(dataMap.get(iso_f55));
                info.setIso_f64(dataMap.get(iso_f64));
                info.setKey_bak_iso_f36(dataMap.get(iso_f36));
                boolean r = dao.save(info);
                logger.info(dataMap.get(iso_f11) + "==>" + transCode + "==>插入交易流水表中==>" + r);

            }
            client.syncSendData((byte[]) msgPkg, handler);
        }
        logger.info("结束二维码消费==>交易码：" + params[0]);
        return super.doInBackground(params);
    }


}

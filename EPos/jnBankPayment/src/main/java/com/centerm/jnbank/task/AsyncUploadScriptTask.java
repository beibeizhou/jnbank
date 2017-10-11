package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;
import com.centerm.jnbank.net.SocketClient;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2016/11/29</br>
 */

public abstract class AsyncUploadScriptTask extends AsyncMultiRequestTask {

    private CommonDao<TradeInfo> dao;
    private List<TradeInfo> tradeList;
    private int index = 0;
    private int size;

    public AsyncUploadScriptTask(Context context, Map<String, String> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(TradeInfo.class, new DbHelper(context));
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(MEDIUM_SLEEP);
        try {
            tradeList = dao.queryBuilder().where().eq("scriptResult", 1).or().eq("scriptResult", 2).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (tradeList == null || tradeList.size() == 0) {
            sleep(LONG_SLEEP);
            return new String[0];
        }
        size = tradeList.size();
        publishProgress(size, index + 1);
        Map<String, String> reqMap = tradeList.get(index).convert2Map();
        reqMap.put(TransDataKey.iso_f11_origin, reqMap.get(TransDataKey.iso_f11));
        reqMap.put(TransDataKey.iso_f60_origin, reqMap.get(TransDataKey.iso_f60));
        Object msgPacket = factory.pack(TransCode.UPLOAD_SCRIPT_RESULT, reqMap);
        final SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, String> resp = factory.unpack(reqTag, respData);
                    String respCode = resp.get(TransDataKey.iso_f39);
                    logger.info("脚本执行结果上送结果==>返回码==>" + respCode);
                    if ("00".equals(respCode)) {
                        //通知上送完成
                        TradeInfo trade = tradeList.get(index);
                        trade.setScriptResult(3);
                        dao.update(trade);
                    }
                    if (++index < size) {
                        logger.debug("继续执行脚本执行结果上送");
                        sleep(MEDIUM_SLEEP);
                        publishProgress(size, index + 1);
                        Map<String, String> reqMap = tradeList.get(index).convert2Map();
                        reqMap.put(TransDataKey.iso_f11_origin, reqMap.get(TransDataKey.iso_f11));
                        reqMap.put(TransDataKey.iso_f60_origin, reqMap.get(TransDataKey.iso_f60));
                        Object msgPacket = factory.pack(TransCode.UPLOAD_SCRIPT_RESULT, reqMap);
                        sendNext(TransCode.UPLOAD_SCRIPT_RESULT, (byte[]) msgPacket);
                    }
                } else {
                    logger.warn("脚本执行结果上送结果==>返回码==>" + code + "==>返回信息==>" + msg);
                    if (++index < size) {
                        logger.debug("继续执行脚本执行结果上送");
                        sleep(MEDIUM_SLEEP);
                        publishProgress(size, index + 1);
                        Map<String, String> reqMap = tradeList.get(index).convert2Map();
                        reqMap.put(TransDataKey.iso_f11_origin, reqMap.get(TransDataKey.iso_f11));
                        reqMap.put(TransDataKey.iso_f60_origin, reqMap.get(TransDataKey.iso_f60));
                        Object msgPacket = factory.pack(TransCode.UPLOAD_SCRIPT_RESULT, reqMap);
                        sendNext(TransCode.UPLOAD_SCRIPT_RESULT, (byte[]) msgPacket);
                    }
                }
            }
        };
        SocketClient client = SocketClient.getInstance(context);
        client.syncSendSequenceData(TransCode.POS_STATUS_UPLOAD, (byte[]) msgPacket, handler);
        return new String[0];
    }
}

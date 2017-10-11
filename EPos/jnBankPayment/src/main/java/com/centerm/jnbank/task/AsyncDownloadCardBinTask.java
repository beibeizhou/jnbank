package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.BinData;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;
import com.centerm.jnbank.net.SocketClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransCode.DOWNLOAD_PARAMS_FINISHED;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;

/**
 * 异步下载卡BIN任务（非卡BIN黑名单）
 * author:wanliang527</br>
 * date:2016/11/30</br>
 */
public abstract class AsyncDownloadCardBinTask extends AsyncMultiRequestTask {

    private CommonDao<BinData> dao;

    public AsyncDownloadCardBinTask(Context context, Map<String, String> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(BinData.class, new DbHelper(context));
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        dataMap.put(TransDataKey.KEY_PARAMS_TYPE, "5");
        String lastBinNo = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO);
        dataMap.put(iso_f62, lastBinNo);
        Object msgPacket = factory.pack(TransCode.DOWNLOAD_CARD_BIN, dataMap);
        final SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, String> resp = factory.unpack(reqTag, respData);
                    String respCode = resp.get(iso_f39);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    String iso62 = resp.get(iso_f62);
                    logger.debug("iso62数据为：" + iso62);
                    int times = 0;//卡BIN下载次数
                    boolean dbResult;
                    switch (reqTag) {
                        case TransCode.DOWNLOAD_CARD_BIN:
                            if ("00".equals(respCode)) {
                                String flag = iso62.substring(0, 1);
                                String lastNo = iso62.substring(1, 6).trim();
                                int intLastNo = Integer.parseInt(lastNo);
                                if ("1".equals(flag)) {
                                    //后续无卡BIN下载
                                    List<BinData> binList = BinData.parse(iso62);
                                    dao.save(binList);
                                    ++intLastNo;
                                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO, intLastNo + "");
                                    publishProgress(0, -1);
                                    sleep(LONG_SLEEP);
                                    Object pkgMsg = factory.pack(DOWNLOAD_PARAMS_FINISHED, dataMap);
                                    sendNext(DOWNLOAD_PARAMS_FINISHED, (byte[]) pkgMsg);
                                } else if ("2".equals(flag)) {
                                    //后续有卡BIN下载
                                    List<BinData> binList = BinData.parse(iso62);
                                    dao.save(binList);
                                    ++intLastNo;
                                    String formatedLastNo = String.format(Locale.CHINA, "%03d", intLastNo);
                                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO, formatedLastNo);
                                    logger.debug("要发送的62域为："+formatedLastNo);
                                    dataMap.put(iso_f62, formatedLastNo);
                                    publishProgress(0, intLastNo);
                                    sleep(MEDIUM_SLEEP);
                                    Object msgPacket = factory.pack(TransCode.DOWNLOAD_CARD_BIN, dataMap);
                                    sendNext(TransCode.DOWNLOAD_CARD_BIN, (byte[]) msgPacket);
                                } else {
                                    //无卡BIN需要更新
                                    publishProgress(0, -2);
                                }
                            }
                            break;
                        case DOWNLOAD_PARAMS_FINISHED:
                            //下载结束报文结果，不关心
                            BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO,"000");
                            break;
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
        publishProgress(0, Integer.valueOf(lastBinNo));
        sleep(LONG_SLEEP);
        SocketClient client = SocketClient.getInstance(context);
        client.syncSendSequenceData(TransCode.DOWNLOAD_CARD_BIN, (byte[]) msgPacket, handler);
        return result;
    }

}

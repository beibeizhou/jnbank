package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;

import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f23;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f48;
import static com.centerm.jnbank.common.TransDataKey.iso_f55;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;
import static com.centerm.jnbank.common.TransDataKey.iso_f64;

/**
 * Created by ysd on 2016/12/20.
 */

public class AsyncUploadIcDataTask extends AsyncMultiRequestTask {
    private List<TradeInfo> tradeInfos;
    private int index;
    private String transCode = TransCode.TRANS_IC_DETAIL;
    private CommonDao<TradeInfo> tradeDao;
    public AsyncUploadIcDataTask(Context context, Map<String, String> dataMap, List<TradeInfo> tradeInfos) {
        super(context, dataMap);
        this.tradeInfos = tradeInfos;
        tradeDao = new CommonDao<>(TradeInfo.class, new DbHelper(context));
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(SHORT_SLEEP);
        if (tradeInfos == null || tradeInfos.size() == 0) {
            return super.doInBackground(params);
        }
        index = 0;
        final TradeInfo cardInfo = tradeInfos.get(index);
        publishProgress(tradeInfos.size(),index+1);
        initData(cardInfo);
        Object msgPkg = factory.pack(transCode, dataMap);
        SequenceHandler handler = new SequenceHandler(){

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                sleep(SHORT_SLEEP);
                taskResult[0] = code;
                taskResult[1] = msg;
                if (respData != null) {
                    Map<String, String> resp = factory.unpack(transCode, respData);
                    String respCode = resp.get(iso_f39);
                    if ("00".equals(respCode)) {
                        logger.error("IC卡第" + (index + 1) + "条记录上送成功");
                        cardInfo.setBatchSuccess(true);
                        //更新上送状态
                        tradeDao.update(cardInfo);
                        if (hasNext()) {
                            TradeInfo info = tradeInfos.get(++index);
                            publishProgress(tradeInfos.size(),index+1);
                            initData(info);
                            Object msgPkg = factory.pack(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    } else {
                        logger.error("IC卡第" + (index + 1) + "条记录被拒绝");
                        cardInfo.setSendCount(99);
                        //更新上送状态
                        tradeDao.update(cardInfo);
                        if (hasNext()) {
                            TradeInfo info = tradeInfos.get(++index);
                            publishProgress(tradeInfos.size(),index+1);
                            initData(info);
                            Object msgPkg = factory.pack(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    }
                } else {
                    logger.error("IC卡第" + (index + 1) + "条记录上送失败");
                    if (hasNext()) {
                        TradeInfo info = tradeInfos.get(++index);
                        publishProgress(tradeInfos.size(),index+1);
                        initData(info);
                        Object msgPkg = factory.pack(transCode, dataMap);
                        sendNext(transCode, (byte[]) msgPkg);
                    }
                }
            }
        };
        client.syncSendSequenceData(transCode, (byte[]) msgPkg, handler);
        return super.doInBackground(params);
    }
    private boolean hasNext() {
        if (index + 1 < tradeInfos.size()) {
            return true;
        }
        return false;
    }

    private void initData(TradeInfo icCard) {
        dataMap.clear();
        dataMap.put(iso_f2, icCard.getIso_f2());//主账号
        dataMap.put(iso_f4, icCard.getIso_f4());//交易金额
        dataMap.put(iso_f11, icCard.getIso_f11());//POS终端流水号
        dataMap.put(iso_f22, icCard.getIso_f22());//服务点输入方式码
        dataMap.put(iso_f23, icCard.getIso_f23());//卡片序列号
        dataMap.put(iso_f41, icCard.getIso_f41());//受卡机终端标识码
        dataMap.put(iso_f42, icCard.getIso_f42());//受卡方标识码
        dataMap.put(iso_f48, "11");
        if (!icCard.getTransCode().equals(TransCode.AUTH_COMPLETE)) {
            dataMap.put(iso_f55, icCard.getIso_f55());//IC卡数据域
        }
        dataMap.put(iso_f60, "00" +BusinessConfig.getInstance().getBatchNo(context) + "203");//60域
        dataMap.put(iso_f62, "610000" + icCard.getIso_f4() + "156");//原批次号+原流水号+交易时间
        dataMap.put(iso_f64, "1234567890123456");//mac填充占位

        int count = icCard.getSendCount();
        icCard.setSendCount(++count);
        //更改上送次数
        tradeDao.update(icCard);
    }
}

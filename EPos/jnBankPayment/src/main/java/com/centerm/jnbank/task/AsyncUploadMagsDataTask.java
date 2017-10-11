package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;
import com.centerm.jnbank.utils.DataHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f48;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;

/**
 * Created by ysd on 2016/12/20.
 */

public class AsyncUploadMagsDataTask extends AsyncMultiRequestTask {
    private List<TradeInfo> tradeInfos;
    private int index;
    private String transCode = TransCode.TRANS_CARD_DETAIL;
    private CommonDao<TradeInfo> tradeDao;
    private List<String> strings = new ArrayList<>();
    private List<TradeInfo> objArray1 = new ArrayList<>();
    private List<List<TradeInfo>> objArray2 = new ArrayList<>();
    public AsyncUploadMagsDataTask(Context context, Map<String, String> dataMap, List<TradeInfo> tradeInfos) {
        super(context, dataMap);
        this.tradeInfos = tradeInfos;
        tradeDao = new CommonDao<>(TradeInfo.class, new DbHelper(context));
        initMagsCardData();
    }

    private void initMagsCardData() {
        StringBuffer buffer = new StringBuffer();
        strings.clear();
        objArray2.clear();
        objArray1.clear();
        if (null != tradeInfos && tradeInfos.size() > 0) {
            for (int i = 1; i <= tradeInfos.size(); i++) {
                TradeInfo info = tradeInfos.get(i - 1);
                String RMB = info.getIso_f49();
                String moneyType = null;
                if (null != RMB && "156".equals(RMB)) {
                    moneyType = "00";
                } else {
                    moneyType = "01";
                }
                buffer.append(moneyType);
                buffer.append(info.getIso_f11());
                buffer.append(DataHelper.formatToXLen(info.getIso_f2(), 20));
                buffer.append(info.getIso_f4());
                objArray1.add(info);
                if (i % 8 == 0) {
                    strings.add("08" + buffer.toString());
                    List<TradeInfo> tempInfos = new ArrayList<>();
                    tempInfos.addAll(objArray1);
                    objArray2.add(tempInfos);
                    buffer.delete(0, buffer.length());
                    objArray1.clear();
                }
            }
            if (buffer.length() > 0) {
                int count = buffer.length() / 40;
                strings.add("0" + count + buffer.toString());
                List<TradeInfo> tempInfos = new ArrayList<>();
                tempInfos.addAll(objArray1);
                objArray2.add(tempInfos);
            }
            buffer.delete(0, buffer.length());
            objArray1.clear();
        }

    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(SHORT_SLEEP);
        if (strings == null || strings.size() == 0) {
            return super.doInBackground(params);
        }
        index = 0;
        initData(strings.get(index));
        publishProgress(strings.size(),index+1);
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
                        logger.error("磁条卡第" + (index + 1) + "批记录上送成功");
                        List<TradeInfo> infos = objArray2.get(index);
                        for (TradeInfo info :
                                infos) {
                            info.setBatchSuccess(true);
                            tradeDao.update(info);
                        }
                        if (hasNext()) {
                            initData(strings.get(++index));
                            publishProgress(strings.size(),index+1);
                            Object msgPkg = factory.pack(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    } else {
                        logger.error("磁条卡第" + (index + 1) + "批记录被拒绝");
                        List<TradeInfo> infos = objArray2.get(index);
                        for (TradeInfo info :
                                infos) {
                            info.setSendCount(99);
                            tradeDao.update(info);
                        }
                        if (hasNext()) {
                            initData(strings.get(++index));
                            publishProgress(strings.size(),index+1);
                            Object msgPkg = factory.pack(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    }
                } else {
                    logger.error("磁条卡第" + (index + 1) + "批记录上送失败");
                    if (hasNext()) {
                        initData(strings.get(++index));
                        publishProgress(strings.size(),index+1);
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
        if (index + 1 < strings.size()) {
            return true;
        }
        return false;
    }

    private void initData(String f48) {
        dataMap.clear();
        dataMap.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
        dataMap.put(iso_f41, BusinessConfig.getInstance().getValue(context,BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
        dataMap.put(iso_f42, BusinessConfig.getInstance().getValue(context,BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
        dataMap.put(iso_f48, f48);
        dataMap.put(iso_f60, "00" + BusinessConfig.getInstance().getBatchNo(context) + "201");//60域
        List<TradeInfo> infos = objArray2.get(index);
        for (TradeInfo info :
                infos) {
            int count = info.getSendCount();
            info.setSendCount(++count);
            //更改上送次数
            tradeDao.update(info);
        }
    }
}

package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;

import java.util.List;
import java.util.Map;

import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f14;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f23;
import static com.centerm.jnbank.common.TransDataKey.iso_f25;
import static com.centerm.jnbank.common.TransDataKey.iso_f26;
import static com.centerm.jnbank.common.TransDataKey.iso_f3;
import static com.centerm.jnbank.common.TransDataKey.iso_f35;
import static com.centerm.jnbank.common.TransDataKey.iso_f36;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f38;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f49;
import static com.centerm.jnbank.common.TransDataKey.iso_f52;
import static com.centerm.jnbank.common.TransDataKey.iso_f53;
import static com.centerm.jnbank.common.TransDataKey.iso_f55;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f61;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;
import static com.centerm.jnbank.common.TransDataKey.iso_f63;

/**
 * Created by ysd on 2016/12/20.
 */

public class AsyncUploadRefundDataTask extends AsyncMultiRequestTask {
    private List<TradeInfo> tradeInfos;
    private int index;
    private String transCode = TransCode.TRANS_FEFUND_DETAIL;
    private CommonDao<TradeInfo> tradeDao;
    public AsyncUploadRefundDataTask(Context context, Map<String, String> dataMap, List<TradeInfo> tradeInfos) {
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
                        logger.error("退货第" + (index + 1) + "条记录上送成功");
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
                        logger.error("退货第" + (index + 1) + "条记录被拒绝");
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
                    logger.error("退货第" + (index + 1) + "条记录上送失败");
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

    private void initData(TradeInfo refundInfo) {
        dataMap.clear();
        dataMap.put(iso_f2, refundInfo.getIso_f2());//主账号
        dataMap.put(iso_f3, refundInfo.getIso_f3());//交易处理码
        dataMap.put(iso_f4, refundInfo.getIso_f4());//交易金额
        dataMap.put(iso_f11, refundInfo.getIso_f11());//POS终端流水号
        dataMap.put(iso_f14, refundInfo.getIso_f14());//卡有效期
        dataMap.put(iso_f22, refundInfo.getIso_f22());//服务点输入方式码
        if (null != refundInfo.getIso_f23()) {
            dataMap.put(iso_f23, refundInfo.getIso_f23());//卡片序列号
        }
        dataMap.put(iso_f25, refundInfo.getIso_f25());//服务点条件码
        if (null != refundInfo.getIso_f26()) {
            dataMap.put(iso_f26, refundInfo.getIso_f26());//服务点PIN获取码
        }
        dataMap.put(iso_f35, refundInfo.getIso_f35());//2磁道数据
        dataMap.put(iso_f36, refundInfo.getIso_f36());//3磁道数据
        dataMap.put(iso_f37, refundInfo.getIso_f37());//检索参考号
        if (null != refundInfo.getIso_f38()) {
            dataMap.put(iso_f38, refundInfo.getIso_f38());//服务点PIN获取码
        }
        dataMap.put(iso_f41, refundInfo.getIso_f41());//受卡机终端标识码
        dataMap.put(iso_f42, refundInfo.getIso_f42());//受卡方标识码
        dataMap.put(iso_f49, refundInfo.getIso_f49());//交易货币代码
        if (null != refundInfo.getIso_f52()) {
            dataMap.put(iso_f52, refundInfo.getIso_f52());//个人标识码数据
        }
        if (null != refundInfo.getIso_f53()) {
            dataMap.put(iso_f53, refundInfo.getIso_f53());//个人标识码数据
        }
        if (null != refundInfo.getIso_f55()) {
            dataMap.put(iso_f55, refundInfo.getIso_f55());//IC卡数据域
        }

        dataMap.put(iso_f60, refundInfo.getIso_f60());//60域
        dataMap.put(iso_f61, refundInfo.getIso_f61());//61域
        if (null != refundInfo.getIso_f62()) {
            dataMap.put(iso_f62, refundInfo.getIso_f62());//IC卡数据域
        }
        dataMap.put(iso_f63, refundInfo.getIso_f63());

        int count = refundInfo.getSendCount();
        refundInfo.setSendCount(++count);
        //更改上送次数
        tradeDao.update(refundInfo);
    }
}

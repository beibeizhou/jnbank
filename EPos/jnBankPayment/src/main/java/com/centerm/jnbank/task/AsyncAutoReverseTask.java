package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.ReverseInfo;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f39;

/**
 * 异步进行自动冲正任务
 * author:wanliang527</br>
 * date:2016/12/4</br>
 */

public class AsyncAutoReverseTask extends AsyncMultiRequestTask {

    private CommonDao<ReverseInfo> dao;
    private List<ReverseInfo> reverseList;
    private int index;//冲正索引
    private int times;//重试次数
    private String transCode;
    private Map<String,String> dataMap;

    public AsyncAutoReverseTask(Context context, Map<String, String> dataMap) {
        super(context, dataMap);
        this.dataMap = new HashMap<>();
        dao = new CommonDao<>(ReverseInfo.class, new DbHelper(context));
        reverseList = dao.query();
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(SHORT_SLEEP);
        if (reverseList == null || reverseList.size() == 0) {
            logger.warn("冲正表信息为空==>任务结束");
            return super.doInBackground(params);
        }
        logger.debug("开始筛选隔日交易");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String today = formatter.format(new Date());
        for (int i = 0; i < reverseList.size(); i++) {
            ReverseInfo info = reverseList.get(i);
            String date = info.getTransTime();
            if (date != null && date.length() >= 8) {
                if (!today.equals(date.substring(0, 8))) {
                    logger.warn("隔日交易不冲正==>当前日期：" + today + "==>此笔交易日期：" + info.getTransTime());
                    reverseList.remove(info);
                    dao.delete(info);
                }
            }
        }
        logger.debug("隔日交易筛选完成");
        if (reverseList.size() == 0) {
            logger.warn("冲正表信息为空==>任务结束");
            return super.doInBackground(params);
        }
        publishProgress(-1);//告诉UI层准备开始冲正
        sleep(LONG_SLEEP);
        logger.info("开始冲正==>待冲正总笔数：" + reverseList.size() + "==>当前索引：" + index);
        times = 0;
        ReverseInfo info = reverseList.get(index);
        publishProgress(index + 1, times + 1);
        Object msgPkg = factory.pack(initReverseData(info), dataMap);
        SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                ReverseInfo info = reverseList.get(index);
                taskResult[0] = code;
                taskResult[1] = msg;
                if (respData != null) {
                    Map<String, String> resp = factory.unpack(transCode, respData);
                    String f39 = resp.get(iso_f39);
                    ISORespCode respCode = ISORespCode.codeMap(f39);
                    logger.warn(info.getIso_f11() + "==>冲正结果返回码==>" + f39);
                    if (ISORespCode.ISO0.equals(respCode)
                            || ISORespCode.ISO12.equals(respCode)
                            || ISORespCode.ISO25.equals(respCode)) {
                    }
                    boolean dbResult = dao.delete(info);
                    logger.info(info.getIso_f11() + "==>冲正成功==>删除冲正表记录==>" + dbResult);
                    if (hasNext()) {
                        logger.info("继续冲正下一笔==>待冲正总笔数：" + reverseList.size() + "==>当前索引：" + index);
                        times = 0;
                        info = reverseList.get(++index);
                        publishProgress(index + 1, times + 1);
                        Object msgPkg = factory.pack(initReverseData(info), dataMap);
                        sleep(MEDIUM_SLEEP);
                        sendNext(transCode, (byte[]) msgPkg);
                    }
                    sleep(LONG_SLEEP);
                } else {
                    //冲正无响应的情况，尝试再次冲正
                    if (++times < BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.PARAM_REVERSE_COUNT)) {
                        logger.info(info.getIso_f11() + "==>冲正失败==>" + "尝试再次发起冲正" + times);
                        publishProgress(index + 1, times + 1);
                        Object msgPkg = factory.pack(initReverseData(info), dataMap);
                        sleep(MEDIUM_SLEEP);
                        sendNext(transCode, (byte[]) msgPkg);
                    } else {
                        boolean dbResult = dao.delete(info);
                        logger.warn(info.getIso_f11() + "==>冲正次数已超限==>删除冲正表记录==>不再进行冲正==>" + dbResult);
                        if (hasNext()) {
                            logger.info("继续冲正下一笔==>待冲正总笔数：" + reverseList.size() + "==>当前索引：" + index);
                            times = 0;
                            info = reverseList.get(++index);
                            publishProgress(index + 1, times + 1);
                            Object msgPkg = factory.pack(initReverseData(info), dataMap);
                            sleep(MEDIUM_SLEEP);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    }
                }
            }
        };
        client.syncSendSequenceData(transCode, (byte[]) msgPkg, handler);
        logger.info("本次冲正结束");
        publishProgress(-2);
        sleep(MEDIUM_SLEEP);
        return super.doInBackground(params);
    }

    private String initReverseData(ReverseInfo reverseInfo) {
        String iso11 = reverseInfo.getIso_f11();
        transCode = reverseInfo.getTransCode() + "_REVERSE";
        logger.debug("冲正信息==>" + reverseInfo.toString());
        logger.info(iso11 + "==>当前冲正交易对应的交易码：" + transCode);
        dataMap.clear();
        dataMap.putAll(reverseInfo.convert2Map());
//        dataMap.put(iso_f11_origin, reverseInfo.getIso_f11());//原流水号
//        dataMap.put(iso_f60_origin, reverseInfo.getIso_f60());//原批次号(60.2域中存储)
        return transCode;
    }

    private boolean hasNext() {
        if (index + 1 < reverseList.size()) {
            return true;
        }
        return false;
    }


    /*private void updateRetryTimes(ReverseInfo info) {
        int times = info.getRetryTimes();
        if (times < BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.PARAM_REVERSE_COUNT)) {
            info.setRetryTimes(++times);
            boolean dbResult = dao.update(info);
            logger.info(info.getIso_f11() + "==>更新冲正重试次数==>" + dbResult);
        } else {

        }
    }*/
}

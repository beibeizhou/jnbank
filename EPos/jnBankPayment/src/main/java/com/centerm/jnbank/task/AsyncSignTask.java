package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumWorkKeyType;
import com.centerm.jnbank.bean.PrinterItem;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.utils.CommonUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f12;
import static com.centerm.jnbank.common.TransDataKey.iso_f13;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;

/**
 * 异步签到任务。签到后需要解析报文头，根据报文头的处理要求，做下一步操作
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public class AsyncSignTask extends AsyncMultiRequestTask {

    //private Map<String, String> stringMap = new HashMap<>();
    private Map<String, String> returnMap;

    public AsyncSignTask(Context context, Map<String, String> dataMap, Map<String, String> returnMap) {
        super(context, dataMap);
        //initParamTip();
        this.returnMap = returnMap;
        if (this.returnMap == null) {
            this.returnMap = new HashMap<>();
        }
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(LONG_SLEEP);
        Object msgPkg = factory.pack(TransCode.SIGN_IN, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> mapData = factory.unpack(TransCode.SIGN_IN, data);
                if (null != mapData) {
                    returnMap.putAll(mapData);
                    String respCode = mapData.get(iso_f39);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    taskResult[0] = isoCode.getCode();
                    taskResult[1] = context.getString(isoCode.getResId());
                    if ("00".equals(respCode)) {
                        //更新批次号
                        String iso60 = mapData.get(iso_f60);
                        if (iso60.length() > 8) {
                            String batch = iso60.substring(2, 8);
                            logger.info("更新批次号==>本地批次号：" + BusinessConfig.getInstance().getBatchNo(context) + "==>平台批次号：" + batch);
                            BusinessConfig.getInstance().setBatchNo(context, batch);
                            String updateFlag = iso60.substring(8, iso60.length());
                            if ("990".equals(updateFlag)) {
                                BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.FLAG_NEED_UPDATE_PARAM, "1");
                            }
                        }

                        //发散工作密钥
                        String workKey = mapData.get(iso_f62);
                        String pik = workKey.substring(2, 42);
                        String mak = workKey.substring(42, 82);
                        String tdk = null;
                        if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
                            tdk = workKey.substring(82, 122);
                        }
                        String pikValue = pik.substring(0, 32);
                        String pikCheckValue = pik.substring(32, 40);
                        String makValue = mak.substring(0, 32);
                    if (makValue.endsWith("0000000000000000")) {
                        logger.info("MAK为单倍长");
                        String left8Bytes = mak.substring(0, 16);
                        makValue = left8Bytes + left8Bytes;
                    }
                        String makCheckValue = mak.substring(32, 40);
                        String tdkValue = tdk == null ? null : tdk.substring(0, 32);
                        String tdkCheckValue = tdk == null ? null : tdk.substring(32, 40);
                        logger.debug("pik == " + pikValue + "   " + pikCheckValue);
                        logger.debug("mak == " + makValue + "   " + makCheckValue);
                        logger.debug("tdk == " + tdkValue + "   " + tdkCheckValue);
                        taskRetryTimes = 0;
                        if (loadWorkKey(pikValue, pikCheckValue, makValue, makCheckValue, tdkValue, tdkCheckValue)) {
                            //重置批结算标识
                            Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
                            try {
                                Calendar calendar = Calendar.getInstance();
                                int year = calendar.get(Calendar.YEAR);
                                String time = mapData.get(iso_f12);
                                String date = mapData.get(iso_f13);
                                String dateTime = year + date + time;
                                DeviceFactory factory = DeviceFactory.getInstance();
                                ISystemService service = factory.getSystemDev();
                                service.updateSysTime(dateTime);//TODO:更新系统时间
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //工作密钥发散成功
                            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, true);
                        } else {
                            //工作密钥发散失败
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

    /**
     * 对比签购单版本，如果版本不一致，获取最新版本，保存到数据库，并且保留最新版本号
     *
     * @param version
     * @param slipVersion
     */
    private void compareAndSaveVersion(String version, String slipVersion) {
        String localVersion = Settings.getSlipVersion(context);
        if (!localVersion.equals(version)) {
            List<PrinterItem> printerItems = new ArrayList<>();
            String[] strings = slipVersion.split("(?=9F..)");
            try {
                for (int i = 1; i < strings.length; i++) {
                    String paramId = strings[i].substring(0, 4);
                    String paramValue = new String(HexUtils.hexStringToByte(strings[i].substring(6, strings[i].length() - 4)), "GBK");
                    String textSize = strings[i].substring(strings[i].length() - 4, strings[i].length() - 2);
                    String range = strings[i].substring(strings[i].length() - 2, strings[i].length());
                   // PrinterItem printerItem = new PrinterItem(stringMap.get(paramId), paramValue, paramId, Integer.parseInt(textSize), Integer.parseInt(range));
                    PrinterItem printerItem = new PrinterItem(paramValue, paramValue, paramId, Integer.parseInt(textSize), Integer.parseInt(range));
                    printerItems.add(printerItem);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            CommonDao commonDao = new CommonDao(PrinterItem.class, new DbHelper(context));
            Settings.setSlipVersion(context, version);
            boolean isDel = commonDao.deleteByWhere(null);
            if (isDel) {
                logger.debug("签购单表清空成功！");
                boolean issave = commonDao.save(printerItems);
                if (issave) {
                    logger.debug("新版本签购单保存成功！");
                }
            }
        }
    }

    /**
     * 发散工作密钥
     *
     * @param pik      pik
     * @param pikCheck 校验值
     * @param mak      mak
     * @param makCheck 校验值
     * @param tdk      mak
     * @param tdkCheck 校验值
     */
    public boolean loadWorkKey(String pik, String pikCheck, String mak, String makCheck, String tdk, String tdkCheck) {
        boolean r1, r2, r3;
        boolean result = false;
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        if (pinPadDev != null) {
            pinPadDev.loadTMK("324C0419C85EEA2694765164AE0E40BC", "9783A7C4");
            r1 = pinPadDev.loadWorkKey(EnumWorkKeyType.PIK, pik, pikCheck);
            r2 = pinPadDev.loadWorkKey(EnumWorkKeyType.MAK, mak, "");
            if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
                r3 = pinPadDev.loadWorkKey(EnumWorkKeyType.TDK, tdk, tdkCheck);
                result = r1 && r2 && r3;
            } else {
                result = r1 && r2;
            }
        }
        return result;
    }

   /* public void initParamTip() {
        stringMap.clear();
        stringMap.put(PrinterParamEnum.SHOP_HEADER.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_HEADER.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NAME.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_NAME.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_TERM_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_TERM_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_SEND_CARD_BRANK.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_SEND_CARD_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_RECEIVE_BRANK.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_RECEIVE_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_CARD_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_CARD_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_BATCH_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_BATCH_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_TRAN_FLOW_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_TRAN_FLOW_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_PERMISION_CODE.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_PERMISION_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_REFERENCE_CODE.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_REFERENCE_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_DATE_TIME.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_DATE_TIME.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_AMOUNT.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_AMOUNT.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_COMMENT.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_COMMENT.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_DESCRIBE.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_DESCRIBE.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED1.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_NOT_USED1.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED2.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_NOT_USED2.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED3.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_NOT_USED3.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED4.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_NOT_USED4.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED5.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_NOT_USED5.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_HEADER.getParamId(), context.getResources().getString(PrinterParamEnum.SHOP_HEADER.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NAME.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_NAME.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_TERM_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_TERM_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_SEND_CARD_BRANK.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_SEND_CARD_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_RECEIVE_BRANK.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_RECEIVE_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_CARD_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_CARD_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_BATCH_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_BATCH_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_TRAN_FLOW_NUM.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_TRAN_FLOW_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_PERMISION_CODE.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_PERMISION_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_REFERENCE_CODE.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_REFERENCE_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_DATE_TIME.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_DATE_TIME.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_AMOUNT.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_AMOUNT.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_COMMENT.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_COMMENT.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_DESCRIBE.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_DESCRIBE.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED1.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_NOT_USED1.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED2.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_NOT_USED2.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED3.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_NOT_USED3.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED4.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_NOT_USED4.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED5.getParamId(), context.getResources().getString(PrinterParamEnum.PERSON_NOT_USED5.getParamTip()));
    }*/
}

package com.centerm.jnbank.activity.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.task.qrcode.AsyncGetQrCodeTask;
import com.centerm.jnbank.task.qrcode.AsyncQueryOrderTask;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransCode.SCAN_PAY_SFT;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_WEI;
import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f12;
import static com.centerm.jnbank.common.TransDataKey.iso_f13;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f38;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f61;
import static com.centerm.jnbank.common.TransDataKey.iso_f64;
import static com.centerm.jnbank.common.TransDataKey.key_resp_code;
import static com.centerm.jnbank.common.TransDataKey.key_resp_msg;

/**
 * author:wanliang527</br>
 * date:2017/1/1</br>
 */

public class ShowCodeActivity extends BaseTradeActivity {

    private ImageView qrCodeShow;
    private TextView countDownShow;
    private TextView tipShow;
    private int countDown = 60;
    private Timer timer;
    private TimerTask task;
    private AsyncGetQrCodeTask codePayTask;
    private AsyncQueryOrderTask queryOrderTask;
    private String codeString;//付款码
    private boolean hasCancel;
    private String iso37,origin60,origin4;
    private CommonDao<TradeInfo> dao;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    countDownShow.setText("" + countDown);
                    break;
                case 2:
                    codePayTask.cancelRequest();
                    //finishTrade();
                    break;
            }
        }
    };


    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        dao = new CommonDao<>(TradeInfo.class, new DbHelper(context));
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_show_qr_code;
    }

    @Override
    public void onInitView() {
        TextView amtShow = (TextView) findViewById(R.id.trans_money_show);
        amtShow.setText("¥ " +DataHelper.formatAmountForShow(dataMap.get(iso_f4)));
        qrCodeShow = (ImageView) findViewById(R.id.code_show);
        countDownShow = (TextView) findViewById(R.id.count_down_show);
        tipShow = (TextView) findViewById(R.id.tip_show);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        reCountDown();//界面重新计时
        executeGetQrCodeTask();//开始获取二维码
    }

    @Override
    public void onBackPressed() {
        onCancelTradeClick(null);
    }


    public void onCancelTradeClick(View view) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>重复的onCancelTradeClick事件，不响应！");
            return;
        }
        finishTrade();
        jumpToResultActivity(StatusCode.QR_CANCEL);
    }

    public void finishTrade(){
        hasCancel = true;
        stopCountDown();
        if (codeString == null) {
            //还未获取到二维码的话，直接返回主界面
            if (codePayTask != null && !codePayTask.isCancelled()) {
                codePayTask.cancel(true);
            }
        } else {
            //已获取到二维码，尝试关闭订单
            if (queryOrderTask != null) {
                queryOrderTask.specialCancel();
            }
        }
    }


    /**
     * 停止倒计时
     */
    private void stopCountDown() {
        countDownShow.setVisibility(View.GONE);
        qrCodeShow.setVisibility(View.VISIBLE);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * 重新倒计时
     */
    private void reCountDown() {
        stopCountDown();
        countDownShow.setVisibility(View.VISIBLE);
        qrCodeShow.setVisibility(View.GONE);
        countDown = 60;
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                logger.debug("显示二维码倒计时："+countDown);
                if (--countDown >= 0) {
                    handler.obtainMessage(1).sendToTarget();
                } else {
                    handler.obtainMessage(2).sendToTarget();
                }
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    private void executeGetQrCodeTask() {
        tipShow.setText("正在获取二维码");
        if (codePayTask != null && !codePayTask.isCancelled()) {
            codePayTask.cancel(true);
        }
        dataMap.put(iso_f22,"031");//反扫
        String txnWay = "";
        if (SCAN_PAY_WEI.equals(transCode)) {
            txnWay = "TX01";
        } else if(SCAN_PAY_SFT.equals(transCode)) {
            txnWay = "SFT01";
        } else {
            txnWay = "ZFB01";
        }
        //47域，扫码支付交易上送通道类型和通道授权码
        dataMap.put(iso_f47, "TXNWAY=" + txnWay );

        codePayTask = new AsyncGetQrCodeTask(context, dataMap, tempMap) {
            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if(StringUtils.isStrNull(tempMap.get(iso_f47))){
                    putResponseCode(strings[0], strings[1]);
                    if (hasCancel) {
                        //do nothing 防止出现点击了结果页面又不对的情况
                        logger.debug("页面已经退出了");
                    } else {
                        finishTrade();
                        jumpToNext("99");
                    }
                    return;
                }
                if (!hasCancel) {
                    iso37 = null;
                    origin60 = null;
                    origin4 = null;
                    iso37 = tempMap.get(iso_f37);
                    origin60 = tempMap.get(iso_f60).substring(2, 8) + tempMap.get(iso_f11);
                    origin4 = tempMap.get(iso_f4);
                    String temp47 = tempMap.get(iso_f47);
                    if (!StringUtils.isStrNull(temp47)) {
                        codeString = temp47.substring(11, temp47.length());
                        logger.debug("code url为：" + codeString);
                        setQrCodeShow(codeString);
                    }
                    tipShow.setText(R.string.tip_scan_qr_code);
                    //开始轮询交易状态
                    initSearchData();
                    executeQueryOrderTask();
                    stopCountDown();
                } else {
                    logger.debug("获取二维码有数据返回，但页面已经关闭");
                }

            }
        };
        codePayTask.executeOnExecutor(Executors.newCachedThreadPool(),transCode);
    }
    private void initSearchData(){
        dataMap.clear();
        dataMap.put(iso_f22,"031");
        String txnWay;
        if (SCAN_PAY_WEI.equals(transCode)) {
            txnWay = "TX01";
        } else if (SCAN_PAY_SFT.equals(transCode)){
            txnWay = "SFT01";
        } else {
            txnWay = "ZFB01";
        }
        //47域，扫码支付交易上送通道类型和通道授权码
        dataMap.put(iso_f47, "TXNWAY=" + txnWay);
        dataMap.put(iso_f37,iso37);
        dataMap.put(iso_f61, origin60);
    }
    private void executeQueryOrderTask() {
        String queryCode = TransCode.SCAN_SERCH;
        queryOrderTask = new AsyncQueryOrderTask(context, dataMap, tempMap) {
            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if (strings[0].equals(StatusCode.QR_TIME_OUT.getStatusCode())){
                    finishTrade();
                    jumpToResultActivity(StatusCode.QR_TIME_OUT);
                }else {
                    if(hasCancel){
                        //do nothing 防止出现点击了结果页面又不对的情况
                    }else {
                        finishTrade();
                        tempMap.put(iso_f4, origin4);
                        onTradeSuccess(transCode,tempMap);
                        putResponseCode(strings[0], strings[1]);
                        jumpToNext("99");
                    }
                }
            }
        };
        queryOrderTask.executeOnExecutor(Executors.newCachedThreadPool(),queryCode);
    }


    private void putResponseCode(String respCode, String respMsg) {
        tempMap.put(iso_f39, respCode);
        tempMap.put(key_resp_code, respCode);
        tempMap.put(key_resp_msg, respMsg);
    }

    private void setQrCodeShow(String code) {
        int width = (int) getResources().getDimension(R.dimen.qr_code_size);
        int height = width;
        Hashtable<EncodeHintType, Object> hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 0);//设置边距
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        qrCodeShow.setImageBitmap(bitmap);
    }

    /**
     * 交易成功。删除冲正表的数据，插入到交易流水
     */
    public void onTradeSuccess(String transCode, Map<String, String> returnData) {

        boolean dbResult;
        String iso11 = null;
        //通过61域找到对应的原始流水号
        String iso61 = dataMap.get(iso_f61);
        if (null != iso61 && iso61.length() == 12) {
            iso11 = iso61.substring(6, 12);
        }
        returnData.put(iso_f11, iso11);
        //********************更新本地流水***********************//
        TradeInfo initialTrade = dao.queryForId(iso11);
        if (initialTrade != null) {
            //保存返回数据
            //initialTrade.update(returnData);
            initialTrade.setIso_f12(returnData.get(iso_f12));
            initialTrade.setIso_f13(returnData.get(iso_f13));
            initialTrade.setIso_f37(returnData.get(iso_f37));
            initialTrade.setIso_f38(returnData.get(iso_f38));
            initialTrade.setIso_f39(returnData.get(iso_f39));
            initialTrade.setIso_f44(returnData.get(iso_f44));
            initialTrade.setIso_f60(returnData.get(iso_f60));
            initialTrade.setIso_f64(returnData.get(iso_f64));
            initialTrade.setFlag(1);//代表交易成功
        } else {
            logger.warn(iso11 + "==>交易类型==>" + transCode + "==>无法在数据库中查询到该数据模型==>新建模型");
            initialTrade = new TradeInfo(transCode, returnData);
        }
        try {
            //TODO:前提是最后一笔非撤销及退货的交易
            CommonManager commonManager = new CommonManager(TradeInfo.class, context);
            long counts = commonManager.getBatchCount();
            long config = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.PARAM_MOST_TRANS);
            logger.info("已存储成功流水数量==>" + (++counts) + "==>终端最大存储数量==>" + config);
            if (counts >= config) {
                logger.warn("交易流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tempMap.putAll(returnData);
        dbResult = dao.update(initialTrade);
        logger.info(iso11 + "==>交易类型==>" + transCode + "==>交易成功==>更新交易流水表==>" + dbResult);
    }
}

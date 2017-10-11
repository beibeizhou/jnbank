package com.centerm.jnbank.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.cpay.midsdk.dev.define.pboc.EmvTag;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumOnlineResult;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumWorkKeyType;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.ElecSignInfo;
import com.centerm.jnbank.bean.ReverseInfo;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.bean.TradePrintData;
import com.centerm.jnbank.channels.helper.BaseRespHelper;
import com.centerm.jnbank.channels.helper.ShengPayRespHelper;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.msg.MessageFactory;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.net.SocketClient;
import com.centerm.jnbank.printer.PrintTransData;
import com.centerm.jnbank.task.AsyncAutoReverseTask;
import com.centerm.jnbank.task.AsyncAutoSignOut;
import com.centerm.jnbank.task.AsyncBatchUploadDown;
import com.centerm.jnbank.task.AsyncCheckBillTask;
import com.centerm.jnbank.task.AsyncDownloadAidTask;
import com.centerm.jnbank.task.AsyncDownloadCapkTask;
import com.centerm.jnbank.task.AsyncDownloadCardBinTask;
import com.centerm.jnbank.task.AsyncDownloadParamTask;
import com.centerm.jnbank.task.AsyncDownloadQPSBlackCardBinTask;
import com.centerm.jnbank.task.AsyncDownloadQPSCardBinTask;
import com.centerm.jnbank.task.AsyncDownloadQpsTask;
import com.centerm.jnbank.task.AsyncDownloadTmkTask;
import com.centerm.jnbank.task.AsyncQueryPrintDataTask;
import com.centerm.jnbank.task.AsyncScanRearchLastTask;
import com.centerm.jnbank.task.AsyncSignTask;
import com.centerm.jnbank.task.AsyncUploadIcDataTask;
import com.centerm.jnbank.task.AsyncUploadMagsDataTask;
import com.centerm.jnbank.task.AsyncUploadRefundDataTask;
import com.centerm.jnbank.task.AsyncUploadScriptTask;
import com.centerm.jnbank.task.qrcode.AsyncQueryOrderTask;
import com.centerm.jnbank.task.qrcode.AsyncSendQrTask;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.view.AlertDialog.ButtonType;
import com.centerm.iso8583.util.SecurityUtil;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import config.BusinessConfig;
import config.Config;

import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.getAllActions;
import static com.centerm.jnbank.common.TransCode.AUTH;
import static com.centerm.jnbank.common.TransCode.AUTH_COMPLETE;
import static com.centerm.jnbank.common.TransCode.CANCEL;
import static com.centerm.jnbank.common.TransCode.CAUSE_REVERSE_SETS;
import static com.centerm.jnbank.common.TransCode.COMPLETE_VOID;
import static com.centerm.jnbank.common.TransCode.DISCOUNT_INTERGRAL;
import static com.centerm.jnbank.common.TransCode.INIT_TERMINAL;
import static com.centerm.jnbank.common.TransCode.NEED_INSERT_TABLE_SETS;
import static com.centerm.jnbank.common.TransCode.REFUND;
import static com.centerm.jnbank.common.TransCode.SALE;
import static com.centerm.jnbank.common.TransCode.SCAN_CANCEL;
import static com.centerm.jnbank.common.TransCode.SCAN_LAST_SERCH;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_ALI;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_SFT;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_WEI;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_S;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_W;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_Z;
import static com.centerm.jnbank.common.TransCode.SIGN_OUT;
import static com.centerm.jnbank.common.TransCode.VOID;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_CARD_CONFIRM_RESULT;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_PIN;
import static com.centerm.jnbank.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.jnbank.common.TransDataKey.headerData;
import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f12;
import static com.centerm.jnbank.common.TransDataKey.iso_f13;
import static com.centerm.jnbank.common.TransDataKey.iso_f14_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f36;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f38;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;
import static com.centerm.jnbank.common.TransDataKey.iso_f55;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f61;
import static com.centerm.jnbank.common.TransDataKey.iso_f64;
import static com.centerm.jnbank.common.TransDataKey.key_batch_upload_count;
import static com.centerm.jnbank.common.TransDataKey.key_is_amount_ok;
import static com.centerm.jnbank.common.TransDataKey.key_oriReference;
import static com.centerm.jnbank.common.TransDataKey.key_oriTransTime;
import static com.centerm.jnbank.common.TransDataKey.key_param_update;
import static com.centerm.jnbank.common.TransDataKey.key_resp_code;
import static com.centerm.jnbank.common.TransDataKey.key_resp_msg;
import static com.centerm.jnbank.task.qrcode.AsyncQueryOrderTask.QUERY_DURATION;

/**
 * 联机交易界面
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class TradingActivity extends BaseTradeActivity implements PrintTransData.PrinterCallBack {
    private int countdownTime = (int) (QUERY_DURATION / 1000);//计时秒数
    private int remainSecond = countdownTime;//倒计时剩余秒数
    private MessageFactory factory;
    private SocketClient socketClient;
    private BaseRespHelper respHelper;
    private CommonDao<ReverseInfo> reverseDao;
    private CommonDao<TradeInfo> tradeDao;
    private CommonDao<TradePrintData> printDataCommonDao;
    private ImageView loadIcon;//加载图标
    private TextView hintShow;//提示文字
    private List<TradeInfo> magsCards = new ArrayList<>();
    private List<TradeInfo> icCards = new ArrayList<>();
    private List<TradeInfo> refundInfos;
    private int icCardsIndex, magsCardsIndex, refundIndex;
    private List<String> strings = new ArrayList<>();
    private List<TradeInfo> objArray1 = new ArrayList<>();
    private List<List<TradeInfo>> objArray2 = new ArrayList<>();
    private List<TradeInfo> jiejiList;
    private List<TradeInfo> daijiList;
    private List<TradeInfo> rejestList;
    private List<TradeInfo> failList;
    private List<TradeInfo> batchDetailList;
    private List<TradeInfo> reverseList;
    private PrintTransData printTransData;
    private double jiejiAmount;
    private double daijiAmount;
    private List<List<TradeInfo>> lists;
    private int queryCount = 10;//默认10查询十次
    private int current = 0;
    private AsyncQueryOrderTask queryOrderTask;
    private boolean hasCancel;
    private Button cancelTrading;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            logger.info("对话框倒计时," + remainSecond);
            if (--remainSecond < 0) {
                finishTrading();
                jumpToResultActivity(StatusCode.QR_TIME_OUT);
                return;
            }
            hintShow.setText("正在查询订单状态\n" + remainSecond + "");
        }
    };
    private AlertDialog alertDialog;
    private Timer timer;
    private TimerTask task;
    private SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private String origin4;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        tempMap.putAll(dataMap);
        receiver = new MyReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, getAllActions());
        factory = new MessageFactory(context);
        reverseDao = new CommonDao<>(ReverseInfo.class, dbHelper);
        tradeDao = new CommonDao<>(TradeInfo.class, dbHelper);
        printDataCommonDao = new CommonDao<>(TradePrintData.class, dbHelper);
        socketClient = SocketClient.getInstance(context);
        switch (posChannel) {
            case SHENGPAY:
                respHelper = new ShengPayRespHelper(context, transCode);
                break;
        }
        String queryStr = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_SCAN_QUERY_COUNT);
        queryCount = Integer.parseInt(queryStr);
        current = 0;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_trading;
    }

    @Override
    public void onInitView() {
        loadIcon = (ImageView) findViewById(R.id.trading_logo_show);
        startAnim();
        hintShow = (TextView) findViewById(R.id.hint_text_show);
        cancelTrading = (Button) findViewById(R.id.cancel_trading);
    }

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        if (isICInsertTrade()) {
            updateHint(getString(R.string.tip_trading_default) + "\n请勿拔卡！");
        }
        if (TransCode.OBTAIN_TMK.equals(transCode)) {
            boolean autoSign = getIntent().getBooleanExtra(TransDataKey.FLAG_AUTO_SIGN, false);
            //下载主密钥
            executeDownloadTmk(autoSign);
        } else if (TransCode.DOWNLOAD_CAPK.equals(transCode)) {
            //下载公钥参数
            executeDownloadCapk(false);
        } else if (TransCode.DOWNLOAD_AID.equals(transCode)) {
            //下载AID参数
            executeDownloadAid(false);
        } else if (TransCode.DOWNLOAD_QPS_PARAMS.equals(transCode)) {
            //下载小额免密参数
            executeDownloadQps(false);
        } else if (TransCode.DOWNLOAD_CARD_BIN_QPS.equals(transCode)) {
            //免密新增bin表更新下载
            executeDownloadQpsCardBin(false);
        } else if (TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS.equals(transCode)) {
            //免密卡bin黑名单更新下载
            executeDownloadQPSBlackCardBin(false);
        } else if (TransCode.LOAD_PARAM.equals(transCode)) {
            executeDownloadParam(false);
        } else if (SCAN_LAST_SERCH.equals(transCode)) {
            executeScanQueryLast();
        } else if (SCAN_PAY_WEI.equals(transCode) || SCAN_PAY_ALI.equals(transCode) || SCAN_PAY_SFT.equals(transCode)) {
            executeQrSaleTask();
        } else {
            if (TransCode.SIGN_IN.equals(transCode)) {
                //签到
                executeSignIn(false);
            } else {
                logger.info("ccccccccccccccccccc");
                if (Config.DebugToggle.UPLOAD_IC_SCRIPT
                        && BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.FLAG_NEED_UPLOAD_SCRIPT)) {
                    //检测到当前有IC卡脚本结果需要上送，则先进行联机脚本上送，再发起交易
                    logger.info("ddddddddddddd");
                    executeUploadScriptResult();
                } else if (BaseActivity.reverseFlag) {
                    //如果当前有冲正信息，需要先进行冲正后才开始进行交易
                    logger.info("eeeeeeeeeeeeeeeee");
                    executeReverseTask();
                } else {
                    logger.info("fffffffffffffffff");
                    beginOnline();
                }
            }
        }
    }


    private void initSearchData() {
        dataMap.clear();
        String txnWay = "";
        if (transCode.equals(SCAN_PAY_WEI)) {
            txnWay = "TX01";
        } else if (transCode.equals(SCAN_PAY_ALI)) {
            txnWay = "ZFB01";
        } else if (transCode.equals(SCAN_PAY_SFT)) {
            txnWay = "SFT01";
        }
        dataMap.put(TransDataKey.iso_f47, "TXNWAY=" + txnWay);
        dataMap.put(TransDataKey.iso_f22, "032");//正扫
    }

    private void executeQrSaleTask() {
        new AsyncSendQrTask(context, dataMap, tempMap) {
            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if ("00".equals(tempMap.get(iso_f39))) {
                    respHelper.onRespSuccess(TradingActivity.this, tempMap);
                } else if ("A7".equals(tempMap.get(iso_f39))) {
                    origin4 = null;
                    origin4 = tempMap.get(iso_f4);
                    String queryCode = TransCode.SCAN_SERCH;
                    //后台提示正在支付，开启订单查询
                    initSearchData();
                    if (null != dataMap.get(iso_f60) && null != dataMap.get(iso_f11)) {
                        TradingActivity.this.dataMap.put(iso_f61, dataMap.get(iso_f60).substring(2, 8) + dataMap.get(iso_f11));
                    }
                    executeQrOrderStatusTask(queryCode, true, TradingActivity.this.dataMap);
                } else {
                    putResponseCode(strings[0], strings[1]);
                    jumpToNext();
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), transCode);
    }

    private void executeQrOrderStatusTask(String transCd, final boolean needGotoResult, final Map<String, String> oriData) {
        queryOrderTask = new AsyncQueryOrderTask(context, oriData, tempMap) {
            @Override
            public void onStart() {
                super.onStart();
                updateHint("正在查询订单状态");
                reset();
                if (task == null) {
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(0);
                        }
                    };
                }
                if (timer == null) {
                    timer = new Timer();
                }
                timer.schedule(task, 0, 1000);
                cancelTrading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if (strings[0].equals(StatusCode.QR_TIME_OUT.getStatusCode())) {
                    finishTrading();
                    jumpToResultActivity(StatusCode.QR_TIME_OUT);
                } else {
                    reset();
                    if (hasCancel) {
                        //do nothing 防止出现点击了结果页面又不对的情况
                    } else {
                        tempMap.put(iso_f4, origin4);
                        onTradeSuccess(transCode, tempMap);
                        putResponseCode(strings[0], strings[1]);
                        jumpToNext();
                    }
                }
            }
        };
        queryOrderTask.executeOnExecutor(Executors.newCachedThreadPool(), transCd);
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
        TradeInfo initialTrade = tradeDao.queryForId(iso11);
        if (initialTrade != null) {
            //保存返回数据
            initialTrade.update(returnData);
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
        dbResult = tradeDao.update(initialTrade);
        logger.info(iso11 + "==>交易类型==>" + transCode + "==>交易成功==>更新交易流水表==>" + dbResult);
    }

    private void reset() {
        remainSecond = countdownTime;
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public void finishTrading() {
        hasCancel = true;
        //已获取到二维码，尝试关闭订单
        if (queryOrderTask != null) {
            queryOrderTask.specialCancel();
        }
        reset();
    }

    /**
     * 除了特殊联机交易类型外，其它交易都调用该方法，开启联机交易的一系列步骤。
     * 包含对内核时间的处理和应答，组报文发起网络请求等等。
     */
    private void beginOnline() {
        if (isICTrade()) {
            //联机前完成内核操作，在内核要求联机时才进行真实联机交易
            if ("1".equals(dataMap.get(FLAG_IMPORT_AMOUNT))) {
                pbocService.importAmount(dataMap.get(iso_f4));
                dataMap.remove(FLAG_IMPORT_AMOUNT);
            } else if ("1".equals(dataMap.get(FLAG_IMPORT_CARD_CONFIRM_RESULT))) {
                pbocService.importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
                dataMap.remove(FLAG_IMPORT_CARD_CONFIRM_RESULT);
            } else if ("1".equals(dataMap.get(FLAG_IMPORT_PIN))) {
                pbocService.importPIN(false, null);
                dataMap.remove(FLAG_IMPORT_PIN);
            } else if ("1".equals(dataMap.get(FLAG_REQUEST_ONLINE))) {
                dataMap.remove(FLAG_REQUEST_ONLINE);
                logger.info("111111111");
                sendData();
            } else {
                logger.warn("内核流程异常，交易终止，不进行联机交易");
                jumpToResultActivity(StatusCode.TRADING_REFUSED);
                pbocService.abortProcess();
            }
        } else {
            logger.info("0000000000000");
            sendData();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopAnim();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    private void executeDownloadAid(final boolean isAuto) {
        new AsyncDownloadAidTask(context, dataMap) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                switch (index) {
                    case -1:
                        updateHint("下载完成，正在结束下载");
                        break;
                    case -2:
                        updateHint("正在导入IC卡参数");
                        break;
                    case -3:
                        updateHint("IC卡参数导入成功");
                        break;
                    default:
                        updateHint("正在下载IC卡参数(" + index + "/" + counts + ")");
                        break;
                }
            }

            @Override
            public void onStart() {
                updateHint("正在获取IC卡参数信息");
            }

            @Override
            public void onFinish(String[] status) {
                BusinessConfig.getInstance().setParam(context, BusinessConfig.Key.FLAG_FIRST_AID_DOWNLOAD,"0");
                boolean needUpdate = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_NEED_UPDATE_CARDBIN).equals("1") ? true : false;
                if (isAuto&&needUpdate){
                    ViewUtils.showToast(context, getString(R.string.tip_download_aid_success));
                    executeDownloadQpsCardBin(true);
                } else if(isAuto) {
                    activityStack.pop();
                    ViewUtils.showToast(context, getString(R.string.tip_download_aid_success));
                } else {
                    tempMap.put(iso_f39, status[0]);
                    putResponseCode(status[0], status[1]);
                    jumpToNext();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,transCode);
    }

    private void executeDownloadCapk(final boolean isAuto) {
        new AsyncDownloadCapkTask(context, dataMap) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                switch (index) {
                    case -1:
                        updateHint("下载完成，正在结束下载");
                        break;
                    case -2:
                        updateHint("正在导入IC卡公钥");
                        break;
                    case -3:
                        updateHint("IC卡公钥导入成功");
                        break;
                    default:
                        updateHint("正在下载IC卡公钥(" + index + "/" + counts + ")");
                        break;
                }
            }

            @Override
            public void onStart() {
                updateHint("正在获取公钥信息");
            }

            @Override
            public void onFinish(String[] status) {
                BusinessConfig.getInstance().setParam(context, BusinessConfig.Key.FLAG_FIRST_CAPK_DOWNLOAD,"0");
                boolean needUpdate = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_FIRST_AID_DOWNLOAD).equals("1") ? true : false;
                if (isAuto&&needUpdate) {
                    ViewUtils.showToast(context, getString(R.string.tip_download_capk_success));
                    executeDownloadAid(true);
                } else if (isAuto) {
                    activityStack.pop();
                    ViewUtils.showToast(context, getString(R.string.tip_download_capk_success));
                }else {
                    tempMap.put(iso_f39, status[0]);
                    putResponseCode(status[0], status[1]);
                    jumpToNext();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,transCode);
    }

    private void executeDownloadQps(final boolean doDownBin) {
        new AsyncDownloadQpsTask(context, dataMap) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                if (index == -1) {
                    updateHint("下载完成，正在结束下载");
                }
            }

            @Override
            public void onStart() {
                updateHint("正在下载非接参数");
            }

            @Override
            public void onFinish(String[] status) {
                if (doDownBin && "00".equals(status[0])) {
                    executeDownloadBin();
                } else {
                    tempMap.put(iso_f39, status[0]);
                    putResponseCode(status[0], status[1]);
                    jumpToNext();
                    /*boolean isLock = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.KEY_IS_LOCK);
                    if (isLock) {
                        DialogFactory.showLockDialog(context);
                    }*/
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    private void executeUploadScriptResult() {
        new AsyncUploadScriptTask(context, dataMap) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                updateHint("上送IC卡脚本处理结果\n" + index + "/" + counts);
            }

            @Override
            public void onStart() {
                updateHint("上送IC卡脚本处理结果");
            }

            @Override
            public void onFinish(String[] status) {
                updateHint("上送完成");
                beginOnline();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    private void executeDownloadBin() {
        new AsyncDownloadCardBinTask(context, dataMap) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                if (index == -1) {
                    updateHint("下载完成，正在结束下载");
                } else if (index == -2) {
                    updateHint("无卡BIN信息需要更新");
                } else {
                    updateHint("正在下载卡BIN信息\n起始号" + index);
                }
            }

            @Override
            public void onStart() {
                updateHint("正在下载卡BIN信息");
            }

            @Override
            public void onFinish(String[] strings) {
                tempMap.put(iso_f39, strings[0]);
                putResponseCode(strings[0], strings[1]);
                jumpToNext();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    private void executeDownloadQpsCardBin(final boolean isAuto) {
        new AsyncDownloadQPSCardBinTask(context, dataMap) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                switch (index) {
                    case -1:
                        updateHint("下载完成，正在结束下载");
                        break;
                    case -2:
                        updateHint("无卡BIN信息需要更新");
                        break;
                    default:
                        updateHint("正在下载卡BIN B表信息\n起始号" + index);
                        break;
                }
            }

            @Override
            public void onStart() {
                updateHint("正在下载卡BIN信息");
            }

            @Override
            public void onFinish(String[] status) {
                BusinessConfig.getInstance().setParam(context, BusinessConfig.Key.FLAG_NEED_UPDATE_CARDBIN, "0");
                boolean needUpdate = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_NEED_UPDATE_BLACK_CARDBIN).equals("1") ? true : false;
                if (isAuto && needUpdate) {
                    ViewUtils.showToast(context, getString(R.string.tip_load_card_bin_b_success));
                    executeDownloadQPSBlackCardBin(true);
                } else if (isAuto) {
                    activityStack.pop();
                    ViewUtils.showToast(context, getString(R.string.tip_load_card_bin_b_success));
                } else {
                    tempMap.put(iso_f39, status[0]);
                    putResponseCode(status[0], status[1]);
                    jumpToNext();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    public void executeDownloadQPSBlackCardBin(final boolean isAuto) {
        new AsyncDownloadQPSBlackCardBinTask(context, dataMap) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                switch (index) {
                    case -1:
                        updateHint("下载完成，正在结束下载");
                        break;
                    case -2:
                        updateHint("无卡BIN信息需要更新");
                        break;
                    default:
                        updateHint("正在下载卡BIN C表信息\n起始号" + index);
                        break;
                }
            }

            @Override
            public void onStart() {
                updateHint("正在下载卡BIN信息");
            }

            @Override
            public void onFinish(String[] status) {
                BusinessConfig.getInstance().setParam(context, BusinessConfig.Key.FLAG_NEED_UPDATE_BLACK_CARDBIN, "0");
                if (isAuto) {
                    activityStack.pop();
                    ViewUtils.showToast(context, getString(R.string.tip_load_black_card_bin_success));
                } else {
                    tempMap.put(iso_f39, status[0]);
                    putResponseCode(status[0], status[1]);
                    jumpToNext();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    /**
     * 终端参数下载
     */
    private void executeDownloadParam(final boolean isAuto) {
        new AsyncDownloadParamTask(context, dataMap) {
            @Override
            public void onStart() {
                updateHint("正在获取终端参数");
            }

            @Override
            public void onFinish(String[] status) {
                boolean needUpdate = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_FIRST_CAPK_DOWNLOAD).equals("1") ? true : false;
                if (isAuto && needUpdate) {
                    ViewUtils.showToast(context, getString(R.string.tip_download_param_success));
                    executeDownloadCapk(true);
                } else if (isAuto) {
                    activityStack.pop();
                    ViewUtils.showToast(context, getString(R.string.tip_download_param_success));
                } else {
                    tempMap.put(iso_f39, status[0]);
                    tempMap.put(key_param_update, status[2]);
                    putResponseCode(status[0], status[1]);
                    jumpToNext();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    /**
     * 执行签到操作
     *
     * @param afterTmkUpdate 该签到操作是否在下载主密钥之后
     */
    private void executeSignIn(final boolean afterTmkUpdate) {
        String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if (StringUtils.isStrNull(operId)) {
            BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_OPER_ID, "01");
        }
        new AsyncSignTask(context, dataMap, tempMap) {
            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                putResponseCode(strings[0], strings[1]);
                if ("00".equals(strings[0])) {
                    //签到完成，紧接着进行非接参数下载
                    boolean needUpdate = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_NEED_UPDATE_PARAM).equals("1") ? true : false;
                    boolean needUpdate2 = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_FIRST_CAPK_DOWNLOAD).equals("1") ? true : false;
                    boolean needUpdate3 = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_FIRST_AID_DOWNLOAD).equals("1") ? true : false;
                    boolean needUpdate4 = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_NEED_UPDATE_CARDBIN).equals("1") ? true : false;
                    boolean needUpdate5 = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_NEED_UPDATE_BLACK_CARDBIN).equals("1") ? true : false;
                    if (needUpdate) {
                        executeDownloadParam(true);
                    } else if (needUpdate2) {
                        executeDownloadCapk(true);
                    } else if (needUpdate3) {
                        executeDownloadAid(true);
                    } else if (needUpdate4) {
                        executeDownloadQpsCardBin(true);
                    } else if (needUpdate5) {
                        executeDownloadQPSBlackCardBin(true);
                    } else {
                        activityStack.pop();
                    }
                    ViewUtils.showToast(context, getString(R.string.tip_sign_in_success));
                } else if (StatusCode.KEY_VERIFY_FAILED.getStatusCode().equals(strings[0])) {
//                    updateHint("签到失败，尝试重新下载主密钥");
                    //密钥发散失败的情况，可能是由于SDK服务异常或者该终端的主密钥已经变了，却没有写入到终端上
                    if (afterTmkUpdate) {
                        //由于下载主密钥之后会进行自动签到，增加该标志为了防止签到和下载主密钥任务递归死循环
//                        jumpToNext();
                    } else {

                    }
                } else {
                    jumpToNext();
                    ViewUtils.showToast(context, "签到失败！");
                }
            }

            @Override
            public void onStart() {
                super.onStart();
                updateHint("签到中，请稍候...");
            }

            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    private void executeDownloadTmk(final boolean doAutoSign) {
        new AsyncDownloadTmkTask(context, dataMap, tempMap) {
            @Override
            public void onStart() {
                super.onStart();
                updateHint("正在下载主密钥");
            }

            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                putResponseCode(strings[0], strings[1]);
                if (doAutoSign && "00".equals(strings[0])) {
                    //下载主密钥完成，紧接着开始签到
                    executeSignIn(true);
                } else {
                    jumpToNext();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,transCode);
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        boolean result = false;
        result = pinPadDev.loadTMK("324C0419C85EEA2694765164AE0E40BC", "9783A7C4");
//                        executeDownloadTmk(true);
        if (result) {
            logger.info("[下发主密钥成功]");
            Settings.setTmkExist(context);//设置主密钥存在的标识
            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
            if (doAutoSign) {
                //下载主密钥完成，紧接着开始签到
                executeSignIn(true);
            } else {
                jumpToNext();
            }
        } else {
            logger.info("[下发主密钥失败]");
        }
    }

    //查询最后一笔二维码交易
    private void executeScanQueryLast() {
        new AsyncScanRearchLastTask(context, dataMap, tempMap) {
            @Override
            public void onStart() {
                updateHint("正在查询末笔交易...");
            }

            @Override
            public void onFinish(String[] strings) {
                tempMap.putAll(returnMap);
                tempMap.put(iso_f39, strings[0]);
                putResponseCode(strings[0], strings[1]);
                if (tempMap.get(iso_f39).equals("00")) {
                    tempMap.put(key_oriTransTime, returnMap.get(iso_f12) + returnMap.get(iso_f13));
                    tempMap.put(iso_f61, dataMap.get(iso_f61));
                    curTradeInfo = tradeDao.queryForId(dataMap.get(iso_f61).substring(6, 12));
                    //********************更新本地流水表***********************//
                    if (curTradeInfo != null) {
                        tempMap.put(iso_f47, curTradeInfo.getIso_f47());
                        if (curTradeInfo.getFlag() != 1 && curTradeInfo.getFlag() != 2 && curTradeInfo.getFlag() != 3 && curTradeInfo.getFlag() != 4 && curTradeInfo.getFlag() != 5) {
                            curTradeInfo.setFlag(1);
                            curTradeInfo.setIso_f12(tempMap.get(iso_f12));
                            curTradeInfo.setIso_f13(tempMap.get(iso_f13));
                            curTradeInfo.setIso_f37(tempMap.get(iso_f37));
                            curTradeInfo.setIso_f38(tempMap.get(iso_f38));
                            curTradeInfo.setIso_f39(tempMap.get(iso_f39));
                            curTradeInfo.setIso_f44(tempMap.get(iso_f44));
                            curTradeInfo.setIso_f64(tempMap.get(iso_f64));
                            boolean isUpdate = tradeDao.update(curTradeInfo);
                            logger.info("末笔查询成功后更新流水状态：" + isUpdate + "流水号：" + dataMap.get(iso_f61).substring(6, 12));

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
                        } else {
                            logger.info("末笔流水状态为：" + curTradeInfo.getFlag() + "，不做更新");
                        }
                    }

                } else {
                    logger.debug("末笔查询到未支付成功");
                }
                jumpToNext();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    private void executeReverseTask() {
        new AsyncAutoReverseTask(context, dataMap) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                if (index == -1) {
                    updateHint("冲正中，请稍候...");
                } else if (index == -2) {
                    updateHint("冲正已完成，开始交易");
                } else {
                    updateHint("正在进行第" + counts + "笔第" + index + "次冲正");
                }
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                String code = strings[0];
                if (StatusCode.CONNECTION_EXCEPTION.getStatusCode().equals(code) ||
                        StatusCode.SOCKET_TIMEOUT.getStatusCode().equals(code) ||
                        StatusCode.UNKNOWN_REASON.equals(code)) {
                    //以上情况认定为网络连接不上，也就没必要继续接下来的交易，直接返回结果
                    putResponseCode(strings[0], strings[1]);
                    jumpToNext("99");
                } else {
                    updateHint(getString(R.string.tip_trading_default));
                    beginOnline();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
    }

    private void startAnim() {
        if (loadIcon != null && loadIcon.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) loadIcon.getDrawable()).start();
        }
    }

    private void stopAnim() {
        if (loadIcon != null && loadIcon.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) loadIcon.getDrawable()).stop();
        }
    }

    private void updateHint(String content) {
        if (content == null) {
            hintShow.setText(R.string.tip_trading_default);
        } else {
            hintShow.setText(content);
        }
    }

    /**
     * 开始发送数据，单个请求。
     */
    private void sendData() {
        logger.info("222222222222222");
        if (transCode.equals(TransCode.SETTLEMENT) && "1".equals(Settings.getValue(context, Settings.KEY.BATCH_SEND_STATUS, "0"))) {
            logger.info("aaaaaaaaaaaaaaaaaa");
            updateHint("继续批结算...");
            String amountStr = Settings.getValue(context, Settings.KEY.BATCH_SEND_RETURN_DATA, "");
            if (!"".equals(amountStr)) {
                onAccountCheckSuccess(amountStr);
            } else {
                logger.error("继续批结算时，获取到对账返回的信息为空！");
            }
            return;
        }
        logger.info("333333333333333333");
        Object msgPacket = factory.pack(transCode, dataMap);
        //组包完成后插入2域数据，用于保存和显示
        logger.info("44444444444444444");
        if (!TextUtils.isEmpty(dataMap.get(iso_f2_result))) {
            dataMap.put(iso_f2, dataMap.get(iso_f2_result));
        }
        logger.info("5555555555555");
        if (msgPacket == null) {
            putResponseCode(StatusCode.PACKAGE_ERROR);
            logger.warn("请求报文为空，退出");
            if (TransCode.REVERSE_SETS.contains(transCode)) {
                activityStack.pop();
            } else {
                jumpToNext("99");
            }
            return;
        }
        logger.info("666666666666");
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> mapData = factory.unpack(transCode, data);
                if (null == mapData) {
                    putResponseCode(StatusCode.UNPACKAGE_ERROR);
                    logger.debug("后台返回数据解包异常");
                    jumpToNext("99");
                    return;
                }
                if (null != dataMap.get(iso_f14_result)) {
                    mapData.put(iso_f14_result, dataMap.get(iso_f14_result));
                }
                respHelper.onRespSuccess(TradingActivity.this, mapData);
                if(transCode==DISCOUNT_INTERGRAL){
                    jumpToNext("3",DISCOUNT_INTERGRAL, (Serializable) mapData);
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                respHelper.onRespFailed(TradingActivity.this, code, msg);
            }
        };
        logger.info("777777777777");
        if (msgPacket instanceof byte[]) {
            String iso11 = dataMap.get(iso_f11);
            logger.info("8888888888");
            socketClient.sendData((byte[]) msgPacket, handler, transCode);
            if (CAUSE_REVERSE_SETS.contains(transCode)) {
                //保存交易信息，用于后续冲正使用
                ReverseInfo record = new ReverseInfo(transCode, dataMap);
                String timeTemp = newFormat.format(new Date());
                if (!StringUtils.isStrNull(timeTemp) && timeTemp.length() >= 14) {
                    logger.debug("冲正记录生成时间：" + timeTemp.substring(8, 14));
                    logger.debug("冲正记录生成时间：" + timeTemp.substring(4, 8));
                    record.setIso_f12(timeTemp.substring(8, 14));
                    record.setIso_f13(timeTemp.substring(4, 8));
                }
                boolean r = reverseDao.save(record);
                logger.info(iso11 + "==>" + transCode + "==>插入冲正表中==>" + r);
            }
            logger.info("9999999999");
            if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
                TradeInfo info = new TradeInfo(transCode, dataMap);
                String timeTemp = newFormat.format(new Date());
                if (!StringUtils.isStrNull(timeTemp) && timeTemp.length() >= 14) {
                    logger.debug("TradeInfo冲正记录生成时间：" + timeTemp.substring(8, 14));
                    logger.debug("TradeInfo冲正记录生成时间：" + timeTemp.substring(4, 8));
                    info.setIso_f12(timeTemp.substring(8, 14));
                    info.setIso_f13(timeTemp.substring(4, 8));
                }
                info.setIso_f55_send(dataMap.get(iso_f55));
                info.setIso_f64(dataMap.get(iso_f64));
                info.setKey_bak_iso_f36(dataMap.get(iso_f36));
                boolean r = tradeDao.save(info);
                logger.info(iso11 + "==>" + transCode + "==>插入交易流水表中==>" + r);
            }
        } else {
            logger.warn("报文格式非字节数组");
        }
    }

    /**
     * 发散主密钥
     *
     * @param value      密钥值
     * @param checkValue 校验值
     */
    public void loadTMK(String value, String checkValue) {
        logger.info("[开始下发主密钥]kek:" + value + " checkValue:" + checkValue);
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        boolean result = false;
        if (pinPadDev != null) {
            //// TODO: 2016/12/1 sdk的问题，目前还无法通过sdk下发kek，也无法下发TEK
            String KEK = Settings.getParam(context, Settings.KEY.KEK);
            String realTmk = SecurityUtil.decrypt3DES(KEK, value);
            result = pinPadDev.loadTMK(realTmk, checkValue);//解密后明文注入  密文用2
        }
        if (result) {
            logger.info("[下发主密钥成功]");
            tempMap.put(iso_f39, "00");//下载主密钥并发散成功
            Settings.setTmkExist(context);//设置主密钥存在的标识
            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
        } else {
            logger.info("[下发主密钥失败]");
            tempMap.put(iso_f39, "01");
            StatusCode code = StatusCode.KEY_VERIFY_FAILED;
            putResponseCode(code);
        }
        jumpToNext();
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
    public void loadWorkKey(String pik, String pikCheck, String mak, String makCheck, String tdk, String tdkCheck) {
        boolean r1, r2, r3;
        boolean result = false;
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        if (pinPadDev != null) {
            r1 = pinPadDev.loadWorkKey(EnumWorkKeyType.PIK, pik, pikCheck);
            r2 = pinPadDev.loadWorkKey(EnumWorkKeyType.MAK, mak, makCheck);
            if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
                r3 = pinPadDev.loadWorkKey(EnumWorkKeyType.TDK, tdk, tdkCheck);
                result = r1 && r2 && r3;
            } else {
                result = r1 && r2;
            }
        }
        if (result) {
            Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
            try {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                String time = tempMap.get(iso_f12);
                String date = tempMap.get(iso_f13);
                String dateTime = year + date + time;
                DeviceFactory factory = DeviceFactory.getInstance();
                ISystemService service = factory.getSystemDev();
                service.updateSysTime(dateTime);//TODO:更新系统时间
            } catch (Exception e) {
                e.printStackTrace();
            }
            //工作密钥发散成功
            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, true);
            tempMap.put(iso_f39, "00");//下载工作密钥并发散成功
            jumpToNext();
        } else {
            StatusCode code = StatusCode.KEY_VERIFY_FAILED;
            putResponseCode(code);
            jumpToNext();
        }
    }

    private TradeInfo curTradeInfo;//当前交易信息
    private boolean hasImportOnlineResp = false;

    /**
     * 交易成功。删除冲正表的数据，插入到交易流水
     */
    public void onTradeSuccess(Map<String, String> returnData) {
        boolean dbResult;
        String iso11 = null;
        if (!transCode.equals(TransCode.INIT_TERMINAL)) {
            iso11 = returnData.get(iso_f11);
            String orgF11 = dataMap.get(iso_f11);
            if (iso11 == null) {
                putResponseCode(StatusCode.UNPACKAGE_ERROR);
                jumpToNext("99");
                return;
            }
            if (orgF11 != null && iso11 != null) {
                if (!iso11.equals(orgF11)) {
                    putResponseCode(StatusCode.FLOW_NUM_ERROR);
                    jumpToNext("99");
                    return;
                }
            }
            curTradeInfo = tradeDao.queryForId(iso11);
            //********************更新本地流水表***********************//
            if (curTradeInfo != null) {
                //保存返回数据
                curTradeInfo.update(returnData);
                curTradeInfo.setFlag(1);//代表交易成功
                if (!TransCode.BALANCE.equals(transCode)) {
                    try {
                        CommonManager commonManager = new CommonManager(TradeInfo.class, context);
                        long counts = commonManager.getBatchCount();
                        CommonManager commonManager2 = new CommonManager(ElecSignInfo.class, context);
                        long signCount = commonManager2.getSignCount();
                        long config = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.PARAM_MOST_TRANS);
                        String tempCount = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.PARAM_MAX_SIGN_CNT);
                        int configSign = Integer.parseInt(tempCount);
                        logger.info("已存储成功流水数量==>" + (++counts) + "==>终端最大存储数量==>" + config);
                        if (counts >= config) {
                            logger.warn("交易流水数量超限==>下次联机前将进行批结算");
                            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, true);
                        }
                        logger.info("已存储电子签名流水数量==>" + (++signCount) + "==>终端最大存储数量==>" + configSign);
                        if (signCount >= configSign) {
                            logger.warn("电子签名流水数量超限==>下次联机前将进行批结算");
                            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, true);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                logger.warn(iso11 + "==>交易类型==>" + transCode + "==>无法在数据库中查询到该数据模型==>新建模型");
                curTradeInfo = new TradeInfo(transCode, returnData);

            }
        }
        tempMap.putAll(returnData);
        putResponseCode(returnData.get(iso_f39), null);
        //**********************判断是否是需要导入联机响应数据的交易*********************//
        if (isImportOnlineRespTrade()) {
            //需要导入联机响应数据
            String iso55 = returnData.get(iso_f55);
            if (StringUtils.isStrNull(iso55)) {
                pbocService.stopProcess();
                logger.info("55域没有数据返回，直接导入成功");
                String iso_f11 = dataMap.get(TransDataKey.iso_f11);
                boolean dbResult1;
                //**********更新冲正表信息*************//
                if (CAUSE_REVERSE_SETS.contains(transCode)) {
                    dbResult1 = reverseDao.deleteById(iso_f11);
                    logger.info(iso_f11 + "交易成功==>删除冲正表记录==>" + dbResult1);
                }
                //************更新数据库****************//
                if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
                    dbResult1 = tradeDao.update(curTradeInfo);
                    saveIcData();
                    logger.info(iso_f11 + "==>交易类型==>" + transCode + "==>交易成功==>更新交易流水表==>" + dbResult1);
                } else {
                    logger.warn(iso_f11 + "==>交易类型==>该交易无需更新数据库");
                }
                //**************界面跳转*******************//
//                boolean isOpenSign = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_IS_OPEN_SIGN).equals("1") ? true : false;
                boolean isOpenSign=false;
                if (isOpenSign) {
                    //支持电子签名跳转到电子签名页面
                    jumpToNext();
                } else {
                    //不支持电子签名跳转到结果页面
                    jumpToNext("2");
                }
            } else {
                try {
                    Map<String, String> f55Map = TlvUtils.tlvToMap(iso55);
                    String tag91 = f55Map.get("91");
                    if (!TextUtils.isEmpty(tag91) && tag91.length() > 3) {
                        String respCode = new String(HexUtils.hexStringToByte(tag91.substring(tag91.length() - 4, tag91.length())));
                        logger.warn("发卡行认证数据：" + tag91 + "==>响应码：" + respCode);
                        EnumOnlineResult or = EnumOnlineResult.ONLINE_APPROVED;
                        if ("01".equals(respCode)) {//发卡行语音参考
                            or = EnumOnlineResult.ONLINE_VOICE_REFERENCE;
                        } else if ("05".equals(respCode)) {//交易拒绝
                            or = EnumOnlineResult.ONLINE_REFUSED;
                        }
                        pbocService.importOnlineResult(true, or, iso55);
                    } else {
                        pbocService.importOnlineResult(true, EnumOnlineResult.ONLINE_APPROVED, iso55);
                    }
                    hasImportOnlineResp = true;
                    //如果有脚本需要保存脚本执行结果（DF31）
                    String finalF55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());
                    if (TlvUtils.tlvToMap(finalF55).containsKey("DF31")) {
                        curTradeInfo.setScriptResult(1);
                        //下次联机时会进行脚本结果通知
                        BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_NEED_UPLOAD_SCRIPT, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    putResponseCode(StatusCode.UNPACKAGE_ERROR);
                    jumpToNext("99");
                }
                String finalF55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());
                curTradeInfo.setIso_f55_send(finalF55);
            }
        } else {
            //********************更新冲正表信息***************************//
            if (CAUSE_REVERSE_SETS.contains(transCode)) {
                dbResult = reverseDao.deleteById(iso11);
                logger.info(iso11 + "交易成功==>删除冲正表记录==>" + dbResult);
            }
            //******************撤销、退货类交易，同步更新原始交易信息****************//

            switch (transCode) {
                case INIT_TERMINAL:
                    String headData = dataMap.get(headerData);
                    logger.info("[HeadData]=" + headData);
                    break;
                case VOID://消费撤销
                case CANCEL://预授权撤销
                case COMPLETE_VOID://预授权完成撤销
                case SCAN_CANCEL://预授权完成撤销
                    String oriBatchNo = null;
                    String oriIso11 = dataMap.get(iso_f61);
                    if (oriIso11 != null && oriIso11.length() >= 12) {
                        oriBatchNo = oriIso11.substring(0, 6);//原始批次号
                        oriIso11 = oriIso11.substring(6, 12);//原始流水号
                    }
                    //从本地查找原始交易信息
                    TradeInfo originInfo = tradeDao.queryForId(oriIso11);
                    if (originInfo != null) {
                        String batchNo = "";
                        String iso60 = originInfo.getIso_f60();
                        if (iso60 != null && iso60.length() >= 8) {
                            batchNo = iso60.substring(2, 8);
                        }
                        if (batchNo.equals(oriBatchNo)) {
                            originInfo.setFlag(2);//已撤销
                            dbResult = tradeDao.update(originInfo);
                            logger.info(iso11 + "==>交易成功==>更新原始交易流水" + oriIso11 + "状态==>" + dbResult);
                        } else {
                            logger.warn(iso11 + "==>更新原始交易状态失败==>批次号不符");
                        }
                    } else {
                        logger.warn(iso11 + "==>更新原始交易状态失败==>无法找到原始交易流水==>" + oriIso11);
                    }
                    break;
                case REFUND:
                case SCAN_REFUND_W:
                case SCAN_REFUND_Z:
                case SCAN_REFUND_S:
                    TradeInfo originInfo2 = null;
                    String iso_f37 = dataMap.get(key_oriReference);
                    QueryBuilder builder = tradeDao.queryBuilder();
                    List<TradeInfo> tradeInfos = null;
                    try {
                        tradeInfos = builder.where().eq("iso_f37", iso_f37).query();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (null != tradeInfos && tradeInfos.size() > 0) {
                        originInfo2 = tradeInfos.get(0);
                    }
                    if (originInfo2 == null) {
                        logger.warn(iso_f37 + "==>无法找到该参考号信息");
                        logger.warn(iso11 + "==>更新原始交易状态失败==>无法找到原始参考号流水==>" + iso_f37);
                        return;
                    }
                    //从本地查找原始交易信息
                    originInfo2.setFlag(4);//已退货
                    dbResult = tradeDao.update(originInfo2);
                    logger.info(iso11 + "==>交易成功==>更新原始参考号" + iso_f37 + "状态==>" + dbResult);
                    break;
                case SIGN_OUT:
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT, false);
                    BusinessConfig.getInstance().setNumber(context, BusinessConfig.Key.KEY_POS_SERIAL, 1);
                    ViewUtils.showToast(context, R.string.tip_sign_out);
                    logger.debug("签退成功");
                    Intent intent = new Intent(context, LoginActivityForFirst.class);
                    context.startActivity(intent);
                    activityStack.removeExcept(LoginActivityForFirst.class);
                    break;
            }
            //**********************更新数据库****************************//
            if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
                dbResult = tradeDao.update(curTradeInfo);
                logger.info(iso11 + "==>交易类型==>" + transCode + "==>交易成功==>更新交易流水表==>" + dbResult);
            } else {
                logger.warn(iso11 + "==>交易类型==>该交易无需更新数据库");
            }
            //**********************跳转界面****************************//
            boolean isOpenSign = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_IS_OPEN_SIGN).equals("1") ? true : false;
            isOpenSign=false;
            //支持电子签名跳转到电子签名页面
            switch (transCode) {
                case SALE:
                case VOID:
                case REFUND:
                case AUTH:
                case CANCEL:
                case AUTH_COMPLETE:
                case COMPLETE_VOID:
                    boolean[] qpsCondition = getQpsCondition();
                    if (qpsCondition[1]) {//免签
                        tempMap.put(TransDataKey.KEY_QPS_NOSIGN_FLAG, "true");//小额免签标识
                        jumpToNext("2");
                    } else {
                        tempMap.put(TransDataKey.KEY_QPS_NOSIGN_FLAG, "false");//小额免签标识
                        if (isOpenSign) {
                            jumpToNext();
                        } else {
                            jumpToNext("2");
                        }
                    }
                    break;
                case DISCOUNT_INTERGRAL:
                    jumpToNext("3",DISCOUNT_INTERGRAL, (Serializable) returnData);
                    break;

                default:
                    jumpToNext();
                    break;

            }
            saveIcData();
        }

    }

    /**
     * 将ic卡数据保存到数据库
     */
    private void saveIcData() {
        String unKnown = null;
        String aid = null;
        String arqc = null;
        String iad = null;
        String atc = null;
        String tvr = null;
        String tsi = null;
        String aip = null;
        String tc = null;
        String appLabel = null;
        String appName = null;
        String isNotNeedSign = tempMap.get(TransDataKey.KEY_QPS_NOSIGN_FLAG);
        String isNotNeedPin = tempMap.get(TransDataKey.KEY_QPS_FLAG);
        String limitAmount = tempMap.get(TransDataKey.KEY_QPS_AMOUNT);
        //boolean isNotNeedSign = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.KEY_NOT_SIGN);
        //boolean isNotNeedPin = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.KEY_NOT_PIN);
        //String limitAmount = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_NOT_SIGN_OR_PIN_AMOUNT);
        logger.debug("是否不用签名：" + isNotNeedSign);
        logger.debug("是否不用密码：" + isNotNeedPin);
        logger.debug("限额是：" + limitAmount);
        String icData = tempMap.get(TransDataKey.KEY_IC_DATA_PRINT);
        if (null != icData) {
            Map<String, String> stringMap = TlvUtils.tlvToMap(icData);
            //不可预知数
            logger.debug("IC卡参数的数据为：" + icData);
            unKnown = stringMap.get("9F37");
            aid = stringMap.get("4F");
            arqc = stringMap.get("9F26");
            iad = stringMap.get("9F10");
            atc = stringMap.get("9F36");
            tvr = stringMap.get("95");
            tsi = stringMap.get("9B");
            aip = stringMap.get("9F82");
            appLabel = stringMap.get("50");
            appName = stringMap.get("9F12");
            tc = stringMap.get("97");
        }
        TradePrintData tradePrintData = new TradePrintData();
        if (null != tempMap.get(iso_f11)) {
            tradePrintData.setIso_f11(tempMap.get(iso_f11));
        }
        if (null != arqc) {
            tradePrintData.setArqc(arqc);
        }
        if (null != tvr) {
            tradePrintData.setTvr(tvr);
        }
        if (null != aid) {
            tradePrintData.setAid(aid);
        }
        if (null != atc) {
            tradePrintData.setAtc(atc);
        }
        if (null != tsi) {
            tradePrintData.setTsi(tsi);
        }
        if (null != unKnown) {
            tradePrintData.setUmpr_num(unKnown);
        }
        if (null != aip) {
            tradePrintData.setAip(aip);
        }
        if (null != iad) {
            tradePrintData.setIad(iad);
        }
        if (null != tc) {
            tradePrintData.setTc(tc);
        }
        if (null != appLabel) {
            tradePrintData.setAppLabel(appLabel);
        }
        if (null != appName) {
            tradePrintData.setAppName(appName);
        }
        if (null != limitAmount) {
            tradePrintData.setAmount(limitAmount);
        }
        if (null != isNotNeedSign) {
            tradePrintData.setNoNeedSign("true".equals(isNotNeedSign) ? true : false);
        }
        if (null != isNotNeedPin) {
            tradePrintData.setNoNeedPin("true".equals(isNotNeedPin) ? true : false);
        }
        tradePrintData.setRePrint(false);
        printDataCommonDao.save(tradePrintData);
    }

    /**
     * 交易失败。
     */
    public void onTradeFailed(String iso11, String code, String msg) {
        if (iso11 == null) {
            iso11 = dataMap.get(iso_f11);
        }
        putResponseCode(code, msg);
        boolean dbResult;
        //********************更新本地流水表记录***********************//
        TradeInfo initialTrade = tradeDao.queryForId(iso11);
        if (initialTrade != null) {
            //保存响应码，该响应码可能是自定义也可能是后台返回的
            initialTrade.setIso_f39(code);
            initialTrade.setFlag(0);
        } else {
            logger.warn(iso11 + "==>交易类型==>" + transCode + "==>无法在数据库中查询到该数据模型");
        }
        logger.warn(iso11 + "==>交易失败==>" + code + "==>" + msg);
        if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
            if (initialTrade != null) {
                dbResult = tradeDao.update(initialTrade);
                logger.warn(iso11 + "==>交易类型==>" + transCode + "==>交易失败==>更新交易流水表==>" + dbResult);
            }
        }
        if (TransCode.CAUSE_REVERSE_SETS.contains(transCode)) {
            //更新冲正表记录
            ReverseInfo reverseInfo = reverseDao.queryForId(iso11);
            if (reverseInfo == null) {
                logger.warn(iso11 + "==>无法查找到对应的冲正表信息");
                return;
            }
            if (StatusCode.SOCKET_TIMEOUT.getStatusCode().equals(code)) {
                logger.warn(iso11 + "==>交易状态未知==>下一次交易前将发起冲正==>冲正原因98");
                reverseInfo.setIso_f39("98");
                dbResult = reverseDao.update(reverseInfo);
                logger.warn(iso11 + "==>更新冲正表39域信息==>" + dbResult);
            } else if (StatusCode.MAC_INVALID.getStatusCode().equals(code)) {
                logger.warn(iso11 + "==>交易状态未知==>下一次交易前将发起冲正==>冲正原因A0");
                reverseInfo.setIso_f39("AO");
                dbResult = reverseDao.update(reverseInfo);
                logger.warn(iso11 + "==>更新冲正表39域信息==>" + dbResult);
            } else if (StatusCode.UNKNOWN_REASON.getStatusCode().equals(code)
                    || StatusCode.TRADING_TERMINATES.getStatusCode().equals(code)
                    || StatusCode.TRADING_REFUSED.getStatusCode().equals(code)) {
                logger.warn(iso11 + "==>交易状态未知==>下一次交易前将发起冲正==>冲正原因06");
                reverseInfo.setIso_f39("O6");
                dbResult = reverseDao.update(reverseInfo);
                logger.warn(iso11 + "==>更新冲正表39域信息==>" + dbResult);
            } else {
                //交易状态确定为失败的情况，需要删除冲正表信息
                dbResult = reverseDao.deleteById(iso11);
                logger.warn(iso11 + "==>交易失败==>删除冲正流水表记录==>" + dbResult + "==>交易类型：" + transCode);
            }
            jumpToNext("99");
            initialTrade.setFlag(3);
            dbResult = tradeDao.update(initialTrade);
        } else if (transCode.equals(TransCode.REFUND)) {
            jumpToNext("99");
        } else if (transCode.equals(TransCode.TRANS_IC_DETAIL) || transCode.equals(TransCode.TRANS_CARD_DETAIL) || transCode.equals(TransCode.TRANS_FEFUND_DETAIL)) {
            DialogFactory.showSelectDialog(context, getString(R.string.tip_dialog_title), getString(R.string.tip_send_data_fail), new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            transCode = TransCode.SETTLEMENT;
                            sendData();
                            break;
                        case NEGATIVE:
                            activityStack.pop();
                            break;
                    }
                }
            });
        } else if (transCode.equals(TransCode.SETTLEMENT_DONE)) {
            DialogFactory.showSelectDialog(context, getString(R.string.tip_dialog_title), getString(R.string.tip_batch_down_fail), new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            batchSendDown("2");
                            break;
                        case NEGATIVE:
                            activityStack.pop();
                            break;
                    }
                }
            });
        } else if (transCode.equals(TransCode.SETTLEMENT)) {
            DialogFactory.showSelectDialog(context, getString(R.string.tip_dialog_title), getString(R.string.tip_batch_fail), new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            transCode = TransCode.SETTLEMENT;
                            sendData();
                            break;
                        case NEGATIVE:
                            activityStack.pop();
                            break;
                    }
                }
            });
        } else if (transCode.equals(SIGN_OUT)) {
            DialogFactory.showMessageDialog(context, getString(R.string.tip_dialog_title), getString(R.string.tip_sign_out_fail), new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(ButtonType button, View v) {
                    activityStack.pop();
                }
            });
        }/* else if ((transCode.equals(SCAN_PAY_WEI) || transCode.equals(SCAN_PAY_ALI) || transCode.equals(SCAN_PAY_SFT)||transCode.equals(SCAN_SERCH)) &&  "A7".equals(code)) {
            sendDataLoop();
        } */
        else if(transCode.equals(DISCOUNT_INTERGRAL)){
            jumpToNext("99");
        }else {
            jumpToNext();
        }
    }

    @Override
    protected void jumpToNext(String conditionId) {
        if (entryFlag) {
            boolean elcFalg = false;
            boolean resultFalg = false;
            logger.info("entryControlInfo = " + entryControlInfo);
            if (!TextUtils.isEmpty(entryControlInfo) && entryControlInfo.length() >= 3) {
                elcFalg = "1".equals(entryControlInfo.substring(2, 3));
                resultFalg = "1".equals(entryControlInfo.substring(1, 2));
            }
            if (!resultFalg)
                super.jumpToNext(conditionId);
            else
                super.jumpToNext("22");
        } else {
            super.jumpToNext(conditionId);
        }
    }

    /**
     * 交易失败
     *
     * @param status 自定义状态码（包含渠道自定义的）
     */
    public void onTradeFailed(String iso11, StatusCode status) {
        onTradeFailed(iso11, status.getStatusCode(), getString(status.getMsgId()));
    }

    /**
     * 交易失败
     *
     * @param iso 规范定义状态码
     */
    public void onTradeFailed(String iso11, ISORespCode iso) {
        onTradeFailed(iso11, iso.getCode(), getString(iso.getResId()));
    }

    private void putResponseCode(String respCode, String respMsg) {
        tempMap.put(iso_f39, respCode);
        tempMap.put(key_resp_code, respCode);
        tempMap.put(key_resp_msg, respMsg);
    }

    private void putResponseCode(StatusCode code) {
        putResponseCode(code.getStatusCode(), getString(code.getMsgId()));
    }

    public void onAccountCheckSuccess(String checkStr) {
        logger.debug("结算222222222222");
        updateHint("正在批结算\n    请稍后");
        magsCards.clear();
        icCards.clear();
        CommonManager commonManager = new CommonManager(TradeInfo.class, context);
        final String amountCode = checkStr.substring(checkStr.length() - 1, checkStr.length());
        Settings.setValue(context, Settings.KEY.IS_BATCH_EQUELS, amountCode);
        String batchNo = BusinessConfig.getInstance().getBatchNo(context);
        logger.debug("结算33333333333333");
        new AsyncCheckBillTask(TradingActivity.this) {
            @Override
            public void onStart() {
                super.onStart();
                updateHint("正在查询终端数据……");
            }

            @Override
            public void onFinish(List<List<TradeInfo>> lists) {
                logger.debug("完成AsyncCheckBillTask111111111");
                super.onFinish(lists);
                magsCards = lists.get(0);
                icCards = lists.get(1);
                refundInfos = lists.get(2);
                switch (amountCode) {
                    case "0":
                        activityStack.pop();
                        ViewUtils.showToast(TradingActivity.this, "对账返回码错误：00");
                        logger.debug("对账返回为0未定义");
                        break;
                    case "1"://对账平
                        logger.debug("对账返回为1对账平");
                        //roopToUploadIc();
                        printTotalData();
                        break;
                    case "2"://对账不平
                        updateHint("  对账不平 \n正在批上送\n    请稍后  ");
                        logger.debug("对账返回为2对账不平");
//                        roopToUploadCard();

//                        batchSendDown("2");
                        printTotalData();
                        break;
                    case "3":
                        logger.debug("对账返回为3，异常情况");
                        break;
                }
            }
        }.execute();
        logger.debug("结算444444444");
    }

    private void roopToUploadCard() {
        if (null != magsCards && magsCards.size() > 0) {
            //uploadMagsCardData(strings.get(0));
            new AsyncUploadMagsDataTask(context, dataMap, magsCards) {
                @Override
                public void onProgress(Integer counts, Integer index) {
                    super.onProgress(counts, index);
                    updateHint("磁条卡数据共" + counts + "批\n正在上送第" + (index) + "批");
                }

                @Override
                public void onStart() {
                    super.onStart();
                    updateHint("正在查询磁条卡数据……");
                }

                @Override
                public void onFinish(String[] strings) {
                    super.onFinish(strings);
                    roopToUploadRefund();
                }
            }.execute();
        } else {
            logger.debug("磁条卡没有交易记录");
            roopToUploadRefund();
        }
    }

    private void roopToUploadRefund() {
        logger.debug("进入退货循环上送方法");
        if (null != refundInfos && refundInfos.size() > 0) {
            uploadRefundDataTask();
        } else {
            logger.debug("退货没有交易记录");
            roopToUploadIc();
        }
    }

    private void uploadRefundDataTask() {
        new AsyncUploadRefundDataTask(context, dataMap, refundInfos) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                updateHint("退货数据共" + counts + "条\n正在上送第" + (index) + "条");
            }

            @Override
            public void onStart() {
                super.onStart();
                updateHint("正在查询退货数据……");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                roopToUploadIc();
            }
        }.execute();
    }

    private void roopToUploadIc() {
        if (icCards != null && icCards.size() > 0) {
            uploadIcDataTask();
        } else {
            logger.debug("IC卡没有交易记录");
            batchSendDown("2");
        }

    }

    private void uploadIcDataTask() {
        new AsyncUploadIcDataTask(context, dataMap, icCards) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                updateHint("IC卡数据共" + counts + "条\n正在上送第" + (index) + "条");
            }

            @Override
            public void onStart() {
                super.onStart();
                updateHint("正在查询IC卡数据……");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                batchSendDown("2");
            }
        }.execute();
    }

    private void otherForSettleDown() {
        //更新批次号+1
        DialogFactory.hideAll();
        updateBatchNo();
        //清空所有表数据
        deleteAllData();
        Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "2");
        Settings.setValue(context, Settings.KEY.BATCH_SEND_RETURN_DATA, "");//对账数据初始化
        BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, false);//设置记录上限标识为false
        BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT, true);
        String path = Config.Path.SIGN_PATH;
        if (FileUtils.getFileSize(path) > 0) {
            FileUtils.deleteAllFiles(path);
        }
        boolean isAutoSignOut = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_AUTO_SIGN_OUT).equals("1") ? true : false;
        if (isAutoSignOut) {
            autoSignOut();
        } else {
            activityStack.pop();
            logger.debug("没有自动签退");
            ViewUtils.showToast(context, getString(R.string.tip_batch_over_please_sign_out));
        }
    }


    /**
     * 更新批次号+1
     */
    private void updateBatchNo() {
        String batchNo = BusinessConfig.getInstance().getBatchNo(context);
        int batchNum = Integer.parseInt(batchNo);
        String newBatch = DataHelper.formatToXLen(++batchNum, 6);
        BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BATCH_NO, batchNo);
        BusinessConfig.getInstance().setBatchNo(context, newBatch);
    }

    /**
     * 批结算完成后，删除所有数据
     */
    private void deleteAllData() {
        List<String> sqls = new ArrayList<>();
        sqls.add("delete from tb_trade_serial");
        sqls.add("delete from tb_reverse");
        sqls.add("delete from tb_reprint");
        sqls.add("delete from tb_elec_sign");
        boolean isDel = tradeDao.deleteBySQL(sqls);
        if (isDel) {
            logger.debug("该批次数据清空完成！");
        } else {
            logger.debug("该批次数据清空失败！");
        }
    }


    /**
     * 批量上送完成
     */
    private void batchSendDown(String flag) {
        dataMap.clear();
        dataMap.put(key_is_amount_ok, flag);
        dataMap.put(key_batch_upload_count, icCards.size() + "");
        new AsyncBatchUploadDown(TradingActivity.this, dataMap) {
            @Override
            public void onStart() {
                super.onStart();
                updateHint("正在请求批上送完成");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if ("00".equals(strings[0])) {
                    updateHint(getString(R.string.tip_batch_send_down));
                    batchSendDown("1");
                    //开始打印
                    printTotalData();
                } else {
                    activityStack.pop();
                    logger.error("上送完成请求返失败（有收到平台返回值）");
                    ViewUtils.showToast(TradingActivity.this, R.string.tip_batch_down_fail);
                }
            }
        }.execute();
    }


    /**
     * 发起自动签退
     */
    private void autoSignOut() {
        logger.debug("发起自动签退");
        dataMap.clear();
        new AsyncAutoSignOut(TradingActivity.this, dataMap) {
            @Override
            public void onStart() {
                super.onStart();
                updateHint(getString(R.string.tip_auto_sign_out));
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if ("00".equals(strings[0])) {
                    BusinessConfig.getInstance().setFlag(TradingActivity.this, BusinessConfig.Key.FLAG_SIGN_IN, false);
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT, false);
                    BusinessConfig.getInstance().setNumber(context, BusinessConfig.Key.KEY_POS_SERIAL, 1);
                    ViewUtils.showToast(TradingActivity.this, R.string.tip_sign_out);
                    logger.debug("签退成功");
                    Intent intent = new Intent(TradingActivity.this, LoginActivityForFirst.class);
                    TradingActivity.this.startActivity(intent);
                    activityStack.removeExcept(LoginActivityForFirst.class);
                } else {
                    activityStack.pop();
                    logger.error("签退请求返失败（有收到平台返回值）");
                    ViewUtils.showToast(TradingActivity.this, R.string.tip_sign_out_fail);
                }
            }
        }.execute();
    }

    private void printTotalData() {
        new AsyncQueryPrintDataTask(context) {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish(List<List<TradeInfo>> lists) {
                TradingActivity.this.lists = lists;
                super.onFinish(lists);
                jiejiList = lists.get(0);
                daijiList = lists.get(1);
                rejestList = lists.get(3);
                failList = lists.get(4);
                batchDetailList = lists.get(5);
                reverseList = lists.get(6);
                beginToPrint(lists);
            }
        }.execute();
    }

    private void beginToPrint(List<List<TradeInfo>> lists) {
        printTransData = PrintTransData.getMenuPrinter();
        printTransData.open(context);
        printTransData.setBatchListener(this);
        printTransData.printBatchTotalData(lists, false);
    }

    private void printDetailData() {
        boolean isTipPrintDetail = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_TIP_PRINT_DETAIL).equals("1") ? true : false;
        if (null != batchDetailList && batchDetailList.size() > 0) {
            if (isTipPrintDetail) {
                DialogFactory.showSelectDialog(context, "提示", "是否打印批结算明细数据？", new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printBatchDetailData(batchDetailList);
                                break;
                            case NEGATIVE:
                                printFailData();
                                break;
                        }
                    }
                });
            } else {
                printTransData.printBatchDetailData(batchDetailList);
            }

        } else {
            printFailData();
        }

    }

    private void printFailData() {
        boolean isPrintFail = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_FAIL_DETAIL).equals("1") ? true : false;
        if (null != reverseList && reverseList.size() > 0 && isPrintFail) {
            DialogFactory.showSelectDialog(context, "提示", "是否打印失败交易？", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            printTransData.printFailData(reverseList);
                            break;
                        case NEGATIVE:
                            otherForSettleDown();
                            break;
                    }
                }
            });
        } else {
            otherForSettleDown();
        }

    }

    @Override
    public void onPrinterFirstSuccess() {
        printDetailData();
    }

    @Override
    public void onPrinterSecondSuccess() {
        printFailData();
    }

    @Override
    public void onPrinterThreeSuccess() {
        otherForSettleDown();
    }

    @Override
    public void onPrinterFirstFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(context,
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printBatchTotalData(lists, false);
                                break;
                            case NEGATIVE:
                                otherForSettleDown();
                                DialogFactory.hideAll();
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterSecondFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(context,
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                if (null != batchDetailList && batchDetailList.size() > 0) {
                                    printTransData.printBatchDetailData(batchDetailList);
                                } else {
                                    printFailData();
                                }
                                break;
                            case NEGATIVE:
                                otherForSettleDown();
                                DialogFactory.hideAll();
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterThreeFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(context,
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                if (null != reverseList && reverseList.size() > 0) {
                                    printTransData.printFailData(reverseList);
                                } else {
                                    otherForSettleDown();
                                }
                                break;
                            case NEGATIVE:
                                otherForSettleDown();
                                DialogFactory.hideAll();
                                break;
                        }
                    }
                });
    }

    private class MyReceiver extends PbocEventReceiver {

        @Override
        public void onImportAmount() {
            pbocService.importAmount(dataMap.get(iso_f4));
        }

        @Override
        public void onImportPin() {
            pbocService.importPIN(false, null);
        }

        @Override
        public void onRequestOnline() {
            sendData();
        }

        @Override
        public void onConfirmCardNo(String cardNo) {
            pbocService.importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
        }

        @Override
        public boolean onTradeApproved() {//交易批准
            if (hasImportOnlineResp && isImportOnlineRespTrade()) {
                String iso11 = dataMap.get(iso_f11);
                boolean dbResult;
                //**********更新冲正表信息*************//
                if (CAUSE_REVERSE_SETS.contains(transCode)) {
                    dbResult = reverseDao.deleteById(iso11);
                    logger.info(iso11 + "交易成功==>删除冲正表记录==>" + dbResult);
                }
                //************更新数据库****************//
                if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
                    dbResult = tradeDao.update(curTradeInfo);
                    saveIcData();
                    logger.info(iso11 + "==>交易类型==>" + transCode + "==>交易成功==>更新交易流水表==>" + dbResult);
                } else {
                    logger.warn(iso11 + "==>交易类型==>该交易无需更新数据库");
                }
                //**************界面跳转*******************//
                boolean isOpenSign = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_IS_OPEN_SIGN).equals("1") ? true : false;
                isOpenSign=false;
                if (isOpenSign) {
                    //支持电子签名跳转到电子签名页面
                    jumpToNext();
                } else {
                    //不支持电子签名跳转到结果页面
                    jumpToNext("2");
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean onTradeTerminated() {//交易终止
            if (hasImportOnlineResp && isImportOnlineRespTrade()) {
                onTradeFailed(null, StatusCode.TRADING_TERMINATES);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean onTradeRefused() {//交易拒绝
            if (hasImportOnlineResp && isImportOnlineRespTrade()) {
                onTradeFailed(null, StatusCode.TRADING_REFUSED);
                return true;
            } else {
                return false;
            }
        }
    }

    public void onCancelQueryClick(View view) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>重复的onCancelTradeClick事件，不响应！");
            return;
        }
        if (null == alertDialog) {
            alertDialog = DialogFactory.showCancelTradingDialog(context, "提示", "点击“确认退出”将退出本次扫码收单，如有异议，请使用扫码末笔查询", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            finishTrading();
                            activityStack.pop();
                            break;
                        case NEGATIVE:
                            break;
                    }
                }
            });
        }
        alertDialog.show();
    }
}

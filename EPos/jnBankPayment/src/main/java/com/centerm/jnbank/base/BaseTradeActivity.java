package com.centerm.jnbank.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EmvTag;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocFlow;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocSlot;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumTransType;
import com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction;
import com.centerm.cpay.midsdk.dev.define.pboc.TransParams;
import com.centerm.jnbank.R;
import com.centerm.jnbank.activity.CheckCardActivty;
import com.centerm.jnbank.activity.InputDirectorPwdActivity;
import com.centerm.jnbank.activity.InputMoneyActivity;
import com.centerm.jnbank.activity.MainActivity;
import com.centerm.jnbank.activity.TradingActivity;
import com.centerm.jnbank.bean.QpsBinData;
import com.centerm.jnbank.bean.QpsBlackBinData;
import com.centerm.jnbank.bean.iso.Iso62Qps;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.xml.process.ComponentNode;
import com.centerm.jnbank.xml.process.TradeProcess;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.cpay.midsdk.dev.define.pboc.EmvTag.EMVTAG_AID;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_ERROR;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_NEED_CHANGE_READ_CARD_TYPE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_AID_SELECT;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_AMOUNT;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_CARD_INFO_CONFIRM;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_EC_TIPS_CONFIRM;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_ONLINE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_PIN;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_TIPS_CONFIRM;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_USER_AUTH;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_RETURN_CARD_LOAD_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_RETURN_CARD_OFFLINE_BALANCE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_RETURN_CARD_TRANS_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_TRADE_APPROVED;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_TRADE_REFUSED;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_TRADE_TERMINATED;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_FIRST_EC_BALANCE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_FIRST_EC_CODE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_LOAD_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_SECOND_EC_BALANCE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_SECOND_EC_CODE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_TRANS_LOG;
import static com.centerm.jnbank.common.TransDataKey.FLAG_AUTO_SIGN;
import static com.centerm.jnbank.common.TransDataKey.KEY_IC_DATA_PRINT;
import static com.centerm.jnbank.common.TransDataKey.iso_f14_forvoid;
import static com.centerm.jnbank.common.TransDataKey.iso_f14_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f23;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f35;
import static com.centerm.jnbank.common.TransDataKey.iso_f36;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f55;
import static com.centerm.jnbank.common.TransDataKey.iso_f55_reverse;
import static com.centerm.jnbank.common.TransDataKey.iso_track2;
import static com.centerm.jnbank.common.TransDataKey.key_bank_card_type;
import static com.centerm.jnbank.utils.CommonUtils.encryptTrackDataTag;

/**
 * 交易类界面的父类
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */

public abstract class BaseTradeActivity extends BaseActivity {

    protected final static String KEY_ONLINE_REQUEST_EVENT = "KEY_ONLINE_REQUEST_EVENT";
    protected static TransParams pbocParams;//PBOC参数，必须为静态变量，供所有交易界面使用
    protected static Iso62Qps qpsParams;//小额免密免签参数
    private TradeProcess tradeProcess;//交易流程对象
    protected Map<String, String> dataMap;//交易数据集合
    protected Map<String, String> tempMap;//临时数据集合
    protected String transCode;//交易码
    protected PbocEventReceiver receiver;//PBOC广播接收
    protected IPbocService pbocService;//PBOC服务
    protected boolean clssForcePin;//闪付凭密标识

    public boolean entryFlag;//支付组件调用方式标志
    public String entryControlInfo;//支付组件调用开关（显示金额；显示结果页；电子签名）
    public int entryPrintCount;//支付组件调用打印联数

    public static void nullQpsParams() {
        qpsParams = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewUtils.disableStatusBar(context);
    }

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        if (pbocParams == null) {
            pbocParams = new TransParams(EnumPbocSlot.SLOT_IC, EnumPbocFlow.PBOC_FLOW);
            //// TODO: 2016/12/13 暂且把电子现金关闭 
            pbocParams.setSupportEc(false);
        }
        initTradeProcess();
        if (isICTrade() || this instanceof CheckCardActivty) {
            //IC卡交易，需要初始化PBOC服务（检卡界面还不知道是否是IC卡交易）
            try {
                pbocService = DeviceFactory.getInstance().getPbocService();
            } catch (Exception e) {
                e.printStackTrace();
                //跳转结果页，各个业务流程定义时，需要把结果页的跳转条件设置为99
                StatusCode status = StatusCode.EMV_KERNEL_EXCEPTION;
                dataMap.put(TransDataKey.key_resp_code, status.getStatusCode());
                dataMap.put(TransDataKey.key_resp_msg, getString(status.getMsgId()));
                jumpToNext("99");
            }
        }
        //if ((TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode)) && qpsParams == null) {
        if (TransCode.SALE.equals(transCode) && qpsParams == null) {
            CommonDao<Iso62Qps> dao = new CommonDao<>(Iso62Qps.class, dbHelper);
            List<Iso62Qps> qpsList = dao.query();
            if (qpsList != null && qpsList.size() > 0) {
                qpsParams = qpsList.get(0);
                logger.warn("查询小额免密免签参数==>" + qpsParams.toString());
            } else {
                logger.debug("未查询到小额免密免签参数==>可进行参数下载");
            }
        }
    }

    /**
     * 初始化交易节点
     */
    protected void initTradeProcess() {
        Intent intent = getIntent();
        String action = intent.getAction();
        transCode = intent.getStringExtra(KEY_TRANSCODE);
        tradeProcess = intent.getParcelableExtra(KEY_PROCESS);
        clssForcePin = intent.getBooleanExtra(KEY_CLSS_FORCE_PIN_FLAG, false);

        //支付组件入口参数
        entryFlag = intent.getBooleanExtra(ENTRY_FALG, false);
        entryControlInfo = intent.getStringExtra(ENTRY_CONTROL_INFO);
        entryPrintCount = intent.getIntExtra(ENTRY_PRINT_COUNT, 2);
        if (tradeProcess == null) {
            logger.warn("TradeProcess is null");
            tradeProcess = new TradeProcess();
        }
        dataMap = tradeProcess.getDataMap();
        tempMap = tradeProcess.getTempMap();
        for (ComponentNode mComponentNode : tradeProcess.getComponentNodeList()) {
            if (mComponentNode.getComponentName().equals(action)) {
                tradeProcess.setCurNode(mComponentNode);
            }
        }
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        int titleId = TransCode.codeMapName(transCode);
        if (titleId == R.string.unknown) {
            titleId = R.string.title_result;
        }
        setTitle(titleId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewUtils.enableStatusBar(context);
        if (receiver != null) {
            try {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 跳转到下一个界面，条件ID为1
     */
    protected void jumpToNext() {
        jumpToNext("1");
    }

    @Override
    public void jumpMethod() {
        jumpToNext("1");
    }

    /**
     * 跳转到到下一个界面
     *
     * @param conditionId 条件ID
     */
    protected void jumpToNext(String conditionId) {
        DialogFactory.hideAll();
        Intent intent = getNextIntent(conditionId);
        if (intent == null) {
            String oper = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
          /*  if ("99".equals(oper) || "00".equals(oper)) {
                activityStack.backTo(MenuActivity.class);
            } else {
                activityStack.backTo(MainActivity.class);
            }*/
            if (activityStack.contain(MenuActivity.class)) {
                activityStack.backTo(MenuActivity.class);
            } else {
                activityStack.backTo(MainActivity.class);
            }
            return;
        }
        if (this instanceof InputDirectorPwdActivity) {
            activityStack.remove(this);
        }
        //如果是支付组件调用，需要判断是否将输入金额移除
        if (entryFlag) {
            boolean amtFalg = false;
            if (!TextUtils.isEmpty(entryControlInfo) && entryControlInfo.length() >= 3) {
                amtFalg = "0".equals(entryControlInfo.substring(0, 1));
            }
            if (amtFalg && this instanceof InputMoneyActivity) {
                activityStack.remove(this);
            }
        }
        //如果是支付组件入口，所有activity出栈，并将数据返回到原有的入口中
        if (intent.getAction().equals("com.centerm.epos.entry")) {
            logger.info("需要跳转到EntryActivity!!!");
            //Android5.0以下使用singleTask属性的activity不能使用使用setResult
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activityStack.backTo(MainActivity.class);
        }
        startActivity(intent);
    }

    /**
     * 跳转到下一个界面并携带数据
     *
     * @param conditionId 条件ID
     * @param key         数据的键
     * @param data        数据
     */
    protected void jumpToNext(String conditionId, String key, Serializable data) {
        Intent intent = getNextIntent(conditionId);
        if (intent != null && key != null && data != null) {
            intent.putExtra(key, data);
        }
        if (intent == null) {
            String oper = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
            /*if ("99".equals(oper) || "00".equals(oper)) {
                activityStack.backTo(MenuActivity.class);
            } else {
                activityStack.backTo(MainActivity.class);
            }*/
            if (activityStack.contain(MenuActivity.class)) {
                activityStack.backTo(MenuActivity.class);
            } else {
                activityStack.backTo(MainActivity.class);
            }
            return;
        }
        if (this instanceof InputDirectorPwdActivity) {
            activityStack.remove(this);
        }
        startActivity(intent);
    }

    /**
     * 跳转到下一个界面并携带数据
     *
     * @param key  键
     * @param data 数据
     */
    protected void jumpToNext(String key, Serializable data) {
        jumpToNext("1", key, data);
    }

    /**
     * 跳转到结果页
     *
     * @param status 状态
     */
    protected void jumpToResultActivity(StatusCode status) {
        tempMap.put(TransDataKey.key_resp_code, status.getStatusCode());
        tempMap.put(TransDataKey.key_resp_msg, getString(status.getMsgId()));
        //结果页的条件码统一定义为99
        jumpToNext("99");
        if (pbocService != null) {
            pbocService.abortProcess();
        }
    }

    protected void jumpToSignIn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                jumpToTradingActivity(TransCode.SIGN_IN);
            }
        }, 200);
    }

    protected void jumpToDownloadTmk() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, TradingActivity.class);
                intent.putExtra(KEY_TRANSCODE, TransCode.OBTAIN_TMK);
                intent.putExtra(FLAG_AUTO_SIGN, true);
                startActivity(intent);
            }
        }, 200);
    }

    private void jumpToTradingActivity(String transCode) {
        Intent intent = new Intent(context, TradingActivity.class);
        intent.putExtra(KEY_TRANSCODE, transCode);
        startActivity(intent);
    }

    private Intent getNextIntent(String conditionId) {
        ComponentNode node = tradeProcess.getNextComponentNode(conditionId);
        if (node == null) {
            logger.warn("当前交易：" + transCode + "==>未找到下一个交易节点");
            return null;
        }
        Intent intent = new Intent();
        intent.setAction(node.getComponentName());
        intent.putExtra(KEY_PROCESS, tradeProcess);
        intent.putExtra(KEY_TRANSCODE, transCode);
        //传递插卡消费和闪付凭密的标识
        intent.putExtra(KEY_INSERT_SALE_FLAG, getIntent().getBooleanExtra(KEY_INSERT_SALE_FLAG, false));
        intent.putExtra(KEY_CLSS_FORCE_PIN_FLAG, clssForcePin);
        //支付组件入口标志
        intent.putExtra(ENTRY_FALG, entryFlag);
        intent.putExtra(ENTRY_CONTROL_INFO, entryControlInfo);
        intent.putExtra(ENTRY_PRINT_COUNT, entryPrintCount);
        return intent;
    }

    /**
     * 获取IC卡卡片相关信息的tag列表
     *
     * @return tag列表
     */
    public List<EmvTag.Tag> getCardInfoTags() {
        List<EmvTag.Tag> tagList = new ArrayList<>();
        tagList.add(EmvTag.Tag._57);//二磁
        tagList.add(EmvTag.Tag._5F24);//卡片失效日期
        tagList.add(EmvTag.Tag._5F34);//卡序列号
        return tagList;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_HOME:
                return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 开启PBOC流程
     */
    protected void beginPbocProcess() {
        IPbocService pbocService = null;
        try {
            pbocService = DeviceFactory.getInstance().getPbocService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("开启PBOC流程==>" + codeMapPbocType() + "==>" + pbocParams.toString());
        pbocService.startProcess(codeMapPbocType(), pbocParams);
    }

    protected EnumTransType codeMapPbocType() {
        if (transCode == null) {
            throw new IllegalArgumentException("TransCode is null");
        }
        switch (transCode) {
            case TransCode.SALE:
                return EnumTransType.TRANS_TYPE_CONSUME;
            case TransCode.BALANCE:
                return EnumTransType.TRANS_TYPE_BALANCE_QUERY;
            case TransCode.VOID:
            case TransCode.COMPLETE_VOID:
                return EnumTransType.TRANS_TYPE_CONSUME_CANCEL;
            case TransCode.AUTH:
            case TransCode.AUTH_COMPLETE:
            case TransCode.CANCEL:
                return EnumTransType.TRANS_TYPE_PRE_AUTH;
            case TransCode.REFUND:
                return EnumTransType.TRANS_TYPE_RETURN;
            default:
                return null;
        }
    }

    /**
     * 判断是否IC卡插卡交易，判断的前提是已经检卡
     *
     * @return 插卡交易返回true，否则返回false
     */
    protected boolean isICInsertTrade() {
        String iso22 = dataMap.get(iso_f22);
        if (iso22 != null && iso22.startsWith("05")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是IC卡交易，包含插卡和非接，判断的前提是已经检卡
     *
     * @return IC卡交易返回true，否则返回false
     */
    protected boolean isICTrade() {
        String iso22 = dataMap.get(iso_f22);
        if (iso22 != null && (iso22.startsWith("05") || iso22.startsWith("07"))) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是挥卡交易，判断的前提是已经检卡
     *
     * @return 挥卡交易返回true，否则返回false
     */
    protected boolean isICClssTrade() {
        String iso22 = dataMap.get(iso_f22);
        if (iso22 != null && iso22.startsWith("07")) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前交易是否需要导入联机响应数据
     *
     * @return 是返回true，否则返回false
     */
    protected boolean isImportOnlineRespTrade() {
        return isICInsertTrade()
                && (TransCode.BALANCE.equals(transCode) || TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode));
    }

    /**
     * 判断是否满足小额免密免签业务要求
     *
     * @return 是返回true，否则返回false
     */
    private boolean[] flags;


    public void resetQpsConditionFlags() {
        flags = null;
    }

    public Iso62Qps getQpsParams() {
        if (qpsParams == null) {
            CommonDao<Iso62Qps> dao = new CommonDao<>(Iso62Qps.class, dbHelper);
            List<Iso62Qps> qpsList = dao.query();
            if (qpsList != null && qpsList.size() > 0) {
                qpsParams = qpsList.get(0);
                logger.warn("查询小额免密免签参数==>" + qpsParams.toString());
            } else {
                logger.debug("未查询到小额免密免签参数==>可进行参数下载");
            }
        }
        return qpsParams;
    }

    public boolean[] getQpsCondition() {
        if (flags != null)
            return flags;
        double printAmount = 0.0;
        flags = new boolean[]{false, false};
        try {
            String iso22 = dataMap.get(iso_f22);//服务点输入方式码
            String amt = dataMap.get(iso_f4);//金额
            String cardNo = dataMap.get(iso_f2_result);//金额
//            String cardType = transDatas.get(TradeInformationTag.IC_PARAMETER_AID);
            logger.info("终端QPS业务参数==>" + (qpsParams == null ? "null" : qpsParams.toString()));
            logger.info("当前交易QPS相关参数==>TransCode==>" + transCode + "==>ISO f22==>" + iso22 + "==>ISO f4==>" +
                    DataHelper.formatIsoF4(amt));
            qpsParams = getQpsParams();
            if (!clssForcePin
                    //&& (TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode))
                    && TransCode.SALE.equals(transCode)
                    && qpsParams != null) {
                if (iso22 == null || !iso22.startsWith("07")) {
                    //非非接方式，不支持QPS业务
                    return flags;
                }
                double currentAmt = DataHelper.parseIsoF4(amt);
                double noPinLimit = qpsParams.getNoPinLimit();
                double noSignLimit = qpsParams.getNoSignLimit();
                int stage = qpsParams.getPromotionStage();//推广阶段
                long start = System.currentTimeMillis();
                if (qpsParams.isNoPinOn() && currentAmt <= noPinLimit) {
                    CommonDao<QpsBinData> qpsBinDao = new CommonDao<>(QpsBinData.class, getDbHelper());
                    CommonDao<QpsBlackBinData> qpsBlackBinDao = new CommonDao<>(QpsBlackBinData.class, getDbHelper());
                    String cardNoPrefix = null;
                    if (!TextUtils.isEmpty(cardNo)) {
                        cardNoPrefix = cardNo.length() > 6 ? cardNo.substring(0, 6) : cardNo;
                    } else {
                        logger.warn("卡号为空，无法判断");
                        return flags;
                    }
                    if (currentAmt <= noPinLimit) {
                        switch (stage) {
                            case 0:
                                //不支持非接快速业务
                                flags[0] = false;
                                break;
                            case 1:
                                //试点阶段一（借贷记卡从BIN表A中判断）
                                List<QpsBinData> binDatas = qpsBinDao.queryBuilder().where().eq("type", "A").and
                                        ().eq

                                        ("cardBin", cardNoPrefix).query();
                                logger.debug("卡号：" + cardNo + "==>查询BIN表A的信息为==>" + binDatas + "==>耗时：" + (System
                                        .currentTimeMillis() - start));
                                if (binDatas != null && binDatas.size() > 0) {
                                    for (int i = 0; i < binDatas.size(); i++) {
                                        if (cardNo.length() == binDatas.get(i).getCardLen()) {
                                            flags[0] = true;
                                            break;
                                        }
                                    }
                                }
                                break;
                            case 2:
                                //试点阶段二（贷记卡全部支持，借记卡从BIN表B中判断）
                                String isjieji = dataMap.get(key_bank_card_type);
                                if (!"true".equals(isjieji)) {
                                    flags[0] = true;
                                    break;
                                }
                                start = System.currentTimeMillis();
                                if (!flags[0]) {
                                    List<QpsBinData> dataList2 = qpsBinDao.queryBuilder().where().eq("cardBin",
                                            cardNoPrefix).and().eq("type", "B").query();
                                    logger.debug("卡号：" + cardNo + "==>查询BIN表AB的信息为==>" + dataList2 + "==>耗时：" + (System
                                            .currentTimeMillis() - start));
                                    if (dataList2.size() > 0) {
                                        for (int i = 0; i < dataList2.size(); i++) {
                                            if (cardNo.length() == dataList2.get(i).getCardLen()) {
                                                flags[0] = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            case 3:
                                //全面推广阶段
                                flags[0] = true;

                                List<QpsBlackBinData> blackBinDatas = qpsBlackBinDao.queryBuilder().where().eq
                                        ("cardBin", cardNoPrefix).query();
                                logger.debug("卡号：" + cardNo + "==>查询黑名单的信息为==>" + blackBinDatas + "==>耗时：" + (System
                                        .currentTimeMillis() - start));
                                if (blackBinDatas != null && blackBinDatas.size() > 0) {
                                    for (int i = 0; i < blackBinDatas.size(); i++) {
                                        if (cardNo.length() == blackBinDatas.get(i).getCardLen()) {
                                            flags[0] = false;
                                            break;
                                        }
                                    }
                                }
                                break;
                        }
                  /*  //金额满足免密限额
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_NOT_PIN, true);
                    printAmount = noPinLimit;
                    flags[0] = true;*/
                    }
                }
                if (flags[0]) {
                    //金额满足免密限额
                    //mTradeModel.setTradeNoPin(true);
                    flags[1] = true;//XXX的个性需求，免签免密仅一个开关控制
                    printAmount = noPinLimit;
                }
                /*if (qpsParams.isNoSignOn() && currentAmt <= noSignLimit) {
                    //金额满足免签限额
                    if (printAmount < noSignLimit) {
                        printAmount = noSignLimit;
                    }
                    //mTradeModel.setTradeSlipNoSign(true);
                    flags[1] = true;
                }*/
                //mTradeModel.setSlipNoSignAmount(printAmount + "");
                DecimalFormat df = new DecimalFormat("#.00");
                tempMap.put(TransDataKey.KEY_QPS_AMOUNT, df.format(printAmount));//免签免密金额
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //其它类型业务无需支持QPS业务
        return flags;
    }


    /**
     * 内核事件广播接收器
     */
    public abstract class PbocEventReceiver extends BroadcastReceiver {

        public abstract void onImportAmount();

        public abstract void onImportPin();

        public abstract void onRequestOnline();

        public abstract void onConfirmCardNo(String cardNo);

        public void onReturnOfflineBalance(String code1, String balance1, String code2, String balance2) {
        }

        public void onReturnCardTransLog(List<Parcelable> data) {
        }

        public void onReturnCardLoadLog(List<Parcelable> data) {
        }

        public boolean onTradeRefused() {
            return false;
        }

        public boolean onTradeTerminated() {
            return false;
        }

        public boolean onTradeApproved() {
            return false;
        }

        public boolean onFallback() {
            return false;
        }

        public boolean onNeedChangeUserFaces() {
            return false;
        }

        public boolean onError() {
            return false;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            logger.debug(getClass().getSimpleName() + "==>接收到广播==>" + action);
            IPbocService pbocService;
            try {
                pbocService = DeviceFactory.getInstance().getPbocService();
            } catch (Exception e) {
                e.printStackTrace();
                jumpToResultActivity(StatusCode.EMV_KERNEL_EXCEPTION);
                return;
            }
            switch (action) {
                case ACTION_REQUEST_AMOUNT:
                    //请求导入金额
                    onImportAmount();
                    break;
                case ACTION_REQUEST_TIPS_CONFIRM:
                    //请求提示信息确认
                    pbocService.importResult(EnumPbocResultType.MSG_CONFIRM, true);
                    break;
                case ACTION_REQUEST_AID_SELECT:
                    //请求AID应用选择
                    pbocService.importAidSelectResult(1);
                    break;
                case ACTION_REQUEST_EC_TIPS_CONFIRM:
                    //请求电子现金提示确认
                    pbocService.importResult(EnumPbocResultType.EC_TIP_CONFIRM, true);
                    break;
                case ACTION_REQUEST_CARD_INFO_CONFIRM:
                    //请求卡号信息确认
                    String cardNo = intent.getExtras().getString(PbocEventAction.KEY_CARD_INFO);
                    onConfirmCardNo(cardNo);
                    break;
                case ACTION_REQUEST_PIN:
                    //请求导入PIN
                    onImportPin();
                    break;
                case ACTION_REQUEST_USER_AUTH:
                    //请求用户认证
                    pbocService.importResult(EnumPbocResultType.USER_AUTH, true);
                    break;
                case ACTION_REQUEST_ONLINE:
                    //请求联机
                    dataMap.put(KEY_ONLINE_REQUEST_EVENT, "1");
                    Map<String, String> aidMap = pbocService.readKernelData(EMVTAG_AID);
                    if (aidMap != null) {
                        String aid = (String) aidMap.values().toArray()[0];
                        dataMap.put(key_bank_card_type, "A000000333010101".equals(aid) + "");
                    }
                    Map<String, String> cardInfo = pbocService.readKernelData(getCardInfoTags());
                    logger.info("IC卡卡片信息读取成功：" + cardInfo.toString());
                    String tag57 = cardInfo.get("57");
                 /*   if(tag57.length() > 0) {
                        if ("F".equals(tag57.substring(tag57.length() - 1, tag57.length())) ||
                                "f".equals(tag57.substring(tag57.length() - 1, tag57.length()))) {
                            tag57 = tag57.substring(0,tag57.length()-1);
                        }
                    }*/
                    logger.debug("等效二磁道数据为：" + tag57);
                    if (!StringUtils.isStrNull(tag57)) {
                        if (tag57.endsWith("F") || tag57.endsWith("f")) {
                            tag57 = tag57.substring(0, tag57.length() - 1);
                        }
                    }
                    logger.debug("截取后的等效二磁道数据为：" + tag57);
                    String tempExpiry = tag57.split("D")[1];
                    String expiry = "";
                    if (tempExpiry != null && tempExpiry.length() >= 4) {
                        expiry = tempExpiry.substring(0, 4);
                    }
                    dataMap.put(iso_f14_result, expiry);
                    dataMap.put(iso_f14_forvoid, expiry);
                    if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
                        dataMap.put(iso_f2_result, tag57.split("D")[0]);
                        dataMap.put(iso_f36, encryptTrackDataTag(tag57.split("D")[0], expiry, tag57, null));
                        dataMap.put(iso_track2,tag57);

                    } else {
                        dataMap.put(iso_f2, tag57.split("D")[0]);
                        dataMap.put(iso_f35, tag57);
                    }

                    dataMap.put(iso_f23, cardInfo.get("5F34"));
                    String iso55;
                    String iso55_reserve = null;
                    /*switch (transCode) {
                        case BALANCE:
                            iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());//读取55域数据
                            break;
                        case SALE:
                        case AUTH:
                            iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());//读取55域数据
                            iso55_reserve = pbocService.readTlvKernelData(EmvTag.getF55Tags2());//读取冲正时用到的55域数据
                            break;
                        default:
                        case VOID:
                            iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());
                            iso55_reserve = iso55;
                            break;
                        case REFUND:
                            iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());
                            iso55_reserve = iso55;
                            pbocService.abortProcess();
                            break;
                    }*/
                    iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());//读取55域数据
                    iso55_reserve = iso55;//读取冲正时用到的55域数据
                    logger.debug("交易为："+transCode+"读取到的55域数据为："+iso55);
                    dataMap.put(iso_f55, iso55);
                    dataMap.put(iso_f55_reverse, iso55_reserve);
                    String print = pbocService.readTlvKernelData(EmvTag.getTagsForPrint());
//                    Map<String, String> stringMap = TlvUtils.tlvToMap(print);
                    logger.debug("ic卡打印信息为：" + print);
                    dataMap.put(KEY_IC_DATA_PRINT, print);
                    tempMap.put(KEY_IC_DATA_PRINT, print);
                    onRequestOnline();
                    break;
                case ACTION_RETURN_CARD_OFFLINE_BALANCE:
                    //返回卡片脱机余额
                    onReturnOfflineBalance(
                            bundle.getString(KEY_FIRST_EC_CODE),
                            bundle.getString(KEY_FIRST_EC_BALANCE),
                            bundle.getString(KEY_SECOND_EC_CODE),
                            bundle.getString(KEY_SECOND_EC_BALANCE));
                    break;
                case ACTION_RETURN_CARD_TRANS_LOG:
                    //返回卡片交易日志
                    onReturnCardTransLog(bundle.getParcelableArrayList(KEY_TRANS_LOG));
                    break;
                case ACTION_RETURN_CARD_LOAD_LOG:
                    //返回卡片圈存日志
                    onReturnCardLoadLog(bundle.getParcelableArrayList(KEY_LOAD_LOG));
                    break;
                case ACTION_TRADE_APPROVED:
                    //交易批准
                    if (!onTradeApproved()) {
                        pbocService.stopProcess();
                    }
                    break;
                case ACTION_TRADE_REFUSED:
                    //交易拒绝
                    if (!onTradeRefused()) {
                        jumpToResultActivity(StatusCode.TRADING_REFUSED);
                        DialogFactory.hideAll();
                    }
                    break;
                case ACTION_TRADE_TERMINATED:
                    //交易终止
                    if (!onTradeTerminated()) {
                        jumpToResultActivity(StatusCode.TRADING_TERMINATES);
                        DialogFactory.hideAll();
                    }
                    break;
                case ACTION_NEED_CHANGE_READ_CARD_TYPE:
                    //交易降级或者采用其它用户界面
                    int resultCode = intent.getExtras().getInt(PbocEventAction.KEY_TRANS_RESULT);
                    if (resultCode == 3) {
                        if (!onFallback()) {
                            jumpToResultActivity(StatusCode.TRADING_FALLBACK);
                            DialogFactory.hideAll();
                        }
                    } else {
                        if (!onNeedChangeUserFaces()) {
                            jumpToResultActivity(StatusCode.TRADING_CHANGE_OTHER_FACE);
                            DialogFactory.hideAll();
                        }
                    }

                    break;
                case ACTION_ERROR:
                    //内核异常
                    if (!onError()) {
                        jumpToResultActivity(StatusCode.EMV_KERNEL_EXCEPTION);
                        DialogFactory.hideAll();
                    }
                    break;
            }
        }
    }
}
package com.centerm.jnbank.base;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.activity.MainActivity;
import com.centerm.jnbank.activity.TradingActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.channels.EnumChannel;
import com.centerm.jnbank.channels.helper.ShengPayMenuHelper;
import com.centerm.jnbank.common.PayStatus;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.task.AsyncSignTask;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.xml.XmlParser;
import com.centerm.jnbank.xml.menu.MenuItem;
import com.centerm.jnbank.xml.process.TradeProcess;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f12;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;
import static com.centerm.jnbank.common.TransDataKey.key_resp_code;
import static com.centerm.jnbank.common.TransDataKey.key_resp_msg;
import static com.centerm.jnbank.utils.DataHelper.parseIsoF4;


/**
 * Created by FL on 2016/12/27 21:32.
 * 支付组件入口
 */

public class EntryActivity extends BaseTradeActivity {
    private TradeInfo tradeInfo;
    private CommonDao<TradeInfo> dao;
    private final static int REQ_PAY = 0x100;
    private final static int RES_PAY = 0x200;
    private final static String transNam = "transName";//交易类型
    private final static String barcodeType = "barcodeType";//支付通道
    private final static String scanType = "scanType";//扫码方式 被扫1 主扫2或其他及默认
    private final static String trans_amt = "amount";//交易金额
    private final static String orderNoSFT = "orderNoSFT";//订单号
    private final static String oldTraceNo = "oldTraceNo";//凭证号    非必填，消费撤销、扫 码撤销时，如果不传入则 调起收单撤销界面
    private final static String oldReferenceNo = "oldReferenceNo";//参考号  非必填，消费退货，如果不传入则调起收单退货界面
    private final static String appId = "appId";//调用者应用包名
    private final static String appIdB = "appIdB";//应用业务名   调用者应用的业务名称，用于一个应用多种类型业务，每个业务类型有各自独特的“扩展数据”时必填。
    private final static String orderInfo = "orderInfo";//订单信息   非必填  “例”：“Iphone7，64G，黑色。。。。。。”
    private final static String orderDetail = "orderDetail";//订单明细 非必填
    private final static String priInfo = "priInfo";//用户联追加打印  非必填
    private final static String priInfo2 = "priInfo2";//用户联追加二维码   非必填
    private final static String printMerchantInfo = "printMerchantInfo";//商户联追加打印   非必填
    private final static String printMerchantInfo2 = "printMerchantInfo2";//商户联追加二维码  非必填
    private final static String priOn = "priOn";//打单页面是否自动关闭  非必填
    private final static String resDataA = "resDataA";//扩展数据1   非必填
    private final static String resDataB = "resDataB";//扩展数据2  非必填
    private final static String reserve47 = "reserve47";//47扩展参数  非必填



    private final static String payState = "payState";//交易状态
    private final static String payreason = "payreason";//失败原因
    private final static String payDetail = "payreason";//交易详情
    private final static String amount = "amount";//交易成功或失败返回的金额
    private final static String traceNo  = "traceNo";//凭证号
    private final static String referenceNo  = "referenceNo";//参考号
    private final static String batchNo = "batchNo";
    private final static String cardNo  = "cardNo";//卡号  除前六位和后四位之外其余位变星号处理
    private final static String cardType  = "cardType";//卡类型   预留参数暂不返回，为空
    private final static String issue  = "issue";//卡类型   预留参数暂不返回，为空
    private final static String terminalId  = "terminalId";//终端号
    private final static String merchantId  = "merchantId";//商户号
    private final static String merchantName  = "merchantName";//商户名称
    private final static String merchantNameEn  = "merchantNameEn";//商户英文名
    private final static String transDate  = "transDate";//交易日期
    private final static String transTime  = "transTime";//交易时间
    private final static String countN  = "countN";//交易总笔数（结算业务） 仅在结算业务有返回，其他业务该字段为空
    private final static String sumAmount  = "sumAmount";//交易总金额（结算业务） 仅在结算业务有返回，其他业务该字段为空





    private final static String control_info = "control_info";

    private final static String print_pages = "print_pages";
    private final static String resp_code = "resp_code";
    private final static String resp_msg = "resp_msg";
    private final static String trans_no = "traceNo";
    private final static String retri_ref_no = "referenceNo";
    private final static String trans_time = "trans_time";
    private final static String auth_no = "auth_no";
    private final static String card_no = "cardNo";
    private final static String card_type = "type";
    private final static String payType = "payType";


    //支付通道
  /*  “0”：银行卡
    “1”：微信支付
    “2”：支付宝
    “3”：XXX钱包*/

    public final static String CARD_TRANS = "0";   //银行卡
    public final static String WEI_TRANS = "1";    //微信
    public final static String ALI_TRANS = "2";    //支付宝
    public final static String SFT_TRANS = "3";    //XXX

    //交易类型
    /*
        “0”：消费
        “1”消费撤销
        “2”退货
        （以上三种消费可选择支付通道，详见barcodeType字段）
        “3”预授权（银行卡）
        “4”结算
        “5”签到
        “6”余额查询
        “7”系统管理
        “8”打印
        “9”末笔查询
        “10”商户信息查询
        “11”预授权完成通知
        “12”预授权完成撤销
        “13”预授权完成请求
        “14”预授权撤销

    */
    public static String SALE = "0";
    public static String VOID = "1";
    public static String REFUND = "2";
    public static String AUTH = "3";
    public static String SETTLEMENT = "4";//结算
    public static String SIGN_IN = "5";
    public static String BALANCE = "6";
    public static String SYS_MANAGEMENT = "7";
    public static String PRINT = "8";
    public static String SCAN_LAST_SERCH = "9";
    public static String QUERY_MERCHANT = "10";
    public static String AUTH_SETTLEMENT = "11";
    public static String COMPLETE_VOID = "12";
    public static String AUTH_COMPLETE = "13";
    public static String CANCEL = "14";

    private String tempTransAmt,tempOrderNoSFT, tempOldTraceNo, tempOldReferenceNo, tempAppid,tempAppIdB,tempOrderInfo,tempOrderDetail,
            tempPriInfo1, tempPriCode1, tempPriInfo2, tempPriCode2,tempPriOn,tempResDataA,tempResDataB,tempReserve47;
    private Intent resultIntent;
    private boolean allBack = false;
    private String payTypeStr = "银行卡";
    private String tranType, transName,tempScanType;
    private LinearLayout loadBlock;

    private String authNoRegEx = "^[0-9]{6}$";
    private String refNoRegEx = "^[0-9]{12}$";
    private String amtRegEx = "^[0-9]{12}$";
    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        dao = new CommonDao<>(TradeInfo.class, dbHelper);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_pay_entry;
    }


    @Override
    public void onInitView() {
        loadBlock = (LinearLayout) findViewById(R.id.load_block);
        logger.info("Entry-onInit被调用");
        /*if (!Settings.hasInit(context)||!Settings.hasTmk(context)) {
            resposeMsg(PayStatus.E01);
            return;
        }*/

        //获取第三方传递的参数
        getIntentParam();
        //交易类型
        if (StringUtils.isStrNull(transName)) {
            resposeMsg(PayStatus.E03);
            return;
        }
        //支付渠道
        if (StringUtils.isStrNull(tranType)) {
            resposeMsg(PayStatus.E04);
            return;
        }
        //校验金额的合法
        if (!TextUtils.isEmpty(tempTransAmt)) {
            if (tempTransAmt.matches(amtRegEx)) {
                try {
                    double amt = DataHelper.parseIsoF4(tempTransAmt);
                    if (amt <= 0) {
                        resposeMsg(PayStatus.E06);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resposeMsg(PayStatus.E06);
                    return;
                }
            } else {
                resposeMsg(PayStatus.E06);
                return;
            }
        }
        //校验原凭证号和合法性
        if(!TextUtils.isEmpty(tempOldTraceNo)){
            if (!tempOldTraceNo.matches(authNoRegEx)) {
                resposeMsg(PayStatus.E08);
                return;
            }

        }
        //校验原参考号合法性
        if(!TextUtils.isEmpty(tempOldReferenceNo)){
            if (!tempOldReferenceNo.matches(refNoRegEx)) {
                resposeMsg(PayStatus.E09);
                return;
            }
        }

        if (!BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.FLAG_SIGN_IN)) {
            String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
            if (StringUtils.isStrNull(operId)) {
                BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.KEY_OPER_ID,"01");
            }
            new AsyncSignTask(context, dataMap, tempMap) {
                @Override
                public void onFinish(String[] strings) {
                    super.onFinish(strings);
                    if ("00".equals(strings[0])) {
                        beginEvent();
                    } else {
                        resposeMsg(PayStatus.E02);
                    }
                }

                @Override
                public void onStart() {
                    super.onStart();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,SIGN_IN);
        } else {
            beginEvent();
        }
    }

    /**
     * 状态码返回
     * @param payStatus
     */
    private void resposeMsg(PayStatus payStatus) {
        resultIntent = new Intent();
        resultIntent.putExtra(payState, payStatus.getStatusCode());
        resultIntent.putExtra(payreason, context.getString(payStatus.getMsgId()));
        setResult(RESULT_CANCELED, resultIntent);
        this.finish();
    }

    /**
     * 通过intent获取第三方参数
     */
    private void getIntentParam() {
        resultIntent = getIntent();
        //获取入口参数
        Bundle bundle = resultIntent.getExtras();
        tranType = bundle.getString(barcodeType);
        transName = bundle.getString(transNam);
        tempScanType = bundle.getString(scanType);
        tempTransAmt = bundle.getString(trans_amt);
        tempOrderNoSFT = bundle.getString(orderNoSFT);
        tempOldTraceNo = bundle.getString(oldTraceNo);
        tempOldReferenceNo = bundle.getString(oldReferenceNo);
        tempAppid = bundle.getString(appId);
        tempAppIdB = bundle.getString(appIdB);
        tempOrderInfo = bundle.getString(orderInfo);
        //tempOrderDetail = bundle.getString(orderDetail);
        tempPriInfo1 = bundle.getString(priInfo);
        tempPriCode1 = bundle.getString(priInfo2);
        tempPriInfo2 = bundle.getString(printMerchantInfo);
        tempPriCode2 = bundle.getString(printMerchantInfo2);
        tempPriOn = bundle.getString(priOn);
        tempResDataA= bundle.getString(resDataA);
        tempResDataB = bundle.getString(resDataB);
        tempReserve47 = bundle.getString(reserve47);
        super.entryFlag = true;
    }

    private void beginEvent() {
        boolean isDefine = false;
        if (transName.equals(BALANCE)) {
            isDefine = true;
            transCode = TransCode.BALANCE;
        } else if (transName.equals(SALE) && tranType.equals(CARD_TRANS)) {
            isDefine = true;
            transCode = TransCode.SALE;
        } else if (transName.equals(SALE) && tranType.equals(WEI_TRANS)) {
            isDefine = true;
            transCode = TransCode.SCAN_PAY_WEI;
        }else if (transName.equals(SALE) && tranType.equals(ALI_TRANS)) {
            isDefine = true;
            transCode = TransCode.SCAN_PAY_ALI;
        }else if (transName.equals(SALE) && tranType.equals(SFT_TRANS)) {
            isDefine = true;
            transCode = TransCode.SCAN_PAY_SFT;
        } else if (transName.equals(VOID)&&tranType.equals(CARD_TRANS)) {
            isDefine = true;
            transCode = TransCode.VOID;
        } else if ((transName.equals(VOID) && tranType.equals(WEI_TRANS))||(transName.equals(VOID) && tranType.equals(ALI_TRANS))||(transName.equals(VOID) && tranType.equals(SFT_TRANS))) {
            isDefine = true;
            transCode = TransCode.SCAN_CANCEL;
        }else if (transName.equals(REFUND) && tranType.equals(CARD_TRANS)) {
            isDefine = true;
            transCode = TransCode.REFUND;
        } else if (transName.equals(REFUND) && tranType.equals(WEI_TRANS)) {
            isDefine = true;
            transCode = TransCode.SCAN_REFUND_W;
        }else if (transName.equals(REFUND) && tranType.equals(ALI_TRANS)) {
            isDefine = true;
            transCode = TransCode.SCAN_REFUND_Z;
        }else if (transName.equals(REFUND) && tranType.equals(SFT_TRANS)) {
            isDefine = true;
            transCode = TransCode.SCAN_REFUND_S;
        }else if (transName.equals(SCAN_LAST_SERCH)) {
            try {
                tradeInfo = queryLastTradeData();
                if (tradeInfo == null) {
                    resposeMsg(PayStatus.E12);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
            isDefine = true;
            transCode = TransCode.SCAN_LAST_SERCH;
        } else if (transName.equals(AUTH)) {
            isDefine = true;
            transCode = TransCode.AUTH;
        } else if (transName.equals(AUTH_COMPLETE)){
            isDefine = true;
            transCode = TransCode.AUTH_COMPLETE;
        } else if (transName.equals(CANCEL)) {
            isDefine = true;
            transCode = TransCode.CANCEL;
        }else if (transName.equals(COMPLETE_VOID)) {
            isDefine = true;
            transCode = TransCode.COMPLETE_VOID;
        }
        if (isDefine) {
            MenuItem item = new MenuItem();
            item.setTransCode(transCode);
            item.setProcessFile(transCode);
            item.setEntag(transCode);
            onProcess(item);
            loadBlock.setVisibility(View.GONE);
            return;
        }
        if (transName.equals(SETTLEMENT)) {
            isDefine = true;
            Intent intent = new Intent(context, TradingActivity.class);
            intent.putExtra(KEY_TRANSCODE, TransCode.SETTLEMENT);
            startActivityForResult(intent, RES_PAY);
        } else if (transName.equals(PRINT)) {
            isDefine = true;
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra(KEY_USER_FLAG, 7);
            startActivityForResult(intent, RES_PAY);
        } else if (transName.equals(SYS_MANAGEMENT)) {
            isDefine = true;
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra(KEY_USER_FLAG, 1);
            startActivityForResult(intent, RES_PAY);
            //跳转到下一级菜单
        } /*else if (transName.equals(AUTH)) {
            isDefine = true;
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra(KEY_USER_FLAG, 8);
            startActivityForResult(intent, RES_PAY);
        }*/

        if (transName.equals(SIGN_IN)) {
            isDefine = true;
            transCode = TransCode.SIGN_IN;
            MenuItem item = new MenuItem();
            item.setTransCode(transCode);
            item.setProcessFile("SIGN_IN");
            item.setEntag(transCode);
        String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if (StringUtils.isStrNull(operId)) {
            BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.KEY_OPER_ID,"01");
        }
            onProcess(item);
            loadBlock.setVisibility(View.GONE);
        }  else if (transName.equals(QUERY_MERCHANT)) {
            loadBlock.setVisibility(View.GONE);
            isDefine = true;
            BusinessConfig config = BusinessConfig.getInstance();
            String termIdValue = config.getParam(context, BusinessConfig.Key.PRESET_TERMINAL_CD);
            String merchantIdValue = config.getParam(context, BusinessConfig.Key.PRESET_MERCHANT_CD);
            String merchantNameValue = config.getParam(context, BusinessConfig.Key.PRESET_MERCHANT_NAME);
            String merchantEnglishNameValue = config.getParam(context, BusinessConfig.Key.PRESET_MERCHANT_ENGLISH_NAME);
            resultIntent = new Intent();
            resultIntent.putExtra(terminalId, termIdValue);
            resultIntent.putExtra(merchantId, merchantIdValue);
            resultIntent.putExtra(merchantName, merchantNameValue);
            resultIntent.putExtra(merchantNameEn, merchantEnglishNameValue);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        //是否找到相应的流程
        if (!isDefine) {
            resposeMsg(PayStatus.E05);
        }
    }

    private void onProcess(MenuItem item) {
        if (item == null) {
            return;
        }
        //进入具体业务流程
        String processFile = item.getProcessFile();
        //优先响应有流程定义的事件
        boolean success = beginProcess(item.getTransCode(), processFile);
        logger.debug("onProcess:" + success);
        if (!success) {
            //没有流程定义，继续寻找事件响应
            success = onNoProcessDefine(item);
        }
        if (!success) {
            logger.error("onProcess中未找到对应流程");
            resposeMsg(PayStatus.E05);
        }
    }
    /**
     * 没有进行流程定义的菜单项在此方法中统一调度和处理
     *
     * @param item 菜单项
     * @return 处理成功返回true，否则返回false
     */
    protected boolean onNoProcessDefine(MenuItem item) {
        EnumChannel posChannel = EnumChannel.valueOf(Settings.getPosChannel(context));
        switch (posChannel) {
            case SHENGPAY:
                return new ShengPayMenuHelper().onTriggerMenuItem(this, item);
        }
        return false;
    }

    /**
     * 启动交易流程
     *
     * @param processFile 流程定义文件
     * @return 启动成功返回true，失败返回false
     */
   /* protected boolean beginProcess(String transCode, String processFile) {
        boolean isContineProcess = true;
        TradeProcess process = XmlParser.parseProcess(context, processFile);
        if (process != null) {
            final Intent intent = new Intent();
            intent.putExtra(ENTRY_FALG, entryFlag);
            intent.putExtra(ENTRY_CONTROL_INFO, entryControlInfo);
            intent.putExtra(ENTRY_PRINT_COUNT, entryPrintCount);
            switch (transCode) {
                case TransCode.SALE:
                case TransCode.SCAN_PAY_ALI:
                case TransCode.SCAN_PAY_WEI:
                case TransCode.SCAN_PAY_SFT:
                    isContineProcess = putAmt(process);
                    intent.setAction(process.getSecondComponentNode().getComponentName());
                    break;
                case TransCode.REFUND:
                case TransCode.SCAN_REFUND_W:
                case TransCode.SCAN_REFUND_Z:
                case TransCode.SCAN_REFUND_S:
                    isContineProcess = putAmt(process);
                    if (!TextUtils.isEmpty(oldReferenceNo)) {
                        logger.info("输入原参考号为：" + tempOldReferenceNo);
                        dataMap = process.getDataMap();
                        dataMap.put(TransDataKey.key_entryReferenceNo, tempOldReferenceNo);
                    }
                    intent.setAction(process.getSecondComponentNode().getComponentName());
                    break;
                case TransCode.VOID:
                    intent.setAction(process.getForthComponentNode().getComponentName());
                    if (null != tradeInfo) {
                            dataMap = process.getDataMap();
                            logger.debug("原交易信息：" + tradeInfo.toString());
                            dataMap.put(iso_f4, tradeInfo.getIso_f4());//金额
                            dataMap.put(iso_f22, "012");//服务点输入码，默认为未指明
                            dataMap.put(iso_f37, tradeInfo.getIso_f37());//检索参考号
                            dataMap.put(key_oriAuthCode, tradeInfo.getIso_f38());//授权标识应答码
                            dataMap.put(iso_f41, tradeInfo.getIso_f41());//受卡机终端标识码
                            dataMap.put(iso_f42, tradeInfo.getIso_f42());//受卡方标识码
                            dataMap.put(iso_f11_origin, tradeInfo.getIso_f11());//原流水号
                            dataMap.put(iso_f60_origin, tradeInfo.getIso_f60());//原交易60域中包含原批次号
                            dataMap.put(key_oriReference, tradeInfo.getIso_f37());
                            dataMap.put(key_oriTransTime, tradeInfo.getIso_f13());
                    }
                    isContineProcess =  putAuthNo(process);
                    break;
                case TransCode.SCAN_CANCEL:
                    intent.setAction(process.getForthComponentNode().getComponentName());
                    dataMap = process.getDataMap();
                    if (null != tradeInfo) {
                        dataMap = process.getDataMap();
                        logger.debug("原交易信息：" + tradeInfo.toString());
                        dataMap.put(iso_f4, tradeInfo.getIso_f4());//金额
                        dataMap.put(iso_f22, "012");//服务点输入码，默认为未指明
                        dataMap.put(iso_f37, tradeInfo.getIso_f37());//检索参考号
                        dataMap.put(key_oriAuthCode, tradeInfo.getIso_f38());//授权标识应答码
                        dataMap.put(iso_f41, tradeInfo.getIso_f41());//受卡机终端标识码
                        dataMap.put(iso_f42, tradeInfo.getIso_f42());//受卡方标识码
                        dataMap.put(iso_f11_origin, tradeInfo.getIso_f11());//原流水号
                        dataMap.put(iso_f60_origin, tradeInfo.getIso_f60());//原交易60域中包含原批次号
                        dataMap.put(key_oriReference, tradeInfo.getIso_f37());
                        dataMap.put(key_oriTransTime, tradeInfo.getIso_f13());
                        if(tradeInfo.getTransCode().equals(TransCode.SCAN_PAY_ALI)){
                            dataMap.put(iso_f22,tradeInfo.getIso_f22());
                            dataMap.put(iso_f47,"TXNWAY=" + "ZFB01");
                        }else if(tradeInfo.getTransCode().equals(TransCode.SCAN_PAY_SFT)) {
                            dataMap.put(iso_f22,tradeInfo.getIso_f22());
                            dataMap.put(iso_f47,"TXNWAY=" + "SFT01");
                        } else if (tradeInfo.getTransCode().equals(TransCode.SCAN_PAY_WEI)) {
                            dataMap.put(iso_f22, tradeInfo.getIso_f22());
                            dataMap.put(iso_f47, "TXNWAY=" + "TX01");
                        }
                    }
                    isContineProcess = putAuthNo(process);
                    break;
                default:
                    intent.setAction(process.getFirstComponentNode().getComponentName());
                    break;
            }
            if (isContineProcess) {
                intent.putExtra(KEY_PROCESS, process);
                intent.putExtra(KEY_TRANSCODE, transCode);
                startActivityForResult(intent, RES_PAY);
            }
            return true;
        }
        return false;
    }*/
    /**
     * 启动交易流程
     *
     * @param processFile 流程定义文件
     * @return 启动成功返回true，失败返回false
     */
    protected boolean beginProcess(String transCode, String processFile) {
        TradeProcess process = XmlParser.parseProcess(context, processFile);
        if (process != null) {
            if (!TextUtils.isEmpty(tempScanType)) {
                logger.info("扫码方式 被扫1 主扫2或其他及默认：" + tempScanType);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.key_scan_type, tempScanType);
            }
            if (!TextUtils.isEmpty(tempTransAmt)) {
                double amt = parseIsoF4(tempTransAmt);
                logger.info("输入金额为：" + amt);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.iso_f4, amt + "");
            }
            if(!TextUtils.isEmpty(tempOldTraceNo)){
                logger.info("输入原交易凭证号为：" + tempOldTraceNo);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.key_entryTraceNo, tempOldTraceNo);
            }
            if(!TextUtils.isEmpty(tempOldReferenceNo)){
                logger.info("输入原参考号为：" + tempOldReferenceNo);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.key_entryReferenceNo, tempOldReferenceNo);
            }
            if(!TextUtils.isEmpty(tempPriInfo1)){
                logger.info("用户联打印信息为：" + tempPriInfo1);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.key_entryPriInfo1, tempPriInfo1);
            }
            if(!TextUtils.isEmpty(tempPriCode1)){
                logger.info("用户联二维码信息为：" + tempPriCode1);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.key_entryPricode1, tempPriCode1);
            }
            if(!TextUtils.isEmpty(tempPriInfo2)){
                logger.info("商户联打印信息为：" + tempPriInfo2);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.key_entryPriInfo2, tempPriInfo2);
            }
            if(!TextUtils.isEmpty(tempPriCode2)){
                logger.info("商户联二维码信息为：" + tempPriCode2);
                dataMap = process.getDataMap();
                dataMap.put(TransDataKey.key_entryPricode2, tempPriCode2);
            }
            final Intent intent = new Intent();
            intent.putExtra(ENTRY_FALG, entryFlag);
            intent.putExtra(ENTRY_CONTROL_INFO, entryControlInfo);
            intent.putExtra(ENTRY_PRINT_COUNT, entryPrintCount);
            //是否需要密码，需要就跳到第一个节点，不需要跳到第二个节点
            if (transCode.equals(TransCode.VOID) || transCode.equals(TransCode.REFUND) || transCode.equals(TransCode.CANCEL)
                    || transCode.equals(TransCode.COMPLETE_VOID) || transCode.equals(TransCode.SCAN_CANCEL)
                    || transCode.equals(TransCode.SCAN_REFUND_W) || transCode.equals(TransCode.SCAN_REFUND_Z) || transCode.equals(TransCode.SCAN_REFUND_S)) {
                boolean isNeedPsw = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_REFUND_VOID_NEED_PSW).equals("1") ? true : false;
                if (isNeedPsw) {
                    intent.setAction(process.getFirstComponentNode().getComponentName());
                } else {
                    intent.setAction(process.getSecondComponentNode().getComponentName());
                }
            } else {
                intent.setAction(process.getFirstComponentNode().getComponentName());
            }
            intent.putExtra(KEY_PROCESS, process);
            intent.putExtra(KEY_TRANSCODE, transCode);
            startActivityForResult(intent, RES_PAY);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logger.info("onActivityResult:requestCode = " + requestCode + " resultCode = " + resultCode);
        if (resultCode == -100) {
            String strIso39 = tempMap.get(iso_f39);
            //如果有返回值说明交易完整，没有退出
            if (!TextUtils.isEmpty(strIso39)) {
                return;
            }
            resposeMsg(PayStatus.E10);
        } else {
            allBack = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        logger.info("onNewIntent:" + intent);
        super.onNewIntent(intent);

        setIntent(intent);//must store the new intent unless getIntent() will return the old one

        initTradeProcess();

    }

    @Override
    public void onBackPressed() {
        activityStack.backTo(MainActivity.class);
        finish();
    }

    private String getIssue() {
        String iss = "";
        if (!TextUtils.isEmpty(tempMap.get(iso_f44))) {
            iss = tempMap.get(iso_f44).split(" ")[0];
            logger.info("发卡行：" + iss);
            if (iss.equals("")) {
                if (!TextUtils.isEmpty(tempMap.get(iso_f47))) {
                    iss = tempMap.get(iso_f47).split("=|\\|")[1].equals("ZFB01") ? "支付宝" : "微信";
                } else {
                    iss = "";
                }
            }
            logger.info("发卡行：" + iss);
        }
        return iss;
    }
    /**
     * 根据凭证号查找交易信息，如果存在则初始化交易数据
     *
     * @param iso11 凭证号
     * @return 查找到相关交易返回true，否则返回false
     */
    private TradeInfo queryAndInitTradeData(String iso11) throws SQLException {
        QueryBuilder builder = dao.queryBuilder();
        TradeInfo tradeInfo = null;
        List<TradeInfo> tradeInfos = builder.where().eq("iso_f11", iso11).and().ne("flag", "0").and().ne("flag", "3").query();
        if (tradeInfos == null||tradeInfos.size()==0) {
            return null;
        }
        tradeInfo = tradeInfos.get(0);
        if (tradeInfo == null) {
            logger.warn(iso11 + "==>无法找到该流水信息");
            //如果找不到对应的交易流水，或者流水对应的交易类型不符
            return tradeInfo;
        }
        if ("2".equals(tradeInfo.getFlag())) {
            logger.warn(iso11 + "==>流水已撤销");
            return null;
        }
        return tradeInfo;
    }

    /**
     * 查找末笔信息
     * @return
     * @throws SQLException
     */
    private TradeInfo queryLastTradeData() throws SQLException {
        DbHelper dbHelper = OpenHelperManager.getHelper(context, DbHelper.class);
        Dao<TradeInfo, String> tradeDao = null;
        tradeDao = dbHelper.getDao(TradeInfo.class);
        Where<TradeInfo, String> where = tradeDao.queryBuilder().orderBy("iso_f11", false).where();
        where.or(
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_ALI)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_SFT)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_WEI)));
        List<TradeInfo> tradeInfos = where.query();
        if (tradeInfos == null||tradeInfos.size()==0) {
            return null;
        }
        tradeInfo = tradeInfos.get(0);
        if (tradeInfo == null) {
            logger.warn("无末笔交易流水");
            return tradeInfo;
        }
        return tradeInfo;
    }

    @Override
    public void onResume() {
        super.onResume();
        int resultCode = RESULT_CANCELED;
        String strIso39 = tempMap.get(iso_f39);
        String strRespCode = tempMap.get(key_resp_code);
        String strRespMsg = tempMap.get(key_resp_msg);
        logger.info("strIso39 = " + strIso39 + " strRespCode = " + strRespCode + " strRespMsg" + strRespMsg);
        if (!TextUtils.isEmpty(strRespCode)) {
            Intent resultIntent = new Intent();
            if (!TextUtils.isEmpty(strIso39) && strIso39.equals("00")) {
                resultCode = RESULT_OK;
            }
            resultIntent.putExtra(payState, strRespCode);
            resultIntent.putExtra(payreason, strRespMsg);
            resultIntent.putExtra(amount, tempMap.get(iso_f4));
            resultIntent.putExtra(traceNo, tempMap.get(iso_f11));
            resultIntent.putExtra(referenceNo, tempMap.get(iso_f37));
            resultIntent.putExtra(batchNo, "111111");
            resultIntent.putExtra(cardNo, tempMap.get(iso_f2_result));
            resultIntent.putExtra(issue, getIssue());
            resultIntent.putExtra(terminalId, tempMap.get(iso_f41));
            resultIntent.putExtra(merchantId, tempMap.get(iso_f42));
            resultIntent.putExtra(merchantName, BusinessConfig.getInstance().getValue(context,BusinessConfig.Key.PRESET_MERCHANT_NAME));
            resultIntent.putExtra(barcodeType, tranType);
            resultIntent.putExtra(transDate, "20170331");
            resultIntent.putExtra(transTime, tempMap.get(iso_f12));
            logger.info("resultIntent:" + resultIntent.getStringExtra(payState));
            EntryActivity.this.setResult(resultCode, resultIntent);
            finish();
        } else if (allBack) {
           resposeMsg(PayStatus.E10);
        }
    }
}

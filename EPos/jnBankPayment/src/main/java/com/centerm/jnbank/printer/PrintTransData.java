package com.centerm.jnbank.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.cpay.midsdk.dev.define.printer.PrintListener;
import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.centerm.jnbank.R;
import com.centerm.jnbank.bean.IssInfo;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.bean.TradePrintData;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.EreceiptCreator;
import com.centerm.jnbank.utils.ImageUtils;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem.FONT_SIZE_HEIGHT_LARGE;
import static com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem.FONT_SIZE_LARGE;
import static com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem.FONT_SIZE_SMALL;
import static com.centerm.jnbank.common.TransDataKey.KEY_HOLDER_NAME;
import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f11_origin;
import static com.centerm.jnbank.common.TransDataKey.iso_f12;
import static com.centerm.jnbank.common.TransDataKey.iso_f13;
import static com.centerm.jnbank.common.TransDataKey.iso_f14;
import static com.centerm.jnbank.common.TransDataKey.iso_f14_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f38;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.key_entryPriInfo1;
import static com.centerm.jnbank.common.TransDataKey.key_entryPriInfo2;
import static com.centerm.jnbank.common.TransDataKey.key_entryPricode1;
import static com.centerm.jnbank.common.TransDataKey.key_entryPricode2;
import static com.centerm.jnbank.common.TransDataKey.key_oriAuthCode;
import static com.centerm.jnbank.common.TransDataKey.key_oriReference;
import static com.centerm.jnbank.common.TransDataKey.key_oriTransTime;

/**
 * v8打印机控制类  这里使用cpay的sdk
 * Created by ysd on 2016/4/20.
 */
public class PrintTransData {
    private static PrintTransData instance;
    private Context context;
    private CpayPrintHelper printHelper;
    private static Logger logger = Logger.getLogger(CommonUtils.class);
    private PrinterCallBack callBack;
    private String tranType;
    private String transCode;
    private Bitmap bitmap;
    private TradePrintData tradePrintData;
    private String clientName;
    private boolean isRePrint;
    private Map<String, String> mapData;
    private final static String RETRACT = "  ";//设置缩进空格
    double saleAmount = 0.0;
    double authCompleteAmount = 0.0;
    double weiSaleAmount = 0.0;
    double aliSaleAmount = 0.0;
    double shengSaleAmount = 0.0;
    double refundAmount = 0.0;
    double weiRefundAmount = 0.0;
    double aliRefundAmount = 0.0;
    double shengRefundAmount = 0.0;
    double saleVoidAmount = 0.0;
    double completeVoidAmount = 0.0;
    double codeVoidAmount = 0.0;
    double saleTotalAmount = 0.0;
    double voidTotalAmount = 0.0;
    double refundTotalAmount = 0.0;
    List<TradeInfo> sale = new ArrayList<>();
    List<TradeInfo> authComplete = new ArrayList<>();
    List<TradeInfo> weiSale = new ArrayList<>();
    List<TradeInfo> aliSale = new ArrayList<>();
    List<TradeInfo> shengSale = new ArrayList<>();
    List<TradeInfo> refund = new ArrayList<>();
    List<TradeInfo> weiRefund = new ArrayList<>();
    List<TradeInfo> aliRefund = new ArrayList<>();
    List<TradeInfo> shengRefund = new ArrayList<>();
    List<TradeInfo> saleVoid = new ArrayList<>();
    List<TradeInfo> completeVoid = new ArrayList<>();
    List<TradeInfo> codeVoid = new ArrayList<>();
    List<TradeInfo> saleTotal = new ArrayList<>();
    List<TradeInfo> voidTotal = new ArrayList<>();
    List<TradeInfo> refundTotal = new ArrayList<>();

    private PrintTransData() {
    }

    public static PrintTransData getMenuPrinter() {
        if (instance == null) {
            instance = new PrintTransData();
        }
        return instance;
    }

    /**
     * 连接打印机服务，获取打印机设备
     *
     * @return
     */
    public boolean open(Context context) {
        this.context = context;
        try {
            DeviceFactory factory = DeviceFactory.getInstance();
            IPrinterDev iPrinterDev = factory.getPrinterDev();
            EnumPrinterStatus status = iPrinterDev.getPrinterStatus();
            printHelper = new CpayPrintHelper(iPrinterDev);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 打印流程控制
     *
     * @param tranType
     * @throws RemoteException
     */
    public void printData(Map<String, String> mapData, boolean isRePrint, TradePrintData tradePrintData, final String tranType, String transCode, PrinterCallBack callBack) throws RemoteException {
        this.tradePrintData = tradePrintData;
        this.isRePrint = isRePrint;
        this.mapData = mapData;
        this.callBack = callBack;
        this.tranType = tranType;
        this.transCode = transCode;
        this.clientName = mapData.get(KEY_HOLDER_NAME);
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printFirst();
    }

    public void printFirst() {
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printShengPay(1);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterFirstSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    printHelper.release();
                    callBack.onPrinterFirstFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        }
    }


    public void printSecond() {
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printShengPay(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    callBack.onPrinterSecondSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    printHelper.release();
                    callBack.onPrinterSecondFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        }
    }

    public void printThird() {
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        try {
            printHelper.init();
            printShengPay(3);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    callBack.onPrinterThreeSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    printHelper.release();
                    callBack.onPrinterThreeFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        }
    }

    /**
     * 打印ic卡参数
     *
     * @throws RemoteException
     */
    private void addICCardParam() throws RemoteException {
        StringBuffer stringBuffer = new StringBuffer();
        if (tradePrintData != null) {
            if (tradePrintData.getArqc() != null) {
                stringBuffer.append("ARQC:" + tradePrintData.getArqc() + "  ");
                //printHelper.addString("ARQC:" + tradePrintData.getArqc(), FONT_SIZE_SMALL);
            }
            if (tradePrintData.getTvr() != null) {
                stringBuffer.append("TVR:" + tradePrintData.getTvr() + "  ");
                //printHelper.addString("TVR:" + tradePrintData.getTvr(), FONT_SIZE_SMALL);
            }
            if (tradePrintData.getAppLabel() != null) {
                stringBuffer.append("AppLabel:" + tradePrintData.getAppLabel() + "  ");
                //printHelper.addString("AppLabel:" + tradePrintData.getAppLabel(), PrinterDataItem.FONT_SIZE_SMALL);
            }
            if (tradePrintData.getAppName() != null) {
                stringBuffer.append("AppName:" + tradePrintData.getAppName() + "  ");
                //printHelper.addString("AppName:" + tradePrintData.getAppName(), PrinterDataItem.FONT_SIZE_SMALL);
            }
         /*   if (tradePrintData.getTsi() != null) {
                stringBuffer.append("TSI:" + tradePrintData.getTsi()+"  ");
                //printHelper.addString("TSI:" + tradePrintData.getTsi(), PrinterDataItem.FONT_SIZE_SMALL);
            }*/
            if (tradePrintData.getAid() != null) {
                stringBuffer.append("AID:" + tradePrintData.getAid() + "  ");
                //printHelper.addString("AID:" + tradePrintData.getAid(), FONT_SIZE_SMALL);
            }
            if (null != tradePrintData.getAtc()) {
                stringBuffer.append("ATC:" + tradePrintData.getAtc() + "  ");
                //printHelper.addString("ATC:" + tradePrintData.getAtc(), FONT_SIZE_SMALL);
            }
            if (null != tradePrintData.getTc()) {
                stringBuffer.append("TC:" + tradePrintData.getTc() + "  ");
                //printHelper.addString("TC:" + tradePrintData.getTc(), PrinterDataItem.FONT_SIZE_SMALL);
            }
            if (null != tradePrintData.getAip()) {
                stringBuffer.append("AIP:" + tradePrintData.getAip() + "  ");
                //printHelper.addString("AIP:" + tradePrintData.getAip(), FONT_SIZE_SMALL);
            }
            if (null != tradePrintData.getIad()) {
                stringBuffer.append("IAD:" + tradePrintData.getIad() + "  ");
                //printHelper.addString("IAD:" + tradePrintData.getIad(), FONT_SIZE_SMALL);
            }
            printHelper.addString(stringBuffer.toString(), 10);
        }
    }

    public String getTranCardType() {
        if (null != mapData) {
            if (null != mapData.get(iso_f22) && mapData.get(iso_f22).length() > 0) {
                String s = mapData.get(iso_f22).substring(0, 2);
                if (s.equals("02")) {
                    return "S";
                } else if (s.equals("05")) {
                    return "I";
                } else if (s.equals("07")) {
                    return "C";
                } else if (s.equals("01")) {
                    if (TransCode.VOID.equals(transCode) || TransCode.COMPLETE_VOID.equals(transCode)) {
                        return "N";
                    } else {
                        return "M";
                    }
                } /*else if (s.equals("00")) {
                    return "N";
                }*/

            } else {
                return "未知";
            }
        }
        return "未知";
    }

    public void printShengPay(int type) {
        String isEnglish = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_ENGLISH);
        if ("1".equals(isEnglish)) {
            boolean isScan = true;//如果是扫码支付不能进行签名打印
            printHelper.init();
            BusinessConfig config = BusinessConfig.getInstance();
            String operId = config.getValue(context, BusinessConfig.Key.KEY_OPER_ID);
            try {
                printHelper.reSetTast();
                addLogoHead();
                //存根栏使用微软雅黑或者相近字体，10号
                String saveType = "";
                switch (type) {
                    case 1:
                        saveType = "商户存根  请妥善保管";
                        break;
                    case 2:
                        saveType = "银行存根  请妥善保管";
                        break;
                    case 3:
                        saveType = "持卡人存根  请妥善保管";
                        break;
                }

                //其它标题使用微软雅黑或者相近字体，8号
                String merName = config.getValue(context, BusinessConfig.Key.PRESET_MERCHANT_NAME);
                int len = merName.length();
                if (len <= 5) {
                    printHelper.addString("商户名称:", PrinterDataItem.Align.LEFT);
                    printHelper.addString(RETRACT + merName, PrinterDataItem.Align.LEFT,FONT_SIZE_HEIGHT_LARGE);
                } else {
                    printHelper.addString("商户名称:", PrinterDataItem.Align.LEFT);
                    printHelper.addString(RETRACT + merName, PrinterDataItem.Align.RIGHT);
                }
                printHelper.addItem("商户编号:", "", mapData.get(iso_f42), 9, 0, 14, 0, false);
                //printHelper.addString("终端编号(TERMINAL NO.)", PrinterDataItem.Align.LEFT);
                //printHelper.addString(RETRACT + mapData.get(iso_f41), PrinterDataItem.Align.RIGHT);
                printHelper.addItem("终端编号:", "", mapData.get(iso_f41), 9, 0, 8, 0, false);
                printHelper.addItem("操作员号:", "", operId, 9, 0, 2, 0, false);
                String iss = "";
                if (!TextUtils.isEmpty(mapData.get(iso_f44))) {
                    String mapData_f5 = mapData.get(iso_f44);
                    int length = mapData_f5.length() / 2;
//                    iss = mapData.get(iso_f44).split(" ")[0];
                    iss=mapData_f5.substring(0, length).trim();
                    DbHelper helper=new DbHelper(context);
//                    CommonDao<IssInfo> dao=new CommonDao<>(IssInfo.class,helper);
//                    List<IssInfo> issInfoList = dao.query();
//                    for (int i = 0; i < issInfoList.size(); i++) {
//                        IssInfo info = issInfoList.get(i);
//                        if(info.getCardIssNo().equals(iss)){
//                            String issName = info.getIssName();
//                            iss=issName;
//                            break;
//                        }
//                    }
                    logger.info("发卡行：" + iss);
                    if (iss.equals("")) {
                        if (!TextUtils.isEmpty(mapData.get(iso_f47))) {
                            String tempType = mapData.get(iso_f47).split("=|\\|")[1];
                            if (tempType.contains("ZFB01")) {
                                iss = "支付宝";
                            } else if (tempType.contains("TX01")) {
                                iss = "微信支付";
                            } else if (tempType.contains("SFT01")) {
                                iss = "支付";
                            }
                        } else {
//                            iss = tranType;
                        }
                    }
                    logger.info("发卡行：" + iss);
                }
                printHelper.addItem("发卡行:", "", iss, 12, 0, 20, 0, false);
                printHelper.addItem("收单行:", "", "江南农村商业银行", 8, 0, 16 , 0, false);
                if (!checkIsCode()) {
                    isScan = false;
                    //扫码支付没有卡号
                    printHelper.addString("卡号:", PrinterDataItem.Align.LEFT);
                    String cardNum = (TextUtils.isEmpty(mapData.get(iso_f2)) ? mapData.get(iso_f2_result) : mapData.get(iso_f2));
                    String isShieldCard = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_SHIELD_CARD);
                    if (tranType.equals(context.getString(R.string.trans_auth))) {
                        if ("1".equals(isShieldCard)) {
                            printHelper.addString(DataHelper.shieldCardNo(cardNum) + " /" + getTranCardType(), PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                        } else {
                            printHelper.addString(cardNum + " /" + getTranCardType(), PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                        }
                    } else {
                        printHelper.addString(DataHelper.shieldCardNo(cardNum) + " /" + getTranCardType(), PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                    }
                }
                printHelper.addItem("有效期:", " ", "2030/12", 8, 0, 8, 0, false);
                if (tranType.length() <= 6) {
                    printHelper.addString("交易类别:", PrinterDataItem.Align.LEFT);
                    printHelper.addString(tranType, PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                } else {
                    printHelper.addString("交易类别:", PrinterDataItem.Align.LEFT);
                    printHelper.addString(tranType, PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                }
                printHelper.addItem("批次号:", "", mapData.get(iso_f60).substring(2, 8), 18, 2, 12, 0, false);
                printHelper.addItem("凭证号:", "", mapData.get(iso_f11), 20, 2, 10, 0, false);
//                printHelper.addItem("参考号(BEFER NO)", "", mapData.get(iso_f37), 18, 2, 12, 0, false);
//                printHelper.addItem("授权码(AUTH NO)", "", mapData.get(iso_f38), 18, 2, 12, 0, false);

                if (!tranType.equals(context.getString(R.string.trans_scan_pay_wei)) && !tranType.equals(context.getString(R.string.trans_scan_pay_ali))) {
                    printHelper.addItem("参考号:", "", mapData.get(iso_f37) == null ? " " : mapData.get(iso_f37), 18, 2, 12, 0, false);
//                    printHelper.addItem("授权码(AUTH NO)", "", mapData.get(iso_f38) == null ? " " : mapData.get(iso_f38), 18, 2, 12, 0, false);
                }
                //预授权交易打印卡有效期
                if (tranType.equals(context.getString(R.string.trans_auth)) && !TextUtils.isEmpty(mapData.get(iso_f14_result))) {
                    printHelper.addItem("授权码:", "", mapData.get(iso_f38), 18, 2, 12, 0, false);
                    printHelper.addItem("卡有效期:", "", mapData.get(iso_f14_result), 22, 2, 8, 0, false);
                }
                printHelper.addString("交易时间:", PrinterDataItem.Align.LEFT);

                printHelper.addString(RETRACT + DataHelper.formatDateAndTime("" + Calendar.getInstance().get(Calendar.YEAR) + mapData.get(iso_f13) + mapData.get(iso_f12)), PrinterDataItem.Align.RIGHT);
                printHelper.addString("交易金额:", PrinterDataItem.Align.LEFT);
                if (tranType.equals(context.getString(R.string.trans_sale)) || tranType.equals(context.getString(R.string.trans_auth)) || tranType.equals(context.getString(R.string.trans_auth_complete))
                        || tranType.equals(context.getString(R.string.trans_scan_pay_wei)) || tranType.equals(context.getString(R.string.trans_scan_pay_ali)) || tranType.equals(context.getString(R.string.trans_scan_pay_sft))) {
                    printHelper.addString("RMB  " + DataHelper.formatIsoF4(mapData.get(iso_f4)), PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                } else {
                    String isMinus = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_MINUS);
                    if ("1".equals(isMinus)) {
                        printHelper.addString("RMB  " + DataHelper.formatIsoF4(mapData.get(iso_f4)), PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                    } else {
                        printHelper.addString("RMB  " + DataHelper.formatIsoF4(mapData.get(iso_f4)), PrinterDataItem.Align.LEFT, FONT_SIZE_HEIGHT_LARGE, true);
                    }
                }
                //撤销交易，打印原交易凭证号;退货交易，打印原交易参考号和原交易日期
                if (tranType.equals(context.getString(R.string.trans_void)) || tranType.equals(context.getString(R.string.trans_scan_cancel))) {
                    printHelper.addString("原交易凭证号:" + mapData.get(iso_f11_origin), PrinterDataItem.Align.LEFT);
                } else if (tranType.equals(context.getString(R.string.trans_refund)) || tranType.equals(context.getString(R.string.trans_scan_refund))) {
                    printHelper.addString("原参考号:  " + mapData.get(key_oriReference), PrinterDataItem.Align.LEFT);
                    printHelper.addString("原交易时间:" + mapData.get(key_oriTransTime), PrinterDataItem.Align.LEFT);
                } else if (tranType.equals(context.getString(R.string.trans_auth_complete))) {
                    printHelper.addString("原授权码:" + mapData.get(key_oriAuthCode), PrinterDataItem.Align.LEFT);
                } else if (tranType.equals(context.getString(R.string.trans_cancel))) {
                    printHelper.addString("原授权码:" + mapData.get(key_oriAuthCode), PrinterDataItem.Align.LEFT);
                } else if (tranType.equals(context.getString(R.string.trans_complete_void))) {
                    printHelper.addString("原交易凭证号:" + mapData.get(iso_f11_origin), PrinterDataItem.Align.LEFT);
                    printHelper.addString("原授权码:" + mapData.get(key_oriAuthCode), PrinterDataItem.Align.LEFT);
                }
                //打印备注
                printHelper.addString("备注:", PrinterDataItem.Align.LEFT);
                //打印IC卡数据
                addICCardParam();
                if (isRePrint) {
                    printHelper.addString("---重打印---", PrinterDataItem.Align.CENTER, 10);
                }
                printHelper.addString(config.getParam(context, BusinessConfig.Key.PARAM_PRINT_REMARK), PrinterDataItem.Align.LEFT, 10, true);
                if (tradePrintData != null && tradePrintData.isNoNeedSign() && !StringUtils.isStrNull(tradePrintData.getAmount())) {
                    printHelper.addString("交易金额未超过" + tradePrintData.getAmount() + "元，免密免签", PrinterDataItem.Align.CENTER);
                }
                if (type == 1) {
                    if (tradePrintData != null && !tradePrintData.isNoNeedSign()) {
                        printHelper.addString("持卡人签名:", PrinterDataItem.Align.LEFT);
                        if (!isScan) {
                            if (null != bitmap) {
                                printHelper.reSetTast();
                                Bitmap resultBit = DataHelper.resize(bitmap, 210, 140);
                                printHelper.addPrinterTask(resultBit, 50, 0, 0);
                            } else {
                                //换行
                                printHelper.printNewLine(3);
                            }
                        } else {
                            printHelper.printNewLine(3);
                        }
                    } else if (tradePrintData == null) {
                        printHelper.addString("持卡人签名:", PrinterDataItem.Align.LEFT);
                        printHelper.printNewLine(3);
                    }
                    //本人确认信息
                    addFooter();
                    String printInfo = mapData.get(key_entryPriInfo2);
                    String printCode = mapData.get(key_entryPricode2);
                    if (!StringUtils.isStrNull(printInfo)) {
                        printHelper.addString(printInfo, PrinterDataItem.Align.CENTER);
                    }
                    if (!StringUtils.isStrNull(printCode)) {
                        Bitmap bitmapQR = ImageUtils.getcodeBmp(printCode);
                        if (null != bitmapQR) {
                            printHelper.reSetTast();
                            Bitmap resultBit = DataHelper.resize(bitmapQR, 200, 200);
                            printHelper.addPrinterTask(resultBit, 50, 0, 0);
                        }
                    }
                } else if (type == 3) {
                    boolean autoQR = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_QRCODE).equals("1") ? true : false;
                    if (autoQR) {
                        printHelper.addString(config.getParam(context, BusinessConfig.Key.PARAM_QRCODE_UP), PrinterDataItem.Align.CENTER);
                        String qrStr = config.getParam(context, BusinessConfig.Key.PARAM_QRCODE);
                        logger.info("二维码打印内容：" + qrStr);
                        if (!StringUtils.isStrNull(qrStr)) {
                            Bitmap bitmapQR = ImageUtils.getcodeBmp(qrStr);
                            if (null != bitmapQR) {
                                printHelper.reSetTast();
                                Bitmap resultBit = DataHelper.resize(bitmapQR, 200, 200);
                                printHelper.addPrinterTask(resultBit, 50, 0, 0);
                            }
                        }
                        printHelper.addString(config.getParam(context, BusinessConfig.Key.PARAM_QRCODE_DOWN), PrinterDataItem.Align.CENTER);
                    }
                    String printInfo = mapData.get(key_entryPriInfo1);
                    String printCode = mapData.get(key_entryPricode1);
                    if (!StringUtils.isStrNull(printInfo)) {
                        printHelper.addString(printInfo, PrinterDataItem.Align.CENTER);
                    }
                    if (!StringUtils.isStrNull(printCode)) {
                        Bitmap bitmapQR = ImageUtils.getcodeBmp(printCode);
                        if (null != bitmapQR) {
                            printHelper.reSetTast();
                            Bitmap resultBit = DataHelper.resize(bitmapQR, 200, 200);
                            printHelper.addPrinterTask(resultBit, 50, 0, 0);
                        }
                    }
                }
                //商户存根需要打印“---机器型号---应用程序版本号---”
           /*     Date date = new Date(Build.TIME);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String buildTime = simpleDateFormat.format(date);*/
                String buildTime = CommonUtils.getBuildTime(context);
//                printHelper.addString("---CENTERM--" + Build.MODEL + "--" + buildTime + "---", PrinterDataItem.Align.CENTER);
                printHelper.addString(saveType, PrinterDataItem.Align.CENTER, 10);//商户存根 请妥善保存
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            printShengPayChinese(type);
        }
    }

    public boolean checkIsCode() {
        switch (transCode) {
            case TransCode.SCAN_PAY_ALI:
            case TransCode.SCAN_PAY_SFT:
            case TransCode.SCAN_PAY_WEI:
            case TransCode.SCAN_CANCEL:
            case TransCode.SCAN_SERCH:
            case TransCode.SCAN_LAST_SERCH:
            case TransCode.SCAN_REFUND_W:
            case TransCode.SCAN_REFUND_Z:
            case TransCode.SCAN_REFUND_S:
                return true;
            default:
                return false;
        }
    }

    private void addLogoHead() throws RemoteException {
        BusinessConfig config = BusinessConfig.getInstance();
        boolean isShowLogo = config.getParam(context, BusinessConfig.Key.FLAG_SWITCH_LOGO).equals("1") ? true : false;
        String defineHeadStr = config.getParam(context, BusinessConfig.Key.PARAM_DEFINE_HEAD);
        if (isShowLogo) {
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.print_logo);
            printHelper.addPrinterTask(logo, 0, 350, 100);
        } else {
            printHelper.addString(defineHeadStr, PrinterDataItem.Align.CENTER, FONT_SIZE_LARGE);
        }

        printHelper.printNewLine(1);
    }

    /**
     * 打印纯中文签购单
     *
     * @param type
     */
    public void printShengPayChinese(int type) {
        boolean isScan = true;//如果是扫码支付不能进行签名打印
        printHelper.init();
        BusinessConfig config = BusinessConfig.getInstance();
        String operId = config.getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        try {
            printHelper.reSetTast();
            addLogoHead();
            //存根栏使用微软雅黑或者相近字体，10号
            String saveType = "";
            switch (type) {
                case 1:
                    saveType = "商户存根  请妥善保管";
                    break;
                case 2:
                    saveType = "银行存根  请妥善保管";
                    break;
                case 3:
                    saveType = "持卡人存根  请妥善保管";
                    break;
            }
            printHelper.addString(saveType, PrinterDataItem.Align.CENTER, 10);
            //分隔符
            addDivider();
            //其它标题使用微软雅黑或者相近字体，8号
            //printHelper.addString("商户名称", PrinterDataItem.Align.LEFT);
            //printHelper.addString(RETRACT + config.getValue(context, BusinessConfig.Key.PRESET_MERCHANT_NAME), PrinterDataItem.Align.RIGHT);
            printHelper.addItem("商户名", "", config.getValue(context, BusinessConfig.Key.PRESET_MERCHANT_NAME), 10, 0, 22, 0, false);
            //printHelper.addString("商户编号", PrinterDataItem.Align.LEFT);
            //printHelper.addString(RETRACT + mapData.get(iso_f42), PrinterDataItem.Align.RIGHT);
            printHelper.addItem("商户号", "", mapData.get(iso_f42), 10, 0, 22, 0, false);
            //printHelper.addString("终端编号", PrinterDataItem.Align.LEFT);
            //printHelper.addString(RETRACT + mapData.get(iso_f41), PrinterDataItem.Align.RIGHT);
            printHelper.addItem("终端号", "", mapData.get(iso_f41), 10, 0, 22, 0, false);
            printHelper.addItem("操作员号", "", operId, 20, 2, 10, 0, false);
            String iss = "";
            if (!TextUtils.isEmpty(mapData.get(iso_f44))) {
                iss = mapData.get(iso_f44).split(" ")[0];
                logger.info("发卡行：" + iss);
                if (iss.equals("")) {
                    if (!TextUtils.isEmpty(mapData.get(iso_f47))) {
                        String tempType = mapData.get(iso_f47).split("=|\\|")[1];
                        if (tempType.contains("ZFB01")) {
                            iss = "支付宝";
                        } else if (tempType.contains("TX01")) {
                            iss = "微信支付";
                        } else if (tempType.contains("SFT01")) {
                            iss = "扫码支付";
                        }
                    } else {
                        iss = tranType;
                    }
                }
                logger.info("发卡行：" + iss);
            }
            printHelper.addItem("发卡行", "", iss, 12, 0, 20, 0, false);
            printHelper.addItem("收单行", "", "48200000", 20, 2, 10, 0, false);
            if (!checkIsCode()) {
                isScan = false;
                //扫码支付没有卡号
                //printHelper.addString("卡号", PrinterDataItem.Align.LEFT);
                String cardNum = (TextUtils.isEmpty(mapData.get(iso_f2)) ? mapData.get(iso_f2_result) : mapData.get(iso_f2));
                String isShieldCard = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_SHIELD_CARD);
                if (tranType.equals(context.getString(R.string.trans_auth))) {
                    if ("1".equals(isShieldCard)) {
                        //printHelper.addString(DataHelper.shieldCardNo(cardNum) + "(" + getTranCardType()+ ")", PrinterDataItem.Align.RIGHT, 10, true);
                        printHelper.addItem("卡号", "", DataHelper.shieldCardNo(cardNum) + "(" + getTranCardType() + ")", 10, 0, 22, 0, false);
                    } else {
                        //printHelper.addString(cardNum + "(" +getTranCardType()+ ")", PrinterDataItem.Align.RIGHT, 10, true);
                        printHelper.addItem("卡号", "", cardNum + "(" + getTranCardType() + ")", 10, 0, 22, 0, false);
                    }
                } else {
                    //printHelper.addString(DataHelper.shieldCardNo(cardNum) + "(" + getTranCardType()+ ")", PrinterDataItem.Align.RIGHT, 10, true);
                    printHelper.addItem("卡号", "", DataHelper.shieldCardNo(cardNum) + "(" + getTranCardType() + ")", 10, 0, 22, 0, false);
                }
            }
            //printHelper.addString("交易类型", PrinterDataItem.Align.LEFT);
            //printHelper.addString(tranType, PrinterDataItem.Align.RIGHT, 10, true);
            printHelper.addItem("交易类型", "", tranType, 10, 0, 22, 0, false);
            printHelper.addItem("批次号", "", mapData.get(iso_f60).substring(2, 8), 20, 2, 10, 0, false);
            printHelper.addItem("凭证号", "", mapData.get(iso_f11), 20, 2, 10, 0, false);
            printHelper.addItem("参考号", "", mapData.get(iso_f37), 18, 2, 12, 0, false);
            printHelper.addItem("授权码", "", mapData.get(iso_f38), 18, 2, 12, 0, false);
            //预授权交易打印卡有效期
            if (tranType.equals(context.getString(R.string.trans_auth)) && !TextUtils.isEmpty(mapData.get(iso_f14))) {
                printHelper.addItem("卡有效期", "", mapData.get(iso_f14), 20, 2, 10, 0, false);
            }
            //printHelper.addString("日期/时间", PrinterDataItem.Align.LEFT);
            //printHelper.addString(RETRACT + DataHelper.formatDateAndTime("" + Calendar.getInstance().get(Calendar.YEAR) + mapData.get(iso_f13) + mapData.get(iso_f12)), PrinterDataItem.Align.RIGHT);
            printHelper.addItem("日期/时间", "", DataHelper.formatDateAndTime("" + Calendar.getInstance().get(Calendar.YEAR) + mapData.get(iso_f13) + mapData.get(iso_f12)), 10, 0, 22, 0, false);
            printHelper.addString("金额", PrinterDataItem.Align.LEFT);
            if (tranType.equals(context.getString(R.string.trans_sale)) || tranType.equals(context.getString(R.string.trans_auth)) || tranType.equals(context.getString(R.string.trans_auth_complete))
                    || tranType.equals(context.getString(R.string.trans_scan_pay_wei)) || tranType.equals(context.getString(R.string.trans_scan_pay_ali)) || tranType.equals(context.getString(R.string.trans_scan_pay_sft))) {
                printHelper.addString("RMB  " + DataHelper.formatIsoF4(mapData.get(iso_f4)), PrinterDataItem.Align.RIGHT, FONT_SIZE_HEIGHT_LARGE, true);
            } else {
                String isMinus = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_MINUS);
                if ("1".equals(isMinus)) {
                    printHelper.addString("RMB  -" + DataHelper.formatIsoF4(mapData.get(iso_f4)), PrinterDataItem.Align.RIGHT, FONT_SIZE_HEIGHT_LARGE, true);
                } else {
                    printHelper.addString("RMB  " + DataHelper.formatIsoF4(mapData.get(iso_f4)), PrinterDataItem.Align.RIGHT, FONT_SIZE_HEIGHT_LARGE, true);
                }
            }
            //撤销交易，打印原交易凭证号;退货交易，打印原交易参考号和原交易日期
            if (tranType.equals(context.getString(R.string.trans_void)) || tranType.equals(context.getString(R.string.trans_scan_cancel))) {
                printHelper.addString("原交易凭证号" + mapData.get(iso_f11_origin), PrinterDataItem.Align.LEFT);
            } else if (tranType.equals(context.getString(R.string.trans_refund)) || tranType.equals(context.getString(R.string.trans_scan_refund))) {
                printHelper.addString("原参考号" + mapData.get(key_oriReference), PrinterDataItem.Align.LEFT);
                printHelper.addString("原交易时间" + mapData.get(key_oriTransTime), PrinterDataItem.Align.LEFT);
            } else if (tranType.equals(context.getString(R.string.trans_auth_complete))) {
                printHelper.addString("原授权码" + mapData.get(key_oriAuthCode), PrinterDataItem.Align.LEFT);
            } else if (tranType.equals(context.getString(R.string.trans_cancel))) {
                printHelper.addString("原授权码" + mapData.get(key_oriAuthCode), PrinterDataItem.Align.LEFT);
            } else if (tranType.equals(context.getString(R.string.trans_complete_void))) {
                printHelper.addString("原交易凭证号" + mapData.get(iso_f11_origin), PrinterDataItem.Align.LEFT);
                printHelper.addString("原授权码" + mapData.get(key_oriAuthCode), PrinterDataItem.Align.LEFT);
            }
            //打印IC卡数据
            addICCardParam();
            //打印备注
            printHelper.addString("备注：", PrinterDataItem.Align.LEFT);
            if (isRePrint) {
                printHelper.addString("---重打印---", PrinterDataItem.Align.CENTER, 10);
            }
            printHelper.addString(config.getParam(context, BusinessConfig.Key.PARAM_PRINT_REMARK), PrinterDataItem.Align.LEFT, 10, true);
            if (tradePrintData != null && tradePrintData.isNoNeedSign() && !StringUtils.isStrNull(tradePrintData.getAmount())) {
                printHelper.addString("交易金额未超过" + tradePrintData.getAmount() + "元，免密免签", PrinterDataItem.Align.CENTER);
            }
            if (type == 1) {
                if (tradePrintData != null && !tradePrintData.isNoNeedSign()) {
                    printHelper.addString("持卡人签名", PrinterDataItem.Align.LEFT);
                    if (!isScan) {
                        if (null != bitmap) {
                            printHelper.reSetTast();
                            Bitmap resultBit = DataHelper.resize(bitmap, 210, 140);
                            printHelper.addPrinterTask(resultBit, 50, 0, 0);
                        } else {
                            //换行
                            printHelper.printNewLine(3);
                        }
                    } else {
                        printHelper.printNewLine(3);
                    }
                } else if (tradePrintData == null) {
                    printHelper.addString("持卡人签名", PrinterDataItem.Align.LEFT);
                    printHelper.printNewLine(3);
                }
                //本人确认信息
                addFooter();
                String printInfo = mapData.get(key_entryPriInfo2);
                String printCode = mapData.get(key_entryPricode2);
                if (!StringUtils.isStrNull(printInfo)) {
                    printHelper.addString(printInfo, PrinterDataItem.Align.CENTER);
                }
                if (!StringUtils.isStrNull(printCode)) {
                    Bitmap bitmapQR = ImageUtils.getcodeBmp(printCode);
                    if (null != bitmapQR) {
                        printHelper.reSetTast();
                        Bitmap resultBit = DataHelper.resize(bitmapQR, 200, 200);
                        printHelper.addPrinterTask(resultBit, 50, 0, 0);
                    }
                }
            } else if (type == 3) {
                boolean autoQR = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_QRCODE).equals("1") ? true : false;
                if (autoQR) {
                    printHelper.addString(config.getParam(context, BusinessConfig.Key.PARAM_QRCODE_UP), PrinterDataItem.Align.CENTER);
                    String qrStr = config.getParam(context, BusinessConfig.Key.PARAM_QRCODE);
                    logger.info("二维码打印内容：" + qrStr);
                    if (!StringUtils.isStrNull(qrStr)) {
                        Bitmap bitmapQR = ImageUtils.getcodeBmp(qrStr);
                        if (null != bitmapQR) {
                            printHelper.reSetTast();
                            Bitmap resultBit = DataHelper.resize(bitmapQR, 200, 200);
                            printHelper.addPrinterTask(resultBit, 50, 0, 0);
                        }
                    }
                    printHelper.addString(config.getParam(context, BusinessConfig.Key.PARAM_QRCODE_DOWN), PrinterDataItem.Align.CENTER);
                }
                String printInfo = mapData.get(key_entryPriInfo1);
                String printCode = mapData.get(key_entryPricode1);
                if (!StringUtils.isStrNull(printInfo)) {
                    printHelper.addString(printInfo, PrinterDataItem.Align.CENTER);
                }
                if (!StringUtils.isStrNull(printCode)) {
                    Bitmap bitmapQR = ImageUtils.getcodeBmp(printCode);
                    if (null != bitmapQR) {
                        printHelper.reSetTast();
                        Bitmap resultBit = DataHelper.resize(bitmapQR, 200, 200);
                        printHelper.addPrinterTask(resultBit, 50, 0, 0);
                    }
                }
            }
            //商户存根需要打印“---机器型号---应用程序版本号---”
       /*     Date date = new Date(Build.TIME);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String buildTime = simpleDateFormat.format(date);*/
            String buildTime = CommonUtils.getBuildTime(context);
            printHelper.addString("---CENTERM--" + Build.MODEL + "--" + buildTime + "---", PrinterDataItem.Align.CENTER);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    public String getSpace(String rightStr) {
        int len = 8;
        String space = "";
        len = len - rightStr.length() * 2;
        for (int i = 0; i < len; i++)
            space += " ";
        return space;
    }

    public void setBatchListener(PrinterCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 上批结算总计单
     */
    public void printLastTotalData(String gsonStr) {
        boolean isPrintVoid = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL).equals("1") ? true : false;
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("上批POS结算总计", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHeader(true);
            printHelper.addItem("类型/TYPE", "笔数/SUM", "金额/AMT", 14, 4, 14, 0, false);
            addDivider();
            JSONArray jsonArray = new JSONArray(gsonStr);
            printHelper.addString("消费");
            printHelper.addItem("--银行卡", jsonArray.get(0) + "", jsonArray.get(1) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--预授权完成", jsonArray.get(2) + "", jsonArray.get(3) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--微信", jsonArray.get(4) + "", jsonArray.get(5) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--支付宝", jsonArray.get(6) + "", jsonArray.get(7) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--XXX", jsonArray.get(8) + "", jsonArray.get(9) + "", 14, 4, 14, 0, false);
            printHelper.printNewLine(1);
            printHelper.addString("退货");
            printHelper.addItem("--银行卡", jsonArray.get(10) + "", jsonArray.get(11) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--微信", jsonArray.get(12) + "", jsonArray.get(13) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--支付宝", jsonArray.get(14) + "", jsonArray.get(15) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--XXX", jsonArray.get(16) + "", jsonArray.get(17) + "", 14, 4, 14, 0, false);
            if (isPrintVoid) {
                printHelper.printNewLine(1);
                printHelper.addString("撤销");
                printHelper.addItem("--消费撤销", jsonArray.get(18) + "", jsonArray.get(19) + "", 14, 4, 14, 0, false);
                printHelper.addItem("--预授权完成撤销", jsonArray.get(20) + "", jsonArray.get(21) + "", 14, 4, 14, 0, false);
                printHelper.addItem("--扫码撤销", jsonArray.get(22) + "", jsonArray.get(23) + "", 14, 4, 14, 0, false);
            }
            addDivider();
            printHelper.addItem("消费总计", jsonArray.get(24) + "", jsonArray.get(25) + "", 14, 4, 14, 0, false);
            printHelper.addItem("退货总计", jsonArray.get(28) + "", jsonArray.get(29) + "", 14, 4, 14, 0, false);
            if (isPrintVoid) {
                printHelper.addItem("撤销总计", jsonArray.get(26) + "", jsonArray.get(27) + "", 14, 4, 14, 0, false);
            }
            addDivider();
//            printHelper.addString("客服电话：4007208888");
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterFirstSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    callBack.onPrinterFirstFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批结算总计单
     *
     * @param lists
     */
    public void printBatchTotalData(final List<List<TradeInfo>> lists, boolean isPre) {
        if (null == lists) {
            logger.error("批结算打印汇总时lists数据异常");
            return;
        }
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            if (isPre) {
                printHelper.addString("上批POS结算总计", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            } else {
                printHelper.addString("POS结算总计", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            }
            if (isPre) {
                printHeader(true);
            } else {
                printHeader(false);
            }
            printHelper.addItem("类型/TYPE", "笔数/SUM", "金额/AMT", 14, 4, 14, 0, false);
            addDivider();
            saleAmount = 0.0;
            authCompleteAmount = 0.0;
            weiSaleAmount = 0.0;
            aliSaleAmount = 0.0;
            shengSaleAmount = 0.0;
            refundAmount = 0.0;
            weiRefundAmount = 0.0;
            aliRefundAmount = 0.0;
            shengRefundAmount = 0.0;
            saleVoidAmount = 0.0;
            completeVoidAmount = 0.0;
            codeVoidAmount = 0.0;
            saleTotalAmount = 0.0;
            voidTotalAmount = 0.0;
            refundTotalAmount = 0.0;
            addTotalData(lists);
            try {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(0, sale.size());
                jsonArray.put(1, DataHelper.saved2Decimal(saleAmount));
                jsonArray.put(2, authComplete.size());
                jsonArray.put(3, DataHelper.saved2Decimal(authCompleteAmount));
                jsonArray.put(4, weiSale.size());
                jsonArray.put(5, DataHelper.saved2Decimal(weiSaleAmount));
                jsonArray.put(6, aliSale.size());
                jsonArray.put(7, DataHelper.saved2Decimal(aliSaleAmount));
                jsonArray.put(8, shengSale.size());
                jsonArray.put(9, DataHelper.saved2Decimal(shengSaleAmount));
                jsonArray.put(10, refund.size());
                jsonArray.put(11, DataHelper.saved2Decimal(refundAmount));
                jsonArray.put(12, weiRefund.size());
                jsonArray.put(13, DataHelper.saved2Decimal(weiRefundAmount));
                jsonArray.put(14, aliRefund.size());
                jsonArray.put(15, DataHelper.saved2Decimal(aliRefundAmount));
                jsonArray.put(16, shengRefund.size());
                jsonArray.put(17, DataHelper.saved2Decimal(shengRefundAmount));
                jsonArray.put(18, saleVoid.size());
                jsonArray.put(19, DataHelper.saved2Decimal(saleVoidAmount));
                jsonArray.put(20, completeVoid.size());
                jsonArray.put(21, DataHelper.saved2Decimal(completeVoidAmount));
                jsonArray.put(22, codeVoid.size());
                jsonArray.put(23, DataHelper.saved2Decimal(codeVoidAmount));
                jsonArray.put(24, saleTotal.size());
                jsonArray.put(25, DataHelper.saved2Decimal(saleTotalAmount));
                jsonArray.put(26, voidTotal.size());
                jsonArray.put(27, DataHelper.saved2Decimal(voidTotalAmount));
                jsonArray.put(28, refundTotal.size());
                jsonArray.put(29, DataHelper.saved2Decimal(refundTotalAmount));
                Settings.setValue(context, Settings.KEY.PREV_BATCH_TOTAL, jsonArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addDivider();
//            printHelper.addString("客服电话：4007208888");

            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterFirstSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    callBack.onPrinterFirstFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 汇总打印
     *
     * @return
     */
    public void printDataALLTrans(final List<List<TradeInfo>> lists) {

        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printHelper.addString("交易汇总", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHeader(false);
            printHelper.addItem("类型/TYPE", "笔数/SUM", "金额/AMT", 14, 4, 14, 0, false);
            addDivider();
            saleAmount = 0.0;
            authCompleteAmount = 0.0;
            weiSaleAmount = 0.0;
            aliSaleAmount = 0.0;
            shengSaleAmount = 0.0;
            refundAmount = 0.0;
            weiRefundAmount = 0.0;
            aliRefundAmount = 0.0;
            shengRefundAmount = 0.0;
            saleVoidAmount = 0.0;
            completeVoidAmount = 0.0;
            codeVoidAmount = 0.0;
            saleTotalAmount = 0.0;
            voidTotalAmount = 0.0;
            refundTotalAmount = 0.0;
            addTotalData(lists);
            addDivider();
//            printHelper.addString("客服电话：4007208888");
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    DialogFactory.hideAll();
                    ViewUtils.showToast(context, R.string.tip_print_over);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
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
                                            printDataALLTrans(lists);
                                            break;
                                        case NEGATIVE:
                                            printHelper.release();
                                            DialogFactory.hideAll();
                                            break;
                                    }
                                }
                            });

                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        }
    }

    private void addTotalData(List<List<TradeInfo>> lists) throws UnsupportedEncodingException, RemoteException {
        boolean isPrintVoid = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL).equals("1") ? true : false;
        List<TradeInfo> infos = lists.get(5);
        sale.clear();
        authComplete.clear();
        weiSale.clear();
        aliSale.clear();
        shengSale.clear();
        refund.clear();
        weiRefund.clear();
        aliRefund.clear();
        shengRefund.clear();
        saleVoid.clear();
        completeVoid.clear();
        codeVoid.clear();
        saleTotal.clear();
        refundTotal.clear();
        voidTotal.clear();
        if (null != infos) {
            for (TradeInfo tradeInfo :
                    infos) {
                switch (tradeInfo.getTransCode()) {
                    case TransCode.SALE:
                        sale.add(tradeInfo);
                        saleTotal.add(tradeInfo);
                        String amountStr1 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr1 && !"".equals(amountStr1)) {
                            saleAmount = DataHelper.formatDouble(saleAmount + DataHelper.parseIsoF4(amountStr1));
                            saleTotalAmount = DataHelper.formatDouble(saleTotalAmount + DataHelper.parseIsoF4(amountStr1));
                        }
                        break;
                    case TransCode.AUTH_COMPLETE:
                        authComplete.add(tradeInfo);
                        saleTotal.add(tradeInfo);
                        String amountStr2 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr2 && !"".equals(amountStr2)) {
                            authCompleteAmount = DataHelper.formatDouble(authCompleteAmount + DataHelper.parseIsoF4(amountStr2));
                            saleTotalAmount = DataHelper.formatDouble(saleTotalAmount + DataHelper.parseIsoF4(amountStr2));
                        }
                        break;
                    case TransCode.SCAN_PAY_WEI:
                        weiSale.add(tradeInfo);
                        saleTotal.add(tradeInfo);
                        String amountStr3 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr3 && !"".equals(amountStr3)) {
                            weiSaleAmount = DataHelper.formatDouble(weiSaleAmount + DataHelper.parseIsoF4(amountStr3));
                            saleTotalAmount = DataHelper.formatDouble(saleTotalAmount + DataHelper.parseIsoF4(amountStr3));
                        }
                        break;
                    case TransCode.SCAN_PAY_ALI:
                        aliSale.add(tradeInfo);
                        saleTotal.add(tradeInfo);
                        String amountStr4 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr4 && !"".equals(amountStr4)) {
                            aliSaleAmount = DataHelper.formatDouble(aliSaleAmount + DataHelper.parseIsoF4(amountStr4));
                            saleTotalAmount = DataHelper.formatDouble(saleTotalAmount + DataHelper.parseIsoF4(amountStr4));
                        }
                        break;
                    case TransCode.SCAN_PAY_SFT:
                        shengSale.add(tradeInfo);
                        saleTotal.add(tradeInfo);
                        String amountStr5 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr5 && !"".equals(amountStr5)) {
                            shengSaleAmount = DataHelper.formatDouble(shengSaleAmount + DataHelper.parseIsoF4(amountStr5));
                            saleTotalAmount = DataHelper.formatDouble(saleTotalAmount + DataHelper.parseIsoF4(amountStr5));
                        }
                        break;
                    case TransCode.REFUND:
                        refund.add(tradeInfo);
                        refundTotal.add(tradeInfo);
                        String amountStr6 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr6 && !"".equals(amountStr6)) {
                            refundAmount = DataHelper.formatDouble(refundAmount + DataHelper.parseIsoF4(amountStr6));
                            refundTotalAmount = DataHelper.formatDouble(refundTotalAmount + DataHelper.parseIsoF4(amountStr6));
                        }
                        break;
                    case TransCode.SCAN_REFUND_W:
                        weiRefund.add(tradeInfo);
                        refundTotal.add(tradeInfo);
                        String amountStr7 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr7 && !"".equals(amountStr7)) {
                            weiRefundAmount = DataHelper.formatDouble(weiRefundAmount + DataHelper.parseIsoF4(amountStr7));
                            refundTotalAmount = DataHelper.formatDouble(refundTotalAmount + DataHelper.parseIsoF4(amountStr7));
                        }
                        break;
                    case TransCode.SCAN_REFUND_Z:
                        aliRefund.add(tradeInfo);
                        refundTotal.add(tradeInfo);
                        String amountStr8 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr8 && !"".equals(amountStr8)) {
                            aliRefundAmount = DataHelper.formatDouble(aliRefundAmount + DataHelper.parseIsoF4(amountStr8));
                            refundTotalAmount = DataHelper.formatDouble(refundTotalAmount + DataHelper.parseIsoF4(amountStr8));
                        }
                        break;
                    case TransCode.SCAN_REFUND_S:
                        shengRefund.add(tradeInfo);
                        refundTotal.add(tradeInfo);
                        String amountStr9 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr9 && !"".equals(amountStr9)) {
                            shengRefundAmount = DataHelper.formatDouble(shengRefundAmount + DataHelper.parseIsoF4(amountStr9));
                            refundTotalAmount = DataHelper.formatDouble(refundTotalAmount + DataHelper.parseIsoF4(amountStr9));
                        }
                        break;
                    case TransCode.VOID:
                        saleVoid.add(tradeInfo);
                        voidTotal.add(tradeInfo);
                        String amountStr10 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr10 && !"".equals(amountStr10)) {
                            saleVoidAmount = DataHelper.formatDouble(saleVoidAmount + DataHelper.parseIsoF4(amountStr10));
                            voidTotalAmount = DataHelper.formatDouble(voidTotalAmount + DataHelper.parseIsoF4(amountStr10));
                        }
                        break;
                    case TransCode.SCAN_CANCEL:
                        codeVoid.add(tradeInfo);
                        voidTotal.add(tradeInfo);
                        String amountStr11 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr11 && !"".equals(amountStr11)) {
                            codeVoidAmount = DataHelper.formatDouble(codeVoidAmount + DataHelper.parseIsoF4(amountStr11));
                            voidTotalAmount = DataHelper.formatDouble(voidTotalAmount + DataHelper.parseIsoF4(amountStr11));
                        }
                        break;
                    case TransCode.COMPLETE_VOID:
                        completeVoid.add(tradeInfo);
                        voidTotal.add(tradeInfo);
                        String amountStr12 = DataHelper.formatAmountForShow(tradeInfo.getIso_f4());
                        if (null != amountStr12 && !"".equals(amountStr12)) {
                            completeVoidAmount = DataHelper.formatDouble(completeVoidAmount + DataHelper.parseIsoF4(amountStr12));
                            voidTotalAmount = DataHelper.formatDouble(voidTotalAmount + DataHelper.parseIsoF4(amountStr12));
                        }
                        break;
                }
            }
        }
        printHelper.addString("消费");
        printHelper.addItem("--银行卡", sale.size() + "", DataHelper.saved2Decimal(saleAmount) + "", 14, 4, 14, 0, false);
        printHelper.addItem("--预授权完成", authComplete.size() + "", DataHelper.saved2Decimal(authCompleteAmount) + "", 14, 4, 14, 0, false);
//        printHelper.addItem("--微信", weiSale.size() + "", DataHelper.saved2Decimal(weiSaleAmount) + "", 14, 4, 14, 0, false);
//        printHelper.addItem("--支付宝", aliSale.size() + "", DataHelper.saved2Decimal(aliSaleAmount) + "", 14, 4, 14, 0, false);
//        printHelper.addItem("--XXX", shengSale.size() + "", DataHelper.saved2Decimal(shengSaleAmount) + "", 14, 4, 14, 0, false);
        printHelper.printNewLine(1);
        printHelper.addString("退货");
        printHelper.addItem("--银行卡", refund.size() + "", DataHelper.saved2Decimal(refundAmount) + "", 14, 4, 14, 0, false);
//        printHelper.addItem("--微信", weiRefund.size() + "", DataHelper.saved2Decimal(weiRefundAmount) + "", 14, 4, 14, 0, false);
//        printHelper.addItem("--支付宝", aliRefund.size() + "", DataHelper.saved2Decimal(aliRefundAmount) + "", 14, 4, 14, 0, false);
//        printHelper.addItem("--XXX", shengRefund.size() + "", DataHelper.saved2Decimal(shengRefundAmount) + "", 14, 4, 14, 0, false);
        if (isPrintVoid) {
            printHelper.printNewLine(1);
            printHelper.addString("撤销");
            printHelper.addItem("--消费撤销", saleVoid.size() + "", DataHelper.saved2Decimal(saleVoidAmount) + "", 14, 4, 14, 0, false);
            printHelper.addItem("--预授权完成撤销", completeVoid.size() + "", DataHelper.saved2Decimal(completeVoidAmount) + "", 14, 4, 14, 0, false);
//            printHelper.addItem("--扫码撤销", codeVoid.size() + "", DataHelper.saved2Decimal(codeVoidAmount) + "", 14, 4, 14, 0, false);
        }
        addDivider();
        printHelper.addItem("消费总计", saleTotal.size() + "", DataHelper.saved2Decimal(saleTotalAmount) + "", 14, 4, 14, 0, false);
        printHelper.addItem("退货总计", refundTotal.size() + "", DataHelper.saved2Decimal(refundTotalAmount) + "", 14, 4, 14, 0, false);
        if (isPrintVoid) {
            printHelper.addItem("撤销总计", voidTotal.size() + "", DataHelper.saved2Decimal(voidTotalAmount) + "", 14, 4, 14, 0, false);
        }
    }


    private void addDetailFooter() throws RemoteException {
        boolean isPrintVoid = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL).equals("1") ? true : false;
        addDivider();
        printHelper.addString("S-消费 R-退货 P-预授权完成");
//        printHelper.addString("S(W)-微信消费 R(W)-微信退货");
//        printHelper.addString("S(Z)-支付宝消费 R(Z)-支付宝退货");
//        printHelper.addString("S(S)-XXX消费 R(S)-XXX退货");
        if (isPrintVoid) {
            printHelper.addString("V-消费撤销 V(C)扫码撤销");
            printHelper.addString("V(P)-预授权完成撤销");
        }
    }

    // 打印交易明细数据
    public void printDetails(final List<TradeInfo> tradeInfos) {
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printHelper.addString("交易明细", PrinterDataItem.Align.CENTER);
            printHeader(false);
            addDivider();
            addDetailData(tradeInfos, false);
            addDetailFooter();
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    DialogFactory.hideAll();
                    ViewUtils.showToast(context, R.string.tip_print_over);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
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
                                            printDetails(tradeInfos);
                                            break;
                                        case NEGATIVE:
                                            printHelper.release();
                                            DialogFactory.hideAll();
                                            break;
                                    }
                                }
                            });

                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批结算明细打印
     *
     * @param tradeInfos
     */
    public void printBatchDetailData(List<TradeInfo> tradeInfos) {
        if (null == tradeInfos) {
            logger.error("打印明细时lists数据异常");
            return;
        }
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("POS结算明细", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHeader(false);
            addDetailData(tradeInfos, false);
            addDetailFooter();
            printHelper.printNewLine(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterSecondSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    callBack.onPrinterSecondFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取冲正流水
     *
     * @param reverseList
     */
    public void printFailData(List<TradeInfo> reverseList) {
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("POS结算失败交易明细", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHeader(false);
            addDetailData(reverseList, true);
            addDetailFooter();
            printHelper.printNewLine(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterThreeSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    callBack.onPrinterThreeFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 交易明细数据构造
     *
     * @param tradeInfos
     * @throws RemoteException
     */
    private void addDetailData(List<TradeInfo> tradeInfos, boolean isFail) throws RemoteException, UnsupportedEncodingException {
    /*    printHelper.addItem("凭证号","类型","卡号",8, 6, 18, 0, false);
        if (isFail) {
            printHelper.addItem("金额","","",12, 6, 10, 0, false);
            printHelper.addString("时间");
        } else {
            printHelper.addItem("金额","","授权码",12, 6, 10, 0, false);
            printHelper.addString("时间");
        }*/
        printHelper.addString("凭证号    类型    时间    授权码");
        printHelper.addString("金额       卡号");
        addDivider();
        for (int i = 1; i <= tradeInfos.size(); i++) {
            String formatCardNum = null;
            TradeInfo info = tradeInfos.get(i - 1);
            String posFlowNo = info.getIso_f11() == null ? "" : info.getIso_f11();//凭证号
            String tranType = info.getKey_trans_code() == null ? "" : info.getKey_trans_code();
            String cardNum = info.getIso_f2() == null ? "" : info.getIso_f2();
            String amount = info.getIso_f4() == null ? "" : info.getIso_f4();
            String permisionNo = info.getIso_f38() == null ? "" : info.getIso_f38();
            String formatCar = DataHelper.shieldCardNo(cardNum);
            if (tranType.length() == 1) {
                printHelper.addString(posFlowNo + "       " + tranType + "    " + DataHelper.formatDateAndTime2("" + Calendar.getInstance().get(Calendar.YEAR) + info.getIso_f13() + info.getIso_f12()) + "    " + permisionNo, PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
            } else {
                printHelper.addString(posFlowNo + "    " + tranType + "    " + DataHelper.formatDateAndTime2("" + Calendar.getInstance().get(Calendar.YEAR) + info.getIso_f13() + info.getIso_f12()) + "    " + permisionNo, PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
            }
            printHelper.addString(DataHelper.formatAmountForShow(amount) + "    " + formatCar, PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
           /* printHelper.addItem(posFlowNo,tranType,formatCar,8, 4, 20,0,false);
            printHelper.addItem(DataHelper.formatAmountForShow(amount),"",permisionNo,12, 6, 10,0,false);
            printHelper.addString(DataHelper.formatDateAndTime2("" + Calendar.getInstance().get(Calendar.YEAR) + info.getIso_f13()+ info.getIso_f12()));*/
        }
    }

    /**
     * 批结算未上送订单详细打印
     *
     * @param rejuseItems
     * @param failItems
     */
    public void printFailDetailData(List<TradeInfo> rejuseItems, List<TradeInfo> failItems) {
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("POS结算未上送明细", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHeader(false);
            if (null != failItems && failItems.size() > 0) {
                printHelper.addString("未成功上送交易明细", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
                toolMethod(failItems);
            }
            if (null != rejuseItems && rejuseItems.size() > 0) {
                printHelper.addString("上送后被平台拒绝交易明细", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
                toolMethod(rejuseItems);
            }
            printHelper.printNewLine(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterThreeSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    callBack.onPrinterThreeFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void toolMethod(List<TradeInfo> infos) {
        try {
            printHelper.addString("凭证号    类型          卡号", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
            printHelper.addString("金额", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
            addDivider();
            for (int i = 1; i <= infos.size(); i++) {
                /*凭证号    类型       卡号     金额*/
                TradeInfo info = infos.get(i - 1);
                String posFlowNo = info.getIso_f11() == null ? "" : info.getIso_f11();//凭证号
                String tranType = info.getTransCode() == null ? "" : info.getTransCode();
                String cardNum = info.getIso_f2() == null ? "" : info.getIso_f2();
                String amount = info.getIso_f4() == null ? "" : info.getIso_f4();
                String transtype = context.getString(TransCode.codeMapName(tranType));
                String formatCardNum = DataHelper.shieldCardNo(cardNum);
                if (transtype.length() == 2) {
                    printHelper.addString(posFlowNo + "   " + transtype + "           " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 3) {
                    printHelper.addString(posFlowNo + "   " + transtype + "         " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 4) {
                    printHelper.addString(posFlowNo + "   " + transtype + "       " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 5) {
                    printHelper.addString(posFlowNo + "   " + transtype + "     " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 6) {
                    printHelper.addString(posFlowNo + "   " + transtype + "   " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 7) {
                    printHelper.addString(posFlowNo + "   " + transtype + " " + formatCardNum, FONT_SIZE_SMALL);
                }
                printHelper.addString(DataHelper.formatAmountForShow(amount), FONT_SIZE_SMALL);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印签购单头部
     *
     * @param isLastBatch 是否是上一批交易
     */
    private void printHeader(boolean isLastBatch) {
        BusinessConfig config = BusinessConfig.getInstance();
        try {
            addDivider();
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = newFormat.format(new Date());
            String merName = config.getValue(context, BusinessConfig.Key.PRESET_MERCHANT_NAME);
            String merCode = config.getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD);
            String terCode = config.getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD);
            printHelper.addString("商户名称 ：");
            printHelper.addString(merName);
            printHelper.addString("商户编号：");
            printHelper.addString(merCode);
            printHelper.addString("终端编号：");
            printHelper.addString(terCode);
            if (isLastBatch) {
                printHelper.addString("批次号：" + config.getValue(context, BusinessConfig.Key.KEY_LAST_BATCH_NO));
            } else {
                printHelper.addString("批次号：" + config.getBatchNo(context));
            }
            printHelper.addString("日期时间：");
            printHelper.addString(time);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setElecBitMap(Bitmap bitmap) {
        this.bitmap = null;
        this.bitmap = bitmap;
    }

    //商户存根
    public void addShopDivider() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append('-');
        }
        sb.append("商户存根");
        for (int i = 0; i < 12; i++) {
            sb.append('-');
        }
        printHelper.addString(sb.toString());
    }

    //持卡人存根
    public void printPersonDivider() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            sb.append('-');
        }
        sb.append("持卡人存根");
        for (int i = 0; i < 11; i++) {
            sb.append('-');
        }
        printHelper.addString(sb.toString());
    }

    public void addFooter() throws RemoteException {
        addDivider();
        boolean isEnglish = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_ENGLISH).equals("1") ? true : false;
        if (isEnglish) {
            printHelper.addString("本人确认以上交易", PrinterDataItem.Align.CENTER);
            printHelper.addString("同意将其记入本卡账户", PrinterDataItem.Align.CENTER);
        } else {
            printHelper.addString("本人确认以上交易", PrinterDataItem.Align.CENTER);
            printHelper.addString("同意将其记入本卡账户", PrinterDataItem.Align.CENTER);
        }
    }

    // 分割线-------
    public void addDivider() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append('-');
        }
        printHelper.addString(sb.toString());
    }

    /*  // 分割线=====
      public void addDivider2() throws RemoteException {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < 32; i++) {
              sb.append('=');
          }
          printHelper.addString(sb.toString());
      }*/
    public interface PrinterCallBack {
        void onPrinterFirstSuccess();

        void onPrinterSecondSuccess();

        void onPrinterThreeSuccess();

        void onPrinterFirstFail(int errorCode, String errorMsg);

        void onPrinterSecondFail(int errorCode, String errorMsg);

        void onPrinterThreeFail(int errorCode, String errorMsg);
    }

    public int converTextSize(int textSize) {
        if (textSize == 1) {
            return FONT_SIZE_SMALL;
        } else if (textSize == 2) {
            return 0;
        } else if (textSize == 3) {
            return FONT_SIZE_HEIGHT_LARGE;
        }
        return 0;
    }

    public EreceiptCreator.FontSize converTextSizeForPic(int textSize) {
        if (textSize == 1) {
            return EreceiptCreator.FontSize.SMALL;
        } else if (textSize == 2) {
            return EreceiptCreator.FontSize.MEDIUM;
        } else if (textSize == 3) {
            return EreceiptCreator.FontSize.LARGE;
        }
        return EreceiptCreator.FontSize.MEDIUM;
    }
}

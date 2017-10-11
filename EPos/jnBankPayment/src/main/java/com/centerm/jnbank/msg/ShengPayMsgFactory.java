package com.centerm.jnbank.msg;

import android.content.Context;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.task.AsyncQueryPrintDataTask;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.jnbank.common.TransCode.AUTH;
import static com.centerm.jnbank.common.TransCode.AUTH_COMPLETE;
import static com.centerm.jnbank.common.TransCode.AUTH_COMPLETE_REVERSE;
import static com.centerm.jnbank.common.TransCode.AUTH_REVERSE;
import static com.centerm.jnbank.common.TransCode.AUTH_SETTLEMENT;
import static com.centerm.jnbank.common.TransCode.BALANCE;
import static com.centerm.jnbank.common.TransCode.CANCEL;
import static com.centerm.jnbank.common.TransCode.CANCEL_REVERSE;
import static com.centerm.jnbank.common.TransCode.COMPLETE_VOID;
import static com.centerm.jnbank.common.TransCode.COMPLETE_VOID_REVERSE;
import static com.centerm.jnbank.common.TransCode.DISCOUNT_INTERGRAL;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_AID;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_CAPK;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_CARD_BIN;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_CARD_BIN_QPS;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_PARAMS_FINISHED;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_QPS_PARAMS;
import static com.centerm.jnbank.common.TransCode.ELC_SIGNATURE;
import static com.centerm.jnbank.common.TransCode.INIT_TERMINAL;
import static com.centerm.jnbank.common.TransCode.LOAD_PARAM;
import static com.centerm.jnbank.common.TransCode.OBTAIN_TMK;
import static com.centerm.jnbank.common.TransCode.POS_STATUS_UPLOAD;
import static com.centerm.jnbank.common.TransCode.REFUND;
import static com.centerm.jnbank.common.TransCode.SALE;
import static com.centerm.jnbank.common.TransCode.SALE_REVERSE;
import static com.centerm.jnbank.common.TransCode.SCAN_CANCEL;
import static com.centerm.jnbank.common.TransCode.SCAN_LAST_SERCH;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_S;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_W;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_Z;
import static com.centerm.jnbank.common.TransCode.SCAN_SERCH;
import static com.centerm.jnbank.common.TransCode.SETTLEMENT;
import static com.centerm.jnbank.common.TransCode.SETTLEMENT_DONE;
import static com.centerm.jnbank.common.TransCode.SIGN_IN;
import static com.centerm.jnbank.common.TransCode.SIGN_OUT;
import static com.centerm.jnbank.common.TransCode.TRANSFER;
import static com.centerm.jnbank.common.TransCode.UPLOAD_SCRIPT_RESULT;
import static com.centerm.jnbank.common.TransCode.VOID;
import static com.centerm.jnbank.common.TransCode.VOID_REVERSE;
import static com.centerm.jnbank.common.TransDataKey.KEY_PARAMS_TYPE;
import static com.centerm.jnbank.common.TransDataKey.KEY_QPS_FLAG;
import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f11_origin;
import static com.centerm.jnbank.common.TransDataKey.iso_f12;
import static com.centerm.jnbank.common.TransDataKey.iso_f13;
import static com.centerm.jnbank.common.TransDataKey.iso_f14;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f23;
import static com.centerm.jnbank.common.TransDataKey.iso_f25;
import static com.centerm.jnbank.common.TransDataKey.iso_f26;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f3;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f38;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;
import static com.centerm.jnbank.common.TransDataKey.iso_f48;
import static com.centerm.jnbank.common.TransDataKey.iso_f49;
import static com.centerm.jnbank.common.TransDataKey.iso_f52;
import static com.centerm.jnbank.common.TransDataKey.iso_f53;
import static com.centerm.jnbank.common.TransDataKey.iso_f59;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f60_origin;
import static com.centerm.jnbank.common.TransDataKey.iso_f61;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;
import static com.centerm.jnbank.common.TransDataKey.iso_f63;
import static com.centerm.jnbank.common.TransDataKey.iso_f64;
import static com.centerm.jnbank.common.TransDataKey.keyFlagFallback;
import static com.centerm.jnbank.common.TransDataKey.keyFlagNoPin;
import static com.centerm.jnbank.common.TransDataKey.key_batch_upload_count;
import static com.centerm.jnbank.common.TransDataKey.key_is_amount_ok;
import static com.centerm.jnbank.common.TransDataKey.key_oriAuthCode;
import static com.centerm.jnbank.common.TransDataKey.key_oriReference;
import static com.centerm.jnbank.common.TransDataKey.key_oriTransTime;
import static com.centerm.jnbank.common.TransDataKey.key_trans_code;

/**
 * author:wanliang527</br>
 * date:2016/10/28</br>
 */

public class ShengPayMsgFactory extends BaseFactory {

    private final static String MCT_FILE_PATH = "msg/mapping/SHENGPAY";
    private static Logger logger = Logger.getLogger(ShengPayMsgFactory.class);
    private static double debitAmount;//借记金额
    private static int debitCount;//借记笔数
    private static double creditAmount;//贷记金额
    private static int creditCount;//贷记金额
    private String isAmountOk;
    private static List<TradeInfo> jiejiList;
    private static List<TradeInfo> daijiList;
    private Context context;
    private boolean isNoPinAndSign = false;
    private CommonManager commonManager;
    private String iso62_21;

    @Override
    public byte[] pack(Context context, String transCode, Map<String, String> data) {
        this.context = context;
        commonManager = new CommonManager(TradeInfo.class, context);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHhmmss");
        data.put(TransDataKey.KEY_TRANS_TIME, formatter.format(new Date()));//终端交易时间
        data.put("headerdata", getHeaderData("0"));//TPDU+报文头
        isNoPinAndSign = "true".equals(data.get(TransDataKey.KEY_QPS_FLAG)) ? true : false;
        if (INIT_TERMINAL.equals(transCode)) {
            //初始化机具
            String posParams = "SI=21|SN=" + getTerminalSn(context) + "|TP=" + Build.MODEL + "|CT=" + getNetType(context);
            data.put(iso_f47, posParams);
        } else if (LOAD_PARAM.equals(transCode)) {
            //下载参数
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f60, getIso60(context, transCode, false));//60域
        } else if (DOWNLOAD_CAPK.equals(transCode)) {
            //下载IC卡公钥参数
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            //IC卡公钥下载370，下载结束371，信息查询372
            data.put(iso_f60, getIso60(context, transCode, false));//60域
//            data.put(iso_f62, "100");//62域
        } else if (DOWNLOAD_AID.equals(transCode)) {
            //下载AID参数
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            //IC卡参数下载380，下载结束381，信息查询382
            data.put(iso_f60, getIso60(context, transCode, false));//60域
//            data.put(iso_f62, "100");//62域
        } else if (OBTAIN_TMK.equals(transCode)) {
            //下载主密钥
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f47, "SI=21|SN=" + getTerminalSn(context) + "|TP=" + Build.MODEL + "|CT=" + getNetType(context));//上送厂商标识、POS终端型号、终端序列号、通讯方式
//            data.put(iso_f60, getIso60(context, transCode, false));//60域
            data.put(iso_f60, "3031313030303030303032333832");//60域
        } else if (SIGN_IN.equals(transCode)) {
            //签到
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
//            data.put(iso_f41, BusinessConfig.getInstance().getValue(context,BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
//            data.put(iso_f42, BusinessConfig.getInstance().getValue(context,BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
//            data.put(iso_f47,"RV="+PackageUtils.getInstalledVersionName(context,context.getPackageName())+"|TV="+BusinessConfig.getInstance().getParamVersion(context));//上送程序版本号和参数版本号
            data.put(iso_f60, getIso60(context, transCode, false));//60域
            String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
            logger.info("当前操作员：" + operId);
            data.put(iso_f63, operId);//63域
//            data.put(iso_f11, "000309");//POS终端流水号，11域
//            data.put(iso_f41, "55280013");//41域
//            data.put(iso_f42, "960304257210001");//42域
////            data.put(iso_f47,"RV="+PackageUtils.getInstalledVersionName(context,context.getPackageName())+"|TV="+BusinessConfig.getInstance().getParamVersion(context));//上送程序版本号和参数版本号
//            data.put(iso_f60, "01100000003");//60域
//            String operId =BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
//            logger.info("当前操作员：" + operId);
//            data.put(iso_f63, "003");//63域
        } else if (SIGN_OUT.equals(transCode)) {
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f60, getIso60(context, transCode, false));//60域
        } else if (BALANCE.equals(transCode) || SALE.equals(transCode) || TRANSFER.equals(transCode)) {
            //余额、消费
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
//            data.put(iso_f23,"001");
            data.put(iso_f25, "00");//服务点条件码，25域
            //交易中不包含PIN 在22域第三位
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
//            data.put(iso_f47,getSSBI(context));//上送sim卡信息
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
//            boolean noPinFlag = "1".equals(data.get(keyFlagNoPin));
            boolean hasPin = !TextUtils.isEmpty(data.get(iso_f52));
            if (hasPin) {
                data.put(iso_f26, "6");//服务点PIN获取码，26域，PIN最大字符数目
            }
            data.put(iso_f53, getIso53(context, hasPin, BusinessConfig
                    .FLAG_ENCRYPT_TRACK_DATA));//53域
            String fallback = data.get(keyFlagFallback);
//            data.put(iso_f59, getIso59(context,data.get(iso_f2_result)));
            if (SALE.equals(transCode)) {
//                data.put(key_trans_code, "S");
                data.put(iso_f3, "000000");//交易处理码，3域
                String cardNum = data.get(iso_f2);
                String[] jncardbin = BusinessConfig.JNCARDBIN;//江南银行card bin信息
                List<String> list = Arrays.asList(jncardbin);
                if (cardNum != null) {
                    String cardNo1 = cardNum.substring(0, 6);
                    logger.info("卡号前6位为:" + cardNo1);
                    if (list.contains(cardNo1)) {
                        //该卡是江南银行行方卡
                        String money = data.get(iso_f4);
                        if (money.length() < 11) {
                            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));
                        } else {
                            data.put(iso_f4, data.get(iso_f4));//金额，4域
                        }
                    } else {
                        data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
                    }
                }
                String iso22 = data.get(iso_f22);
                if (hasPin) {
                    data.put(iso_f22, iso22.substring(0, 2) + "1");
                } else {
                    data.put(iso_f22, iso22.substring(0, 2) + "2");
                }
                //data.put(iso_f48, "0000");
//                data.put(iso_f62,iso_field_62.getIso62(context,"605424",true,false,true));
            } else if (BALANCE.equals(transCode)) {
                data.put(iso_f3, "310000");//交易处理码，3域
            } else if (TRANSFER.equals(transCode)) {
                data.put(iso_f3, "400000");//交易处理码，3域
                data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            }
//            data.put(iso_f60, "22000004000601");//60域
            data.put(iso_f60, getIso60(context, transCode, "1".equals(fallback)));//60域
//            data.put(iso_f62,"422020202020202020202020202020204132303630303430323032303531363030303030313032303434303131393930363036303030303337303730384634334532433344303830383331313330352020");//62域
//                data.put(iso_f62,"B000000000000000A0000000000000000000000000000000000000000000000000000000000000000");
            String useIntegral = BusinessConfig.getInstance().getParam(context, BusinessConfig.USE_INTEGRAL);
            iso62_21 = Iso_21_62.getIso62_21(context, data.get(iso_f2));
            if (TextUtils.isEmpty(iso62_21)) {
                data.put(iso_f62, "B000000000000000A0000000000000000000000000000000000000000000000000000000000000000");
            } else {
                if (TextUtils.isEmpty(useIntegral) || useIntegral.equals("B")) {
                    data.put(iso_f62, "B" + "000000000000000" + iso62_21);
                } else if (useIntegral.equals("A")) {
                    data.put(iso_f62, "A" + BusinessConfig.getInstance().getParam(context, BusinessConfig.INTEGRAL_BALANCE) + iso62_21);
                }
            }
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (VOID.equals(transCode)) {
            //消费撤销
            data.put(iso_f3, "200000");
//            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            String iso_f22Str = data.get(iso_f22);
            if (iso_f22Str != null && (("01".equals(iso_f22Str) || "02".equals(iso_f22Str) || "03".equals(iso_f22Str) || "05".equals(iso_f22Str)) || "07".equals(iso_f22Str))) {
                logger.debug("消费撤销时iso_f22为：" + iso_f22Str);
                iso_f22Str = iso_f22Str + "2";
                data.put(iso_f22, iso_f22Str);
            }
            data.put(iso_f25, "00");
            data.put(iso_f37, data.get(iso_f37));
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
//            data.put(iso_f38, data.get(key_oriAuthCode));
//            data.put(iso_f47,getSSBI(context));//上送sim卡信息
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
            boolean hasPin = !TextUtils.isEmpty(data.get(iso_f52));
            if (hasPin) {
                data.put(iso_f26, "6");//服务点PIN获取码
            }
            data.put(iso_f53, getIso53(context, hasPin, BusinessConfig
                    .FLAG_ENCRYPT_TRACK_DATA));//53域
//            data.put(iso_f59, getIso59(context,data.get(iso_f2_result)));
            data.put(iso_f60, getIso60(context, transCode, false));
            data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get
                    (iso_f11_origin));//原批次号+原流水号
            data.put(iso_f62, "B               A20600402020516000001020440119906060000370708F43E2C3D0808311305  ");
            data.put(iso_f64, "1234567890123456");//mac填充占位
            data.put(key_trans_code, "V");
        } else if (SALE_REVERSE.equals(transCode)) {
            //消费冲正
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            if (TextUtils.isEmpty(data.get(iso_f39))) {
                data.put(iso_f39, "06");//冲正原因
            }
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (VOID_REVERSE.equals(transCode)) {
            //消费撤销冲正
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            if (TextUtils.isEmpty(data.get(iso_f39))) {
                data.put(iso_f39, "06");//冲正原因
            }
            data.put(iso_f61, data.get(iso_f61));//原批次号+原流水号
            data.put(iso_f62, data.get(iso_f62));
        } else if (AUTH_REVERSE.equals(transCode)) {
            //预授权冲正
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            if (TextUtils.isEmpty(data.get(iso_f39))) {
                data.put(iso_f39, "06");//冲正原因
            }
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (CANCEL_REVERSE.equals(transCode)) {
            //预授权撤销冲正
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            if (TextUtils.isEmpty(data.get(iso_f39))) {
                data.put(iso_f39, "06");//冲正原因
            }
            //data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get(iso_f11_origin));//原批次号+原流水号
            data.put(iso_f61, data.get(iso_f61));//原批次号+原流水号
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (AUTH_COMPLETE_REVERSE.equals(transCode)) {
            //预授权完成请求冲正
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            if (TextUtils.isEmpty(data.get(iso_f39))) {
                data.put(iso_f39, "06");//冲正原因
            }
            //data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get(iso_f11_origin) );//原批次号+原流水号+原交易日期
            data.put(iso_f61, data.get(iso_f61));//原批次号+原流水号
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (COMPLETE_VOID_REVERSE.equals(transCode)) {
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            if (TextUtils.isEmpty(data.get(iso_f39))) {
                data.put(iso_f39, "06");//冲正原因
            }
            data.put(iso_f61, data.get(iso_f61));//原批次号+原流水号
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (AUTH.equals(transCode)) {
            //预授权
            data.put(iso_f3, "030000");//交易处理码，3域
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
//            data.put(iso_f11, "000001");//POS终端流水号，11域

            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            boolean hasPin = !TextUtils.isEmpty(data.get(iso_f52));
            String iso22 = data.get(iso_f22);
            if (hasPin) {
                data.put(iso_f22, iso22.substring(0, 2) + "1");
                data.put(iso_f26, "12");//服务点PIN获取码，26域，PIN最大字符数目
            } else {
                data.put(iso_f22, iso22.substring(0, 2) + "2");
            }
            data.put(iso_f25, "06");//服务点条件码，25域
            data.put(iso_f26, "12");//服务点PIN获取码，26域，PIN最大字符数目
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
//            data.put(iso_f47,getSSBI(context));//上送sim卡信息
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
            String iso53 = "1".equals(data.get(keyFlagNoPin)) ? "0600000000000000" : "2600000000000000";
            data.put(iso_f53, iso53);//53域
            //需要打印凭条的交易，要上送签购单打印版本号
            String slipVersion = Settings.getSlipVersion(context);
            if (TextUtils.isEmpty(slipVersion)) {
                slipVersion = "000000000000";
            } else if (slipVersion.length() > 12) {
                slipVersion = slipVersion.substring(0, 12);
            }
            if ("true".equals(data.get(KEY_QPS_FLAG))) {
                //小额免密标识
                StringBuilder sBuilder = new StringBuilder();
                sBuilder.append(slipVersion);
                for (int i = 0; i < 36 - slipVersion.length(); i++) {
                    sBuilder.append("0");
                }
                sBuilder.append("1");
                slipVersion = sBuilder.toString();
            }
//            data.put(iso_f59, getIso59(context,data.get(iso_f2_result)));
            String fallback = data.get(keyFlagFallback);
            data.put(iso_f60, getIso60(context, transCode, false));//60域
            data.put(iso_f62, "A               A20600402020516000001020440119906060000370708F43E2C3D0808311305  ");
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (REFUND.equals(transCode)) {
            data.put(key_trans_code, "R");
            data.put(iso_f3, "200000");
            data.put(iso_f22, data.get(iso_f22) + "2");
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f25, "00");//服务点条件码，25域
            data.put(iso_f37, data.get(key_oriReference));//服务点条件码，25域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为15
            String fallback = data.get(keyFlagFallback);
            data.put(iso_f60, getIso60(context, transCode, "1".equals(fallback)));
            if (null == data.get(iso_f60_origin)) {
                data.put(iso_f61, "000000" + "000000" + data.get(key_oriTransTime));//原批次+原流水+原交易时间
            } else {
                data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get(iso_f11_origin) + data.get(key_oriTransTime));//原批次+原流水+原交易时间
            }
            data.put(iso_f62, "B               A20600402020516000001020440119906060000370708F43E2C3D0808311305  ");
            data.put(iso_f63, "000");
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (CANCEL.equals(transCode)) {
            //预授权撤销
            data.put(iso_f3, "200000");//交易处理码，3域
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
//            data.put(iso_f22, data.get(iso_f22));//服务点输入方式码，22域   不确定上送什么值要确认下
            String iso_f22Str = data.get(iso_f22);
            if (iso_f22Str != null && (("01".equals(iso_f22Str) || "02".equals(iso_f22Str) || "03".equals(iso_f22Str) || "05".equals(iso_f22Str)) || "07".equals(iso_f22Str))) {
                logger.debug("预授权撤销时iso_f22为：" + iso_f22Str);
                iso_f22Str = iso_f22Str + "2";
                data.put(iso_f22, iso_f22Str);
            }
            data.put(iso_f25, "06");//服务点条件码，25域
            data.put(iso_f38, data.get(key_oriAuthCode));
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
//            data.put(iso_f47,getSSBI(context));//上送sim卡信息
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
            boolean hasPin = !TextUtils.isEmpty(data.get(iso_f52));
            if (hasPin) {
                data.put(iso_f26, "6");//服务点PIN获取码，26域，PIN最大字符数目
            }
//            data.put(iso_f53, getIso53(context, hasPin, BusinessConfig
//                    .FLAG_ENCRYPT_TRACK_DATA));//53域
            String fallback = data.get(keyFlagFallback);
            data.put(iso_f60, getIso60(context, transCode, false));//60域
            data.put(iso_f61, "000000000000" + data.get(key_oriTransTime));//原批次号+原流水号+原交易日期
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (AUTH_COMPLETE.equals(transCode)) {
            data.put(iso_f3, "000000");//交易处理码，3域
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f22, "051");//服务点输入方式码，22域   不确定上送什么值要确认下
           /* String iso_f22Str = data.get(iso_f22);
            if (iso_f22Str != null && (("01".equals(iso_f22Str) || "02".equals(iso_f22Str) || "03".equals(iso_f22Str) || "05".equals(iso_f22Str))|| "07".equals(iso_f22Str))) {
                logger.debug("预授权完成请求时iso_f22为："+iso_f22Str);
                iso_f22Str = iso_f22Str + "2";
                data.put(iso_f22, iso_f22Str);
            }*/
            data.put(iso_f25, "06");//服务点条件码，25域
            data.put(iso_f38, data.get(key_oriAuthCode));
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
           /* boolean hasPin = !TextUtils.isEmpty(data.get(iso_f52));
            data.put(iso_f53, getIso53(context, hasPin, BusinessConfig
                    .FLAG_ENCRYPT_TRACK_DATA));//53域
            data.put(iso_f59, getIso59(context,data.get(iso_f2_result)));
            String fallback = data.get(keyFlagFallback);*/
            data.put(iso_f60, getIso60(context, transCode, false));//60域
            data.put(iso_f61, "000000000000" + data.get(key_oriTransTime));//原批次号+原流水号+原交易日期
            data.put(iso_f62, "A               Y20600402020516000001020440119906060000370708F43E2C3D0808311305  ");
            data.put(iso_f64, "1234567890123456");//mac填充占位
//            data.put(key_trans_code, "P");
        } else if (AUTH_SETTLEMENT.equals(transCode)) {
            data.put(iso_f3, "000000");//交易处理码，3域
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
//            data.put(iso_f22, data.get(iso_f22) + "2");//服务点输入方式码，22域   不确定上送什么值要确认下
            String iso_f22Str = data.get(iso_f22);
            if (iso_f22Str != null && (("01".equals(iso_f22Str) || "02".equals(iso_f22Str) || "03".equals(iso_f22Str) || "05".equals(iso_f22Str)) || "07".equals(iso_f22Str))) {
                logger.debug("消费撤销时iso_f22为：" + iso_f22Str);
                iso_f22Str = iso_f22Str + "2";
                data.put(iso_f22, iso_f22Str);
            }
            data.put(iso_f25, "06");//服务点条件码，25域
            data.put(iso_f38, data.get(key_oriAuthCode));
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f47, getSSBI(context));//上送sim卡信息
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156F
            data.put(iso_f59, getIso59(context, data.get(iso_f2_result)));
            String fallback = data.get(keyFlagFallback);
            data.put(iso_f60, getIso60(context, transCode, "1".equals(fallback)));//60域
            data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get(iso_f11_origin) + data.get(key_oriTransTime));//原批次号+原流水号
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (COMPLETE_VOID.equals(transCode)) {
            //预授权完成 撤销
            data.put(iso_f3, "200000");//交易处理码，3域
            data.put(iso_f4, data.get(iso_f4));//金额，4域
//            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            data.put(iso_f11, data.get(TransDataKey.key_voucherNo));//POS终端流水号，11域,凭证号
            data.put(iso_f14, data.get(data.get(iso_f14)));//POS终端流水号，11域,凭证号
            String iso_f22Str = data.get(iso_f22);
            /*if (iso_f22Str != null && (("01".equals(iso_f22Str) || "02".equals(iso_f22Str) || "03".equals(iso_f22Str) || "05".equals(iso_f22Str))|| "07".equals(iso_f22Str))) {
                logger.debug("预授权完成撤销时iso_f22为："+iso_f22Str);
                iso_f22Str = iso_f22Str + "2";
                data.put(iso_f22, iso_f22Str);
            }*/

            data.put(iso_f22, iso_f22Str);//刷卡时填022，不刷卡时填012
            data.put(iso_f23, data.get(iso_f23));
            data.put(iso_f25, "06");//服务点条件码，25域
//            data.put(iso_f35, "");//2磁道数据
            data.put(iso_f37, data.get(key_oriReference));//检索参考号--请求时同原始预授权完成交易
            data.put(iso_f38, data.get(key_oriAuthCode));//授权标识应答码
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
            boolean hasPin = !TextUtils.isEmpty(data.get(iso_f52));
            if (hasPin) {
                data.put(iso_f26, "12");//服务点PIN获取码
            }
            data.put(iso_f53, getIso53(context, hasPin, BusinessConfig
                    .FLAG_ENCRYPT_TRACK_DATA));//53域
            String fallback = data.get(keyFlagFallback);
//            data.put(iso_f59, getIso59(context,data.get(iso_f2_result)));
            data.put(iso_f60, getIso60(context, transCode, false));//60域
            data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get(iso_f11_origin) + data.get(key_oriTransTime));//原批次号+原流水号
            data.put(iso_f64, "1234567890123456");//mac填充占位
//            data.put(key_trans_code, "V(P)");
        } else if (SETTLEMENT.equals(transCode)) {//批结算
            initAmountAndCount(context);
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f48, DataHelper.formatAmount(debitAmount) + DataHelper.formatToXLen(debitCount, 3) + DataHelper.formatAmount(creditAmount) + DataHelper.formatToXLen(creditCount, 3) + "0");
//            data.put(iso_f48, "00000088000909700000000000300300000000000000000000000000000000");//结算总额
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
            data.put(iso_f60, getIso60(context, transCode, false));//60域
            String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
            logger.info("当前操作员：" + operId);
            data.put(iso_f63, operId);//63域
        } else if (SETTLEMENT_DONE.equals(transCode)) {//批上送
            isAmountOk = data.get(key_is_amount_ok);
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            if ("1".equals(isAmountOk)) {
                //批上送结束
                data.put(iso_f48, DataHelper.formatToXLen(data.get(key_batch_upload_count), 4));
            }
            data.put(iso_f60, getIso60(context, transCode, false));//60域
        } else if (POS_STATUS_UPLOAD.equals(transCode)) {
            //POS终端状态上送，为了下载终端参数，因此需要先获取下载参数的类型
            //下载AID参数
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            //IC卡参数下载380，下载结束381，信息查询382
            String paramsType = data.get(KEY_PARAMS_TYPE);
            String i60 = "00" + "000001";
            if ("1".equals(paramsType) || "3".equals(paramsType)) {
                i60 += "372";
            } else if ("2".equals(paramsType)) {
                i60 += "382";
            }
            data.put(iso_f60, i60);//60域
            data.put(iso_f62, "100");//62域
        } else if (DOWNLOAD_QPS_PARAMS.equals(transCode)) {
            //下载非接参数
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            String iso60 = "00" + BusinessConfig.getInstance().getBatchNo(context);
            //非接参数下载，394开始下载，395下载结束
            iso60 += "394";
            data.put(iso_f60, iso60);
        } else if (DOWNLOAD_CARD_BIN.equals(transCode)) {
            //下载卡BIN信息下载（非卡BIN黑名单）
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            String iso60 = "00" + BusinessConfig.getInstance().getBatchNo(context);
            //960开始下载，961下载结束
            iso60 += "960";
            data.put(iso_f60, iso60);

            String iso62 = data.get(iso_f62);
            data.put(iso_f62, DataHelper.fillRightSpace(iso62, 5));
        } else if (DOWNLOAD_CARD_BIN_QPS.equals(transCode)) {
            //下载卡BIN信息下载（非卡BIN黑名单）
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            String iso60 = "00" + BusinessConfig.getInstance().getBatchNo(context);
            //960开始下载，961下载结束
            iso60 += "396";
            data.put(iso_f60, iso60);

            String iso62 = data.get(iso_f62);
            data.put(iso_f62, DataHelper.fillRightSpace(iso62, 3));
        } else if (DOWNLOAD_BLACK_CARD_BIN_QPS.equals(transCode)) {
            //下载卡BIN信息下载（非卡BIN黑名单）
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            String iso60 = "00" + BusinessConfig.getInstance().getBatchNo(context);
            //960开始下载，961下载结束
            iso60 += "398";
            data.put(iso_f60, iso60);

            String iso62 = data.get(iso_f62);
            data.put(iso_f62, DataHelper.fillRightSpace(iso62, 3));
        } else if (DOWNLOAD_PARAMS_FINISHED.equals(transCode)) {
            //下载参数结束通知
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            String paramsType = data.get(KEY_PARAMS_TYPE);
            String i60 = "00" + "000001";
            if ("1".equals(paramsType) || "3".equals(paramsType)) {
                i60 += "371";
            } else if ("2".equals(paramsType)) {
                i60 += "381";
            }
            data.put(iso_f60, i60);
        } else if (UPLOAD_SCRIPT_RESULT.equals(transCode)) {
            //IC卡脚本结果上送
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            String origin60 = data.get(iso_f60_origin);
            data.put(iso_f60, "00" + BusinessConfig.getInstance().getBatchNo(context) + "951"
                    + origin60.substring(11, 12) + origin60.substring(12, 13));
            data.put(iso_f61, origin60.substring(2, 8) + data.get(iso_f11_origin) + data.get
                    (iso_f13));//原批次号+原流水号+原交易日期
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (TransCode.SCAN_PAY_ALI.equals(transCode) || TransCode.SCAN_PAY_WEI.equals(transCode)
                || TransCode.SCAN_PAY_SFT.equals(transCode)) {

            //扫码支付
            data.put(iso_f3, "450000");//交易处理码，3域
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f22, "001");//服务点输入方式码
            data.put(iso_f25, "01");
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
            data.put(iso_f60, "22" + "000001");//60域
            data.put(iso_f62, data.get(TransDataKey.iso_f62));
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (SCAN_CANCEL.equals(transCode)) {
            //扫码撤销
            data.put(iso_f3, "680000");//交易处理码，3域
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f22, data.get(iso_f22));
            data.put(iso_f25, "00");
            data.put(iso_f38, data.get(key_oriAuthCode));
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
            data.put(iso_f60, "23" + BusinessConfig.getInstance().getBatchNo(context));//60域
            data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get(iso_f11_origin));//原批次号+原流水号
            data.put(iso_f64, "1234567890123456");//mac填充占位
            data.put(key_trans_code, "V(C)");
        } else if (SCAN_SERCH.equals(transCode)) {
            //扫码结果查询
            data.put(iso_f3, "670000");//交易处理码
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f25, "00");
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f60, "01" + BusinessConfig.getInstance().getBatchNo(context) + "000");//60域
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (SCAN_LAST_SERCH.equals(transCode)) {
            //扫码结果查询
            data.put(iso_f3, "690000");//交易处理码
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f25, "00");
            getScanLast(context, data);
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f60, "01" + BusinessConfig.getInstance().getBatchNo(context) + "000");//60域
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (ELC_SIGNATURE.equals(transCode)) {
            data.put(iso_f11, data.get(iso_f11));//POS终端流水号，11域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (SCAN_REFUND_W.equals(transCode) || SCAN_REFUND_Z.equals(transCode) || SCAN_REFUND_S.equals(transCode)) {
            //扫码退货
            switch (transCode) {
                case TransCode.SCAN_REFUND_W:
                    data.put(key_trans_code, "R(W)");
                    data.put(iso_f47, "TXNWAY=" + "TX01");
                    break;
                case TransCode.SCAN_REFUND_Z:
                    data.put(key_trans_code, "R(Z)");
                    data.put(iso_f47, "TXNWAY=" + "ZFB01");
                    break;
                case TransCode.SCAN_REFUND_S:
                    data.put(key_trans_code, "R(S)");
                    data.put(iso_f47, "TXNWAY=" + "SFT01");
                    break;
            }
            data.put(iso_f3, "200000");
            data.put(iso_f22, "032");
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));//金额，4域
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f25, "00");//服务点条件码，25域
            data.put(iso_f37, data.get(key_oriReference));//服务点条件码，25域
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f49, "156");//交易货币代码，49域，人民币的货币代码为156
//            data.put(iso_f59, Settings.getSlipVersion(context));//59域
            String fallback = data.get(keyFlagFallback);
            data.put(iso_f60, getIso60(context, transCode, "1".equals(fallback)));
            if (null == data.get(iso_f60_origin) || null == data.get(iso_f11_origin)) {
                data.put(iso_f61, "000000" + "000000" + data.get(key_oriTransTime));//原批次+原流水+原交易时间
            } else {
                data.put(iso_f61, data.get(iso_f60_origin).substring(2, 8) + data.get(iso_f11_origin) + data.get(key_oriTransTime));//原批次+原流水+原交易时间
            }
            data.put(iso_f63, "000");
            data.put(iso_f64, "1234567890123456");//mac填充占位
        } else if (DISCOUNT_INTERGRAL.equals(transCode)) {
            //工会卡积分和折扣查询
            data.put(iso_f3, "320000");
            data.put(iso_f4, DataHelper.formatAmount(Double.valueOf(data.get(iso_f4))));
            data.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
            data.put(iso_f22, "052");
            data.put(iso_f23, "001");
            data.put(iso_f25, "00");
            data.put(iso_f41, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD));//41域
            data.put(iso_f42, BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD));//42域
            data.put(iso_f49, "156");
            data.put(iso_f60, getIso60(context, transCode, false));
            data.put(iso_f64, "1234567890123456");//mac填充占位
        }
//        else if (ECHO_TEST.equals(transCode)) {
//            //回响测试
//            data.put(iso_f41, BusinessConfig.getInstance().getIsoField(context, 41));//终端号
//            data.put(iso_f42, BusinessConfig.getInstance().getIsoField(context, 42));//商户号
//            data.put(iso_f60,getIso60(context,))
//        }
        logger.warn("交易类型：" + transCode + "==>ISO_F22：" + data.get(iso_f22));
        try {
            return addMessageLen(mapToIso8583(context, MCT_FILE_PATH, transCode, data));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, String> unPack(Context context, String transCode, Object oriMsg) {
        return iso8583ToMap(context, MCT_FILE_PATH, transCode, removeMessaageLen((byte[])
                oriMsg));
    }

    private String getHeaderData(String dealReq) {
        String status = "0";
        String encryptFlag = "1";
        String keyIndex = "0";
        BusinessConfig config = BusinessConfig.getInstance();
//        return config.getParam(context, BusinessConfig.Key.PARAM_TPDU)
//                + BusinessConfig.HEADER_APP_TYPE//应用类型
//                + BusinessConfig.HEADER_APP_VERSION //软件版本号
//                + status //终端状态
//                + dealReq //处理要求
//                + encryptFlag//加密标志
//                + keyIndex//密钥索引
//                + BusinessConfig.HEADER_APP_RESERVE;//保留位
        return "6000090000603100311305";
    }


    /**
     * 组合53域数据（安全控制信息）
     *
     * @param context          Context
     * @param hasPin           是否有PIN（对应PIN加密方法）
     * @param trackEncryptFlag 磁道加密标志
     * @return 组合户的53域数据
     */
    private String getIso53(Context context, boolean hasPin, boolean trackEncryptFlag) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hasPin ? "2" : "0");
        stringBuilder.append("6");
        stringBuilder.append(trackEncryptFlag ? "1" : "0");
        for (int i = 0; i < 13; i++) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }

    //21号改造内容
    private String getIso59(Context context, String isoF2) {
        //59域DATA
        boolean canupLoadF59 = true;
        byte[] bTerminalHardwareSN = null;
        String random = null;
        try {
            bTerminalHardwareSN = CommonUtils.getTerminalHardwareSn();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("59域获取终端序列号为：" + bTerminalHardwareSN);
        if (bTerminalHardwareSN == null) {
            canupLoadF59 = false;
        }
        if (!StringUtils.isStrNull(isoF2) && isoF2.length() > 6) {
            random = isoF2.substring(isoF2.length() - 6, isoF2.length());
        }
        byte[] terminalHardwareSNEncrypt = null;
        if (canupLoadF59) {
            try {
                terminalHardwareSNEncrypt = CommonUtils.getSNK(HexUtils.bytesToHexString(bTerminalHardwareSN)
                        , random);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.debug("59域获取加密mac为：" + terminalHardwareSNEncrypt);
        if (terminalHardwareSNEncrypt == null) {
            canupLoadF59 = false;
        }
        String buildTime = CommonUtils.getBuildTime(context);
        String isoString = "04";
        String isoString4 = null;
        if (null != buildTime) {
            isoString4 = buildTime.replace("-", "");
        }

        try {
            int iLen;
            if (!canupLoadF59) {
                //如果获取SN号转换后为空，判为存量终端则02上送SN号
                try {
                    String isoString1 = DeviceFactory.getInstance().getSystemDev().getTerminalSn();
                    iLen = ("01" + DataHelper.formatToXLen(isoString.length(), 3) + isoString
                            + "02" + DataHelper.formatToXLen(isoString1.length(), 3) + isoString1
                            + "05" + DataHelper.formatToXLen(isoString4.length(), 3) + isoString4).length();
                    return "A2" + DataHelper.formatToXLen(iLen, 3)
                            + "01" + DataHelper.formatToXLen(isoString.length(), 3) + isoString
                            + "02" + DataHelper.formatToXLen(isoString1.length(), 3) + isoString1
                            + "05" + DataHelper.formatToXLen(isoString4.length(), 3) + isoString4;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String isoString1 = new String(bTerminalHardwareSN, "ISO-8859-1");
                String isoString2 = isoF2.substring(isoF2.length() - 6, isoF2.length());
                String isoString3 = new String(terminalHardwareSNEncrypt, "ISO-8859-1");

                if (isoString3.length() > 8) {
                    isoString3 = isoString3.substring(0, 8);
                }

                iLen = ("01" + DataHelper.formatToXLen(isoString.length(), 3) + isoString
                        + "02" + DataHelper.formatToXLen(isoString1.length(), 3) + isoString1
                        + "03" + DataHelper.formatToXLen(isoString2.length(), 3) + isoString2
                        + "04" + DataHelper.formatToXLen(isoString3.length(), 3) + isoString3
                        + "05" + DataHelper.formatToXLen(isoString4.length(), 3) + isoString4).length();
                return "A2" + DataHelper.formatToXLen(iLen, 3)
                        + "01" + DataHelper.formatToXLen(isoString.length(), 3) + isoString
                        + "02" + DataHelper.formatToXLen(isoString1.length(), 3) + isoString1
                        + "03" + DataHelper.formatToXLen(isoString2.length(), 3) + isoString2
                        + "04" + DataHelper.formatToXLen(isoString3.length(), 3) + isoString3
                        + "05" + DataHelper.formatToXLen(isoString4.length(), 3) + isoString4;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 组合60域的数据
     *
     * @param context Context
     * @return 60域数据
     */
    private String getIso60(Context context, String transCode, boolean isfallback) {
        //1-消息类型码：00管理类交易；01查询；03积分查询；10预授权/冲正；11预授权撤销/冲正；20预授权完成（请求）/冲正；21预授权完成撤销/冲正；22消费/冲正；23
        // 消费撤销/冲正；24预授权完成（通知）；99获取主密钥
        //2-批次号：xxxxxx
        //3-网络信息管理码：000默认值（非网络管理类交易）；001单倍长密钥；003双倍长密钥；004双倍长密钥（包含磁道密钥）
        //4-终端读取能力：0不可预知；2磁条卡；5磁条卡+接触式；6磁条卡+接触式+非接触式
        //5-IC卡条件代码：0默认；1代表IC卡进行刷卡的不规范操作；2代表降级交易
        String iso60_1 = null, iso60_2 = null, tmp_iso60_2, iso60_3 = null, iso60_4 = null, iso60_5 = null, iso60_6 =
                null, iso60_7 = null;
        tmp_iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
        iso60_2 = tmp_iso60_2;
  /*      switch (transCode) {
            case OBTAIN_TMK:
                iso60_1 = "99";
                if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
                    iso60_3 = "004";
                } else {
                    iso60_3 = "003";
                }
                break;
            case SIGN_IN:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
                    iso60_3 = "004";
                } else {
                    iso60_3 = "003";
                }
                break;
            case SIGN_OUT:
                iso60_1 = "00";
                iso60_3 = "002";
                break;
            case BALANCE:
                iso60_1 = "01";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case SALE:
                iso60_1 = "22";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case VOID:
                iso60_1 = "23";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case REFUND:
                //// TODO: 2016/11/6 IC卡脱机交易退货填27，其它交易退货填25
                iso60_1 = "25";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case AUTH:
                iso60_1 = "10";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case CANCEL:
                iso60_1 = "11";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case AUTH_COMPLETE:
                iso60_1 = "20";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case AUTH_SETTLEMENT:
                iso60_1 = "24";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case COMPLETE_VOID:
                iso60_1 = "21";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case SETTLEMENT:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "201";
                break;
            case SETTLEMENT_DONE:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                if ("1".equals(isAmountOk)) {
                    iso60_3 = "207";
                } else {
                    iso60_3 = "206";
                }
                break;
        }
*/

        switch (transCode) {
            case OBTAIN_TMK:
                iso60_1 = "00";
                iso60_3 = "380";
                break;
            case LOAD_PARAM:
                iso60_1 = "00";
                iso60_2 = tmp_iso60_2;
                iso60_3 = "360";
                break;

            case DOWNLOAD_CAPK:
                iso60_1 = "00";
                iso60_2 = tmp_iso60_2;
                iso60_3 = "370";
                break;
            case DOWNLOAD_AID:
                iso60_1 = "00";
                iso60_2 = tmp_iso60_2;
                iso60_3 = "380";
                break;

            case POS_STATUS_UPLOAD:

                iso60_1 = "00";
                iso60_2 = "000001";
                iso60_3 = "382";
                break;

            case SIGN_IN:
                iso60_1 = "00";
                iso60_2 = tmp_iso60_2;
                iso60_3 = "003";
//                if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
//                    iso60_3 = "004";
//                } else {
//                    iso60_3 = "003";
//                }
                break;
            case SIGN_OUT:
                iso60_1 = "00";
                iso60_3 = "002";
                break;
            case BALANCE:
                iso60_1 = "01";
                iso60_2 = "000001";
                break;
            case SALE:
                iso60_1 = "22";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                if (isNoPinAndSign) {
                    iso60_6 = "9";
                }
                break;
            case TRANSFER:
                iso60_1 = "47";
                break;
            case VOID:
                iso60_1 = "23";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case REFUND:
                iso60_1 = "25";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case SCAN_REFUND_W:
            case SCAN_REFUND_Z:
            case SCAN_REFUND_S:
                //// TODO: 2016/11/6 IC卡脱机交易退货填27，其它交易退货填25
                iso60_1 = "25";
//                iso60_3 = "000";
//                iso60_4 = "6";
//                iso60_5 = isfallback ? "2" : "0";
                break;
            case AUTH:
                iso60_1 = "10";
                iso60_2 = tmp_iso60_2;
                break;
            case CANCEL:
                iso60_1 = "11";
                iso60_2 = tmp_iso60_2;
                break;
            case AUTH_COMPLETE:
                iso60_1 = "20";
                iso60_2 = tmp_iso60_2;
                break;
            case AUTH_SETTLEMENT:
                iso60_1 = "24";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case COMPLETE_VOID:
                iso60_1 = "21";
                iso60_2 = tmp_iso60_2;
                break;
            case SETTLEMENT:
                iso60_1 = "00";
                iso60_2 = tmp_iso60_2;
                iso60_3 = "201";
                break;
            case SETTLEMENT_DONE:
                iso60_1 = "00";
                iso60_2 = tmp_iso60_2;
                //201/202 若对账平则上送207结束通知
                if ("1".equals(isAmountOk)) {
                    iso60_3 = "207";
                } else {
                    iso60_3 = "201";
                }

                break;
            case DISCOUNT_INTERGRAL:
                iso60_1 = "22";
                iso60_2 = tmp_iso60_2;
                break;
        }

        StringBuilder builder = new StringBuilder();
        if (iso60_1 != null) {
            builder.append(iso60_1);
        }
        if (iso60_2 != null) {
            builder.append(iso60_2);
        }
        if (iso60_3 != null) {
            builder.append(iso60_3);
        }
        if (iso60_4 != null) {
            builder.append(iso60_4);
        }
        if (iso60_5 != null) {
            builder.append(iso60_5);
        }
        if (iso60_6 != null) {
            builder.append(iso60_6);
        }
        if (iso60_7 != null) {
            builder.append(iso60_7);
        }
        logger.info("IOS F60 ==> " + builder.toString());
        return builder.toString();
    }

    private String getIso62(Context context) {
        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            String tag = "Sequence No";
            String sn;
            if (Config.DEBUG_FLAG) {
                sn = Config.DEBUG_SN;
            } else {
                sn = service.getTerminalSn();
                logger.debug("获取到的终端sn号为：" + sn);
            }
            String value = BusinessConfig.NET_LISCENSE_NO + sn;
            int len = value.length();
            return tag + len + value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void initAmountAndCount(final Context context) {
        new AsyncQueryPrintDataTask(context) {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish(List<List<TradeInfo>> lists) {
                super.onFinish(lists);
                jiejiList = lists.get(0);
                daijiList = lists.get(1);
                sumCreditAmountAndCount();
                sumDebitAmountAndCount();
            }
        }.execute();

    }


    /**
     * 借记总金额及总笔数
     */
    public static void sumDebitAmountAndCount() {
        debitAmount = 0.0;
        debitCount = 0;
        if (jiejiList != null && jiejiList.size() > 0) {
            for (TradeInfo info :
                    jiejiList) {
                debitAmount += DataHelper.parseIsoF4(info.getIso_f4());
            }
            debitCount = jiejiList.size();
        } else {
            logger.debug("查询到成功的借记交易为空！");
        }
        debitAmount = DataHelper.formatDouble(debitAmount);
    }


    /**
     * 贷记总金额及总笔数
     */
    public static void sumCreditAmountAndCount() {
        creditAmount = 0.0;
        creditCount = 0;
        if (daijiList != null && daijiList.size() > 0) {
            for (TradeInfo info :
                    daijiList) {
                creditAmount += DataHelper.parseIsoF4(info.getIso_f4());
            }
            creditCount = daijiList.size();
        } else {
            logger.debug("查询到成功的借记交易为空！");
        }
        creditAmount = DataHelper.formatDouble(creditAmount);
    }

    //获取终端SN号
    private String getTerminalSn(Context context) {
        String sn = null;
        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            if (Config.DEBUG_FLAG) {
                sn = Config.DEBUG_SN;
            } else {
                sn = service.getTerminalSn();
                logger.debug("获取到的终端sn号为：" + sn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sn;
    }

    //add by fl at 20161130
    private String getNetType(Context context) {
        String netType = "2";
        try {
            if (CommonUtils.isWifi(context)) {
                netType = "4";
            }
            return netType;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netType;
    }

    //获取sim卡的msi和基站信息
    public static String getSSBI(Context context) {
        if (CommonUtils.isWifi(context)) {
            return null;
        }
        String ssbi = "";
        String location = "", MncMcc;
        //获取sim卡序列号TelephoneManager
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        CellLocation cel = manager.getCellLocation();
        //获取sim卡的序列卡号
        String simSerialNumber = manager.getSimSerialNumber();
        if (TextUtils.isEmpty(simSerialNumber)) {
            return null;
        }
        ssbi += "SS=" + simSerialNumber;
        String operator = manager.getNetworkOperator();
        logger.info("simSerialNumber = " + simSerialNumber);
        logger.info("operator = " + operator);
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        if (mnc > 9) {
            MncMcc = "" + mnc + "," + mcc;
        } else {
            MncMcc = "0" + mnc + "," + mcc;
        }

        logger.debug("MncMcc:" + MncMcc);
        //移动联通 GsmCellLocation
        if (cel instanceof GsmCellLocation) {
            logger.info("移动联通");
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cel;
            int nGSMCID = gsmCellLocation.getCid();
            if (nGSMCID > 0) {
                if (nGSMCID != 65535) {
                    logger.info("cell = " + nGSMCID);
                    logger.info("lac = " + gsmCellLocation.getLac());
                    logger.info("cell = " + Integer.toHexString(nGSMCID));
                    logger.info("lac = " + Integer.toHexString(gsmCellLocation.getLac()));
                    location = Integer.toHexString(nGSMCID) + "," + MncMcc + "," + Integer.toHexString(gsmCellLocation.getLac());
                    logger.info("location" + location);
                }
            }
            ssbi += "|BI=" + location;
            return ssbi;
        }
        //电信   CdmaCellLocation
        if ((mnc == 3 || mnc == 5 || mnc == 11)) {
            logger.info("电信");
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cel;
            if (cdmaCellLocation == null)
                return null;
            int sid = cdmaCellLocation.getSystemId();
            int nid = cdmaCellLocation.getNetworkId();
            int bid = cdmaCellLocation.getBaseStationId();
            logger.info("sid = " + sid);
            logger.info("nid = " + nid);
            logger.info("bid = " + bid);
            logger.info("sid16 = " + Integer.toHexString(sid));
            logger.info("nid16 = " + Integer.toHexString(nid));
            logger.info("bid16 = " + Integer.toHexString(bid));
            location = mcc + "," + Integer.toHexString(sid) + "," + Integer.toHexString(nid) + "," + Integer.toHexString(bid);
            ssbi += "|BI=" + location;

        }
        return ssbi;
    }
/*    private static String int2HexString2(int num){
        byte[] bytes = new byte[2];
        bytes[0] = (byte)((num&0xFF00)>>8);
        bytes[1] = (byte)(num&0x00FF);
        return HexUtil.bcd2str(bytes);
    }*/

    //add by fl at 20170221
    private void getScanLast(Context context, Map<String, String> data) {
        try {
            List<TradeInfo> tradeInfos = commonManager.getLastCode();
            if (null != tradeInfos && tradeInfos.size() > 0) {
                logger.info("获取到扫码流水");
                TradeInfo info = tradeInfos.get(0);
                Map<String, String> mapData = info.convert2Map();
                data.put(iso_f22, mapData.get(iso_f22));
                data.put(iso_f47, mapData.get(iso_f47).split("\\|")[0]);
                data.put(iso_f37, mapData.get(iso_f37));
                data.put(iso_f61, mapData.get(iso_f60).substring(2, 8) + mapData.get(iso_f11));
                //原交易时间-用于结果显示
                data.put(key_oriTransTime, mapData.get(iso_f12) + mapData.get(iso_f13));
            } else {
                logger.debug("未查询到末笔扫码流水");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("查询到末笔扫码流水出错");
        }
    }
}

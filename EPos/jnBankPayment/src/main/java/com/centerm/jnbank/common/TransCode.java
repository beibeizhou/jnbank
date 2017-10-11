package com.centerm.jnbank.common;

import com.centerm.jnbank.R;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * author:wanliang527</br>
 * date:2016/10/30</br>
 */

public class TransCode {

    private static Logger logger = Logger.getLogger(TransCode.class);

    public final static String OBTAIN_TMK = "OBTAIN_TMK";//获取主密钥
    public final static String INIT_TERMINAL = "INIT_TERMINAL";//初始化机具
    public final static String LOAD_PARAM = "LOAD_PARAM";//参数下载
    public final static String SIGN_IN = "POS_SIGN_IN";//签到
    public final static String SIGN_OUT = "POS_SIGN_OUT";//签退
    public final static String DOWNLOAD_CAPK = "DOWNLOAD_CAPK";//公钥下载
    public final static String DOWNLOAD_AID = "DOWNLOAD_AID";//IC卡参数下载
    public final static String DOWNLOAD_QPS_PARAMS = "DOWNLOAD_QPS_PARAMS";//非接参数下载
    public final static String DOWNLOAD_CARD_BIN = "DOWNLOAD_CARD_BIN";//卡BIN参数下载
    public final static String DOWNLOAD_CARD_BIN_QPS = "DOWNLOAD_CARD_BIN_QPS";//卡BIN参数下载,免密新增卡
    public final static String DOWNLOAD_BLACK_CARD_BIN_QPS = "DOWNLOAD_BLACK_CARD_BIN_QPS";//卡BIN参数下载,免密新增卡
    public final static String POS_STATUS_UPLOAD = "POS_STATUS_UPLOAD";//POS终端状态上送
    public final static String DOWNLOAD_PARAMS = "DOWNLOAD_PARAMS";//IC卡公钥/参数/TMS参数/卡BIN黑名单下载
    public final static String DOWNLOAD_PARAMS_FINISHED = "DOWNLOAD_PARAMS_FINISHED";//IC卡公钥/参数/TMS参数/卡BIN黑名单下载结束
    public final static String UPLOAD_SCRIPT_RESULT = "UPLOAD_SCRIPT_RESULT";//IC卡脚本结果上送
    public final static String BALANCE = "BALANCE";//余额查询
    public final static String TRANSFER = "TRANSFER";//转账
    public final static String SALE = "SALE";//消费
    public final static String SALE_FROMTOP = "SALE";//消费
    public final static String VOID = "VOID";//消费撤销
    public final static String REFUND = "REFUND";//退货
    public final static String AUTH = "AUTH";//预授权
    public final static String CANCEL = "CANCEL";//预授权撤销
    public final static String AUTH_COMPLETE = "AUTH_COMPLETE";//预授权完成请求
    public final static String AUTH_SETTLEMENT = "AUTH_SETTLEMENT";//预授权完成通知
    public final static String COMPLETE_VOID = "COMPLETE_VOID";//预授权完成撤销
    public final static String ELC_SIGNATURE = "ELC_SIGNATURE";//电子签名
    public final static String SCAN_PAY_WEI = "SCAN_PAY_WEI";//扫码支付
    public final static String SCAN_PAY_ALI = "SCAN_PAY_ALI";//扫码支付
    public final static String SCAN_PAY_SFT = "SCAN_PAY_SFT";//扫码支付
    public final static String SCAN_CANCEL = "SCAN_CANCEL";//扫码撤销
    public final static String SCAN_SERCH = "SCAN_SERCH";//扫码查询
    public final static String SCAN_LAST_SERCH = "SCAN_LAST_SERCH";//扫码末笔查询
    public final static String SCAN_REFUND_W = "SCAN_REFUND_W";//微信扫码退货
    public final static String SCAN_REFUND_Z = "SCAN_REFUND_Z";//支付宝扫码退货
    public final static String SCAN_REFUND_S = "SCAN_REFUND_S";
    public final static String DISCOUNT_INTERGRAL="DISCOUNT_INTERGRAL";//工会卡积分折扣查询

    public final static String REVERSE = "REVERSE";//冲正类交易
    public final static String SALE_REVERSE = "SALE_REVERSE";//消费冲正
    public final static String VOID_REVERSE = "VOID_REVERSE";//消费撤销冲正
    public final static String AUTH_REVERSE = "AUTH_REVERSE";//预授权冲正
    public final static String CANCEL_REVERSE = "CANCEL_REVERSE";//预授权撤销冲正
    public final static String AUTH_COMPLETE_REVERSE = "AUTH_COMPLETE_REVERSE";//预授权完成请求冲正
    public final static String COMPLETE_VOID_REVERSE = "COMPLETE_VOID_REVERSE";//预授权完成撤销冲正

    public final static String SETTLEMENT = "SETTLEMENT";//批结算
    public final static String SETTLEMENT_DONE = "SETTLEMENT_DONE";//批上送结束
    public final static String TRANS_IC_DETAIL = "TRANS_IC_DETAIL";//IC卡联机交易明细上送
    public final static String TRANS_CARD_DETAIL = "TRANS_CARD_DETAIL";//磁条卡联机交易明细上送
    public final static String TRANS_FEFUND_DETAIL = "TRANS_FEFUND_DETAIL";//磁条卡联机交易明细上送

    public final static String PRINT_LAST = "PRINT_LAST";//打印最后一笔
    public final static String PRINT_DETAIL = "PRINT_DETAIL";//打印交易明细
    public final static String PRINT_BATCH_SUMMARY = "PRINT_BATCH_SUMMARY";//打印上批次总计
    public final static String PRINT_SUMMARY = "PRINT_SUMMARY";//打印当批次总计

    public static Set<String> CAUSE_REVERSE_SETS = new HashSet<>();//可能引发冲正的交易类型
    public static Set<String> NO_MAC_SETS = new HashSet<>();//无需计算或者验证MAC的交易类型
    public static Set<String> REVERSE_SETS = new HashSet<>();//冲正类交易
    public static Set<String> NEED_INSERT_TABLE_SETS = new HashSet<>();//需要插入到交易表中的交易
    public static Set<String> SIGNED_BEFORE_TRADING_SETS = new HashSet<>();//交易前必须签到过
    public static Set<String> DEBIT_SETS = new HashSet<>();//借记类交易
    public static Set<String> CREDIT_SETS = new HashSet<>();//贷记类交易

    static {
        CAUSE_REVERSE_SETS.add(SALE);
        CAUSE_REVERSE_SETS.add(VOID);
        CAUSE_REVERSE_SETS.add(AUTH);
        CAUSE_REVERSE_SETS.add(CANCEL);
        CAUSE_REVERSE_SETS.add(AUTH_COMPLETE);
        CAUSE_REVERSE_SETS.add(COMPLETE_VOID);

        NO_MAC_SETS.add(OBTAIN_TMK);
        NO_MAC_SETS.add(SIGN_IN);
        NO_MAC_SETS.add(SIGN_OUT);
        NO_MAC_SETS.add(SETTLEMENT);
        NO_MAC_SETS.add(SETTLEMENT_DONE);
        NO_MAC_SETS.add(TRANS_IC_DETAIL);
        NO_MAC_SETS.add(TRANS_CARD_DETAIL);
        NO_MAC_SETS.add(TRANS_FEFUND_DETAIL);
        NO_MAC_SETS.add(POS_STATUS_UPLOAD);
        NO_MAC_SETS.add(DOWNLOAD_PARAMS);
        NO_MAC_SETS.add(DOWNLOAD_CAPK);
        NO_MAC_SETS.add(DOWNLOAD_AID);
        NO_MAC_SETS.add(DOWNLOAD_QPS_PARAMS);
        NO_MAC_SETS.add(DOWNLOAD_CARD_BIN);
        NO_MAC_SETS.add(DOWNLOAD_CARD_BIN_QPS);
        NO_MAC_SETS.add(DOWNLOAD_BLACK_CARD_BIN_QPS);
        NO_MAC_SETS.add(DOWNLOAD_PARAMS_FINISHED);
        //add by fl 添加初始化机具 为管理类，无需计算mac
        NO_MAC_SETS.add(INIT_TERMINAL);
        NO_MAC_SETS.add(LOAD_PARAM);

        REVERSE_SETS.add(REVERSE);
        REVERSE_SETS.add(SALE_REVERSE);
        REVERSE_SETS.add(VOID_REVERSE);
        REVERSE_SETS.add(AUTH_REVERSE);
        REVERSE_SETS.add(CANCEL_REVERSE);
        REVERSE_SETS.add(AUTH_COMPLETE_REVERSE);
        REVERSE_SETS.add(COMPLETE_VOID_REVERSE);

        NEED_INSERT_TABLE_SETS.add(SALE);
        NEED_INSERT_TABLE_SETS.add(BALANCE);
        NEED_INSERT_TABLE_SETS.add(VOID);
        NEED_INSERT_TABLE_SETS.add(REFUND);
        NEED_INSERT_TABLE_SETS.add(AUTH);
        NEED_INSERT_TABLE_SETS.add(CANCEL);
        NEED_INSERT_TABLE_SETS.add(AUTH_COMPLETE);
        NEED_INSERT_TABLE_SETS.add(AUTH_SETTLEMENT);
        NEED_INSERT_TABLE_SETS.add(COMPLETE_VOID);
        NEED_INSERT_TABLE_SETS.add(SCAN_PAY_ALI);
        NEED_INSERT_TABLE_SETS.add(SCAN_PAY_WEI);
        NEED_INSERT_TABLE_SETS.add(SCAN_PAY_SFT);
        NEED_INSERT_TABLE_SETS.add(SCAN_CANCEL);
        NEED_INSERT_TABLE_SETS.add(SCAN_REFUND_W);
        NEED_INSERT_TABLE_SETS.add(SCAN_REFUND_Z);
        NEED_INSERT_TABLE_SETS.add(SCAN_REFUND_S);

        SIGNED_BEFORE_TRADING_SETS.add(BALANCE);
        SIGNED_BEFORE_TRADING_SETS.add(SALE);
        SIGNED_BEFORE_TRADING_SETS.add(VOID);
        SIGNED_BEFORE_TRADING_SETS.add(REFUND);
        SIGNED_BEFORE_TRADING_SETS.add(AUTH);
        SIGNED_BEFORE_TRADING_SETS.add(AUTH_COMPLETE);
        SIGNED_BEFORE_TRADING_SETS.add(AUTH_SETTLEMENT);
        SIGNED_BEFORE_TRADING_SETS.add(CANCEL);
        SIGNED_BEFORE_TRADING_SETS.add(COMPLETE_VOID);
        SIGNED_BEFORE_TRADING_SETS.add(SCAN_PAY_WEI);
        SIGNED_BEFORE_TRADING_SETS.add(SCAN_PAY_ALI);
        SIGNED_BEFORE_TRADING_SETS.add(SCAN_PAY_SFT);
        SIGNED_BEFORE_TRADING_SETS.add(SCAN_CANCEL);
        SIGNED_BEFORE_TRADING_SETS.add(SCAN_REFUND_W);
        SIGNED_BEFORE_TRADING_SETS.add(SCAN_REFUND_Z);
        SIGNED_BEFORE_TRADING_SETS.add(SCAN_REFUND_S);

        DEBIT_SETS.add(SALE);
        DEBIT_SETS.add(AUTH);
        DEBIT_SETS.add(AUTH_COMPLETE);
        DEBIT_SETS.add(AUTH_SETTLEMENT);
        DEBIT_SETS.add(SCAN_PAY_ALI);
        DEBIT_SETS.add(SCAN_PAY_WEI);
        DEBIT_SETS.add(SCAN_PAY_SFT);

        CREDIT_SETS.add(VOID);
        CREDIT_SETS.add(REFUND);
        CREDIT_SETS.add(COMPLETE_VOID);
        CREDIT_SETS.add(CANCEL);
        CREDIT_SETS.add(SCAN_CANCEL);
        CREDIT_SETS.add(SCAN_REFUND_W);
        CREDIT_SETS.add(SCAN_REFUND_Z);
        CREDIT_SETS.add(SCAN_REFUND_S);
    }

    public static int codeMapName(String transCode) {
        if (transCode == null) {
            logger.warn(transCode + "==>未知交易名称");
            return R.string.unknown;
        }
        switch (transCode) {
            case SALE:
                return R.string.trans_sale;
            case BALANCE:
                return R.string.trans_balance;
            case VOID:
                return R.string.trans_void;
            case REFUND:
                return R.string.trans_refund;
            case AUTH:
                return R.string.trans_auth;
            case AUTH_COMPLETE:
                return R.string.trans_auth_complete;
     /*       case AUTH_SETTLEMENT:
                return R.string.trans_auth_settlement;*/
            case CANCEL:
                return R.string.trans_cancel;
            case COMPLETE_VOID:
                return R.string.trans_complete_void;
            case SCAN_PAY_WEI:
                return R.string.trans_scan_pay_wei;
            case SCAN_PAY_ALI:
                return R.string.trans_scan_pay_ali;
            case SCAN_PAY_SFT:
                return R.string.trans_scan_pay_sft;
            case SCAN_CANCEL:
                return R.string.trans_scan_cancel;
            case SCAN_REFUND_W:
            case SCAN_REFUND_Z:
            case SCAN_REFUND_S:
                return R.string.trans_scan_refund;
            case SCAN_SERCH:
                return R.string.trans_scan_query;
            case SCAN_LAST_SERCH:
                return R.string.trans_scan_last;
            case DISCOUNT_INTERGRAL:
                return R.string.discount_intergal;
            default:
                logger.warn(transCode + "==>未知交易名称");
                return R.string.unknown;
        }
    }

}

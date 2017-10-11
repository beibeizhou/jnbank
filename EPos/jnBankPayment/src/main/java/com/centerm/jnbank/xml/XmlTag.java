package com.centerm.jnbank.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class XmlTag {

    public static class MenuTag {
        public final static String Root = "Root";
        public final static String MENU = "MENU";
        public final static String MENU_ITEM = "MENU_ITEM";
        public final static String SALE = "SALE";
        public final static String VOID = "VOID";
        public final static String REFUND = "REFUND";
        public final static String AUTH = "AUTH";//预授权
        public final static String AUTH_COMPLETE = "AUTH_COMPLETE";
        public final static String CANCEL = "CANCEL";//预授权撤销
        public final static String COMPLETE_VOID = "COMPLETE_VOID";
        public final static String AUTH_SETTLEMENT = "AUTH_SETTLEMENT";
        public final static String BALANCE = "BALANCE";
        public final static String PRINT = "PRINT";
        public final static String PRINT_LAST = "PRINT_LAST";
        public final static String PRINT_ANY = "PRINT_ANY";
        public final static String PRINT_DETAIL = "PRINT_DETAIL";
        public final static String PRINT_SUMMARY = "PRINT_SUMMARY";
        public final static String PRINT_BATCH_SUMMARY = "PRINT_BATCH_SUMMARY";
        public final static String MANAGEMENT = "MANAGEMENT";
        public final static String SIGN_IN = "SIGN_IN";
        public final static String POS_SIGN_IN = "POS_SIGN_IN";
        public final static String OPER_SIGN_IN = "OPER_SIGN_IN";
        public final static String SIGN_OUT = "SIGN_OUT";
        public final static String QUERY = "QUERY";//交易查询
        public final static String SETTLEMENT = "SETTLEMENT";
        public final static String LOCK = "LOCK";
        public final static String CLEAR_TRADE_SERIAL = "CLEAR_TRADE_SERIAL";
        public final static String POS_VERSION = "POS_VERSION";
        //        public final static String SETTLEMENT_DONE = "SETTLEMENT_DONE";
        public final static String MERCHANTS_SETTINGS = "MERCHANTS_SETTINGS";
        public final static String SYSTEM_SETTINGS = "SYSTEM_SETTINGS";
        public final static String TRANS_SETTINGS = "TRANS_SETTINGS";
        public final static String TRANS_SWITCH = "TRANS_SWITCH";
        public final static String TRANS_PSW = "TRANS_PSW";
        public final static String TRANS_CARD = "TRANS_CARD";
        public final static String TRANS_SIGN = "TRANS_SIGN";
        public final static String VISA_FREE_PARAM = "VISA_FREE_PARAM";
        public final static String SCAN_PARAM = "SCAN_PARAM";//扫码参数
        public final static String SETTLE_SET = "SETTLE_SET";
        public final static String TRANS_OTHER = "TRANS_OTHER";
        public final static String OTHER = "OTHER";
        public final static String DOWNLOAD_CAPK = "DOWNLOAD_CAPK";
        public final static String DOWNLOAD_AID = "DOWNLOAD_AID";
        public final static String DOWNLOAD_CARD_BIN = "DOWNLOAD_CARD_BIN";
        public final static String DOWNLOAD_QPS_PARAMS = "DOWNLOAD_QPS_PARAMS";
        public final static String LOAD_PARAM = "LOAD_PARAM";
        public final static String PREFER_QUICK_PAY = "PREFER_QUICK_PAY";
        public final static String COMMUNICATION_SETTING = "COMMUNICATION_SETTING";

        public final static String OBTAIN_TMK = "OBTAIN_TMK";//获取主密钥
        public final static String SALE_BY_INSERT = "SALE_BY_INSERT";//插卡消费
        public final static String QUICK_SALE_NEED_PASWD = "QUICK_SALE_NEED_PASWD";//消费凭密
        public final static String QUICK_AUTH_NEED_PASWD = "QUICK_AUTH_NEED_PASWD";//预授权凭密
        public final static String SUPER_MANAGEMENT = "SUPER_MANAGEMENT";
        public final static String INJECT_TEK = "INJECT_TEK";//注入TEK
        public final static String INIT_TERMINAL = "INIT_TERMINAL";//终端初始化

        public final static String SCAN_PAY_WEI = "SCAN_PAY_WEI";//扫码支付
        public final static String SCAN_PAY_ALI = "SCAN_PAY_ALI";//扫码支付
        public final static String SCAN_PAY_SFT = "SCAN_PAY_SFT";//扫码支付
        public final static String SCAN_CANCEL = "SCAN_CANCEL";//扫码撤销
        public final static String SCAN_SERCH = "SCAN_SERCH";//扫码查询
        public final static String SCAN_LAST_SERCH = "SCAN_LAST_SERCH";//扫码末笔查询
        public final static String SCAN_REFUND_W = "SCAN_REFUND_W";//微信扫码退货
        public final static String SCAN_REFUND_Z = "SCAN_REFUND_Z";//支付宝扫码退货
        public final static String SCAN_REFUND_S = "SCAN_REFUND_S";//XXX扫码退货


        public final static String XIAOFEI = "XIAOFEI";//消费
//         <XIAOFEI chnTag="消费" iconResName="icon_menu_xf" />
//    <CHEXIAO chnTag="撤销" iconResName="icon_menu_chex" />
//    <TUIHUO chnTag="退货" iconResName="icon_menu_th" />
//    <YUSHOUQUAN chnTag="预授权" iconResName="icon_menu_yusq" />
//    <FENQI chnTag="pos分期" iconResName="icon_menu_posfq" />
//    <JIAOYICHAXUN chnTag="交易查询" iconResName="icon_menu_jiaoycx" />
//    <DAYIN chnTag="打印" iconResName="icon_menu_dy" />
//    <GUANLI chnTag="管理" iconResName="icon_menu_gl" />
//    <QITA chnTag="其他" iconResName="icon_menu_qit" />




        public final static Set<String> OFFLINE_MENU = new HashSet<>();//脱机类菜单
        public final static Set<String> TRADING_AFTER_SETTLEMENT_MENU = new HashSet<>();//达到交易流水上限时，必须结算后才允许交易的菜单

        static {
            OFFLINE_MENU.add("MERCHANTS_SETTINGS");
            OFFLINE_MENU.add("SYSTEM_SETTINGS");
            OFFLINE_MENU.add("TRANS_SETTINGS");
            OFFLINE_MENU.add("COMMUNICATION_SETTING");
            OFFLINE_MENU.add("PREFER_QUICK_PAY");
            OFFLINE_MENU.add("QEURY_OPER");
            OFFLINE_MENU.add("MODIFY_PASWD");
            OFFLINE_MENU.add("POS_VERSION");
            OFFLINE_MENU.add("LOCK");
            OFFLINE_MENU.add("QUERY");
            OFFLINE_MENU.add("PRINT_LAST");
            OFFLINE_MENU.add("PRINT_ANY");
            OFFLINE_MENU.add("PRINT_DETAIL");
            OFFLINE_MENU.add("PRINT_SUMMARY");
            OFFLINE_MENU.add("PRINT_BATCH_SUMMARY");
            OFFLINE_MENU.add("OPER_SIGN_IN");
            OFFLINE_MENU.add("TRANS_SWITCH");
            OFFLINE_MENU.add("TRANS_PSW");
            OFFLINE_MENU.add("TRANS_CARD");
            OFFLINE_MENU.add("SETTLE_SET");
            OFFLINE_MENU.add("SCAN_PARAM");
            OFFLINE_MENU.add("TRANS_SIGN");
            OFFLINE_MENU.add("VISA_FREE_PARAM");
            OFFLINE_MENU.add("TRANS_OTHER");

            TRADING_AFTER_SETTLEMENT_MENU.add("SALE");
            TRADING_AFTER_SETTLEMENT_MENU.add("VOID");
            TRADING_AFTER_SETTLEMENT_MENU.add("REFUND");
            TRADING_AFTER_SETTLEMENT_MENU.add("AUTH");
            TRADING_AFTER_SETTLEMENT_MENU.add("AUTH_COMPLETE");
            TRADING_AFTER_SETTLEMENT_MENU.add("CANCEL");
            TRADING_AFTER_SETTLEMENT_MENU.add("COMPLETE_VOID");
            TRADING_AFTER_SETTLEMENT_MENU.add("SALE_BY_INSERT");
            TRADING_AFTER_SETTLEMENT_MENU.add("QUICK_SALE_NEED_PASWD");
            TRADING_AFTER_SETTLEMENT_MENU.add("QUICK_AUTH_NEED_PASWD");
            TRADING_AFTER_SETTLEMENT_MENU.add("SCAN_PAY_SFT");
            TRADING_AFTER_SETTLEMENT_MENU.add("SCAN_PAY_WEI");
            TRADING_AFTER_SETTLEMENT_MENU.add("SCAN_PAY_ALI");
        }


    }


    public static class MessageTag {
        public final static String header = "header";
        public final static String transCode = "transCode";
        public final static String termId = "termId";
        public final static String pinPadId = "pinPadId";
        public final static String serial = "serial";
        public final static String transTime = "KEY_TRANS_TIME";
        public final static String random = "random";
        public final static String respCode = "respCode";
        public final static String respMsg = "respMsg";
        public final static String req = "req";
        public final static String resp = "resp";
        public final static String message = "message";
    }

}

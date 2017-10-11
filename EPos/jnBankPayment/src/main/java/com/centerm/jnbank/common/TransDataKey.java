package com.centerm.jnbank.common;

/**
 * 交易过程中临时数据的键名
 * author:wanliang527</br>
 * date:2016/10/29</br>
 */

public class TransDataKey {

    public final static String keyFlagFallback = "keyFlagFallback";//降级交易的标志
    public final static String keyFlagNoPin = "keyFlagNoPin";//无PIN标志
    public final static String keyBalanceAmt = "keyBalanceAmt";//余额
    public final static String keyLocalMac = "keyLocalMac";
    public final static String keyIsAdmin = "keyIsAdmin";
    public final static String FLAG_IMPORT_AMOUNT = "FLAG_IMPORT_AMOUNT";//内核等待金额导入的标识
    public final static String FLAG_IMPORT_CARD_CONFIRM_RESULT = "FLAG_IMPORT_CARD_CONFIRM_RESULT";//内核等待卡号信息确认的标识
    public final static String FLAG_IMPORT_PIN = "FLAG_IMPORT_PIN";//内核等待导入PIN的标识
    public final static String FLAG_REQUEST_ONLINE = "FLAG_REQUEST_ONLINE";//内核请求联机的标识
    public final static String FLAG_AUTO_SIGN = "FLAG_AUTO_SIGN";//自动签到的标识
    public final static String FLAG_REQUEST_UNIONCARD="FLAG_REQUEST_UNIONCARD";//进入工会卡查询界面的标识

    public final static String KEY_PARAMS_TYPE = "KEY_PARAMS_TYPE";//需要下载的参数类型，1-IC卡公钥下载；2-IC参数下载；3-国密公钥下载；4-非接参数下载；5-卡BIN下载
    public final static String KEY_PARAMS_COUNTS = "KEY_PARAMS_COUNTS";//已经下载的参数条数
    public final static String KEY_CAPK_INFO = "KEY_CAPK_INFO";//CAPK信息（RID和索引）
    public final static String KEY_IC_DATA_PRINT = "KEY_IC_DATA_PRINT";//IC卡数据，用于打印
    public final static String KEY_QPS_FLAG = "KEY_QPS_FLAG";//小额免密业务标识
    public final static String KEY_QPS_NOSIGN_FLAG = "KEY_QPS_NOSIGN_FLAG";//小额免签业务标识
    public final static String KEY_QPS_AMOUNT = "KEY_QPS_AMOUNT";//小额免签免密金额
    public final static String KEY_TRANS_TIME = "KEY_TRANS_TIME";//终端本地交易时间
    public final static String KEY_IC_SCRIPT_RESULT = "KEY_IC_SCRIPT_RESULT";//IC卡脚本执行结果
    public final static String KEY_HOLDER_NAME = "KEY_HOLDER_NAME";//持卡人姓名

    public final static String headerData = "headerdata";
    public final static String iso_f2 = "iso_f2";
    public final static String iso_f2_result = "iso_f2_result";//保存卡号，用于结果显示，但不上送
    public final static String iso_f3 = "iso_f3";
    public final static String iso_f4 = "iso_f4";
    public final static String iso_f5 = "iso_f5";
    public final static String iso_f6 = "iso_f6";
    public final static String iso_f7 = "iso_f7";
    public final static String iso_f8 = "iso_f8";
    public final static String iso_f9 = "iso_f9";
    public final static String iso_f10 = "iso_f10";
    public final static String iso_f11 = "iso_f11";
    public final static String iso_f11_origin = "iso_f11_origin";//原交易的11域
    public final static String iso_f12 = "iso_f12";
    public final static String iso_f13 = "iso_f13";
    //    public final static String iso_f13_receive = "iso_f13_receive";
    public final static String iso_f14 = "iso_f14";
    public final static String iso_f14_result = "iso_f14_result";
    public final static String iso_f14_forvoid= "iso_f14_forvoid";
    public final static String iso_f15 = "iso_f15";
    public final static String iso_f16 = "iso_f16";
    public final static String iso_f17 = "iso_f17";
    public final static String iso_f18 = "iso_f18";
    public final static String iso_f19 = "iso_f19";
    public final static String iso_f20 = "iso_f20";
    public final static String iso_f21 = "iso_f21";
    public final static String iso_f22 = "iso_f22";
    public final static String iso_f23 = "iso_f23";
    public final static String iso_f24 = "iso_f24";
    public final static String iso_f25 = "iso_f25";
    public final static String iso_f26 = "iso_f26";
    public final static String iso_f27 = "iso_f27";
    public final static String iso_f28 = "iso_f28";
    public final static String iso_f29 = "iso_f29";
    public final static String iso_f30 = "iso_f30";
    public final static String iso_f31 = "iso_f31";
    public final static String iso_f32 = "iso_f32";
    public final static String iso_f33 = "iso_f33";
    public final static String iso_f34 = "iso_f34";
    public final static String iso_track2 = "iso_track2";
    public final static String iso_track3 = "iso_track3";
    public final static String iso_f35 = "iso_f35";
    public final static String iso_f36 = "iso_f36";
    public final static String iso_f37 = "iso_f37";
    public final static String iso_f38 = "iso_f38";
    public final static String iso_f39 = "iso_f39";
    public final static String iso_f40 = "iso_f40";
    public final static String iso_f41 = "iso_f41";
    public final static String iso_f42 = "iso_f42";
    public final static String iso_f43 = "iso_f43";
    public final static String iso_f44 = "iso_f44";
    public final static String iso_f45 = "iso_f45";
    public final static String iso_f46 = "iso_f46";
    public final static String iso_f47 = "iso_f47";
    public final static String iso_f48 = "iso_f48";
    public final static String iso_f49 = "iso_f49";
    public final static String iso_f50 = "iso_f50";
    public final static String iso_f51 = "iso_f51";
    public final static String iso_f52 = "iso_f52";
    public final static String iso_f53 = "iso_f53";
    public final static String iso_f54 = "iso_f54";
    public final static String iso_f55 = "iso_f55";
    public final static String iso_f55_reverse = "iso_f55_reverse";
    public final static String iso_f56 = "iso_f56";
    public final static String iso_f57 = "iso_f57";
    public final static String iso_f58 = "iso_f58";
    public final static String iso_f59 = "iso_f59";
    public final static String iso_f60 = "iso_f60";
    public final static String iso_f60_origin = "iso_f60_origin";//原交易的60域
    public final static String iso_f61 = "iso_f61";
    public final static String iso_f62 = "iso_f62";
    public final static String iso_f63 = "iso_f63";
    public final static String iso_f64 = "iso_f64";
    public final static String key_flag = "key_flag";
    public final static String key_noPinFlag = "key_noPinFlag";
    public final static String key_noSignFlag = "key_noSignFlag";
    public final static String key_oriTransTime = "key_oriTransTime";
    public final static String key_oriAuthCode = "key_oriAuthCode";
    public final static String key_voucherNo = "key_voucherNo";
    public final static String key_oriReference = "key_oriReference";
    public final static String key_retryTimes = "key_retryTimes";
    public final static String key_resp_code = "key_resp_code";
    public final static String key_resp_msg = "key_resp_msg";
    public final static String key_is_amount_ok = "key_is_amount_ok";
    public final static String key_batch_upload_count = "key_batch_upload_count";
    public final static String key_param_update = "key_param_update";//是否有参数更新
    public final static String key_trans_code = "key_trans_code";//交易类型码
    public final static String key_void_amt = "key_void_amt";//撤销金额
    public final static String key_bank_card_type = "key_bank_card_type";//银行卡类型，true 借记  false 贷记，准贷记

    //add by ysd 支付组件入口的部分交易信息
    public final static String key_scan_type = "key_scan_type";//扫码方式 被扫:1 主扫:2或其他及默认
    public final static String key_entryTraceNo = "key_entryTraceNo";//原交易流水
    public final static String key_entryReferenceNo = "key_entryReferenceNo";//原参考号
    public final static String key_entryCardNo = "key_entryCardNo";//鉴权卡号
    public final static String key_entryReserve47 = "key_entryReserve47";//47域追加的字段
    public final static String key_entryPriInfo1 = "key_entryPriInfo1";//打印信息，行业应用需要打印的备注信息
    public final static String key_entryPricode1 = "key_entryPricode1";//打印信息，行业应用需要打印的备注信息
    public final static String key_entryPriInfo2 = "key_entryPriInfo2";//打印信息，行业应用需要打印的备注信息
    public final static String key_entryPricode2 = "key_entryPricode2";//打印信息，行业应用需要打印的备注信息
}

package config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.centerm.jnbank.common.Settings.getStringMetaData;
import static config.BusinessConfig.Key.FLAG_ALI_SWITH;
import static config.BusinessConfig.Key.FLAG_AUTH_HAND_CARD;
import static config.BusinessConfig.Key.FLAG_AUTH_SWITH;
import static config.BusinessConfig.Key.FLAG_AUTO_SIGN_OUT;
import static config.BusinessConfig.Key.FLAG_BALANCE_SWITH;
import static config.BusinessConfig.Key.FLAG_BANK_SWITH;
import static config.BusinessConfig.Key.FLAG_CANCEL_PSW;
import static config.BusinessConfig.Key.FLAG_CANCEL_SWITH;
import static config.BusinessConfig.Key.FLAG_COMPLETE_PSW;
import static config.BusinessConfig.Key.FLAG_COMPLETE_SWITH;
import static config.BusinessConfig.Key.FLAG_COMPLETE_VOID_CARD;
import static config.BusinessConfig.Key.FLAG_COMPLETE_VOID_PSW;
import static config.BusinessConfig.Key.FLAG_COMPLETE_VOID_SWITH;
import static config.BusinessConfig.Key.FLAG_FEIJIE;
import static config.BusinessConfig.Key.FLAG_FIRST_AID_DOWNLOAD;
import static config.BusinessConfig.Key.FLAG_FIRST_CAPK_DOWNLOAD;
import static config.BusinessConfig.Key.FLAG_IS_OPEN_SIGN;
import static config.BusinessConfig.Key.FLAG_NEED_UPDATE_BLACK_CARDBIN;
import static config.BusinessConfig.Key.FLAG_NEED_UPDATE_CARDBIN;
import static config.BusinessConfig.Key.FLAG_NEED_UPDATE_PARAM;
import static config.BusinessConfig.Key.FLAG_PREFER_CLSS;
import static config.BusinessConfig.Key.FLAG_PRINT_ENGLISH;
import static config.BusinessConfig.Key.FLAG_PRINT_FAIL_DETAIL;
import static config.BusinessConfig.Key.FLAG_PRINT_MINUS;
import static config.BusinessConfig.Key.FLAG_PRINT_PAPER;
import static config.BusinessConfig.Key.FLAG_PRINT_QRCODE;
import static config.BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL;
import static config.BusinessConfig.Key.FLAG_REFUND_SWITH;
import static config.BusinessConfig.Key.FLAG_REFUND_VOID_NEED_PSW;
import static config.BusinessConfig.Key.FLAG_SALE_SWITH;
import static config.BusinessConfig.Key.FLAG_SCAN_INTERVAL_TIME;
import static config.BusinessConfig.Key.FLAG_SCAN_QUERY_COUNT;
import static config.BusinessConfig.Key.FLAG_SCAN_WAIT_TIME;
import static config.BusinessConfig.Key.FLAG_SHENG_SWITH;
import static config.BusinessConfig.Key.FLAG_SHIELD_CARD;
import static config.BusinessConfig.Key.FLAG_SWITCH_LOGO;
import static config.BusinessConfig.Key.FLAG_TIP_COMFIRM_SIGN;
import static config.BusinessConfig.Key.FLAG_TIP_PRINT_DETAIL;
import static config.BusinessConfig.Key.FLAG_TRACE_PSW;
import static config.BusinessConfig.Key.FLAG_USE_DISCOUNT;
import static config.BusinessConfig.Key.FLAG_USE_INTEGRAL;
import static config.BusinessConfig.Key.FLAG_VOID_CARD;
import static config.BusinessConfig.Key.FLAG_VOID_PSW;
import static config.BusinessConfig.Key.FLAG_VOID_SWITH;
import static config.BusinessConfig.Key.FLAG_WEI_SWITH;
import static config.BusinessConfig.Key.KEY_BATCH_NO;
import static config.BusinessConfig.Key.KEY_LAST_BIN_NO;
import static config.BusinessConfig.Key.KEY_POS_SERIAL;
import static config.BusinessConfig.Key.PARAM_CENTERM_NUM1;
import static config.BusinessConfig.Key.PARAM_CENTERM_NUM2;
import static config.BusinessConfig.Key.PARAM_CENTERM_NUM3;
import static config.BusinessConfig.Key.PARAM_COUNT_RETRY;
import static config.BusinessConfig.Key.PARAM_CURRENT_TIME;
import static config.BusinessConfig.Key.PARAM_DEFINE_HEAD;
import static config.BusinessConfig.Key.PARAM_ELC_COUNT;
import static config.BusinessConfig.Key.PARAM_FLAG_SHIELD;
import static config.BusinessConfig.Key.PARAM_FLAG_SWITCH;
import static config.BusinessConfig.Key.PARAM_IP_PORT;
import static config.BusinessConfig.Key.PARAM_MAX_RESIGN_CNT;
import static config.BusinessConfig.Key.PARAM_MAX_SIGN_CNT;
import static config.BusinessConfig.Key.PARAM_MOST_REFUND;
import static config.BusinessConfig.Key.PARAM_MOST_TRANS;
import static config.BusinessConfig.Key.PARAM_OUTLINE;
import static config.BusinessConfig.Key.PARAM_PASSWARD;
import static config.BusinessConfig.Key.PARAM_PRINT_COUNT;
import static config.BusinessConfig.Key.PARAM_PRINT_REMARK;
import static config.BusinessConfig.Key.PARAM_QRCODE;
import static config.BusinessConfig.Key.PARAM_QRCODE_DOWN;
import static config.BusinessConfig.Key.PARAM_QRCODE_UP;
import static config.BusinessConfig.Key.PARAM_REVERSE_COUNT;
import static config.BusinessConfig.Key.PARAM_SIGN_OUT_TIME;
import static config.BusinessConfig.Key.PARAM_SIGN_UPLOAD_TIME;
import static config.BusinessConfig.Key.PARAM_SWITCH_DIALING;
import static config.BusinessConfig.Key.PARAM_TPDU;
import static config.BusinessConfig.Key.PARAM_VERSION;
import static config.BusinessConfig.Key.PARAM_WHEN_UPLOAD;
import static config.BusinessConfig.Key.PRESET_MERCHANT_CD;
import static config.BusinessConfig.Key.PRESET_MERCHANT_ENGLISH_NAME;
import static config.BusinessConfig.Key.PRESET_MERCHANT_NAME;
import static config.BusinessConfig.Key.PRESET_TERMINAL_CD;

/**
 * 业务参数配置
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public class BusinessConfig {


    /**
     * 默认的AID参数
     */
    public static final String[] AID = new String[]{
            "9F0608A000000333010101DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000",
            "9F0608A000000333010102DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000",
            "9F0608A000000333010103DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000",
            "9F0608A000000333010106DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000"};
    /**
     * 默认的CAPK参数
     */
    public static final String[] CAPK = new String[]{
            "9F0605A0000003339F220101DF05083230313431323331DF060101DF070101DF028180BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93BDF040103DF0314E881E390675D44C2DD81234DCE29C3F5AB2297A0",
            "9F0605A0000003339F220102DF05083230323131323331DF060101DF070101DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF040103DF031403BB335A8549A03B87AB089D006F60852E4B8060",
            "9F0605A0000003339F220103DF05083230323431323331DF060101DF070101DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF040103DF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26",
            "9F0605A0000003339F220104DF05083230323531323331DF060101DF070101DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF040103DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5"};

    /**
     * 默认的江南银行行方卡bin信息
     */
    public static final String[] JNCARDBIN=new String[]{
            "621363","621397","622891","623189","623576","622452","622324"
    };

    public final static String TPDU = "6000030000";
    public final static String HEADER_APP_TYPE = "60";//应用类别：IC卡金融支付类应用
    public final static String HEADER_APP_VERSION = "31";//软件总版本号：新版本必须上送“31”
    public final static String HEADER_APP_RESERVE = "0000";//保留位
    public final static String NET_LISCENSE_NO = "3137";//银联入网许可证号
    public final static int CHECK_CARD_TIMEOUT = 60 * 1000;//默认检卡超时时间
    public final static int CHECK_CARD_RETRY_TIMES = 3;//默认检卡重试次数
    public final static int NET_RESP_TIMEOUT = 60;//默认网络响应超时时间
    public final static int NET_CONNECT_TIMEOUT = 10;//默认网络连接超时时间
    public final static int REVERSE_RETRY_TIMES = 3;//默认冲正重试次数
    public final static double REFUND_AMOUNT_LIMITED = 99999999.99;//退货金额上限
    public final static int PASSWD_RETRY_TIMES = 3;//输密重试次数
    public final static boolean FLAG_VOID_NEED_PIN = false;//消费撤销时是否需要输入密码
    public final static boolean FLAG_VOID_NEED_CHECK_CARD = true;//消费撤销时是否需要检卡
    public final static boolean FLAG_ENCRYPT_TRACK_DATA = false;//联机上送时是否加密磁道信息true
    public final static boolean CLSS_CARD_PREFERED = false;// 设置是否挥卡优先
    private final static String SP_FILE_NAME = "business_config";//用于存储参数的XML文件名称
    private static BusinessConfig instance;
    public  static String MAINKEYINDEX="06";//生产密钥索引
    public static String USE_INTEGRAL="USE_INTEGRAL";//是否使用积分的标记
    public static String INTEGRAL_BALANCE="INTEGRAL_BALANCE";//积分余额

    /**
     * 参数值对应的Key
     */
    public static class Key {
        public final static String FLAG_TRADE_STORAGE_WARNING = "FLAG_TRADE_STORAGE_WARNING";//交易流水存储预警，每次交易前需要判断该值是否为true，如果为true，需要强制进行批结算后才可以再次消费
        public final static String FLAG_NEED_UPLOAD_SCRIPT = "FLAG_NEED_UPLOAD_SCRIPT";//需要上送IC卡脚本执行结果的标识，用于下一次联机时上送脚本通知
        public final static String FLAG_SIGN_IN = "FLAG_SIGN_IN";//签到标识
        public final static String FLAG_TEK = "FLAG_TEK";//TEK存在的标识
        public final static String KEY_POS_SERIAL = "KEY_POS_SERIAL";//终端流水号
        public final static String KEY_BATCH_NO = "KEY_BATCH_NO";//当前批次号
        public final static String KEY_LAST_BIN_NO = "KEY_LAST_BIN_NO";//最后一条卡BIN编号，用于下次请求时传入
        public final static String KEY_OPER_ID = "KEY_OPER_ID";//当前登录的操作员ID
        public final static String KEY_LAST_OPER_ID = "KEY_LAST_OPER_ID";//上一个操作员ID
        public final static String KEY_IS_LOCK = "KEY_IS_LOCK";// 是否锁机
        public final static String KEY_IS_BATCH_BUT_NOT_OUT = "KEY_IS_BATCH_BUT_NOT_OUT";// 是否批结算但是未签退
        public final static String PRESET_MERCHANT_CD = "PRESET_MERCHANT_CD";//商户号
        public final static String PRESET_MERCHANT_NAME = "PRESET_MERCHANT_NAME";//商户名
        public final static String PRESET_MERCHANT_ENGLISH_NAME = "PRESET_MERCHANT_ENGLISH_NAME";//商户名
        public final static String PRESET_TERMINAL_CD = "PRESET_TERMINAL_CD";//终端号
        public final static String KEY_LAST_BATCH_NO = "KEY_LAST_BATCH_NO";//上一批次号
        public final static String PRINT_COUNT = "PRINT_COUNT";//打印联数
        public final static String KEY_LAST_SIGNIN_DATE = "KEY_LAST_SIGNIN_DATE";//上一次签到日期
        public final static String PARAM_VERSION = "PARAM_VERSION";//参数版本号 00
        public final static String PARAM_PASSWARD = "PARAM_PASSWARD";//安全密码 03
        public final static String PARAM_CURRENT_TIME = "PARAM_CURRENT_TIME";//当前时间 05
        public final static String PARAM_MOST_REFUND = "PARAM_MOST_REFUND";//最大退货金额 08
        public final static String PARAM_FLAG_SWITCH = "PARAM_FLAG_SWITCH";//开关位图 09
        public final static String PARAM_IP_PORT = "PARAM_IP_PORT";//服务器IP端口 0A
        public final static String PARAM_TPDU = "PARAM_TPDU";//TPDU 0B
        public final static String PARAM_SWITCH_DIALING = "PARAM_SWITCH_DIALING";//是否预拨号 0C
        public final static String PARAM_OUTTIME = "PARAM_OUTTIME";//交易超时时间 0D
        public final static String PARAM_COUNT_RETRY = "PARAM_COUNT_RETRY";//交易重拔次数 0E
        public final static String PARAM_OUTLINE = "PARAM_OUTLINE";//外线号码 0F
        public final static String PARAM_CENTERM_NUM1 = "PARAM_CENTERM_NUM1";//中心交易号码1 10
        public final static String PARAM_CENTERM_NUM2 = "PARAM_CENTERM_NUM2";//中心交易号码2 11
        public final static String PARAM_CENTERM_NUM3 = "PARAM_CENTERM_NUM3";//中心交易号码3 12
        public final static String PARAM_FLAG_SHIELD = "PARAM_FLAG_SHIELD";//屏蔽设置 13
        public final static String PARAM_REVERSE_COUNT = "PARAM_REVERSE_COUNT";//冲正重发次数
        public final static String PARAM_PRINT_COUNT = "PARAM_PRINT_COUNT";//打印张数 15
        public final static String PARAM_MOST_TRANS = "PARAM_MOST_TRANS";//终端存储的最大交易笔数
        public final static String PARAM_PRINT_REMARK = "PARAM_PRINT_REMARK";//签购单备注 17
        public final static String PARAM_ELC_COUNT = "PARAM_ELC_COUNT";//电子现金笔数 18
        public final static String PARAM_QRCODE_UP = "PARAM_QRCODE_UP";//原样打印在二维码上方的文字 20
        public final static String PARAM_QRCODE = "PARAM_QRCODE";//二维码链接内容转换为二维码打印 21
        public final static String PARAM_QRCODE_DOWN = "PARAM_QRCODE_DOWN";//原样打印在二维码下方的文字 22
        public final static String FLAG_SWITCH_LOGO = "FLAG_SWITCH_LOGO";//是否显示logo 19


        public final static String FLAG_BALANCE_SWITH = "FLAG_BALANCE_SWITH";//余额查询开关
        public final static String FLAG_SALE_SWITH = "FLAG_SALE_SWITH";//消费开关
        public final static String FLAG_VOID_SWITH = "FLAG_VOID_SWITH";//消费撤销开关
        public final static String FLAG_AUTH_SWITH = "FLAG_AUTH_SWITH";//预授权开关
        public final static String FLAG_CANCEL_SWITH = "FLAG_CANCEL_SWITH";//预授权撤销开关
        public final static String FLAG_COMPLETE_SWITH = "FLAG_COMPLETE_SWITH";//预授权完成请求
        public final static String FLAG_COMPLETE_VOID_SWITH = "FLAG_COMPLETE_VOID_SWITH";//预授权完成撤销
        public final static String FLAG_REFUND_SWITH = "FLAG_REFUND_SWITH";//退货
        public final static String FLAG_SHENG_SWITH = "FLAG_SHENG_SWITH";
        public final static String FLAG_WEI_SWITH = "FLAG_WEI_SWITH";//微信开关
        public final static String FLAG_ALI_SWITH = "FLAG_ALI_SWITH";//支付宝开关
        public final static String FLAG_BANK_SWITH = "FLAG_BANK_SWITH";//银行卡开关
        public final static String FLAG_PRINT_ENGLISH = "FLAG_PRINT_ENGLISH";//是否打印英文
        public final static String FLAG_PRINT_PAPER = "FLAG_PRINT_PAPER";//是否打印签购单

        public final static String FLAG_PRINT_QRCODE = "FLAG_PRINT_QRCODE";//是否打印二维码
        public final static String FLAG_VOID_CARD = "FLAG_VOID_CARD";//消费撤销是否用卡
        public final static String FLAG_VOID_PSW = "FLAG_VOID_PSW";//撤销撤销是否输密
        public final static String FLAG_COMPLETE_VOID_CARD = "FLAG_COMPLETE_VOID_CARD";//预授权完成撤销是否用卡
        public final static String FLAG_COMPLETE_VOID_PSW = "FLAG_COMPLETE_VOID_PSW";//预授权完成撤销是否输密
        public final static String FLAG_CANCEL_PSW = "FLAG_CANCEL_PSW";//预授权撤销输密
        public final static String FLAG_COMPLETE_PSW = "FLAG_COMPLETE_PSW";//预授权完成输密
        public final static String FLAG_AUTH_HAND_CARD = "FLAG_AUTH_HAND_CARD";//预授权完成（请求）是否允许手输卡号
        public final static String FLAG_SCAN_QUERY_COUNT = "FLAG_SCAN_QUERY_COUNT";//扫码查询次数
        public final static String FLAG_TIP_PRINT_DETAIL = "FLAG_TIP_PRINT_DETAIL";//是否提示打印详细数据
        public final static String FLAG_PRINT_VOID_DETAIL = "FLAG_PRINT_VOID_DETAIL";//结算是否打印撤销数据
        public final static String FLAG_PRINT_FAIL_DETAIL = "FLAG_PRINT_FAIL_DETAIL";//提示打印失败
        public final static String FLAG_SCAN_WAIT_TIME = "FLAG_SCAN_WAIT_TIME";//扫码等待时间 22
        public final static String FLAG_SCAN_INTERVAL_TIME = "FLAG_SCAN_INTERVAL_TIME";//查询间隔时间
        public final static String FLAG_ISS_CHINESE = "FLAG_ISS_CHINESE";//是否发卡行中文
        public final static String FLAG_REC_CHINESE = "FLAG_REC_CHINESE";//是否收单行中文22
        public final static String FLAG_PRINT_MINUS = "FLAG_PRINT_MINUS";//是否打印负号
        public final static String FLAG_SHIELD_CARD = "FLAG_SHIELD_CARD";//是否屏蔽预授权卡号
        public final static String FLAG_AUTO_SIGN_OUT = "FLAG_AUTO_SIGN_OUT";//是否自动签退
        public final static String FLAG_REFUND_VOID_NEED_PSW = "FLAG_REFUND_VOID_NEED_PSW";//是否退货和撤销需要输入管理员密码
        public final static String FLAG_PREFER_CLSS = "FLAG_PREFER_CLSS";//挥卡优先
        public final static String FLAG_USE_INTEGRAL="FLAG_USE_INTEGRAL";//是否使用积分的标签
        public final static String FLAG_USE_DISCOUNT="FLAG_USE_DISCOUNT";//是否使用折扣的标签
        public final static String FLAG_FEIJIE = "FLAG_FEIJIE";//是否支持非接
        public final static String FLAG_TRACE_PSW = "FLAG_TRACE_PSW";//是否磁道加密

        public final static String FLAG_IS_OPEN_SIGN = "FLAG_IS_OPEN_SIGN";//是否开启电子签名
        public final static String FLAG_TIP_COMFIRM_SIGN = "FLAG_TIP_COMFIRM_SIGN";//是否提示确认签名
        public final static String PARAM_SIGN_OUT_TIME = "PARAM_SIGN_OUT_TIME";//电子签名超时时间
        public final static String PARAM_SIGN_UPLOAD_TIME = "PARAM_SIGN_UPLOAD_TIME";//电子签名上送重试次数
        public final static String PARAM_MAX_SIGN_CNT = "PARAM_MAX_SIGN_CNT";//电子签名最大交易笔数
        public final static String PARAM_MAX_RESIGN_CNT = "PARAM_MAX_RESIGN_CNT";//电子签名最大重签次数
        public final static String PARAM_WHEN_UPLOAD = "PARAM_WHEN_UPLOAD";//上送时点
        public final static String PARAM_DEFINE_HEAD = "PARAM_DEFINE_HEAD";//签购单打印头部
        public final static String FLAG_IS_QUERY_INTEGRAL ="FLAG_IS_QUERY_INTEGRAL";//消费是否查询积分
        public final static String FLAG_IS_QUERY_DISCOUNT ="FLAG_IS_QUERY_DISCOUNT";//消费是否查询工会卡

        public final static String FLAG_FIRST_CAPK_DOWNLOAD = "FLAG_FIRST_CAPK_DOWNLOAD";//是否首次公钥下载
        public final static String FLAG_FIRST_AID_DOWNLOAD = "FLAG_FIRST_AID_DOWNLOAD";//是否首次IC卡参数下载
        public final static String FLAG_NEED_UPDATE_PARAM = "FLAG_NEED_UPDATE_PARAM";//是否需要更新终端参数
        public final static String FLAG_NEED_UPDATE_CARDBIN = "FLAG_NEED_UPDATE_CARDBIN";//是否需要更新卡BIN数据
        public final static String FLAG_NEED_UPDATE_BLACK_CARDBIN = "FLAG_NEED_UPDATE_BLACK_CARDBIN";//是否需要更新卡BIN黑名单




    }

    /**
     * 部分参数的默认值
     */
    private class Default {
        public final static boolean flagPrefferClss = true;
        public final static boolean flagNotSign = false;
        public final static boolean flagNotPin = false;
        public final static boolean flagIsLock = false;
        public final static boolean flagIsAutoSignOut = true;
        public final static boolean flagIsBatchButNotOut = false;
        public final static int numTradeInfos = 500;
        public final static String cardBinNo = "000";
        public final static int posSerial = 1;
        public final static String posBatch = "000001";
        public final static int msgMaxRetryTimes = 3;

    }

    public static BusinessConfig getInstance() {
        if (instance == null) {
            synchronized (BusinessConfig.class) {
                if (instance == null) {
                    instance = new BusinessConfig();
                }
            }
        }
        return instance;
    }

    private SharedPreferences getDefaultPres(Context context) {
        return context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean getFlag(Context context, String key) {
        boolean def = false;
        if (Key.KEY_IS_LOCK.equals(key)) {
            def = Default.flagIsLock;
        }
        return getDefaultPres(context).getBoolean(key, def);
    }

    public void setFlag(Context context, String key, boolean value) {
        getDefaultPres(context).edit().putBoolean(key, value).commit();
        if (Key.FLAG_SIGN_IN.equals(key)&&value) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMdd");
            setValue(context, Key.KEY_LAST_SIGNIN_DATE, formatter.format(new Date()));
        }
    }

    public String getValue(Context context, String key) {
        String def = null;
        if (KEY_LAST_BIN_NO.equals(key)) {
            def = Default.cardBinNo;
        } else if (KEY_BATCH_NO.equals(key)) {
            def = Default.posBatch;
        } else if (PRESET_MERCHANT_NAME.equals(key)) {
            def = "";
        } else if (PRESET_MERCHANT_CD.equals(key)) {
            def = "";
        } else if (PRESET_TERMINAL_CD.equals(key)) {
            def = "";
        }
        return getDefaultPres(context).getString(key, def);
    }

    public void setValue(Context context, String key, String value) {
        getDefaultPres(context).edit().putString(key, value).commit();
    }

    public int getNumber(Context context, String key) {
        int def = 0;
        if (PARAM_MOST_TRANS.equals(key)) {
            def = Default.numTradeInfos;
        } else if (KEY_POS_SERIAL.equals(key)) {
            def = Default.posSerial;
        } else if (PARAM_REVERSE_COUNT.equals(key)) {
            def = Default.msgMaxRetryTimes;
        }
        return getDefaultPres(context).getInt(key, def);
    }

    public void setNumber(Context context, String key, int num) {
        getDefaultPres(context).edit().putInt(key, num).commit();
    }

    /**
     * 获取保存的8583域数据。其中41域（受卡机终端标识码），42域（受卡方标识码），43域（商户名称）
     *
     * @param context    context
     * @param fieldIndex 域的索引值
     * @return 已保存的该域的值
     */
    public String getIsoField(Context context, int fieldIndex) {
        return getDefaultPres(context).getString("ISO_" + fieldIndex, null);
    }

    /**
     * 保存8583数据域
     *
     * @param context    context
     * @param fieldIndex 域索引
     * @param value      值
     */
    public void setIsoField(Context context, int fieldIndex, String value) {
        getDefaultPres(context).edit().putString("ISO_" + fieldIndex, value.trim()).apply();
    }

    /**
     * 获取终端流水号
     *
     * @param context      context
     * @param autoIncrease 是否自增
     * @param autoSave     自增后是否自动保存
     * @return 当前终端流水号
     */
    public String getPosSerial(Context context, boolean autoIncrease, boolean autoSave) {
        int serial = getNumber(context, KEY_POS_SERIAL);
        if (autoIncrease) {
            if (serial == 999999) {
                serial = 1;
            } else {
                serial++;
            }
        }
        if (autoSave) {
            setNumber(context, KEY_POS_SERIAL, serial);
        }
        String str = Integer.toString(serial);
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < 6 - str.length(); i++) {
            sBuilder.append("0");
        }
        sBuilder.append(str);
        return sBuilder.toString();
    }

    /**
     * 获取终端流水号
     *
     * @param context context
     * @return 终端流水号
     */
    public String getPosSerial(Context context) {
        return getPosSerial(context, true, true);
    }

    /**
     * 设置终端流水号
     *
     * @param context context
     * @param serial  流水号
     * @return 设置成功返回true，否则返回false
     */
    public boolean setPosSerial(Context context, String serial) {
        try {
            Integer i = Integer.valueOf(serial);
            if (i > 0 && i < 999999) {
                setNumber(context, KEY_POS_SERIAL, i);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取批次号
     *
     * @param context context
     * @return 当前批次号
     */
    public String getBatchNo(Context context) {
        return getValue(context, KEY_BATCH_NO);
    }

    /**
     * 设置批次号
     *
     * @param context context
     * @param value   批次号
     * @return 设置成功返回true，否则返回false
     */
    public boolean setBatchNo(Context context, String value) {
        try {
            Integer i = Integer.valueOf(value);
            if (i > 0 && i < 999999) {
                String str = Integer.toString(i);
                StringBuilder sBuilder = new StringBuilder();
                for (int j = 0; j < 6 - str.length(); j++) {
                    sBuilder.append("0");
                }
                sBuilder.append(str);
                setValue(context, KEY_BATCH_NO, sBuilder.toString());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置参数版本（同步）
     *
     * @param context Context
     * @param param   参数版本
     */
    public void setParamVersion(Context context, String param) {
        getDefaultPres(context).edit().putString(PARAM_VERSION, param).apply();
    }

    /**
     * 获取参数版本
     *
     * @param context Context
     * @return 参数版本
     */
    public String getParamVersion(Context context) {
        String value = getDefaultPres(context).getString(PARAM_VERSION, "0000");
        return value;
    }

    //获取参数值
    public String getParam(Context context, String key) {
        String value = getDefaultPres(context).getString(key, null);
        if (value == null) {
            value = getStringMetaData(context, key);
        }
        if (value == null) {
            value = getDefaultParam(key);
        }
        return value;
    }

    //设置参数值
    public void setParam(Context context, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            getDefaultPres(context).edit().putString(key, value).commit();
        }
    }

    private static String getDefaultParam(String key) {
        String value = "";
        switch (key) {
            case PARAM_VERSION:
                return "0000";
            case PARAM_PASSWARD:
                return "123456";
            case PARAM_CURRENT_TIME:
                return "";
            case PARAM_MOST_REFUND:
                return "1000000";
            case PARAM_FLAG_SWITCH:
                return "";
            case PARAM_IP_PORT:
                return "";
            case PARAM_TPDU:
                return "6000030000";
            case PARAM_SWITCH_DIALING:
                return "1";
            case PARAM_COUNT_RETRY:
                return "2";
            case PARAM_OUTLINE:
                return "";
            case PARAM_CENTERM_NUM1:
                return "";
            case PARAM_CENTERM_NUM2:
                return "";
            case PARAM_CENTERM_NUM3:
                return "";
            case PARAM_FLAG_SHIELD:
                return "";
            case PARAM_REVERSE_COUNT:
                return "3";
            case PARAM_PRINT_COUNT:
                return "2";
            case PARAM_PRINT_REMARK:
                return "";
            case PARAM_ELC_COUNT:
                return "1";
            case FLAG_SWITCH_LOGO:
                return "1";
            case PARAM_QRCODE_UP:
                return "";
            case PARAM_QRCODE:
                return "";
            case PARAM_QRCODE_DOWN:
                return "";
            case FLAG_BALANCE_SWITH:
                return "1";
            case FLAG_SALE_SWITH:
                return "1";
            case FLAG_VOID_SWITH:
                return "1";
            case FLAG_AUTH_SWITH:
                return "1";
            case FLAG_CANCEL_SWITH:
                return "1";
            case FLAG_COMPLETE_SWITH:
                return "1";
            case FLAG_COMPLETE_VOID_SWITH:
                return "1";
            case FLAG_REFUND_SWITH:
                return "1";
            case FLAG_SHENG_SWITH:
                return "1";
            case FLAG_WEI_SWITH:
                return "1";
            case FLAG_ALI_SWITH:
                return "1";
            case FLAG_BANK_SWITH:
                return "1";
            case FLAG_PRINT_ENGLISH:
                return "1";
            case FLAG_PRINT_QRCODE:
                return "1";
            case FLAG_PRINT_PAPER:
                return "1";
            case FLAG_VOID_CARD:
                return "1";
            case FLAG_VOID_PSW:
                return "1";
            case FLAG_COMPLETE_VOID_PSW:
                return "1";
            case FLAG_COMPLETE_VOID_CARD:
                return "1";
            case FLAG_COMPLETE_PSW:
                return "1";
            case FLAG_CANCEL_PSW:
                return "1";
            case FLAG_AUTH_HAND_CARD:
                return "0";
            case FLAG_TIP_PRINT_DETAIL:
                return "1";
            case FLAG_PRINT_VOID_DETAIL:
                return "0";
            case FLAG_PRINT_FAIL_DETAIL:
                return "0";
            case FLAG_PRINT_MINUS:
                return "1";
            case FLAG_SHIELD_CARD:
                return "0";
            case FLAG_AUTO_SIGN_OUT:
                return "1";
            case FLAG_REFUND_VOID_NEED_PSW:
                return "1";
            case FLAG_PREFER_CLSS:
                return "1";
            case FLAG_USE_INTEGRAL:
                return "1";
            case FLAG_USE_DISCOUNT:
                return "1";
            case FLAG_FEIJIE:
                return "1";
            case FLAG_TRACE_PSW:
                return "1";
            case FLAG_IS_OPEN_SIGN:
                return "0";
            case FLAG_TIP_COMFIRM_SIGN:
                return "1";
            case PARAM_SIGN_OUT_TIME:
                return "150";
            case PARAM_SIGN_UPLOAD_TIME:
                return "3";
            case PARAM_MAX_SIGN_CNT:
                return "300";
            case PARAM_MAX_RESIGN_CNT:
                return "3";
            case PARAM_WHEN_UPLOAD:
                return "0";
            case FLAG_SCAN_QUERY_COUNT:
                return "10";
            case FLAG_SCAN_WAIT_TIME:
                return "90";
            case FLAG_SCAN_INTERVAL_TIME:
                return "10";
            case FLAG_FIRST_CAPK_DOWNLOAD:
                return "1";
            case FLAG_FIRST_AID_DOWNLOAD:
                return "1";
            case FLAG_NEED_UPDATE_PARAM:
                return "0";
            case FLAG_NEED_UPDATE_CARDBIN:
                return "1";
            case FLAG_NEED_UPDATE_BLACK_CARDBIN:
                return "1";
            case PRESET_MERCHANT_ENGLISH_NAME:
                return "ShengPay";
            case PARAM_DEFINE_HEAD:
                return "银行卡POS签购单";
        }
        return value;
    }


}

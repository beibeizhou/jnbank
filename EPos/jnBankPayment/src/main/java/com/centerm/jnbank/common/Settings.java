package com.centerm.jnbank.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.centerm.jnbank.activity.MainActivity;
import com.centerm.jnbank.channels.EnumChannel;

import org.apache.log4j.Logger;

import config.BusinessConfig;

import static com.centerm.jnbank.common.Settings.KEY.FLAG_INIT;
import static com.centerm.jnbank.common.Settings.KEY.FLAG_TMK_EXIST;


/**
 * author: linwanliang</br>
 * date:2016/7/19</br>
 * 应用偏好设置信息，业务类参数设置请逐步迁移到{@link BusinessConfig}
 */
public class Settings {

    private static Logger logger = Logger.getLogger(Settings.class);

    public static class KEY {
        public final static String POS_CHANNEL = "POS_CHANNEL";//渠道
        public final static String KEK = "KEK";//KEK
        public final static String COMMON_IP1 = "COMMON_IP1";//IP
        public final static String COMMON_IP2 = "COMMON_IP2";//IP
        public final static String COMMON_IP_PARAM = "COMMON_IP_PARAM";//IP
        public final static String COMMON_DOMAIN_NAME = "COMMON_DOMAIN_NAME";//参数域名
        public final static String COMMON_PORT1 = "COMMON_PORT1";//端口
        public final static String COMMON_PORT2 = "COMMON_PORT2";//端口
        public final static String COMMON_PORT_PARAM = "COMMON_PORT_PARAM";//参数端口
        public final static String CLSS_CARD_PREFERED = "CLSS_CARD_PREFERED";//挥卡优先
        public final static String NET_RESP_TIMEOUT = "NET_RESP_TIMEOUT";//网络响应超时时间
        public final static String NET_CONNECT_TIMEOUT = "NET_CONNECT_TIMEOUT";//网络连接超时时间
        public final static String NORMAL_EXIT_FLAG = "NORMAL_EXIT_FLAG";//程序正常退出的标志
        public final static String AUTO_SIGN_OUT = "FLAG_AUTO_SIGN_OUT";//是否自动签退
        public final static String SLIP_VERSION = "SLIP_VERSION";//签购单版本号
        public final static String OPER_ID = "OPER_ID";//当前操作员
        public final static String PRINT_THREE = "PRINT_THREE";//是否三联打印
        public final static String OFF_LINE_CONSUME = "OFF_LINE_CONSUME";//是否三联打印

        public final static String BATCH_SEND_STATUS = "BATCH_SEND_STATUS";//0批结算初始化 1 批结未完成 2批结算完成
        public final static String PREV_BATCH_TOTAL = "PREV_BATCH_TOTAL";//上批次总计gson串
        public final static String BATCH_SEND_RETURN_DATA = "BATCH_SEND_RETURN_DATA";//对账返回数据
        public final static String IS_BATCH_EQUELS = "IS_BATCH_EQUELS";//是否对账平
        public final static String IC_AID_VERSION = "IC_AID_VERSION";//AID参数版本
        public final static String IC_CAPK_VERSION = "IC_CAPK_VERSION";//公钥参数版本
        public final static String FLAG_TMK_EXIST = "FLAG_TMK_EXIST";//TMK存在的标识
        public final static String FLAG_INIT = "FLAG_INIT";//TMK存在的标识
        public final static String FIRST_TIME_LOADING = "FIRST_TIME_LOADING";//程序是否第一次加载
        public final static String REVERSE_RETRY_TIMES = "REVERSE_RETRY_TIMES";//冲正重试次数
        public final static String LOCATION_LATITUDE = "LOCATION_LATITUDE";//纬度
        public final static String LOCATION_LONGTITUDE = "LOCATION_LONGTITUDE";//经度
        public final static String BASE_STATION_INFO = "BASE_STATION_INFO";//基站信息

        public static final String QPS_BIN_EXISTS = "QPS_BIN_EXISTS";
    }

    private static SharedPreferences getDefaultPres(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getStringMetaData(Context context, String key) {
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return info.metaData.getString(key);
    }

    private static int getIntMetaData(Context context, String key) {
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        int value = info.metaData.getInt(key);
        return value;
    }



    /**
     * 获取当前渠道名称
     *
     * @param context Context
     * @return AID参数版本
     */
    public static String getPosChannel(Context context) {
        String channel = getDefaultPres(context).getString(KEY.POS_CHANNEL, null);
        if (channel == null) {
            channel = getStringMetaData(context, KEY.POS_CHANNEL);
        }
        return channel;
    }

    /**
     * 设置当前渠道（异步）
     *
     * @param context Context
     * @param channel 渠道
     */
    public static void setPosChannel(Context context, EnumChannel channel) {
        if (channel != null) {
            getDefaultPres(context).edit().putString(KEY.POS_CHANNEL, channel.name()).apply();
        }
    }



    /**
     * 获取IP地址
     *
     * @param context Context
     * @return IP地址
     */
    public static String getCommonIp1(Context context) {
        String value = getDefaultPres(context).getString(KEY.COMMON_IP1, null);
        if (value == null) {
            value = getStringMetaData(context, KEY.COMMON_IP1);
        }
        return value;
    }
    public static String getCommonIp2(Context context) {
        String value = getDefaultPres(context).getString(KEY.COMMON_IP2, null);
        if (value == null) {
            value = getStringMetaData(context, KEY.COMMON_IP2);
        }
        return value;
    }
    public static String getParamIp(Context context) {
        String value = getDefaultPres(context).getString(KEY.COMMON_IP_PARAM, null);
        if (value == null) {
            value = getStringMetaData(context, KEY.COMMON_IP_PARAM);
        }
        return value;
    }
    //获取域名
    public static String getDomainName(Context context) {
        String value = getDefaultPres(context).getString(KEY.COMMON_DOMAIN_NAME, null);
        if (value == null) {
            value = getStringMetaData(context, KEY.COMMON_DOMAIN_NAME);
        }
        return value;
    }

    /**
     * 设置IP地址（同步）
     *
     * @param context Context
     * @param ip      IP
     */
    public static void setCommonIp1(Context context, String ip) {
        if (!TextUtils.isEmpty(ip)) {
            getDefaultPres(context).edit().putString(KEY.COMMON_IP1, ip).commit();
        }
    }
    public static void setCommonIp2(Context context, String ip) {
        if (!TextUtils.isEmpty(ip)) {
            getDefaultPres(context).edit().putString(KEY.COMMON_IP2, ip).commit();
        }
    }
    public static void setParamIp(Context context, String ip) {
        //if (!TextUtils.isEmpty(ip)) {
            getDefaultPres(context).edit().putString(KEY.COMMON_IP_PARAM, ip).commit();
        //}
    }
    //设置域名
    public static void setDomainName(Context context, String name) {
        if (!TextUtils.isEmpty(name)) {
            getDefaultPres(context).edit().putString(KEY.COMMON_DOMAIN_NAME, name).commit();
        }
    }

    /**
     * 获取端口号
     *
     * @param context Context
     * @return 服务器端口号
     */
    public static int getCommonPort1(Context context) {
        int value = getDefaultPres(context).getInt(KEY.COMMON_PORT1, -1);
        if (value == -1) {
            value = getIntMetaData(context, KEY.COMMON_PORT1);
        }
        return value;
    }
    public static int getCommonPort2(Context context) {
        int value = getDefaultPres(context).getInt(KEY.COMMON_PORT2, -1);
        if (value == -1) {
            value = getIntMetaData(context, KEY.COMMON_PORT2);
        }
        return value;
    }

    /**
     * 获取参数端口号
     *
     * @param context Context
     * @return 服务器端口号
     */
    public static int getCommonPortParam(Context context) {
        int value = getDefaultPres(context).getInt(KEY.COMMON_PORT_PARAM, -1);
        if (value == -1) {
            value = getIntMetaData(context, KEY.COMMON_PORT_PARAM);
        }
        return value;
    }

    /**
     * 设置端口号（同步）
     *
     * @param context Context
     * @param port    端口号
     */
    public static void setCommonPort1(Context context, int port) {
        if (!(port < 0 || port > 65535)) {
            getDefaultPres(context).edit().putInt(KEY.COMMON_PORT1, port).commit();
        }
    }
    public static void setCommonPort2(Context context, int port) {
        if (!(port < 0 || port > 65535)) {
            getDefaultPres(context).edit().putInt(KEY.COMMON_PORT2, port).commit();
        }
    }

    public static void setBaseStationInfo(Context context, String baseinfo) {
        getDefaultPres(context).edit().putString(KEY.BASE_STATION_INFO, baseinfo).commit();
    }

    public static String getBaseStationInfo(Context context) {
        return getDefaultPres(context).getString(KEY.BASE_STATION_INFO, "");
    }

    /**
     * 设置参数端口号（同步）
     *
     * @param context Context
     * @param port    端口号
     */
    public static void setCommonPortParam(Context context, int port) {
        if (!(port < 0 || port > 65535)) {
            getDefaultPres(context).edit().putInt(KEY.COMMON_PORT_PARAM, port).commit();
        }
    }

    //获取参数值
    public static String getParam(Context context,String key) {
        String value = getDefaultPres(context).getString(key, null);
        if (value == null) {
            value = getStringMetaData(context, key);
        }
        return value;
    }

    //设置参数值
    public static void setParam(Context context, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            getDefaultPres(context).edit().putString(key, value).commit();
        }
    }
    /**
     * 获取网络响应超时时间
     *
     * @param context Context
     * @return 网络超时时间
     */
    public static int getRespTimeout(Context context) {
        return getDefaultPres(context).getInt(KEY.NET_RESP_TIMEOUT, BusinessConfig.NET_RESP_TIMEOUT);
    }

    /**
     * 设置网络响应超时时间
     *
     * @param context Context
     * @param timeout 超时时间（毫秒）
     */
    public static void setRespTimeout(Context context, int timeout) {
        if (timeout > 0) {
            getDefaultPres(context).edit().putInt(KEY.NET_RESP_TIMEOUT, timeout).apply();
        }
    }

    /**
     * 获取连接超时时间
     *
     * @param context context
     * @return 连接超时时间
     */
    public static int getConnectTimeout(Context context) {
        return getDefaultPres(context).getInt(KEY.NET_CONNECT_TIMEOUT, BusinessConfig.NET_CONNECT_TIMEOUT);
    }

    /**
     * 设置连接超时时间
     *
     * @param context context
     * @return 连接超时时间
     */
    public static void setConnectTimeout(Context context, int timeout) {
        if (timeout > 0) {
            getDefaultPres(context).edit().putInt(KEY.NET_CONNECT_TIMEOUT, timeout).apply();
        }
    }


    /**
     * 判断程序上次退出是否为正常退出
     *
     * @param context Context
     * @return 是否正常退出的标志
     */
    public static boolean isLastNormalExit(Context context) {
        boolean isNormal = getDefaultPres(context).getBoolean(KEY.NORMAL_EXIT_FLAG, false);
        if (isNormal) {
            resetNormalExitFlag(context);
        }
        logger.info("上次程序是否正常退出：" + isNormal);
        return isNormal;
    }

    /**
     * 重置程序正常退出的标志
     *
     * @param context Context
     */
    private static void resetNormalExitFlag(Context context) {
        getDefaultPres(context).edit().putBoolean(KEY.NORMAL_EXIT_FLAG, false).commit();
    }

    /**
     * 将程序标识为正常退出，该方法需在{@link MainActivity#onDestroy()}方法中调用
     * 如果程序非正常退出，下次进入时需要重新签到
     *
     * @param context
     */
    public static void setNormalExitFlag(Context context) {
        getDefaultPres(context).edit().putBoolean(KEY.NORMAL_EXIT_FLAG, true).commit();
    }

    /**
     * 获取签购单版本号
     *
     * @param context Context
     * @return 签购单版本号
     */
    public static String getSlipVersion(Context context) {
        return getDefaultPres(context).getString(KEY.SLIP_VERSION, "000000000000");
    }

    /**
     * 设置当前签购单版本号
     *
     * @param context     Context
     * @param slipVersion 签购单版本号
     */
    public static void setSlipVersion(Context context, String slipVersion) {
        logger.info("正在保存新签购单版本：" + slipVersion);
        getDefaultPres(context).edit().putString(KEY.SLIP_VERSION, slipVersion).apply();
    }

    /**
     * 获取是否打印三联
     *
     * @param context Context
     */
    public static String getPrintThree(Context context) {
        return getDefaultPres(context).getString(KEY.PRINT_THREE, "N");
    }

    /**
     * 设置是否打印三联
     *
     * @param context      Context
     * @param isPrintThree 是否打印三联
     */
    public static void setPrintThree(Context context, String isPrintThree) {
        getDefaultPres(context).edit().putString(KEY.PRINT_THREE, isPrintThree).apply();
    }

    /**
     * 获取是否脱机消费
     *
     * @param context Context
     */
    public static String getOffLineConsume(Context context) {
        return getDefaultPres(context).getString(KEY.OFF_LINE_CONSUME, "N");
    }

    /**
     * 设置是否脱机消费
     *
     * @param context          Context
     * @param isOffLineConsume 是否脱机消费
     */
    public static void setOffLineConsume(Context context, String isOffLineConsume) {
        getDefaultPres(context).edit().putString(KEY.OFF_LINE_CONSUME, isOffLineConsume).apply();
    }

    /**
     * 获取保存的8583域数据。其中41域（受卡机终端标识码），42域（受卡方标识码），43域（商户名称）
     *
     * @param context    context
     * @param fieldIndex 域的索引值
     * @return 已保存的该域的值
     */
    public static String getIsoField(Context context, int fieldIndex) {
        return getDefaultPres(context).getString("Iso" + fieldIndex, null);
    }

    /**
     * 保存8583数据域
     *
     * @param context    context
     * @param fieldIndex 域索引
     * @param value      值
     */
/*    public static void setIsoField(Context context, int fieldIndex, String value) {
        getDefaultPres(context).edit().putString("Iso" + fieldIndex, value.trim()).apply();
    }*/

    /**
     * 获取当前操作员ID，默认为01
     *
     * @param context context
     * @return 操作员ID
     */
    public static String getOperId(Context context) {
        return getDefaultPres(context).getString(KEY.OPER_ID, null);
    }

    public static void setValue(Context context, String key, String value) {
        getDefaultPres(context).edit().putString(key, value).apply();
    }

    public static String getValue(Context context, String key, String defValue) {
        return getDefaultPres(context).getString(key, defValue);
    }

    public static void setValue(Context context, String key, boolean value) {
        getDefaultPres(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getValue(Context context, String key, boolean defValue) {
        return getDefaultPres(context).getBoolean(key, defValue);
    }

    public static void setValue(Context context, String key, int count) {
        getDefaultPres(context).edit().putInt(key, count).apply();
    }

    public static int getValue(Context context, String key, int count) {
        return getDefaultPres(context).getInt(key, count);
    }

    /**
     * 获取TMK存在的标识
     *
     * @param context context
     * @return 是否有TMK
     */
    public static boolean hasTmk(Context context) {
        return getDefaultPres(context).getBoolean(FLAG_TMK_EXIST, false);
    }

    /**
     * 设置TMK已存在
     *
     * @param context
     */
    public static void setTmkExist(Context context) {
        getDefaultPres(context).edit().putBoolean(FLAG_TMK_EXIST, true).apply();
    }


    /**
     * 终端是否初始化
     * @param context
     * @return
     */
    public static boolean hasInit(Context context) {
        return getDefaultPres(context).getBoolean(FLAG_INIT, false);
    }

    /**
     * 初始化终端
     * @param context
     */
    public static void setInit(Context context) {
        getDefaultPres(context).edit().putBoolean(FLAG_INIT, true).apply();
    }
}

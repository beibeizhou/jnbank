package config;

import android.os.Environment;

import com.centerm.jnbank.R;
import com.centerm.jnbank.utils.CommonUtils;

import java.io.File;


/**
 * 通用配置
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */

public class Config {
    public final static String SECRET_KEY_CODE = "12121122";

    public final static boolean DEBUG_FLAG = false;
    public final static String DEBUG_SN = "D1FL100000003";
    public final static int DEFAULT_MENU_ITEM_ICON = R.drawable.ic_launcher;

    public final static String DB_NAME = "epos.db";
    public final static int DB_VERSION = 1;
    public final static long TRADE_INFO_LIST_PAGE_SIZE = 20;
    public final static int MAX_RETRY_TIMES = 3;
    public final static long LOCATION_INTERVAL = 10 * 60 * 1000;//上一次定位成功，后续10分钟定位一次
    public final static long LOCATION_INTERVAL_SHORT = 1 * 60 * 1000;//上一次定位失败，后续1分钟定位一次
    public final static long APP_VERSION_CHECK_INTERVAL = 10 * 60 * 1000;//应用版本检测服务间隔
    public final static long UPLOAD_FAIL_ELEC_SIGN_INTERVAL = 1 * 60 * 1000;//检测上传电子签名图片
    public final static long PAGE_TIMEOUT = 30*1000;//页面超时时间

    public static class Path {
        public final static String ROOT = Environment.getExternalStorageDirectory() + File.separator
                + "EPos";
        public final static String DOWNLOAD_PATH = ROOT + File.separator + "files";//下载文件存放目录
        public final static String RECEIPT_PATH = ROOT + File.separator + "receipts";//电子签购单存放目录
        public final static String SIGN_PATH = ROOT + File.separator + "elecsign";//电子签名图片存放目录
        public final static String CODE_PATH = ROOT + File.separator + "code";//二维码存放路径
        public final static String CODE_FILE_NAME = "erweima.png";//二维码文件名
        public static final String DEFAULT_LOG_PATH = CommonUtils.getSDPath() + "/cpayResource/log/";    //收单日志日志文件的存放目录
    }

    public static class DebugToggle {
        public final static boolean UPLOAD_IC_SCRIPT = false;//是否开启IC卡脚本通知业务
    }

    public static class XML {
        public final static String PROCESS_PATH = "process/";
        public final static String MSG_DEFINE_PATH = "msg/define/";
        public final static String MSG_ADAPTER_PATH = "msg/adapter/";
    }

    public final static String DEFAULT_ADMIN_ACCOUNT = "99";//系统管理员账号
    public final static String DEFAULT_ADMIN_PWD = "12345678";//系统管理员密码
    public final static String DEFAULT_MSN_ACCOUNT = "00";//主管帐号
    public final static String DEFAULT_MSN_PWD = "123456";//主管密码

    public final static String OPT_NO_TIP = "OPT_NO_TIP";
    public final static String OPT_PWD_TIP = "OPT_PWD_TIP";
    public final static String OPT_TYPE_TIP = "OPT_TYPE_TIP";

    public final static int OPT_TYPE_UPDATE = 1;
    public final static int OPT_TYPE_CREATE = 2;
    public final static int BATCH_MAX_UPLOAD_TIMES = 4;
    public final static int PRINT_NEXT_TIME = 4;//打印时自动打印下一联时间
    public final static int HTTP_RESPONSE_TIMEOUT = 60 * 1000;//默认响应超时时间
    public final static int HTTP_CONNECTION_TIMEOUT = 5 * 1000;//默认连接超时时间

}

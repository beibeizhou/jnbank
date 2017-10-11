package com.centerm.jnbank.base;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.cpay.midsdk.dev.define.system.GlobalTouchListener;
import com.centerm.jnbank.ActivityStack;
import com.centerm.jnbank.R;
import com.centerm.jnbank.channels.EnumChannel;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.StopWatch;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.xml.XmlTag;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.apache.log4j.Logger;

import java.util.List;

import config.BusinessConfig;
import config.Config;

/**
 * author:wanliang527</br>
 * date:2016/10/21</br>
 */

public abstract class BaseActivity extends AppCompatActivity {

    public static final int REQ_REVERSE = 0x12;//前往联机交互界面
    public static final int REQ_INPUT_DIREOTOR_PWD = 0x13;//前往输入主管密码的界面

    public static final String KEY_RECREATE_FLAG = "KEY_RECREATE_FLAG";
    public static final String KEY_PROCESS = "KEY_PROCESS";
    public static final String KEY_MENU = "KEY_MENU";
    public static final String KEY_TRANSCODE = "KEY_TRANSCODE";
    public static final String KEY_ORIGIN_INFO = "KEY_ORIGIN_INFO";
    public static final String KEY_TRADE_INFO = "KEY_TRADE_INFO";
    public static final String KEY_USER_FLAG = "KEY_USER_FLAG";//登录用户标识，1-系统管理员；2-主管操作员
    public static final String KEY_INSERT_SALE_FLAG = "KEY_INSERT_SALE_FLAG";//插卡消费标识（插卡消费与消费使用的是同一个流程以及同样的交易码）
    public static final String KEY_CLSS_FORCE_PIN_FLAG = "KEY_CLSS_FORCE_PIN_FLAG";//闪付凭密标识

    public static final String ENTRY_FALG = "ENTRY_FALG";//支付组件标识
    public static final String ENTRY_CONTROL_INFO = "ENTRY_CONTROL_INFO";//支付组件开关标识
    public static final String ENTRY_PRINT_COUNT = "ENTRY_PRINT_COUNT";//支付组件打印标识
    public static boolean reverseFlag;//冲正标识，决定下次联机前是否进行冲正

    protected Logger logger = Logger.getLogger(this.getClass());
    protected Context context;
    protected ActivityStack activityStack = ActivityStack.getInstance();
    protected EnumChannel posChannel;
    protected DbHelper dbHelper;
    private boolean isPause;
    private SecretCodeResponder codeResponder;//暗码事件响应
    private long lastKeyEventTime;//上一次实体按键的时间
    private StringBuilder secretCode = new StringBuilder();//暗码组合器

    protected long pageTimeout = Config.PAGE_TIMEOUT;
    private GlobalTouchListener touchListener;
    private StopWatch stopWatch;
    private StopWatch.TimeoutHandler timeoutHandler;
    public static boolean isActive; //全局变量


    @Override
    protected void onResume() {
        isPause = false;
        if (!isActive) {
            //app 从后台唤醒，进入前台
            isActive = true;
            logger.info( "程序从后台唤醒");

        }
        super.onResume();

    }

    /**
     * 初始化数据对象，在{@method onInitView}和{@method onLayoutId}之前进行
     */
    public void onInitLocalData(Bundle savedInstanceState) {
        posChannel = EnumChannel.valueOf(Settings.getPosChannel(this));
    }

    public void afterInitView() {
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    /**
     * 在此方法中返回Activty的布局ID
     *
     * @return 返回该Activity界面布局ID
     */
    public abstract int onLayoutId();

    /**
     * 初始化界面视图
     */
    public abstract void onInitView();

    /**
     * @return 是否开启数据库模块
     */
    public boolean isOpenDataBase() {
        return false;
    }

    public void openPageTimeout(final boolean isSignPage) {
        if (stopWatch == null) {
            stopWatch = new StopWatch(context, this.pageTimeout);
        }
        if (timeoutHandler == null) {
            timeoutHandler = new StopWatch.TimeoutHandler() {
                @Override
                public void onTimeout() {
                    stopWatch.stop();//停止计时任务
                    AlertDialog dialog = null;
                    if (isSignPage) {
                         dialog = DialogFactory.showMessageDialog(context, "提示", "长时间未签名\n是否跳过电子签名", new AlertDialog.ButtonClickListener() {
                            @Override
                            public void onClick(AlertDialog.ButtonType button, View v) {
                                switch (button) {
                                    case POSITIVE:
                                        jumpMethod();
                                        break;
                                    case NEGATIVE:
                                        stopWatch.start();//重新开始计时任务
                                        break;
                                }
                            }
                        }, 30);
                    } else {
                         dialog = DialogFactory.showMessageDialog(context, "提示", "长时间未操作\n是否返回主界面", new AlertDialog.ButtonClickListener() {
                            @Override
                            public void onClick(AlertDialog.ButtonType button, View v) {
                                switch (button) {
                                    case POSITIVE:
                                        onBackPressed();
                                        break;
                                    case NEGATIVE:
                                        stopWatch.start();//重新开始计时任务
                                        break;
                                }
                            }
                        }, 30);
                    }
                    if (null != dialog) {
                        dialog.setAutoPerformPositive(true);
                    }
                }
            };
        }
        stopWatch.setTimeoutHandler(timeoutHandler);
        stopWatch.start();
        if (touchListener == null) {
            touchListener = new GlobalTouchListener() {
                @Override
                public void onTouch(int i) {
                    stopWatch.reset();//计时复位
                }
            };
            try {
                ISystemService systemService = DeviceFactory.getInstance().getSystemDev();
                systemService.addGlobalTouchListener(touchListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void jumpMethod(){};

    public void closePageTimeout() {
        if (touchListener != null) {
            try {
                ISystemService systemService = DeviceFactory.getInstance().getSystemDev();
                systemService.removeGlobalTouchListener(touchListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (stopWatch != null) {
            stopWatch.stop();
            stopWatch = null;
        }
    }

    private final void OpenDatabase() {
        if (isOpenDataBase())
            dbHelper = OpenHelperManager.getHelper(context.getApplicationContext(), DbHelper.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        if(!(this instanceof EntryActivity)) {
            activityStack.push(this);
        }
        OpenDatabase();
        onInitLocalData(savedInstanceState);
        int layoutId = onLayoutId();
        if (layoutId > 0) {
            setContentView(layoutId);
            _initView();
        }
        onInitView();
        afterInitView();

    }

    private final void _initView() {
        View v = findViewById(R.id.imgbtn_back);
        if (v != null)
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
    }


    /**
     * 检测终端设备是否准备就绪，准备就绪即可开始交易
     *
     * @return 如果准备就绪，返回true，否则返回false
     */
    protected boolean isDeviceReady() {
        DeviceFactory factory = DeviceFactory.getInstance();
        return factory.isAvailable();
    }

    protected boolean[] isNeedLoadOrSignin(String menuTag) {
        boolean needLoad = Settings.hasInit(context);
        boolean needTmk = Settings.hasTmk(context);
        boolean needSigned = !BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.FLAG_SIGN_IN);
        switch (menuTag) {
            case XmlTag.MenuTag.OBTAIN_TMK:
                return new boolean[]{!needLoad,false, false};
            case XmlTag.MenuTag.DOWNLOAD_CAPK:
            case XmlTag.MenuTag.DOWNLOAD_AID:
            case XmlTag.MenuTag.DOWNLOAD_CARD_BIN:
            case XmlTag.MenuTag.DOWNLOAD_QPS_PARAMS:
            case XmlTag.MenuTag.LOAD_PARAM:
            case XmlTag.MenuTag.POS_SIGN_IN:
                return new boolean[]{!needLoad,!needTmk, false};
            case XmlTag.MenuTag.BALANCE:
            case XmlTag.MenuTag.SALE:
            case XmlTag.MenuTag.VOID:
            case XmlTag.MenuTag.REFUND:
            case XmlTag.MenuTag.AUTH:
            case XmlTag.MenuTag.AUTH_COMPLETE:
            case XmlTag.MenuTag.AUTH_SETTLEMENT:
            case XmlTag.MenuTag.CANCEL:
            case XmlTag.MenuTag.COMPLETE_VOID:
            case XmlTag.MenuTag.SCAN_PAY_WEI:
            case XmlTag.MenuTag.SCAN_PAY_ALI:
            case XmlTag.MenuTag.SCAN_PAY_SFT:
            case XmlTag.MenuTag.SCAN_CANCEL:
            case XmlTag.MenuTag.SCAN_LAST_SERCH:
            case XmlTag.MenuTag.SCAN_REFUND_W:
            case XmlTag.MenuTag.SCAN_REFUND_Z:
            case XmlTag.MenuTag.SCAN_REFUND_S:
                return new boolean[]{!needLoad,!needTmk, needSigned};
            default:
                return new boolean[]{false,false, false};
        }
    }


    protected boolean isNeedSignOut(String menuTag) {
        boolean needSignOut = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT);
        switch (menuTag) {
            case XmlTag.MenuTag.POS_SIGN_IN:
            case XmlTag.MenuTag.BALANCE:
            case XmlTag.MenuTag.SALE:
            case XmlTag.MenuTag.VOID:
            case XmlTag.MenuTag.REFUND:
            case XmlTag.MenuTag.AUTH:
            case XmlTag.MenuTag.AUTH_COMPLETE:
            case XmlTag.MenuTag.AUTH_SETTLEMENT:
            case XmlTag.MenuTag.CANCEL:
            case XmlTag.MenuTag.COMPLETE_VOID:
            case XmlTag.MenuTag.SALE_BY_INSERT:
            case XmlTag.MenuTag.QUICK_SALE_NEED_PASWD:
            case XmlTag.MenuTag.QUICK_AUTH_NEED_PASWD:
            case XmlTag.MenuTag.SCAN_PAY_WEI:
            case XmlTag.MenuTag.SCAN_PAY_ALI:
            case XmlTag.MenuTag.SCAN_PAY_SFT:
            case XmlTag.MenuTag.SCAN_CANCEL:
            case XmlTag.MenuTag.SCAN_SERCH:
            case XmlTag.MenuTag.SCAN_LAST_SERCH:
            case XmlTag.MenuTag.SCAN_REFUND_W:
            case XmlTag.MenuTag.SCAN_REFUND_Z:
            case XmlTag.MenuTag.SCAN_REFUND_S:
                return needSignOut;
            default:
                return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    public void onBackPressed() {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>重复的onBackPressed事件，不响应！");
            return;
        }
        activityStack.pop();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (codeResponder != null) {
            long now = System.currentTimeMillis();
            if (now - lastKeyEventTime > 2000) {
                secretCode.setLength(0);
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                return super.dispatchKeyEvent(event);
            }
            lastKeyEventTime = now;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    secretCode.append("1");
                    break;
                case KeyEvent.KEYCODE_HOME:
                    secretCode.append("2");
                    break;
                case KeyEvent.KEYCODE_BACK:
                    secretCode.append("3");
                    break;
            }
            String code = secretCode.toString();
//            logger.info("暗码："+code);
            if (Config.SECRET_KEY_CODE.equals(code) && codeResponder != null) {
                logger.debug(getClass().getSimpleName() + "==>响应暗码事件");
                codeResponder.onResponse();
                secretCode.setLength(0);
            }
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
            return super.dispatchKeyEvent(event);
        } else {
//            logger.warn("不存在响应：" + event.getKeyCode());
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_RECREATE_FLAG, true);
        logger.info("onSaveInstanceState, recreate flag is true");
    }


    @Override
    protected void onPause() {
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (!isAppOnForeground()) {
            //app 进入后台
            isActive = false;//记录当前已经进入后台
            logger.info("程序进入后台");
        }
        super.onStop();
    }

    /**
     * APP是否处于前台唤醒状态
     *
     * @return
     */
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        if (isOpenDataBase()) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
//        if(conn!=null){
//            unbindService(conn);
//        }
        super.onDestroy();
    }

    @Override
    public void setTitle(int titleId) {
        TextView titleShow = (TextView) findViewById(R.id.txtvw_title);
        if (titleShow == null) {
            logger.warn(this.getClass().getSimpleName() + "==>设置标题失败");
            return;
        }
        titleShow.setText(titleId);
    }

    public boolean isPause() {
        return isPause;
    }

    /**
     * 隐藏整个标题栏
     */
    public void hideTitleBar() {
        View view = findViewById(R.id.title_block);
        if (view != null) {
            view.setVisibility(View.GONE);
        } else {
            logger.warn("==>隐藏标题栏失败");
        }
    }

    public void showTitleBar() {
        View view = findViewById(R.id.title_block);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        } else {
            logger.warn("==>显示标题栏失败");
        }
    }

    /**
     * 隐藏返回按钮
     */
    public void hideBackBtn() {
        ImageButton backBtn = (ImageButton) findViewById(R.id.imgbtn_back);
        if (backBtn == null) {
            logger.warn(this.getClass().getSimpleName() + "==>隐藏返回按钮失败");
            return;
        }
        backBtn.setVisibility(View.GONE);
    }

    /**
     * 显示返回按钮
     */
    public void showBackBtn() {
        final ImageButton backBtn = (ImageButton) findViewById(R.id.imgbtn_back);
        if (backBtn == null) {
            logger.warn(this.getClass().getSimpleName() + "==>显示返回按钮失败");
            return;
        }
    }

    /**
     * 显示右侧的功能按钮，按钮点击事件由{@link #onRightButtonClick(View)}方法触发
     *
     * @param label 按钮标签
     */
    public void showRightButton(String label) {
        Button button = (Button) findViewById(R.id.btn_title_right);
        if (button != null) {
            button.setVisibility(View.GONE);
            button.setText(label);
        } else {
            logger.warn(this.getClass().getSimpleName() + "==>显示[" + label + "]按钮失败");
        }
    }

    public void onRightButtonClick(View view) {
    }

    /**
     * 结束当前界面并跳转到登录界面
     */
/*    protected void jumpToLogin() {
        //在此处处理，在用户签退或者退出时，将操作员账号置空
        BusinessConfig config = BusinessConfig.getInstance();
        config.setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
        Intent intent = new Intent(context, LoginActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activityStack.removeExcept(LoginActivity.class);
            }
        }, 300);
        startActivity(intent);
    }*/


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            //点击空白处将输入法隐藏
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }



    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    protected SecretCodeResponder getCodeResponder() {
        return codeResponder;
    }

    protected void setCodeResponder(SecretCodeResponder codeResponder) {
        this.codeResponder = codeResponder;
    }
    /**
     * 暗码事件响应者
     */
    protected interface SecretCodeResponder {
        void onResponse();
    }

}
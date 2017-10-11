package com.centerm.jnbank;

import android.content.Context;

import com.centerm.jnbank.utils.ViewUtils;

/**
 * 未知异常捕获器
 * author:wanliang527</br>
 * date:2016/10/30</br>
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler instance;
    private Context context;

    public static CrashHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler(context);
                }
            }
        }
        return instance;
    }

    private CrashHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ViewUtils.showToast(context, "程序未知异常，请重新进入应用");
    }
}

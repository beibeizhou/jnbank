package com.centerm.jnbank.utils;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;

/**
 * 跟View相关的工具类
 *
 * @author wanliang527
 * @date 2014-1-17
 */
public class ViewUtils {
    public static final String STATUS_BAR_SERVICE = "statusbar";
    static Toast toast = null;

    public final static LayoutParams MATCH_MATCH = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    public final static LayoutParams MATCH_WRAP = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    public final static LayoutParams WRAP_MATCH = new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    public final static LayoutParams WRAP_WRAP = new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    /**
     * 显示Toast
     *
     * @param c
     * @param txt
     */
    public static void showToast(Context c, String txt) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(c.getApplicationContext(), txt, Toast.LENGTH_SHORT);
        toast.show();
        //Toast.makeText(c, txt, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示Toast
     *
     * @param c
     * @param stringId
     */
    public static void showToast(Context c, int stringId) {
        Toast.makeText(c, stringId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取LayoutInflater对象
     *
     * @param context
     * @return
     */
    public static LayoutInflater getInflater(Context context) {
        return (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 设置View的背景图
     *
     * @param v
     * @param obj 可以是Drawable对象也可以是colorid、resId
     * @return 设置成功返回true，否则返回false
     */
    public static boolean setViewBackground(View v, Object obj) {
        if (v == null || obj == null)
            return false;
        if (obj instanceof Drawable) {
            v.setBackgroundDrawable((Drawable) obj);
            return true;
        }
        if (obj instanceof Integer) {
            try {
                v.setBackgroundResource((Integer) obj);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    v.setBackgroundColor((Integer) obj);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public static int getThemeResource(Context context, int attrName) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attrName, value, true);
        return value.resourceId;
    }

    public static void enableStatusBar(Context context) {
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, 0x00000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableStatusBar(Context context) {
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, 0x00010000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String gb2312ToUtf8(String str) {
        String urlEncode = "";
        try {
            urlEncode = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlEncode;
    }
}

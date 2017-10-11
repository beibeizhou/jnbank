package com.centerm.jnbank.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.view.LockDialog;

import org.apache.log4j.Logger;

import config.Config;

/**
 * 对话框工厂
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */

public class DialogFactory {
    private static Logger logger = Logger.getLogger(DialogFactory.class);
    private static Dialog current;

    /**
     * 显示信息提示类对话框，含有一个确认按钮，点击确认按钮后，对话框消失
     *
     * @param context context
     * @param title
     * @param message
     */
    public static void showMessageDialog(Context context, String title, String message) {
        showMessageDialog(context, title, message, null);
    }

    public static void showMessageDialog(Context context, String title, String message, boolean autoDismiss) {
        showMessageDialog(context, title, message, null, autoDismiss);
    }

    /**
     * 显示信息提示类对话框，含有一个确认按钮，点击确认按钮后，对话框消失
     *
     * @param context
     * @param title
     * @param message
     * @param listener
     */

    public static void showMessageDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        showMessageDialog(context, title, message, listener, true);
    }

    public static void showMessageDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, boolean autoDismiss) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.hideNegative();
        dialog.setAutoDismiss(autoDismiss);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        if (null != listener) {
            dialog.setClickListener(listener);
        }
        current = dialog;
        current.show();
    }

    public static AlertDialog showMessageDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, int timeout) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.setTimeout(timeout);
        dialog.setAutoDismiss(true);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        if (null != listener) {
            dialog.setClickListener(listener);
        }
        current = dialog;
        current.show();
        return dialog;
    }


    /**
     * 显示打印的提示框
     *
     * @param context
     * @param listener
     */

    public static void showPrintDialog(Context context, AlertDialog.ButtonClickListener listener) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.hideTitle();
        dialog.setAutoPerformPositive(true);
        dialog.setDialogMsg(context.getString(R.string.tip_print_next));
        if (null != listener) {
            dialog.setClickListener(listener);
        }
        dialog.show(Config.PRINT_NEXT_TIME);
        current = dialog;
    }


    /**
     * 显示选择对话框，默认选择按钮为“确认”和“取消”
     *
     * @param context  context
     * @param title    标题
     * @param message  信息
     * @param listener 按钮监听器
     */
    public static void showSelectDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        showSelectDialog(context, title, message, listener, true);
    }

    public static void showSelectDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, boolean autoDismiss) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.setAutoDismiss(autoDismiss);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        current = dialog;
        current.show();
    }

    public static AlertDialog showCancelTradingDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.setAutoDismiss(false);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setButtonText(context, "确认退出", "取  消");
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        return dialog;
    }
    public static void showLockDialog(Context context) {
        hideAll();
        LockDialog lockDialog = new LockDialog(context);
        lockDialog.show();
    }

    /**
     * 显示选择对话框，默认选择按钮为“确认”和“取消”
     *
     * @param context  context
     * @param title    标题
     * @param message  信息
     * @param listener 按钮监听器
     */
    public static void showSelectPirntDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setButtonText(context, "重试", "取消");
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        current = dialog;
        current.show();

    }

    /**
     * 显示加载对话框
     *
     * @param context context
     * @param message 提示信息
     */
    public static void showLoadingDialog(Context context, String message) {
        hideAll();
        current = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Dialog);
        ((ProgressDialog) current).setMessage(message);
        ((ProgressDialog) current).setProgressStyle(ProgressDialog.STYLE_SPINNER);
        current.setCanceledOnTouchOutside(false);
        current.setCancelable(false);
        current.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });
        current.show();
    }

 /*   public static ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        return dialog;
    }
*/

    public static void hideAll() {
        if (current != null) {
            try {
                current.dismiss();
                current = null;
                logger.info("隐藏所有对话框成功");
            } catch (Exception e) {
                logger.warn("隐藏所有对话框失败==>"+e.toString());
            }
        }
    }

    private Dialog createDialog(Context context) {
        return new Dialog(context);
    }


    public static Dialog createMessageDialog(final Context mCtx, String title) {
        final Dialog dialog = new Dialog(mCtx, R.style.DialogStyle);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = mCtx.getResources().getDisplayMetrics().widthPixels * 540 / 720;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.setContentView(R.layout.dialog_message);
        TextView txtvwMessage = (TextView) dialog.findViewById(R.id.txtvw_message);
        txtvwMessage.setText(title);
        dialog.findViewById(R.id.btn_message_sure).setOnClickListener(new CloseDialog(dialog));
        dialog.findViewById(R.id.btn_message_cancel).setOnClickListener(new CloseDialog(dialog));
        dialog.setCancelable(false);
        return dialog;
    }

    private static class CloseDialog implements View.OnClickListener {

        private Dialog dialog;

        public CloseDialog(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            if (dialog != null)
                dialog.dismiss();
        }
    }

/*    public interface ButtonClickListener {
        void onClick(View view);
    }*/

}

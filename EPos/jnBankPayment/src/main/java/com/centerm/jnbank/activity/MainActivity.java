package com.centerm.jnbank.activity;

import android.content.Intent;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.jnbank.base.MenuActivity;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.view.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

import config.BusinessConfig;


public class MainActivity extends MenuActivity {
    private Intent sendSignService;


    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitView() {
        super.onInitView();
        com.centerm.jnbank.net.NetUtils.higherNetUseExample(context, NetworkCapabilities.TRANSPORT_CELLULAR);
        hideBackBtn();
    }

    @Override
    public void onRightButtonClick(View view) {
        super.onRightButtonClick(view);
        tipToExit();
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
//        Settings.setValue(this, Settings.KEY.LOCATION_LATITUDE, "" + 31.701298);
//        Settings.setValue(this, Settings.KEY.LOCATION_LONGTITUDE, "" + 119.957581);
    }


    @Override
    public void afterInitView() {
        super.afterInitView();
        boolean isLock = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.KEY_IS_LOCK);
        if (isLock) {
            DialogFactory.showLockDialog(context);
        } else {
            beginAutoSign();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //标识应用为正常退出
        Settings.setNormalExitFlag(context);
    }

    @Override
    public void onBackPressed() {
        tipToExit();
    }

    private void tipToExit() {
        if (BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT)) {
            DialogFactory.showSelectDialog(context, null, "批结算完成，立即签退！", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            Intent intent = new Intent(context, TradingActivity.class);
                            intent.putExtra(KEY_TRANSCODE, TransCode.SIGN_OUT);
                            context.startActivity(intent);
                            break;
                    }
                }
            });
            return;
        }
        super.onBackPressed();
    }

    private boolean beginAutoSign() {
        BusinessConfig config = BusinessConfig.getInstance();
        String today = new SimpleDateFormat("MMdd").format(new Date());
        String lastSignDate = config.getValue(context, BusinessConfig.Key.KEY_LAST_SIGNIN_DATE);
        logger.warn("上次签到日期：" + lastSignDate);
        //每日强制签到
        if (!today.equals(lastSignDate)) {
            config.setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
        }
        if (!NetUtils.isNetConnected(context)) {
            return false;
        }
       /* if (!Settings.hasInit(context)) {
            ViewUtils.showToast(context,"请初始化机具！");
            return false;
        }
        if (!Settings.hasTmk(context)) {
            ViewUtils.showToast(context,"请下载主密钥！");
            return false;
        }*/
        boolean hasSignined = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.FLAG_SIGN_IN);
        String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if (!hasSignined && !(StringUtils.isStrNull(operId) || "00".equals(operId) || "99".equals(operId))) {
            //普通操作员，每次进入到主界面必须签到（如POS在使用过程中掉电，重新开机后操作员需要重新签到）
            beginProcess(TransCode.SIGN_IN, "AUTO_SIGN_IN");
            return true;
        }
        return false;
    }
}


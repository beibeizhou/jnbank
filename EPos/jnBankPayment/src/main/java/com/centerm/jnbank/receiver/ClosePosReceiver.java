package com.centerm.jnbank.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import config.BusinessConfig;

/**
 * Created by ysd on 2017/1/21.
 */

public class ClosePosReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isLock =  BusinessConfig.getInstance().getFlag(context,BusinessConfig.Key.KEY_IS_LOCK);
        if (!isLock) {
            BusinessConfig config = BusinessConfig.getInstance();
            //config.setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
        }
    }
}

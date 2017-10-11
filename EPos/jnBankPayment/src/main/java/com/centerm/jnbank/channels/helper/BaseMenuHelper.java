package com.centerm.jnbank.channels.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

/**
 * author:wanliang527</br>
 * date:2016/11/11</br>
 */

public abstract class BaseMenuHelper implements IMenuHelper {

    protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    protected void jumpTo(Context context, Class<? extends Activity> clz) {
        Intent intent = new Intent(context, clz);
        context.startActivity(intent);
    }
}

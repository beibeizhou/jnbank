package com.centerm.jnbank.net;

import android.content.Context;

import org.apache.log4j.Logger;

/**
 * author: wanliang527</br>
 * date:2016/10/10</br>
 */

public class NetClient {
    private Logger logger = Logger.getLogger(this.getClass());
    private static NetClient instance;
    private Context context;

    private NetClient() {
    }

    public static NetClient getInstance(Context context) {
        if (instance == null) {
            synchronized (NetClient.class) {
                if (instance == null) {
                    instance = new NetClient();
                }
            }
        }
        instance.context = context.getApplicationContext();
        return instance;
    }



}

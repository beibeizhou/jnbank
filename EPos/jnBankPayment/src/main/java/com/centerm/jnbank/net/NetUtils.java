package com.centerm.jnbank.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import org.xutils.common.util.LogUtil;

/**
 * 创建日期：2017/9/21 0021 on 19:52
 * 描述:
 * 作者:周文正
 */

public class NetUtils {
    private static ConnectivityManager cm;
    /**切换网络，需要移动网络可以使用的情况下才行
     * @param cnt
     * @param type
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void higherNetUseExample(Context cnt , int type){
        Log.e("huang","higherNetUseExa" +
                "mple():"+type);
        cm = (ConnectivityManager)cnt.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder req = new NetworkRequest.Builder();
        req.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        req.addTransportType(type);
        NetworkRequest request = req.build();
        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                LogUtil.d("gdrcu -----> onAvailable()");
                ConnectivityManager.setProcessDefaultNetwork(network);
                NetworkInfo info =  cm.getNetworkInfo(network);
                LogUtil.d( "当前网络:"+info.getTypeName()+" type:"+info.getType());

            }
        };
        cm.requestNetwork(request, callback);

    }
}

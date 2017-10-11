package com.centerm.jnbank.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by yuhc on 2017/3/10.
 */

public class UseMobileNetwork extends Thread {
    private static final String TAG = UseMobileNetwork.class.getSimpleName();
    ConnectivityManager connectivityManager;

    public UseMobileNetwork(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
    }

    //经测试，此接口功能无效，即不会释放移动网络
    public static void releaseMobileNetwork(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            manager.bindProcessToNetwork(null);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager.setProcessDefaultNetwork(null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        super.run();
        // android 4.x时的API
        // startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, FEATURE_ENABLE_HIPRI);
        // requestNetwork(NetworkRequest, ConnectivityManager.NetworkCallback)
        //  requestRouteToHost(int networkType, int hostAddress)
        //  setProcessDefaultNetwork(Network) and getSocketFactory()
        // 以下操作要求API在21以上，包括21。
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        //当WIFI网络能够访问公网时，需要添加此属性，否则还是走不了移动网络。
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
//                          WIFI网络：NetworkCapabilities.TRANSPORT_WIFI
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest networkRequest = builder.build();
        NetworkCallback networkCallback = new NetworkCallback();
        connectivityManager.requestNetwork(networkRequest, networkCallback);
//        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            boolean result = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = connectivityManager.bindProcessToNetwork(network);
            }else
                result = ConnectivityManager.setProcessDefaultNetwork(network);
            Log.d(TAG, "^_^ Mobile Network Is Available! result:"+result+"^_^");

            //该方法可释放移动网络
//            connectivityManager.unregisterNetworkCallback(this);
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            Log.d(TAG, "^_^ Mobile Network Is Lost! ^_^");
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            Log.d(TAG, "^_^ Mobile Network onCapabilitiesChanged! ^_^");
        }

        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            Log.d(TAG, "^_^ Mobile Network onLinkPropertiesChanged! ^_^");
        }

        @Override
        public void onLosing(Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            Log.d(TAG, "^_^ Mobile Network onLosing! ^_^");
        }
    }

}

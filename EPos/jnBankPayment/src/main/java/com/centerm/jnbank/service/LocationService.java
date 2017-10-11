package com.centerm.jnbank.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.centerm.jnbank.common.Settings;

import org.xutils.common.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

import config.Config;

/**
 * Created by zhouwenzheng on 2017/6/9.
 */

public class LocationService extends Service implements BDLocationListener{
    private long locationInterval = Config.LOCATION_INTERVAL;

    private LocationClient locationClient;
    private Timer timer;
    private TimerTask locationTask;
    private TimerTask versionCheckTask;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new LocationClient(getApplicationContext());
        locationClient.setLocOption(initLocationOptions());
        locationClient.registerLocationListener(this);
        timer = new Timer();
        resetLocationTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationTask != null) {
            locationTask.cancel();
        }
        if (versionCheckTask != null) {
            versionCheckTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
        locationTask = null;
        versionCheckTask = null;
        timer = null;
    }

    private void resetLocationTask() {
        if (locationTask != null) {
            locationTask.cancel();
            locationTask = null;
        }
        locationTask = new TimerTask() {
            @Override
            public void run() {
                LogUtil.d("开始定位==>定位频率==>" + locationInterval / 1000 / (double) 60 + "分钟");
                locationClient.start();
            }
        };
        if (timer != null) {
            timer.schedule(locationTask, 5 * 1000, locationInterval);
        }
    }



    private LocationClientOption initLocationOptions() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        return option;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        locationClient.stop();
        int locType = bdLocation.getLocType();
        LogUtil.d("定位完成(61,66,161为成功)==>返回码：" + locType + "==>正在关闭定位");
        LogUtil.d("当前位置信息==>纬度：" + bdLocation.getLatitude() + "==>经度：" + bdLocation.getLongitude());
        if (locType == 61 || locType == 66 || locType == 161) {
            Settings.setValue(this, Settings.KEY.LOCATION_LATITUDE, "" + bdLocation.getLatitude());
            Settings.setValue(this, Settings.KEY.LOCATION_LONGTITUDE, "" + bdLocation.getLongitude());
            if (locationInterval != Config.LOCATION_INTERVAL) {
                locationInterval = Config.LOCATION_INTERVAL;
                resetLocationTask();
            }
        } else {
            Settings.setValue(this, Settings.KEY.LOCATION_LATITUDE, null);
            Settings.setValue(this, Settings.KEY.LOCATION_LONGTITUDE, null);
            if (locationInterval != Config.LOCATION_INTERVAL_SHORT) {
                //定位失败的话，缩短定位间隔时间
                locationInterval = Config.LOCATION_INTERVAL_SHORT;
                resetLocationTask();
            }
        }
    }


}

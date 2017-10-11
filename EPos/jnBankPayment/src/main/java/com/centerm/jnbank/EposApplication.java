package com.centerm.jnbank;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.baidu.mapapi.SDKInitializer;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumSDKType;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumAidCapkOperation;
import com.centerm.jnbank.bean.PrinterItem;
import com.centerm.jnbank.channels.EnumChannel;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.service.LocationService;

import org.apache.log4j.Logger;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import config.BusinessConfig;
import config.LogConfiguration;


/**
 * author:wanliang527</br>
 * date:2016/10/21</br>
 */

public class EposApplication extends Application {
    private Logger logger = Logger.getLogger(EposApplication.class);
    public static EnumChannel posChannel;

    @Override
    public void onCreate() {
        super.onCreate();
        LogConfiguration.obtainDefault().configure();
        posChannel = EnumChannel.valueOf(Settings.getPosChannel(getApplicationContext()));
        DeviceFactory factory = DeviceFactory.getInstance();
        DeviceFactory.InitCallback callback = new DeviceFactory.InitCallback() {
            @Override
            public void onResult(boolean b) {
                if (b) {
                    SDKInitializer.initialize(getApplicationContext());
                    //开启定位服务
                    startService(new Intent(EposApplication.this, LocationService.class));
                    logger.debug("百度定位服务开启");
                }
            }
        };
        factory.init(getApplicationContext(), EnumSDKType.CPAY_SDK,callback);
        initPrintMode();
        x.Ext.init(this);
    }
    private void initPrintMode() {
        CommonDao<PrinterItem> commonDao = new CommonDao<>(PrinterItem.class, new DbHelper(getApplicationContext()));
        commonDao.deleteByWhere("1=1");
        List<PrinterItem> items = commonDao.query();
        if (items.size() == 0) {
            List<PrinterItem> printerItems = new ArrayList<>();
            PrinterItem item1 = new PrinterItem("POS 签购单","POS 签购单","9F01",1,1);
            PrinterItem item2 = new PrinterItem("POS 签购单","POS 签购单","9F51",1,1);
            PrinterItem item3 = new PrinterItem("商户名称(MERCHANT NAME)","商户名","9F02",2,2);
            PrinterItem item4 = new PrinterItem("商户名称(MERCHANT NAME)","商户名","9F52",2,2);
            PrinterItem item5 = new PrinterItem("商户编号（MERCHANT NO）","商户号","9F03",2,3);
            PrinterItem item6 = new PrinterItem("商户编号（MERCHANT NO）","商户号","9F53",2,3);
            PrinterItem item7 = new PrinterItem("终端编号（TERMINAL NO）","终端号","9F04",2,4);
            PrinterItem item8 = new PrinterItem("终端编号（TERMINAL NO）","终端号","9F54",2,4);
            //PrinterItem item11 = new PrinterItem("收单行","收单行","9F06",2,6);
            //PrinterItem item12 = new PrinterItem("收单行","收单行","9F56",2,6);
            PrinterItem item13 = new PrinterItem("交易卡号（CARD NO）","卡号","9F07",2,5);
            PrinterItem item14 = new PrinterItem("交易卡号（CARD NO）","卡号","9F57",2,5);
            PrinterItem item9 = new PrinterItem("发卡行（ISS NO）","发卡行","9F05",2,6);
            PrinterItem item10 = new PrinterItem("发卡行（ISS NO）","发卡行","9F55",2,6);
            PrinterItem item15 = new PrinterItem("批次号（BATCH NO）","批次号","9F08",2,8);
            PrinterItem item16 = new PrinterItem("批次号（BATCH NO）","批次号","9F58",2,8);
            PrinterItem item17 = new PrinterItem("凭证号（VOUCHER NO）","凭证号","9F09",2,9);
            PrinterItem item18 = new PrinterItem("凭证号（VOUCHER NO）","凭证号","9F59",2,9);
            PrinterItem item19 = new PrinterItem("授权码（AUTH NO）","授权码","9F10",2,10);
            PrinterItem item20 = new PrinterItem("授权码（AUTH NO）","授权码","9F60",2,10);
            PrinterItem valid1 = new PrinterItem("有效期(EXP DATE)","","9F16",2,11);//商户联有效期
            PrinterItem valid2 = new PrinterItem("有效期(EXP DATE)","","9F66",2,11);//持卡人联有效期
            PrinterItem item23 = new PrinterItem("日期时间（TIME）","日期时间","9F12",2,12);
            PrinterItem item24 = new PrinterItem("日期时间(TIME)","日期时间","9F62",2,12);
            PrinterItem item21 = new PrinterItem("参考号(REF N0)","参考号","9F11",2,13);
            PrinterItem item22 = new PrinterItem("参考号(REF N0)","参考号","9F61",2,13);
            PrinterItem item25 = new PrinterItem("交易金额(AMOUNT)","金额","9F13",2,14);
            PrinterItem item26 = new PrinterItem("交易金额(AMOUNT)","金额","9F63",2,14);
            PrinterItem item27 = new PrinterItem("备注(REFERENCE)","备注","9F14",2,15);
            PrinterItem item28 = new PrinterItem("备注(REFERENCE)","备注","9F64",2,15);
            PrinterItem item29 = new PrinterItem("","说明","9F15",2,16);
            PrinterItem item30 = new PrinterItem("","说明","9F65",2,16);
            PrinterItem item31 = new PrinterItem("原凭证号","","9F17",2,17);
            PrinterItem item32 = new PrinterItem("原凭证号","","9F67",2,17);
            printerItems.add(item1);
            printerItems.add(item2);
            printerItems.add(item3);
            printerItems.add(item4);
            printerItems.add(item5);
            printerItems.add(item6);
            printerItems.add(item7);
            printerItems.add(item8);
            printerItems.add(item9);
            printerItems.add(item10);
            //printerItems.add(item11);
            //printerItems.add(item12);
            printerItems.add(item13);
            printerItems.add(item14);
            printerItems.add(item15);
            printerItems.add(item16);
            printerItems.add(item17);
            printerItems.add(item18);
            printerItems.add(item19);
            printerItems.add(item20);
            printerItems.add(item21);
            printerItems.add(item22);
            printerItems.add(item23);
            printerItems.add(item24);
            printerItems.add(item25);
            printerItems.add(item26);
            printerItems.add(item27);
            printerItems.add(item28);
            printerItems.add(valid1);
            printerItems.add(valid2);
            printerItems.add(item29);
            printerItems.add(item30);
            printerItems.add(item31);
            printerItems.add(item32);
            commonDao.save(printerItems);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DeviceFactory.getInstance().release();
    }

    private void importIcParamsInBackground() {
        if (Settings.getValue(getApplicationContext(), Settings.KEY.IC_AID_VERSION, null) == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                        logger.info("正在导入默认AID参数");
                        for (int i = 0; i < BusinessConfig.AID.length; i++) {
                            String aidValue = BusinessConfig.AID[i];
                            pbocService.updateAID(EnumAidCapkOperation.UPDATE, aidValue);
                        }
                        Settings.setValue(getApplicationContext(), Settings.KEY.IC_AID_VERSION, "000000");
                        logger.info("导入默认AID参数成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        if (Settings.getValue(getApplicationContext(), Settings.KEY.IC_CAPK_VERSION, null) == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                        logger.info("正在导入默认CAPK参数");
                        for (int i = 0; i < BusinessConfig.CAPK.length; i++) {
                            String capk = BusinessConfig.CAPK[i];
                            pbocService.updateCAPK(EnumAidCapkOperation.UPDATE, capk);
                        }
                        Settings.setValue(getApplicationContext(), Settings.KEY.IC_CAPK_VERSION, "000000");
                        logger.info("导入默认CAPK参数成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    public static Context getAppContext(){
        EposApplication eposApplication=new EposApplication();
        /*Context applicationContext = eposApplication.getApplicationContext();*/
        return eposApplication;
    }
}

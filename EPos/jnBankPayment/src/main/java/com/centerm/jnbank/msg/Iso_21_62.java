package com.centerm.jnbank.msg;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.smartpos.util.HexUtil;

import java.io.UnsupportedEncodingException;


/**
 * 创建日期：2017/9/27
 * 描述:21号文
 * 作者:hqy
 */

public class Iso_21_62 {
    public static StringBuffer buffer = new StringBuffer();
    public static Context context;
    public static String strCardNum;//卡号
    public static String bTerminalHardwareSN;
    public static byte[] bTerminalHardwareSN1;
    public static String version;
    public static String strIso62_21;

    /**
     *
     * @param mContext 当前上下文
     * @param strCn 卡号
     * @return
     */
    public static String getIso62_21(Context mContext, String strCn) {
        context = mContext;
        strCardNum = strCn;
        getJWD();
        return strIso62_21;
    }

    /**
     * 01+02-百度地图经纬度获取
     */
    public static void getJWD() {
        String longtitude = Settings.getValue(context, Settings.KEY.LOCATION_LONGTITUDE, null);
        String latitude = Settings.getValue(context, Settings.KEY.LOCATION_LATITUDE, null);
        Log.d("经纬度", longtitude + "===" + latitude);
        if (latitude != null && longtitude != null) {
            buffer.append("01" + stringtoHex(longtitude) + longtitude
                    + "02" + stringtoHex(latitude) + latitude);
            Log.d("+0102===", "01" + stringtoHex(longtitude) + longtitude
                    + "02" + stringtoHex(latitude) + latitude);

        } else {
            Log.d("经纬度", "经纬度没取到");
        }
        getRWRZ();
    }

    /**
     * 03-终端入网认证编号
     */
    public static void getRWRZ() {
        String strBh = "";//P3100
        buffer.append("03" + stringtoHex(strBh) + strBh);
        Log.d("+03===", "03" + stringtoHex(strBh) + strBh);
        getSBLX();
    }

    /**
     * 04-设备类型(04：智能POS)
     */
    public static void getSBLX() {
        String strSblx = "04";
        buffer.append("04" + stringtoHex(strSblx) + strSblx);
        Log.d("+04===", "04" + stringtoHex(strSblx) + strSblx);
        getZDXL();
    }

    /**
     * 05-终端序列号
     */
    public static void getZDXL() {
        bTerminalHardwareSN1 = CommonUtils.getTerminalHardwareSn();
        try {
            if (bTerminalHardwareSN1 == null || new String(bTerminalHardwareSN1, "ISO-8859-1").length() < 1) {
                Log.d("", "硬件序列号为空或者长度小于1");
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            bTerminalHardwareSN = new String(bTerminalHardwareSN1, "ISO-8859-1");
//            Log.d("硬件序列号", bTerminalHardwareSN);
//            Log.d("硬件序列号", HexUtil.bytesToHexString(bTerminalHardwareSN1));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        buffer.append("05" + stringtoHex(bTerminalHardwareSN) + bTerminalHardwareSN);
        Log.d("+05===", "05" + stringtoHex(bTerminalHardwareSN) + bTerminalHardwareSN);
        getJMSJYZ();
    }

    /**
     * 06-加密随机因子(卡号后六位)
     */
    public static void getJMSJYZ() {
        String strNum = strCardNum.substring(strCardNum.length() - 6, strCardNum.length());
        buffer.append("06" + stringtoHex(strNum) + strNum);
        Log.d("+06===", "06" + stringtoHex(strNum) + strNum);
        getYJXLH();
    }

    /**
     * 07-硬件序列号密文数据
     */
    public static void getYJXLH() {

        String jmSN = HexUtil.bytesToHexString(bTerminalHardwareSN1);
        buffer.append("07" + stringtoHex(jmSN) + jmSN);
        Log.d("+07===", "07" + stringtoHex(jmSN) + jmSN);
        getYYBB();
    }

    /**
     * 08-应用程序版本号
     * 8位，当长度不足时，右补空格。若多于8位则取后八位
     */
    public static void getYYBB() {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = String.valueOf(packInfo.versionCode);
            int cha = 8 - version.length();
            if (cha > 0) {
                for (int i = 0; i < cha; i++) {
                    version = version + "0";
                }
            } else {
                version = version.substring(version.length() - 8, version.length());
            }
        } catch (Exception e) {

        }
        buffer.append("08" + stringtoHex(version) + version);
        strIso62_21 = "PI" + buffer.toString().length() + buffer.toString();
        Log.d("+08===", "08" + stringtoHex(version) + version);
        Log.d("+strIso62_21===", strIso62_21);
    }

    //获取16进制长度
    public static String stringtoHex(String ss) {
        String s = Integer.toHexString(ss.length()).toUpperCase();
        StringBuffer buffer = new StringBuffer();
        if (s.length() < 2) {
            buffer.append("0");
        }
        String s1 = buffer.append(s).toString();
        return s1;
    }

}

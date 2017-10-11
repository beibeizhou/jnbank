package com.centerm.jnbank.msg;

import android.content.Context;
import android.util.Log;

import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.utils.BytesUtil;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DateTimeUtil;
import com.centerm.smartpos.util.HexUtil;

import org.json.JSONException;
import org.xutils.common.util.LogUtil;

import java.io.UnsupportedEncodingException;

/**
 * 创建日期：2017/8/25 0025 on 14:13
 * 描述:
 * 作者:周文正
 */

public class iso_field_62 {
    public final static String ISWRNT = "T3011"; //支持免密免签
    public final static String NOTWRNT = "T3010";  //不支持免密免签
    public static String toHex(byte[] bytes){
        String s = DataHelper.formatToHexLen(bytes.length, 2).toUpperCase();

        StringBuffer buffer = new StringBuffer();
        if (s.length()<2){
            buffer.append("0");
        }
        String s1 = buffer.append(s).toString();
        return s1;
    }

    public static String stringtoHex(String ss){
        String s = Integer.toHexString(ss.length()).toUpperCase();
        StringBuffer buffer = new StringBuffer();
        if (s.length()<2){
            buffer.append("0");
        }
        String s1 = buffer.append(s).toString();
        return s1;
    }

    /**
     *
     * @param context
     * @param isoF2   随机因子，即卡号后六位
     * @param addT5   是否加T5标签
     * @param isneedwrnt   是否支持小额双免
     * @param addT3   是否加T3标签
     * @return
     */
    public static String getIso62(Context context, String isoF2, boolean addT5, boolean isneedwrnt, boolean addT3){
        byte[] bTerminalHardwareSN1 = CommonUtils.getTerminalHardwareSn();
        //新的杰收银在存量终端上
        try {
            if (bTerminalHardwareSN1==null||new String(bTerminalHardwareSN1, "ISO-8859-1").length()<1){
                LogUtil.w("硬件序列号获取不到");
                if (addT3){
                    if (isneedwrnt){
                        return ISWRNT;
                    }else{
                        return NOTWRNT;
                    }
                }else {
                    return "";
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuffer buffer=new StringBuffer();
        StringBuffer buffer1=new StringBuffer();
        //获取基站信息
        try {
            String gsmCellLocationInfo = CommonUtils.getGSMCellLocationInfo(context);
            Settings.setBaseStationInfo(context,gsmCellLocationInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //百度地图经纬度获取
        String longtitude = Settings.getValue(context, Settings.KEY.LOCATION_LONGTITUDE, null);
        String latitude = Settings.getValue(context, Settings.KEY.LOCATION_LATITUDE, null);
        Log.d("经纬度",longtitude+"==="+latitude);
        if (latitude != null && longtitude != null) {
            int iLen = ("JD" + stringtoHex(longtitude)+longtitude
                    + "WD" + stringtoHex(latitude) + latitude
            ).length();
            LogUtil.d("经纬度长度" + iLen + "");
            String lolavalue = "JD" + stringtoHex(longtitude)+longtitude
                    + "WD" + stringtoHex(latitude) + latitude;
            LogUtil.d("经纬度值" + lolavalue.toString());
            buffer1.append(lolavalue);
        }else {
            String baseStationInfo = Settings.getBaseStationInfo(context);
            if (baseStationInfo!=null&&baseStationInfo.length()>0){
                buffer1.append(baseStationInfo);
            }
        }
        String t4 = buffer1.toString();
        if (t4!=null&&t4.length()>0){
            String T4value="T4" + DataHelper.formatToHexLen(t4.length(), 2).toUpperCase() +buffer1.toString();
            buffer.append(T4value);
        }
        if (addT5){
            String T5value = iso_field_62.getIso62T5(context, isoF2);
            if (T5value!=null&&T5value.length()>0){
                buffer.append(T5value);
            }
        }
        if (buffer==null||buffer.toString().length()<1){
            if (addT3){

            }else {
                return "";
            }

        }
        if (addT3){
            if(isneedwrnt){
                Log.d("62域最终报文：",ISWRNT+buffer.toString());
                return ISWRNT+buffer.toString();
            }else{
                Log.d("62域最终报文：",NOTWRNT+buffer.toString());
                return NOTWRNT+buffer.toString();
            }
        }else{
            Log.d("62域最终报文：",buffer.toString());
            return buffer.toString();
        }
    }


    //21号改造内容
    public static String getIso62T5(Context context, String isoF2) {
        //59域DATA
        byte[] bTerminalHardwareSN1 = CommonUtils.getTerminalHardwareSn();
        try {
            if(bTerminalHardwareSN1==null||new String(bTerminalHardwareSN1, "ISO-8859-1").length()<1){
                LogUtil.w("硬件序列号为空或者长度小于1");
                return "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            String bTerminalHardwareSN =new String(bTerminalHardwareSN1, "ISO-8859-1");
            LogUtil.d("硬件序列号" + bTerminalHardwareSN);
            LogUtil.d("硬件序列号" + HexUtil.bytesToHexString(bTerminalHardwareSN1));
            byte[] bshortF2;
            if (isoF2.length() > 6) {
                bshortF2 = BytesUtil.getBytes(isoF2.substring(isoF2.length() - 6, isoF2.length()), "GBK");
            } else {
                return null;
            }
            byte[] terminalHardwareSNEncrypt = CommonUtils.getSNK(HexUtil.bytesToHexString(bTerminalHardwareSN1)
                    , HexUtil.bytesToHexString(bshortF2));
            String versionCode = CommonUtils.getAppVersionCodeAndFillBlock(context);
            byte[] bVersionCode = BytesUtil.getBytes(versionCode, "GBK");
            LogUtil.d("应用版本号" + bVersionCode+"==="+versionCode);
            String isoString = "04";
            String isoString2 = isoF2.substring(isoF2.length() - 6, isoF2.length());
            LogUtil.d("随机因子" +isoString2);
            String isoString3 =  new String(terminalHardwareSNEncrypt, "ISO-8859-1");
            LogUtil.d("SN密文" +isoString3);
            String yyyyMMdd = DateTimeUtil.getCurrentDate("yyyyMMdd");

            int iLen = ("TY02" + isoString
                    + "SN" +toHex(bTerminalHardwareSN1)  + bTerminalHardwareSN
                    + "RD" + DataHelper.formatToHexLen(isoString2.length(), 2) + isoString2
                    + "ED" + "10" + isoString3
                    + "VE" + "08" +yyyyMMdd).length();
            LogUtil.d("62数据长度" +iLen+"");
            String field62="T5" + DataHelper.formatToHexLen(iLen, 2).toUpperCase() + "TY02" + isoString
                    + "SN" + toHex(bTerminalHardwareSN1) + bTerminalHardwareSN
                    + "RD" + DataHelper.formatToHexLen(isoString2.length(), 2) + isoString2
                    + "ED" + "10" + isoString3
                    + "VE" + "08" + yyyyMMdd;
            LogUtil.d("T5数据值" +field62.toString());
            return field62;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}

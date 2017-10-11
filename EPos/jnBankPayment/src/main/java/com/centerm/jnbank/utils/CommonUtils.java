package com.centerm.jnbank.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.cloudsys.sdk.common.utils.PackageUtils;
import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pinpad.PinPadConfig;
import com.centerm.jnbank.EposApplication;
import com.centerm.jnbank.R;
import com.centerm.jnbank.bean.BinData;
import com.centerm.jnbank.bean.IssInfo;
import com.centerm.jnbank.channels.EnumChannel;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.xutils.common.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import config.BusinessConfig;
import config.Config;
import config.KeyIndexConfig;
import jxl.Sheet;
import jxl.Workbook;


/**
 * author:wanliang527</br>
 * date:2016/10/28</br>
 */

public class CommonUtils {
    private static long lastClickTime;
    private static Logger logger = Logger.getLogger(CommonUtils.class);
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";

    /**
     * 根据2磁道信息判断是否是IC卡
     *
     * @param track2data 2磁道信息
     * @return IC卡返回true，否则返回false
     */
    public static boolean isIcCard(String track2data) {
        if ("".equals(track2data) || track2data == null) {
            return false;
        }
        if ((!track2data.contains("=")) && (!track2data.contains("D"))) {
            return false;
        }
        String temp[];
        String key;
        if (track2data.contains("=")) {
            temp = track2data.split("=");
            if (5 < temp[1].length()) {
                key = temp[1].substring(4, 5);
            } else {
                return false;
            }
        } else if (track2data.contains("D")) {
            temp = track2data.split("D");
            if (5 < temp[1].length()) {
                key = temp[1].substring(4, 5);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return "2".equals(key) || "6".equals(key);
    }

    /**
     * 加密磁道数据
     *
     * PAN，卡有效期，磁道二数据，磁道三数据以TLV格式组合后，使用0x00补齐长度为8的倍数，使用DEK进行3-DES加密
     *
     *
     * @return 加密后的磁道信息
     */
    public static String encryptTrackDataTag(String cardNum,String date,String track2,String track3){
        if (TextUtils.isEmpty(track3)&& TextUtils.isEmpty(track2)) {
            logger.warn("磁道信息为空无法加密");
            return null;
        }
        IPinPadDev pinPad = CommonUtils.getPinPadDev();
        if (pinPad == null ) {
            logger.warn("密码键盘异常，无法加密磁道信息，返回全F");
            return "FFFFFFFFFFFFFFFF";
        }
        if(track2.length()>37){
            track2 = track2.substring(0,37);
        }
//        String pan = cardNum.substring(cardNum.length()-13,cardNum.length()-1);
        String pan = cardNum;
        //组TLV--长度格式不同，使用16进制格式
        String tagPan = "0A",tagDate = "0E",tagTrack2 = "C2",tagTrack3 = "C3";
        String panData = TextUtils.isEmpty(pan)?"":(tagPan+String.format("%02X",pan.length())+pan + (pan.length()%2==0?"":"0"));
        String dateData = TextUtils.isEmpty(date)?"":(tagDate + String.format("%02X",date.length())+date);
        String track2Data = TextUtils.isEmpty(track2)?"":(tagTrack2 + String.format("%02X",track2.length()) + track2 + (track2.length()%2==0?"":"0"));
        String track3Data = TextUtils.isEmpty(track3)?"":(tagTrack3 + String.format("%02X",track3.length()) + track3 + (track3.length()%2==0?"":"0"));
        String encyptData = panData + dateData + track2Data + track3Data;
        //填充8的倍数加密
        int len  = (encyptData.length()+15)&0xFFF0;
        encyptData +="0000000000000000".substring(0,len-encyptData.length());
        logger.info("[36域加密数据]:"+encyptData);
//        byte[] encryByte = pinPad.encryData(EnumDataEncryMode.ECB, HexUtil.hexStringToByte(encyptData),"");
        byte[] encryByte = pinPad.encryData(null, null, encyptData);
        encyptData = HexUtils.bcd2str(encryByte);
        return encyptData;

    }

    /**
     * 加密磁道数据
     * @param trackInfo 待加密磁道信息
     * @return 加密后的磁道信息
     */
    public static String encryTrackData(String trackInfo) {
        if (TextUtils.isEmpty(trackInfo)) {
            logger.warn("磁道信息为空无法加密");
            return null;
        }
        IPinPadDev pinPad = CommonUtils.getPinPadDev();
        if (pinPad == null || TextUtils.isEmpty(trackInfo)) {
            logger.warn("密码键盘异常，无法加密磁道信息，返回全F");
            return "FFFFFFFFFFFFFFFF";
        }
        String track = trackInfo.length() % 2 == 0 ? trackInfo : trackInfo + "0";
        int len = track.length();
        String waitEncryData = null;
        String encryData = null;
        String finalData = null;
        if (len <= 16) {
            waitEncryData = track;
            encryData = HexUtils.bytesToHexString(pinPad.encryData(null, null, waitEncryData));
            finalData = encryData;
        } else if (len == 18) {
            waitEncryData = track.substring(0, 16);
            encryData = HexUtils.bytesToHexString(pinPad.encryData(null, null, waitEncryData));
            finalData = encryData + track.substring(16, 18);
        } else {
            waitEncryData = track.substring(len - 18, len - 2);
            encryData = HexUtils.bytesToHexString(pinPad.encryData(null, null, waitEncryData));
            finalData = track.substring(0, len - 18) + encryData + track.substring(len - 2, len);
        }
        logger.debug("原数据：" + trackInfo);
        logger.debug("待加密数据段：" + waitEncryData);
        logger.debug("加密后数据：" + encryData);
        logger.debug("最终磁道数据：" + finalData);
        return finalData;
    }


    /**
     * 获取密码键盘设备对象
     *
     * @return
     */
    public static IPinPadDev getPinPadDev() {
        try {
            IPinPadDev pinPadDev = DeviceFactory.getInstance().getPinPadDev();
            PinPadConfig config = new PinPadConfig();
            KeyIndexConfig keyConfig = new KeyIndexConfig();
//            EnumChannel channel = EposApplication.posChannel;
//            config.setTmkIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TMK));
//            config.setMakIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.MAK));
//            config.setTdkIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TDK));
//            config.setPikIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.PIK));
//            Log.e("TMK",keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TMK)+"");
//            Log.e("MAK",keyConfig.getIndex(channel, KeyIndexConfig.KeyType.MAK)+"");
//            Log.e("TDK",keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TDK)+"");
//            Log.e("PIK",keyConfig.getIndex(channel, KeyIndexConfig.KeyType.PIK)+"");
            config.setTmkIndex(6);
            config.setMinLen(4);
            pinPadDev.config(config);
            return pinPadDev;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断是否为合法IP
     *
     * @return the ip
     */
    public static boolean isIp(String addr) {
        String[] addrs = addr.split("\\.");
        int len = addrs.length;
        String str = addr.substring(addr.length() - 1, addr.length());
        if (str.equals(".")) {
            return false;
        }
        if (len != 4) {
            return false;
        }
        boolean one = false, other = false;
        try {
            if (Integer.parseInt(addrs[0]) <= 223)
                one = true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < len; i++) {
            try {
                if (Integer.parseInt(addrs[i]) < 256) {
                    other = true;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                break;
            }
        }
        return one && other;
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return 网络可用返回true，否则返回false
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 读取excel数据到数据库里
     *
     * @param context
     */
    public static boolean readExcelToDB(Context context) {
        try {
            List<BinData> datas = new ArrayList<>();
            DbHelper dbHelper = new DbHelper(context);
            CommonDao<BinData> commonDao = new CommonDao<>(BinData.class, dbHelper);
            InputStream is = context.getAssets().open("binData.xls");
            Workbook book = Workbook.getWorkbook(is);
            book.getNumberOfSheets();
            // 获得第一个工作表对象
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows();
            logger.debug("excel表格总共有" + rows + "条数据");
            BinData binData = null;
            for (int i = 1; i < rows; ++i) {
                String cardBinNo = (sheet.getCell(0, i)).getContents();
                String cardBinLenth = (sheet.getCell(1, i)).getContents();
                String cardBin = (sheet.getCell(2, i)).getContents();
                String cardNumberLenth = (sheet.getCell(3, i)).getContents();
                String cardType = (sheet.getCell(4, i)).getContents();
                String orgNo = (sheet.getCell(5, i)).getContents();
                String cardOrg = (sheet.getCell(6, i)).getContents();
                binData = new BinData(cardBinNo.trim(), cardBinLenth.trim(), cardBin.trim(), cardNumberLenth.trim(), cardType.trim(), orgNo.trim(), cardOrg.trim());
                datas.add(binData);
                if (i % 50 == 0) {
                    commonDao.save(datas);
                    datas.clear();
                }
                //保存最后一条卡BIN的编号，用于联机更新BIN表时的起始编号
                if (i == rows - 1) {
                    int no = Integer.valueOf(cardBinNo.trim());
                    logger.info("保存卡BIN表最后一条记录编号==>" + (++no));
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO, "" + no);
                }
            }
            datas.clear();
            book.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取excel数据到数据库里
     *
     * @param context
     */
    public static boolean readIssExcelToDB(Context context) {
        try {
            List<IssInfo> datas = new ArrayList<>();
            DbHelper dbHelper = new DbHelper(context);
            CommonDao<IssInfo> commonDao = new CommonDao<>(IssInfo.class, dbHelper);
            InputStream is = context.getAssets().open("issueCardData.xls");
            Workbook book = Workbook.getWorkbook(is);
            book.getNumberOfSheets();
            // 获得第一个工作表对象
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows();
            logger.debug("excel表格总共有" + rows + "条数据");
            IssInfo issInfo = null;
            for (int i = 1; i < rows; ++i) {
                String cardIssNo = (sheet.getCell(0, i)).getContents();
                String cardIssName = (sheet.getCell(1, i)).getContents();
                issInfo = new IssInfo(cardIssNo.trim(), cardIssName.trim());
                datas.add(issInfo);
                if (i % 50 == 0) {
                    commonDao.save(datas);
                    datas.clear();
                }
            }
            datas.clear();
            book.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断终端是否处于充电状态
     *
     * @param context context
     * @return 充电状态返回true，否则返回false
     */
    public static boolean isOnCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        return isCharging;
    }

    public static float getBatteryPercent(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);
        //当前剩余电量
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        //电量最大值
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        //电量百分比
        float batteryPct = level / (float) scale;
        return batteryPct;
    }

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        logger.debug("time时间："+time);
        logger.debug("lastClickTime时间："+lastClickTime);
        logger.debug("timeD："+timeD);
        if ( 0 < timeD && timeD < 500) {
            logger.debug("time时间inner："+time);
            logger.debug("lastClickTime时间inner："+lastClickTime);
            logger.debug("timeD："+timeD);
            return true;
        }
        lastClickTime = time;
        return false;
    }
    public synchronized static boolean isOneFastClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        logger.debug("time时间："+time);
        logger.debug("lastClickTime时间："+lastClickTime);
        logger.debug("timeD："+timeD);
        if ( 0 < timeD && timeD < 1500) {
            logger.debug("time时间inner："+time);
            logger.debug("lastClickTime时间inner："+lastClickTime);
            logger.debug("timeD："+timeD);
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static boolean compareToUpdate(String oriVersion, Context context){
        if (StringUtils.isStrNull(oriVersion)) {
            logger.debug("后台版本数据异常");
            return false;
        }
        String curVersion = PackageUtils.getInstalledVersionName(context, context.getPackageName());
        String platVersion = oriVersion.substring(1, oriVersion.length());
        int[] platVersionNum = stringVersionToNumber(platVersion);
        int[] curVersionNum = stringVersionToNumber(curVersion);
        if (platVersionNum[0] == -1) {
            logger.debug("后台返回版本号异常");
            return false;
        }
        if (curVersionNum[0] == -1) {
            logger.debug("当前版本号异常");
            return false;
        }
        if (platVersionNum[0] > curVersionNum[0]) {
            return true;
        }
        if (platVersionNum[1] > curVersionNum[1]) {
            return true;
        }
        if (platVersionNum[2] > curVersionNum[2]) {
            return true;
        }
        return false;
    }

    private static int[] stringVersionToNumber(String curVersion) {
        String[] strings = curVersion.split("\\.");
        int firstVersion = -1;
        int secVersion = -1;
        int thirdVersion = -1;
        if (StringUtils.isStrNull(strings[0])||StringUtils.isStrNull(strings[1])||StringUtils.isStrNull(strings[2])) {
            logger.error("版本号数据异常");
            return new int[]{-1,-1,-1};
        }
        try {
            firstVersion = Integer.parseInt(strings[0]);
            secVersion = Integer.parseInt(strings[1]);
            thirdVersion = Integer.parseInt(strings[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("版本号数据转换异常");
            return new int[]{-1,-1,-1};
        }
        return new int[]{firstVersion,secVersion,thirdVersion};
    }


    /*
   * 判断当前网络是否是wifi
   */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**将二进制转换成16进制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }


    public static String getBuildTime(Context mContext){
        String content = null;
        Resources resources=mContext.getResources();
        InputStream is=null;
        try{
            is=resources.openRawResource(R.raw.date);
            byte buffer[]=new byte[is.available()];
            is.read(buffer);
            content=new String(buffer);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(is!=null)
            {
                try{
                    is.close();
                }catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }


    public static byte[] getTerminalHardwareSn(){
        try {
            IPinPadDev pinPadDev = DeviceFactory.getInstance().getPinPadDev();
            byte[] bResultSn = pinPadDev.getHardWareSN();
            return bResultSn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getSNK(String data, String random){
        try {
            IPinPadDev pinPadDev = DeviceFactory.getInstance().getPinPadDev();
            byte[] bResultSn = pinPadDev.getMacForSNK(data, random);
            return bResultSn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    public static String getAppVersionCodeAndFillBlock(Context context){
        String version = "";
        //  获取packagemanager的实例
        PackageManager packageManager  =  context.getPackageManager();
        //  getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo  = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version  =  "" + packInfo.versionCode;
        for (int i = 0; i < 8; i++) {
            version += " ";
        }
        version = version.substring(0,8);
        return version;
    }

    /**
     * 获取手机基站信息
     * @throws JSONException
     */
    public static String getGSMCellLocationInfo(Context context) throws JSONException {
        int jizhantype=-1;
        int lac = -1;
        int cellid = -1;
        int mcc = -1;
        int mnc = -1;
        int sId =-1;
        int iLen =-1;
        String field59="";
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = manager.getNetworkOperator();
        /**通过operator获取 MCC 和MNC */
        if (operator.isEmpty()) {
            return "";
        }
        StringBuffer buffer1 = new StringBuffer();
        int type = manager.getNetworkType();
        mcc = Integer.parseInt(operator.substring(0, 3));
        mnc = Integer.parseInt(operator.substring(3));
        if (Integer.toString(mnc).length()<2){
            buffer1.append("0");
        }
        String mnc1 = buffer1.append(mnc).toString();
        //先获取当前的基站
        CellLocation location =  manager.getCellLocation();
        StringBuffer buffer = new StringBuffer();
        if (location instanceof GsmCellLocation) {
            GsmCellLocation gsmLocation = ((GsmCellLocation) manager.getCellLocation());
            if (gsmLocation == null)
            {
                return "";
            }
            lac = gsmLocation.getLac();
            cellid = gsmLocation.getCid();
            jizhantype=1;
            if (lac != -1 && cellid != -1) {
                buffer.append(jizhantype+",");
                buffer.append(mcc+",");
                buffer.append(mnc1+",");
                buffer.append(lac+",");
                buffer.append(cellid+",");
                iLen = ("TY01" + jizhantype
                        + "MC03460"
                        + "MN02" + mnc1
                        + "LT" + DataHelper.formatToHexLen(Integer.toString(lac).length(), 2) + lac
                        + "CI" +  DataHelper.formatToHexLen(Integer.toString(cellid).length(), 2) + cellid).length();
                field59="J1" +DataHelper.formatToHexLen(iLen, 2).toUpperCase()+"TY01" + jizhantype
                        + "MC03460"
                        + "MN02" + mnc1
                        + "LT" + DataHelper.formatToHexLen(Integer.toString(lac).length(), 2) + lac
                        + "CI" +  DataHelper.formatToHexLen(Integer.toString(cellid).length(), 2) + cellid;
            }
        } else if (location instanceof CdmaCellLocation) {

            CdmaCellLocation cdma = (CdmaCellLocation) manager.getCellLocation();
            if (cdma == null) {
                return "";
            }
            lac = cdma.getNetworkId();
            cellid = cdma.getBaseStationId();
            sId = cdma.getSystemId();
            jizhantype=2;
            if (lac != -1 && cellid != -1 && sId!=-1) {
                buffer.append(jizhantype+",");
                buffer.append(mcc+",");
                buffer.append(sId+",");
                buffer.append(lac+",");
                buffer.append(cellid+",");
                iLen = ("TY01" + jizhantype
                        + "MC03460"
                        + "SI" +DataHelper.formatToHexLen(Integer.toString(sId).length(), 2) + sId
                        + "NI" + DataHelper.formatToHexLen(Integer.toString(lac).length(), 2) + lac
                        + "BI" +  DataHelper.formatToHexLen(Integer.toString(cellid).length(), 2) + cellid).length();
                field59="J1" +DataHelper.formatToHexLen(iLen, 2).toUpperCase()+"TY01" + jizhantype
                        + "MC03460"
                        + "SI" +DataHelper.formatToHexLen(Integer.toString(sId).length(), 2) + sId
                        + "NI" + DataHelper.formatToHexLen(Integer.toString(lac).length(), 2) + lac
                        + "BI" +  DataHelper.formatToHexLen(Integer.toString(cellid).length(), 2) + cellid;
            }
        }
       /* //查看如果有临近基站就上送  --有的终端获取不到邻近基站
        List<NeighboringCellInfo> cellInfos = manager.getNeighboringCellInfo();
        if (cellInfos.size() > 0) {
            for (NeighboringCellInfo info:
                    cellInfos) {
                if (info.getLac() != -1 && info.getCid() != -1) {
                    buffer.append(jizhantype+",");
                    buffer.append(mcc+",");
                    buffer.append(mnc+",");
                    buffer.append(info.getLac()+",");
                    buffer.append(info.getCid()+",");
                }
            }
        }*/
        if (jizhantype ==-1 || mcc == -1 || mnc == -1 || lac == -1 || cellid == -1 || sId == -1) {
            return "";
        }
        String info = buffer.toString().substring(0,buffer.toString().length()-1);

        LogUtil.d("基站信息值" +field59.toString());
        LogUtil.d("基站信息长度" +iLen+"");
        Log.e("终端位置","基站信息为："+info);
        return field59.toString();
    }
    /**
     * 在字符串后填充字符串
     *
     * @param srcStr  源字符串
     * @param fillStr 填充的字符串
     * @param len     返回的字符串长度
     */
    public static String fillAtBack(String srcStr, String fillStr, int len) {
        if (TextUtils.isEmpty(srcStr)) {
            srcStr = fillStr;
        }
        String str = srcStr + fillStr;
        return str.substring(0, len);
    }
    /**
     * 获取当前的系统时间字符串
     *
     * @param dateFormat 日期格式，例如格式yyyyMMddhhmmss
     */
    public static String getCurrentDate(String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return formatter.format(new Date());
    }

    public static String getRandomWithLength(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append("0123456789".charAt(random.nextInt(10)));
        }
        return sb.toString();
    }
    /**
     * 获取SD卡路径
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }
        if (sdDir != null)
            return sdDir.toString();
        else
            return "/mnt/sdcard";
    }
}

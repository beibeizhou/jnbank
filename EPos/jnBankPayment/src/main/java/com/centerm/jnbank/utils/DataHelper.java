package com.centerm.jnbank.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * author: linwanliang</br>
 * date:2016/7/20</br>
 */
public class DataHelper {
    /**
     * 根据指定长度在字符串末尾补F
     *
     * @param str 需要在末尾补F的字符串
     * @param len 补充后的长度
     * @return 返回补F后的字符串
     */
    public static String addFlast(String str, int len) {
        String reStr = "";
        if (str.length() > len) {
            return str;
        }
        while (str.length() < len) {
            str += "F";
        }
        reStr = str;
        return reStr;
    }

    /**
     * Map集合复制
     *
     * @param resource
     * @param target
     */
    public static void copyMap(Map<String, String> resource, Map<String, String> target) {
        if (resource == null || target == null) {
            return;
        }
        for (String key : resource.keySet()) {
            target.put(key, resource.get(key));
        }
    }

    /**
     * 保留两位小数。该方法有局限性，数值不允许超过9999999999999.99
     * 示例：1.1==》1.10,3==》3.00,1.23==》1.23,2.567==》2.56
     *
     * @param d 需要格式化的数值
     * @return 保留2位小数
     */
    public static String saved2Decimal(double d) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(d);
    }

    /**
     * 格式化金额，转换成8583接口报文要求的金额格式
     *
     * @param d 数值
     * @return 1.1==》000000000110，1.23==》000000000123
     */
    public static String formatAmount(double d) {
        String str = saved2Decimal(d).replace(".", "");
        StringBuilder stringBuilder = new StringBuilder();
        if (str.length() < 12) {
            for (int i = 0; i < 12 - str.length(); i++) {
                stringBuilder.append("0");
            }
        }
        stringBuilder.append(str);
        return stringBuilder.toString();
    }


    @NonNull
    public static String formatToXLen(int num, int x) {
        String numStr = String.valueOf(num);
        return formatToXLen(numStr, x);
    }

    @NonNull
    public static String formatToXLen(String num, int x) {
        int len = num.length();
        int count = x - len;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < count; i++) {
            buffer.append("0");
        }
        buffer.append(num);
        return buffer.toString();
    }

    public static String formatAmountForShow(String amt) {
        if (amt != null && amt.length() == 12) {
            return formatIsoF4(amt);
        } else {
            try {
                double d = Double.valueOf(amt);
                return saved2Decimal(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return amt;
    }


    public static String formatIsoF4(String isoF4) {
        if (TextUtils.isEmpty(isoF4) || isoF4.length() != 12) {
            return isoF4;
        }
        long i = Long.valueOf(isoF4.substring(0, 10));
        return i + "." + isoF4.substring(10, 12);
    }

    /**
     * 将4域金额转换成double数据
     *
     * @param isoF4
     * @return
     */
    public static double parseIsoF4(String isoF4) {
        String str = formatIsoF4(isoF4);
        return Double.parseDouble(str);
    }

    public static double formatDouble(double amt) {
        BigDecimal bg = new BigDecimal(amt);
        double calculateResult = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return calculateResult;
    }

    @NonNull
    public static String formatToHexLen(int num, int x) {
        String numStr = Integer.toHexString(num);
        return formatToXLen(numStr, x);
    }

    public static String formatIsoF12F13(String isoF12, String isoF13) {
        StringBuilder sBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(isoF13) && isoF13.length() == 4) {
            sBuilder.append(isoF13.substring(0, 2))
                    .append("月")
                    .append(isoF13.substring(2, 4))
                    .append("日");
        }
        sBuilder.append("  ");
        if (!TextUtils.isEmpty(isoF12) && isoF12.length() == 6) {
            sBuilder.append("   ");
            sBuilder.append(isoF12.substring(0, 2))
                    .append(":")
                    .append(isoF12.substring(2, 4))
                    .append(":")
                    .append(isoF12.substring(4, 6));
        }
        return sBuilder.toString();
    }

    /**
     * 卡号脱敏操作
     *
     * @param cardNo 卡号
     * @return 脱敏后的卡号
     */
    public static String shieldCardNo(String cardNo) {
        if (TextUtils.isEmpty(cardNo) || cardNo.length() < 11) {
            return cardNo;
        }
        String front = cardNo.substring(0, 6);
        String behind = cardNo.substring(cardNo.length() - 4, cardNo.length());
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(front);
        for (int i = 0; i < cardNo.length() - 10; i++) {
            sBuilder.append("*");
        }
        sBuilder.append(behind);
        return sBuilder.toString();
    }




    /**
     * 格式化日期和时间
     *
     * @param time
     * @return
     */
    public static String formatDateAndTime(String time) {
        String year;
        String month;
        String day;
        String hour;
        String minute;
        String second;
        if (time.length() != 14) {
            return time;
        } else {
            year = time.substring(0, 4);
            month = time.substring(4, 6);
            day = time.substring(6, 8);
            hour = time.substring(8, 10);
            minute = time.substring(10, 12);
            second = time.substring(12, 14);
            return year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
        }
    }

    /**
     * 格式化日期和时间
     *
     * @param time
     * @return
     */
    public static String formatDateAndTime2(String time) {
        String year;
        String month;
        String day;
        String hour;
        String minute;
        String second;
        if (time.length() != 14) {
            return time;
        } else {
            year = time.substring(0, 4);
            month = time.substring(4, 6);
            day = time.substring(6, 8);
            hour = time.substring(8, 10);
            minute = time.substring(10, 12);
            second = time.substring(12, 14);
            return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        }
    }

    public static String extractName(String track1) {
        if (TextUtils.isEmpty(track1)) {
            return track1;
        }
        String[] strs = track1.split("\\^");
        if (strs.length < 2) {
            return strs[0];
        } else {
            String str = strs[1];
            if (!TextUtils.isEmpty(str)) {
                return str.trim();
            }
        }
        return track1;
    }

    public static String subZeroAndDot(String s) {
        if (s != null) {
            if (s.indexOf(".") > 0) {
                s = s.replaceAll("0+?$", "");//去掉多余的0
                s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
            }
        } else {
            s = "";
        }
        return s;
    }


    /**
     * Compress image by size, this will modify image width/height.
     * Used to get thumbnail
     *
     * @param image
     * @param pixelW target pixel of width
     * @param pixelH target pixel of height
     * @return
     */
    public static Bitmap ratio(Bitmap image, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if (os.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = 3;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        //压缩好比例大小后再进行质量压缩
//      return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    public static Bitmap resize(Bitmap bitmap, int newW, int newH) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        // 缩放图片的尺寸
        float scaleWidth = (float) newW / bitmapWidth;
        float scaleHeight = (float) newH / bitmapHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 产生缩放后的Bitmap对象
        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
        // save file
     /*   if (!bitmap.isRecycled()) {
            bitmap.recycle();//记得释放资源，否则会内存溢出
        }*/
       /* if (!resizeBitmap.isRecycled()) {
            resizeBitmap.recycle();
        }*/
        return resizeBitmap;
    }

    /**
     * 右补空格
     *
     * @param content 原内容
     * @param length  长度
     * @return
     */
    public static String fillRightSpace(String content, int length) {
        StringBuilder sBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(content)) {
            sBuilder.append(content);
        }
        int l = sBuilder.toString().length();
        if (l < length) {
            for (int i = 0; i < (length - l); i++) {
                sBuilder.append(" ");
            }
        }
        return sBuilder.toString();
    }

    public static Map<String, String> hexAscTlv2Map(String hexAscStr, int tLen, int lLen) {
        Map<String, String> map = new HashMap<>();
        int index = 0;
        try {
            while (index < hexAscStr.length()) {
                String tag = new String(hexAscStr.substring(index, index + tLen * 2));
                index += (tLen * 2);
                String len = new String(hexAscStr.substring(index, index + lLen * 2));
                index += (lLen * 2);
                String value = new String(HexUtils.hexStringToByte(hexAscStr.substring(index, index + Integer.valueOf(len) * 2)),"GBK");
                index += Integer.valueOf(len) * 2;
                map.put(tag, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
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

    /**
     * 左补0
     *
     * @param content 原内容
     * @param length  长度
     * @return
     */
    public static String fillLeftZero(String content, int length) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        StringBuilder sBuilder = new StringBuilder();
        int l = content.length();
        if (l < length) {
            for (int i = 0; i < (length - l); i++) {
                sBuilder.append("0");
            }
        }
        sBuilder.append(content);
        return sBuilder.toString();
    }

}

package com.centerm.jnbank.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.newland.pay.tools.pensigner.JBigConvert;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

/**
 * Created by ysd on 2016/11/26.
 */

public class ImageUtils {
/**
 　　* 将bitmap转换成base64字符串
 　　*
 　　* @param bitmap
 　　* @return base64 字符串
 　　*/
    public static String bitmaptoString(Bitmap bitmap, int bitmapQuality) {
        // 将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, bitmapQuality, bStream);
        byte[] bytes = bStream.toByteArray();
//        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        string = HexUtils.bcd2str(bytes);
        return string;
        }

    /**
     　　* 将base64转换成bitmap图片
     　　*
     　　* @param string base64字符串
     　　* @return bitmap
     　　*/
        public static Bitmap stringtoBitmap(String string) {
       // 将字符串转换成Bitmap类型
       Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                   bitmapArray.length);
            } catch (Exception e) {
            e.printStackTrace();
            }
        return bitmap;
        }


    /**
     * 根据二维码字符串生成图片
     *
     * @param codeurl
     */
    public static Bitmap getcodeBmp(String codeurl) {
        int QR_WIDTH = 290, QR_HEIGHT = 290;
        Bitmap bitmap;
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//        hints.put(EncodeHintType.MARGIN, 0);//设置边距
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(codeurl, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
        for (int y = 0; y < QR_HEIGHT; y++) {
            for (int x = 0; x < QR_WIDTH; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * QR_WIDTH + x] = 0xff000000;
                } else {
                    pixels[y * QR_WIDTH + x] = 0xffffffff;
                }
            }
        }
        bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
        return bitmap;
    }

    public static String bitmaptoJBJGString(Context context, Bitmap bitmap) {
        String jbigStr;
        byte[] jbigData = JBigConvert.convertToJBIG(context,bitmap,100);
        jbigStr = HexUtils.bcd2str(jbigData);
        return jbigStr;
    }
}

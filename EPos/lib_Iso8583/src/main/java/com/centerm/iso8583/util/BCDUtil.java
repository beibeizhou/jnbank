/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  BCDUtil.java
 * @Version : 1.0
 * @Create on:  2013-01-18
 * @Author   :  Tianxiaobo
 *
 * @ChangeList
 * ---------------------------------------------------
 * Date         Editor              ChangeReasons
 *
 *
 */
package com.centerm.iso8583.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;

import com.centerm.iso8583.ISOConfig;

/**
 * 功能描述：这个类封装了进行BCD压缩编码的相关操作
 * 
 * @author Tianxiaobo
 * 
 */
public class BCDUtil {

	/**
	 * 功能描述：检查其数据是否能进行BCD
	 * 
	 * @param val
	 *            待检查的数据
	 * @return 都在 0x00 ~ 0x0F, 0x30 ~ 0x39的范围中，则返回true， 否则false
	 */
	public static boolean canbeBCD(byte[] val) {
		if (null == val) {
			throw new IllegalArgumentException("canbeBCD接口参数不能为null");
		}
		for (int i = 0; i < val.length; i++) {
			boolean flag1 = (val[i] > -1 && val[i] < 0x10);
			boolean flag2 = (val[i] > 0x2F && val[i] < 0x3A);
			boolean flag3 = (val[i] > 0x40 && val[i] < 0x47);
			boolean flag4 = (val[i] > 0x60 && val[i] < 0x67);
			if (!(flag1 || flag2 || flag3 || flag4)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 功能描述：对给定的数据进行BCD转换，如果长度为奇数，则在最前端补零
	 * 
	 * @param val
	 *            待转换数据，需满足canbeBCD()。
	 * @return 以字节数组的形式返回压缩后的内容
	 * @throws Exception 
	 */
	public static byte[] doBCD(byte[] val) throws Exception {
		if (val == null) { // 检查参数是否为null
			throw new IllegalArgumentException("不能进行BCD压缩, 传入的参数为null");
		}
		if (!canbeBCD(val)) { // 检查参数的内容是否合法
			throw new IllegalArgumentException(
					"不能进行BCD, 传入的参数非法：含有 不在[0x00~0x0F],[0x30 ~ 0x39], [0x41 ~ 0x46],[0x61~0x66]的范围中的数据");
		}
		for (int i = 0; i < val.length; i++) {
			if (val[i] > -1 && val[i] < 0x0A) { // 将不可显示字符转化为可显示字符
				val[i] = (byte) (val[i] + 0x30);
			}
			if (val[i] > 0x09 && val[i] < 0x10) { // 将不可显示字符转化为可显示字符
				val[i] = (byte) (val[i] + 0x37);
			}
		}
		byte[] bcdData = null;
		String bcdStr = new String(val,ISOConfig.charSet);
		if (bcdStr.length() % 2 == 0) {
			bcdData = str2Bcd(bcdStr);
		} else {
			bcdStr = "0" + bcdStr;
			bcdData = str2Bcd(bcdStr); // 长度为奇数在最前端补零
		}
		return bcdData;
	}

	/**
	 * 功能描述：这个函数实现了左靠压缩功能，如果长度为奇数，在内容末尾补零
	 * 
	 * @param val
	 *            需要压缩的内容，类型为byte[]
	 * @return 以字节数组的方式返回压缩后的内容
	 * @throws Exception 
	 */
	public static byte[] doBCDLEFT(byte val[]) throws Exception {
		if (val == null) { // 检查参数是否为null
			throw new IllegalArgumentException("不能进行BCD压缩, 传入的参数为null");
		}
		if (!canbeBCD(val)) { // 检查参数的内容是否合法
			throw new IllegalArgumentException(
					"不能进行BCD, 传入的参数非法：含有 不在[0x00~0x0F],[0x30 ~ 0x39], [0x41 ~ 0x46],[0x61~0x66]的范围中的数据");
		}
		for (int i = 0; i < val.length; i++) {
			if (val[i] > -1 && val[i] < 0x0A) { // 将不可显示字符转化为可显示字符
				val[i] = (byte) (val[i] + 0x30);
			}
			if (val[i] > 0x09 && val[i] < 0x10) { // 将不可显示字符转化为可显示字符
				val[i] = (byte) (val[i] + 0x37);
			}
		}
		byte[] bcdData = null;
		String bcdStr = new String(val,ISOConfig.charSet);
		if (bcdStr.length() % 2 == 0) {
			bcdData = str2Bcd(bcdStr);
		} else {
			bcdStr = bcdStr + "0";
			bcdData = str2Bcd(bcdStr); // 长度为奇数在末尾补零
		}
		return bcdData;
	}

	/**
	 * 功能描述：对压缩的BCD内容进行解压缩操作，例如有字节数组<br/>
	 * byte [] data = new
	 * byte[]{1,2,3,4,5,6,7,8};压缩之后为data={0x12,0x34,0x56,0x78};<br/>
	 * 解压缩之后的内容为"12345678"
	 * 
	 * @param bcdData
	 *            需要进行还原的BCD内容
	 * @return 以字符串的形式返回还原后的内容
	 */
	public static String bcd2Str(byte[] bcdData) {
		return DataConverter.bytesToHexString(bcdData);
	}

	/**
	 * 功能描述：10进制字符串转化为BCD压缩码，例如字符串str =
	 * "12345678",压缩之后的字节数组内容为{0x12,0x34,0x56,0x78}；
	 * 
	 * @param asc
	 *            需要进行压缩的ASCII码表示的字符串
	 * @return 以字节数组返回压缩后的内容
	 * @throws Exception 
	 */
	public static byte[] str2Bcd(String asc) throws Exception {
		if(null == asc || "".equals(asc)){
			throw new IllegalArgumentException("str2Bcd接口参数不能为null");
		}
		int len = asc.length();
		int mod = len % 2;

		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}

		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}

		byte bbt[] = new byte[len];
		abt = asc.getBytes(ISOConfig.charSet);
		int j, k;

		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}
			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}
			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}
}
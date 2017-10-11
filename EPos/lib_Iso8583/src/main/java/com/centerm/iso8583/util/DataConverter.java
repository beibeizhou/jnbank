/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  DataConverter.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

import com.centerm.iso8583.ISOConfig;

/**
 * 功能描述：这个类提供了数据类型转换的相关操作
 * 
 * @author Tianxiaobo
 * 
 */
public class DataConverter {
	/**
	 * 功能描述：将16进制的字符串转换为字节数组,例如有16进制字符串"12345678"<br/>
	 * 转换后的结果为：{18, 52 ,86 ,120 };
	 * 
	 * @param hex
	 *            需要转换的16进制字符串
	 * @return 以字节数组返回转换后的结果
	 */
	public static byte[] hexStringToByte(String hex) {
		if(null == hex || "".equals(hex)){
			throw new IllegalArgumentException("hexStringToByte接口参数不能为空");
		}
		hex = hex.toUpperCase();
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * 功能描述：把字节数组转换为十六进制字符串，例如有字节数组<br/>
	 * byte [] data = new byte[]{18, 52 ,86 ,120 };转换之后的结果为："12 34 56 78"
	 * 
	 * @param bArray
	 *            所要进行转换的数组内容
	 * @return 返回转换后的结果，内容用空格隔开
	 */
	public static final String bytesToHexString(byte[] bArray) {
		if(null == bArray){
			throw new IllegalArgumentException("bytesToHexString接口参数不能为null");
		}
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		int j = 0; // 此处定义的j用于控制每行输出的数据个�?
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
			j++;
		}
		return sb.toString();
	}
	/**
	 * 按照指定格式将字节数组转换为可打印16进制字符串
	 * @param bArray 字节数组
	 * @return 转换后的字符串
	 * @createtor：Administrator
	 * @date:2014-8-18 下午5:11:36
	 */
	public static final String bytesToHexStringForPrint(byte[] bArray) {
		if(null == bArray){
			throw new IllegalArgumentException("bytesToHexStringForPrint接口参数不能为null");
		}
		StringBuffer sb = new StringBuffer(bArray.length);
		sb.append("\r\n");
		String sTemp;
		int j = 0; // 此处定义的j用于控制每行输出的数据个�?
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase()).append(" ");
			j++;
			if (j % 16 == 0) {
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	/**
	 * 功能描述：把字节数组转换为对象
	 * 
	 * @param bytes
	 *            所要转换的字节数组
	 * @return 返回Object类型的对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static final Object bytesToObject(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream oi = new ObjectInputStream(in);
		Object o = oi.readObject();
		oi.close();
		return o;
	}

	/**
	 * 功能描述：把两个字节的字节数组转化为整型数据，高位补零，例如：<br/>
	 * 有字节数组byte[] data = new byte[]{1,2};转换后int数据的字节分布如下：<br/>
	 * 00000000 00000000 00000001 00000010,函数返回258
	 * 
	 * @param lenData
	 *            需要进行转换的字节数组
	 * @return 字节数组所表示整型值的大小
	 */
	public static int bytesToIntWhereByteLengthEquals2(byte lenData[]) {
		if (lenData.length != 2) {
			throw new IllegalArgumentException("所要转换的数组长度不符合要求，转换失败");
		}
		byte fill[] = new byte[] { 0, 0 };
		byte real[] = new byte[4];
		System.arraycopy(fill, 0, real, 0, 2);
		System.arraycopy(lenData, 0, real, 2, 2);
		int len = DataConverter.byteToInt(real);
		return len;

	}

	/**
	 * 功能描述：把可序列化对象转换成字节数组
	 * 
	 * @param s
	 *            可序列化对象
	 * @return 字节数组
	 * @throws IOException
	 */
	public static final byte[] objectToBytes(Serializable s) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream ot = new ObjectOutputStream(out);
		ot.writeObject(s);
		ot.flush();
		ot.close();
		return out.toByteArray();
	}

	public static final String objectToHexString(Serializable s)
			throws IOException {
		return bytesToHexString(objectToBytes(s));
	}

	public static final Object hexStringToObject(String hex)
			throws IOException, ClassNotFoundException {
		return bytesToObject(hexStringToByte(hex));
	}

	/**
	 * 功能描述：BCD码转换成阿拉伯数字，例如有数组byte[] data = new byte[]{0x12,0x34,0x56};<br/>
	 * 转换为阿拉伯数字字符串后为"123456"
	 * 
	 * @param bytes
	 *            所要转换的十进制BCD字节数组
	 * @return BCD码表示的阿拉伯数组内容
	 */
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
				.toString().substring(1) : temp.toString();
	}


	/**
	 * 功能描述：MD5加密字符串，返回加密后的16进制字符串
	 * 
	 * @param origin
	 *            需要进行加密的字符串
	 * @return 以十六进制返回加密后的字符串
	 * @throws Exception 
	 */
	public static String MD5EncodeToHex(String origin) throws Exception {
		return bytesToHexString(MD5Encode(origin));
	}

	/**
	 * 功能描述：MD5加密字符串
	 * 
	 * @param origin
	 *            需要加密的字符串
	 * @return 以字节数组返回加密后的字符串
	 * @throws Exception 
	 */
	public static byte[] MD5Encode(String origin) throws Exception {
		return MD5Encode(origin.getBytes(ISOConfig.charSet));
	}

	/**
	 * 功能描述：MD5加密字节数组，返回加密后的字节数组
	 * 
	 * @param bytes
	 *            需要进行加密的字节数组
	 * @return 加密后的字节数组
	 */
	public static byte[] MD5Encode(byte[] bytes) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			return md.digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	/**
	 * 功能描述：将一个整型数据转换为字节数组
	 * 
	 * @param intValue
	 *            需要转换的整型数据
	 * @return 该整型所对应的字节数组
	 */
	public static byte[] intToByte(int intValue) {
		byte[] result = new byte[4];
		result[0] = (byte) ((intValue & 0xFF000000) >> 24);
		result[1] = (byte) ((intValue & 0x00FF0000) >> 16);
		result[2] = (byte) ((intValue & 0x0000FF00) >> 8);
		result[3] = (byte) ((intValue & 0x000000ff));

		return result;
	}

	/**
	 * 功能描述：将byte数组转化为int类型的数据
	 * 
	 * @param byteVal
	 *            需要转化的字节数组
	 * @return 字节数组所表示的整型数据
	 */
	public static int byteToInt(byte[] byteVal) {
		int result = 0;
		for (int i = 0; i < byteVal.length; i++) {
			int tmpVal = (byteVal[i] << (8 * (3 - i)));
			switch (i) {
			case 0:
				tmpVal = tmpVal & 0xFF000000;
				break;
			case 1:
				tmpVal = tmpVal & 0x00FF0000;
				break;
			case 2:
				tmpVal = tmpVal & 0x0000FF00;
				break;
			case 3:
				tmpVal = tmpVal & 0x000000FF;
				break;
			}
			result = result | tmpVal;
		}
		return result;
	}

	/**
	 * 功能描述：把char类型的数据转换为byte[]字节
	 * 
	 * @param ch
	 *            char类型数据
	 * @return 该char类型数据对应的字节内容
	 */
	public static byte[] charToByte(char ch) {
		int temp = (int) ch;
		byte[] b = new byte[2];
		for (int i = b.length - 1; i > -1; i--) {
			b[i] = new Integer(temp & 0xFF).byteValue(); // 将最高位保存在最低位
			temp = temp >> 8; // 向右移位
		}
		return b;
	}

	/**
	 * 功能描述：把byte字节数组转化为char类型数据
	 * 
	 * @param b
	 *            需要转换的字节数组
	 * @return 返回字节数组所表示的char类型数据
	 */
	public static char byteToChar(byte[] b) {
		int s = 0;
		if (b[0] > 0)
			s += b[0];
		else
			s += 256 + b[0];
		s *= 256;
		if (b[1] > 0)
			s += b[1];
		else
			s += 256 + b[1];
		char ch = (char) s;
		return ch;
	}

	/**
	 * 功能描述：把double类型的数据转换为字节数组
	 * 
	 * @param d
	 *            需要转换的double类型的数据内容
	 * @return 该double类型数据对应的字节数组
	 */
	public static byte[] doubleToByte(double d) {
		byte[] b = new byte[8];
		long l = Double.doubleToLongBits(d);
		for (int i = 0; i < b.length; i++) {
			b[i] = new Long(l).byteValue();
			l = l >> 8;
		}
		return b;
	}

	/**
	 * 功能描述：把字节数组转换为double类型的数据
	 * 
	 * @param b
	 *            要进行转换的字节数组
	 * @return 返回该字节数组对应的double类型数据
	 */
	public static double byteToDouble(byte[] b) {
		long l;

		l = b[0];
		l &= 0xFF;
		l |= ((long) b[1] << 8);
		l &= 0xFFFF;
		l |= ((long) b[2] << 16);
		l &= 0xFFFFFF;
		l |= ((long) b[3] << 24);
		l &= 0xFFFFFFFF;
		l |= ((long) b[4] << 32);
		l &= 0xFFFFFFFFFFl;
		l |= ((long) b[5] << 40);
		l &= 0xFFFFFFFFFFFFl;
		l |= ((long) b[6] << 48);
		l &= 0xFFFFFFFFFFFFFFl;
		l |= ((long) b[7] << 56);

		return Double.longBitsToDouble(l);
	}

	/**
	 * 功能描述：将byte字节数组转换为double类型的数据
	 * 
	 * @param data
	 *            需要转的字节数组
	 * @return 转换后的double类型的数据
	 */
	public static double fromByteToDouble(byte[] data) {
		String str = "";
		char[] chardata = new char[10];
		for (int i = 0; i < data.length; i++) {
			chardata[i] = (char) data[i];
			str += chardata[i];
		}
		double num = Double.parseDouble(str);
		return num;
	}

	/**
	 * 功能描述：将字节数组转换为对应的二进制串
	 * 
	 * @param data
	 *            需要转换的字节数组
	 * @return 该字节数组对应的二进制字符串
	 */
	public static String byteToBinaryString(byte[] data) {
		if(null == data){
			throw new IllegalArgumentException("byteToBinaryString接口参数不能为null");
		}
		StringBuffer result = new StringBuffer();
		BitSet bs = new BitSet(data.length * 8);
		int pos = 0;
		for (int i = 0; i < data.length; i++) {
			int bit = 128;
			for (int b = 0; b < 8; b++) {
				bs.set(pos++, (data[i] & bit) != 0); // pos先参与set赋值后加1
				bit >>= 1;
			}
		}
		for (int i = 0; i < data.length * 8; i++) {
			if (bs.get(i)) {
				result.append("1");
			} else {
				result.append("0");
			}
		}
		return result.toString();
	}

	/**
	 * 功能描述：把二进制字符串转换为对应的字节数组
	 * 
	 * @param str
	 *            需要转换的0、1二进制字符串
	 * @return 二进制所表示的字节数组
	 */
	public static byte[] binaryStrToBytes(String str) {
		if(null == str || "".equals(str)){
			throw new IllegalArgumentException("binaryStrToBytes接口参数不能为或null");
		}
		if (str.length() % 8 != 0) {
			throw new IllegalArgumentException("所要转换的二进制字符串长度不是8的整数倍，转换失败");
		}
		char str_data[] = str.toCharArray();
		BitSet bs = new BitSet(str_data.length);
		for (int i = 0; i < str_data.length; i++) {
			switch (str_data[i]) {
			case '0':
				bs.set(i, false);
				break;
			case '1':
				bs.set(i, true);
				break;
			default:
				throw new IllegalArgumentException("所要转换的二进制字符串含有0、1之外的字符，无法进行转换");
			}
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte data[] = new byte[str.length() / 8];
		// Write bitmap into stream
		int pos = 128; // // 用来做位运算： -- 1000 0000（初值最高位为1，然后右移一位，等等）
		int b = 0; // 用来做位运算：初值二进制位全0
		for (int i = 0; i < str.length(); i++) {
			if (bs.get(i)) {
				b |= pos;
			}
			pos >>= 1;
			if (pos == 0) { // 到一个字节时（8位），就写入
				bos.write(b);
				pos = 128;
				b = 0;
			}
		}
		data = bos.toByteArray();
		return data;
	}

	/**
	 * 功能描述：在一个字符串的末尾补零，直至该字符串的长度是16的整数倍
	 * 
	 * @param str
	 *            待补位的字符串
	 * @return 返回补位后的字符串
	 */
	public static String addZeroRightToMod16Equal0(String str) {
		if(null == str || "".equals(str)){
			throw new IllegalArgumentException("addZeroRightToMod16Equal0接口参数不能为空或null");
		}
		StringBuffer sbf = null;
		if (str.length() % 16 != 0) {
			sbf = new StringBuffer(str);
			while (sbf.length() % 16 != 0) {
				sbf.append("0");
			}
			return sbf.toString(); // 将补位后的值返回
		} else {
			return str;
		}
	}

	/**
	 * 功能描述:对16进制字符串进行逐个8字节切割异或，字符串长度必须为16的整数倍
	 * 
	 * @param str
	 *            待进行计算的字符串
	 * @return 返回异或后的结果
	 */
	public static byte[] getStringXor(String macBlockData) {
		if(null == macBlockData || "".equals(macBlockData)){
			throw new IllegalArgumentException("getStringXor接口参数不能为空或null");
		}
		if(!isValidHexStr(macBlockData)){	//非有效的十六进制字符串
			throw new IllegalArgumentException("getStringXor接口所要异或的字符串含有0~F之外的字符");
		}
		if(macBlockData.length() % 16 != 0){
			throw new IllegalArgumentException("getStringXor接口所要异或的字符串长度不是16的整数倍");
		}
		byte[] macInfo = null;
		SecurityUtil sec = new SecurityUtil();
		if (macBlockData.length() % 16 != 0) { // 如果该字符串长度不是16的倍数，返回null
			return null;
		} else {
			byte[][] mabInfo = new byte[macBlockData.length() / 16][8];
			String[] mabInfo1 = new String[macBlockData.length() / 16];
			for (int i = 0; i < mabInfo.length; i++) { // 该循环用于对mabInfo进行初始化赋值
				mabInfo1[i] = macBlockData.substring(i * 16, i * 16 + 16); // 分别截取8位进行存储
			}
			for (int i = 0; i < mabInfo1.length; i++) {
				mabInfo[i] = DataConverter.hexStringToByte(mabInfo1[i]
						.toUpperCase());
			}
			macInfo = mabInfo[0]; // 将第一个mabInfo的值赋值给macInfo
			for (int i = 1; i < mabInfo.length; i++) { // 循环进行异或运算
				try {
					macInfo = sec.xor(macInfo, mabInfo[i]); // 两个字符串进行异或运算
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		ISOConfig.log("macBlockData逐个8字节异或之后的值为" + macInfo);
		return macInfo;
	}
	/**
	 * 判断字符串是否为有效的十六进制字符串
	 * @param str 要判断的16进制字符串
	 * @return true 为有效的十六进制字符串 false 无效的十六进制字符串
	 * @createtor：Administrator
	 * @date:2014-8-18 下午5:26:37
	 */
	public static boolean isValidHexStr(String str){
		if(null == str || "".equals(str)){
			throw new IllegalArgumentException("isValidHexStr接口参数不能为空或null");
		}
		boolean flag = true;
		if(null != str && !"".equals(str)){
			String temp = str.toUpperCase();
			for(int i = 0;i < temp.length();i++){
				if("0123456789ABCDEF".indexOf(temp.substring(i,i+1)) == -1){
					flag = false;
					break;
				}
			}
		}
		return flag;
	}
}

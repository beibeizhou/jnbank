package com.centerm.iso8583.util;
/**
 *  Copyright 2013, Fujian Centerm Information Co.,Ltd.  All right reserved.
 *  THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF  FUJIAN CENTERM PAY CO.,
 *  LTD.  THE CONTENTS OF THIS FILE MAY NOT BE DISCLOSED TO THIRD
 *  PARTIES, COPIED OR DUPLICATED IN ANY FORM, IN WHOLE OR IN PART,
 *  WITHOUT THE PRIOR WRITTEN PERMISSION OF  FUJIAN CENTERM PAY CO., LTD.
 *
 *  TLV函数
 *  Edit History:
 *
 *    2013/09/11 - Created by Xrh.
 *    
 *  Edit History：
 *  
 *   2013/10/22 - Modified by Xrh.
 *   L字段长度改为无符号整型
 */

import java.util.HashMap;
import java.util.Map;
/**
 * tlv报文解析工具类
 * @author RuiHua Xie
 *
 */
public class TlvUtil {
	/**
	 * 构造函数
	 * @param tlv 要执行解析的tlv十六进制串
	 * @return Map<String,String> 存储tag--value
	 */
	public static Map<String, String> tlvToMap(String tlv){
		return tlvToMap(hexStringToByte(tlv));
	}
	
	/**
	 * 若tag标签的第一个字节后四个bit为“1111”,则说明该tag占两个字节
	 * 例如“9F33”;否则占一个字节，例如“95”
	 * @param tlv 要执行解析的tlv报文
	 * @return 以Map的形式返回解包后的数据内容
	 */
	public static Map<String, String> tlvToMap(byte[] tlv){
		Map map = new HashMap<String, String>();
		int index = 0;
		while(index < tlv.length){
			if( (tlv[index]&0x1F)== 0x1F){ //tag双字节
				byte[] tag = new byte[2];
				System.arraycopy(tlv, index, tag, 0, 2);
				index+=2;
				
				int length = 0;
				if(tlv[index]>>7 == 0){	 //表示该L字段占一个字节
					length = tlv[index];	//value字段长度
					index++;
				}else {   //表示该L字段不止占一个字节
					
					int lenlen = tlv[index]&0x7F; //获取该L字段占字节长度
					index++;
					
					for (int i = 0; i < lenlen; i++) {
						length =length<<8;
						length += tlv[index]&0xff;  //value字段长度 &ff转为无符号整型
						index++;
					}
				}
				
				byte[] value =  new byte[length];
				System.arraycopy(tlv, index, value, 0, length);
				index += length;
				map.put(bcd2str(tag), bcd2str(value));
			}else{//tag单字节
				byte[] tag = new byte[1];
				System.arraycopy(tlv, index, tag,0 , 1);
				index++;
				
				int length = 0;
				if(tlv[index]>>7 == 0){	 //表示该L字段占一个字节
					length = tlv[index];	//value字段长度
					index++;
				}else {   //表示该L字段不止占一个字节
					
					int lenlen = tlv[index]&0x7F; //获取该L字段占字节长度
					index++;
					
					for (int i = 0; i < lenlen; i++) {
						length =length<<8;
						length += tlv[index]&0xff;  //value字段长度&ff转为无符号整型
						index++;
					}
				}
				
				byte[] value =  new byte[length];
				System.arraycopy(tlv, index, value, 0, length);
				index += length;
				map.put(bcd2str(tag), bcd2str(value));
			}
		}
		
		return map;
	}
	/**
	 * 压缩bcd转换为字符串
	 * @param bcds 压缩bcd字节数组
	 * @return String 对应的十进制字符串
	 */
	public static String bcd2str(byte[] bcds) {
		char[] ascii = "0123456789abcdef".toCharArray();
		byte[] temp = new byte[bcds.length * 2];
		
		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString().toUpperCase();
	}
	/**
	 * 把十六进制字符串转换为字节数组
	 * @param hex  十六进制字符串
	 * @return  byte[] 十六进制串对应的byte数组
	 */
	public static byte[] hexStringToByte(String hex) {
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
	/**
	 * 字符型数据转byte数据
	 * @param c 字符
	 * @return byte类型的值
	 */
	private static byte toByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
}
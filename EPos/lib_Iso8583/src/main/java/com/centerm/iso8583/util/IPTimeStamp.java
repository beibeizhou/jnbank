/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IPTimeStamp.java
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
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.Random ;

import com.centerm.iso8583.ISOConfig;
/**
 * 功能描述：进行时间日期字符串处理相关操作
 * @author Tianxiaobo
 *
 */
public class IPTimeStamp 
{
	private SimpleDateFormat sdf = null ;
	private String ip = null ;
	public IPTimeStamp(){
	}
	public IPTimeStamp(String ip)
	{
		this.ip = ip ;
	}
	/**
	 * 功能描述：获取IP时间戳
	 * @return 以字符串形式返回32位的时间内容，例如12700000000120130129102933456789
	 */
	public String getIPTimeRand()
	{
		StringBuffer buf = new StringBuffer() ;
		if(this.ip != null)
		{
			String s[] = this.ip.split("\\.") ;
			for(int i=0;i<s.length;i++)
			{
				buf.append(this.addZeroLeft(s[i],3)) ;
			}
		}
		buf.append(this.getTimeStamp()) ;
		Random r = new Random() ;
		for(int i=0;i<3;i++)
		{
			buf.append(r.nextInt(10)) ;
		}
		return buf.toString() ;
	}
	/**
	 * 功能描述:按照指定格式返回时间
	 * @return 返回的时间格式为yyyy-MM-dd HH:mm:ss.SSS
	 */
	public  String getDate()
	{
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") ;
		return this.sdf.format(new Date()) ;
	}
	/**
	 * 功能描述:按照指定的格式返回时间
	 * @return 返回的时间格式为yyyy-MM-dd HH:mm:ss
	 */
	public String getTime(){
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return this.sdf.format(new Date());
	}
	/**
	 * 功能描述:获取时间戳
	 * @return 返回时间戳的格式为yyyyMMddHHmmssSSS
	 */
	public String getTimeStamp()
	{
		this.sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS") ;
		return this.sdf.format(new Date()) ;
	}
	/**
	 * 功能描述:字符串左补零操作
	 * @param str 待补零的字符串
	 * @param len 补零之后的字符串长度
	 * @return 补零后的字符串内容
	 */
	public static String addZeroLeft_old(String str,int len)
	{
		StringBuffer s = new StringBuffer() ;
		s.append(str) ;
		while(s.length() < len)
		{
			s.insert(0,"0") ;
		}
		return s.toString() ;
	}
	
	public static String addZeroLeft(String str,int len){
		
		byte[] b={};
		try {
			b = str.getBytes(ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] bzero= new byte[len];
		
		for(int i=0;i<len;i++){
			bzero[i] = '0';
		}
		System.arraycopy(b, 0, bzero, len-b.length, b.length);
		
		try {
			return new String(bzero,ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 功能描述:字符串右补零操作
	 * @param str 待补零的字符串
	 * @param len 补零之后的字符串长度
	 * @return 补零后的字符串内容
	 */
	public static String addZeroRight_old(String str,int len)
	{
		StringBuffer s = new StringBuffer() ;
		s.append(str) ;
		while(s.length() < len)
		{
			s.append("0");
		}
		return s.toString() ;
	}
	public static String addZeroRight(String str,int len){
		
		byte[] b={};
		try {
			b = str.getBytes(ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] bzero= new byte[len];
		
		for(int i=0;i<len;i++){
			bzero[i] = '0';
		}
		System.arraycopy(b, 0, bzero, 0, b.length);
		
		try {
			return new String(bzero,ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 功能描述:字符串左补空格操作
	 * @param str 待补空格的字符串
	 * @param len 补空格之后的字符串长度
	 * @return 补空格之后的字符串内容
	 */
	public static String addSpaceLeft_old(String str,int len){
		StringBuffer s = new StringBuffer();
		s.append(str);
		while(s.length() < len){
			s.insert(0," ");
		}
		return s.toString();
	}
	public static String addSpaceLeft(String str,int len){
		
		byte[] b={};
		try {
			b = str.getBytes(ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] bzero= new byte[len];
		
		for(int i=0;i<len;i++){
			bzero[i] = ' ';
		}
		System.arraycopy(b, 0, bzero, len-b.length, b.length);
		
		try {
			return new String(bzero,ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 功能描述：字符串右补空格操作
	 * @param str 待补空格的字符串
	 * @param len 补空格后字符串的长度
	 * @return 补空格后的字符串
	 */
	public static String addSpaceRight_old(String str,int len){
		StringBuffer s = new StringBuffer();
		s.append(str);
		while(s.length() < len){
			s.append(" ");
		}
		return s.toString();
	}
	
	public static String addSpaceRight(String str,int len){
		
		byte[] b={};
		try {
			b = str.getBytes(ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] bzero= new byte[len];
		
		for(int i=0;i<len;i++){
			bzero[i] = ' ';
		}
		System.arraycopy(b, 0, bzero, 0, b.length);
		
		try {
			return new String(bzero,ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args){
		
		String a="1你好23";
		System.out.println("["+addZeroLeft(a,12)+"]");
		System.out.println("["+addZeroRight(a,12)+"]");
		System.out.println("["+addSpaceLeft(a,12)+"]");
		System.out.println("["+addSpaceRight(a,12)+"]");
	}
}
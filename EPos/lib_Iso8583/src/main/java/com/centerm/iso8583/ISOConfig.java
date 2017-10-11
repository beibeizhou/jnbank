package com.centerm.iso8583;
/**
 * 组件包整体配置信息表
 * @author Tianxiaobo
 *
 */
public class ISOConfig {
	/**是否为调试模式*/
	public static boolean isDebug = true;
	/**组件包所使用的字符集，默认为GBK*/
	public static String charSet = "GBK";
	/**
	 * 设置APP字符集
	 * @param charset
	 * @createtor：Administrator
	 * @date:2014-8-18 下午1:42:01
	 */
	public static  void setCharset(String charset){
		charSet = charset;
		log("当前使用的字符集为" + charset);
	}
	/**
	 * 打印信息到控制台
	 * @param str 要打印的信息内容 
	 * @createtor：Administrator
	 * @date:2014-8-18 下午1:45:08
	 */
	public static void  log(String str){
		if(isDebug){
			System.out.println("ISO8583_LOG-->" + str);
		}
	}
}

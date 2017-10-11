/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  MessageFactory.java
 * @Version : 1.0
 * @Create on:  2013-01-18
 * @Author   :  XiaoBo Tian
 *
 * @ChangeList
 * ---------------------------------------------------
 * Date         Editor              ChangeReasons
 *
 *
 */ 
package com.centerm.iso8583;

import com.centerm.iso8583.impl.Iso8583Impl;
import com.centerm.iso8583.inf.IExchange;
import com.centerm.iso8583.util.SystemInf;
/**
 * 功能描述：工厂类提供了访问报文操作的方法
 * @author Tianxiaobo
 */
public class MessageFactory {
	/**
	 * 功能描述：返回一个ISO8583实现类对象
	 * @return 返回一个报文组解包接口IExchange的实现类对象，用于进行8583组解包操作
	 */
	public static IExchange getIso8583Message(){
		return new Iso8583Impl();
	}
	/**
	 * 功能描述：返回一个SystemInf对象，用于获取版本信息
	 * @return SystemInf对象
	 */
	public static SystemInf getSystemInfo(){
		return new SystemInf();
	}
}

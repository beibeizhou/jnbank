/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoDataMode.java
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
package com.centerm.iso8583.enums;
/**
 * 功能描述：这个枚举描述了数据的来源与输出方式
 * @author Tianxiaobo
 */
public enum IsoDataMode {
	/**表明数据的来源根据key值从HashMap中获取*/
	Key,
	/**表明数据的来源从xml配置文件中读取*/
	Value
}

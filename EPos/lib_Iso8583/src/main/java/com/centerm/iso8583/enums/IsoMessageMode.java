/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoMessageMode.java
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
 * 功能：这枚举用于标识获取报文格式控制对象时的方式，有组包和解包两种方式
 * @author Tianxiaobo
 */
public enum IsoMessageMode {
	/**表示获取的控制对象为组包格式控制*/
	PACK,
	/**表示获取的控制对象为解包格式控制*/
	UNPACK
}

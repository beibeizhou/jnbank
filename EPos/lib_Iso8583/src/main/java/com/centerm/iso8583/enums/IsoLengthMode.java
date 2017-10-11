/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoLengthType.java
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
 * 功能描述：这个枚举描述了长度类型可能的取值范围
 * @author Tianxiaobo
 */
public enum IsoLengthMode {
	/**域的长度计算使用字节长度*/
	BYTELEN,
	/**域的长度计算使用字符长度，为默认计算方式，可不配置*/
	CHARLEN
}

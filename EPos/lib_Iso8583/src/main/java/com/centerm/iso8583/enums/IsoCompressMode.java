/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoCompressMode.java
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
package com.centerm.iso8583.enums;
/**
 * 功能描述：这个枚举中描述了数据域压缩方式可能的取值
 * @author Tianxiaobo
 */
public enum IsoCompressMode {
	/**表示数据使用BCD压缩*/
	BCD,
	/**表示数据使用ASCII码存储*/
	ASC,
	/**表示数据使用二进制存储*/
	BIN
}

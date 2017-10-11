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
public enum IsoLengthType {
	/**说明域的内容长度值为固定长度*/
	FIX,
	/**说明域数据长度的值为二字节变长，使用ASCII存储，占用2字节*/
	LLASC,
	/**说明域数据长度的值为三字节变长，使用ASCII存储，占用3字节*/
	LLLASC,
	/**说明域数据长度的值为二字节变长，使用BCD存储，占用1个字节*/
	LLBCD,
	/**说明域数据长度的值为三字节变长，使用BCD存储，占用2个字节，前端补零*/
	LLLBCD
}

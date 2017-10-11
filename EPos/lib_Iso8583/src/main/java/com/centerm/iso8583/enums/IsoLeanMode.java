/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoLeanMode.java
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
 * 功能描述：这个枚举列出了数据域的靠拢方式，针对ASC和BCD有不同的方式
 * @author Tianxiaobo
 */
public enum IsoLeanMode {
	/**表示左靠，长度不够右边补空格*/
	RIGHTSPACE,
	/**表示右靠，长度不够左边补空格*/
	LEFTSPACE,
	/**使用左靠BCD压缩,数据长度为奇数的话，末尾补零*/
	RIGHTZERO,
	/**使用右靠BCD压缩,数据长度为奇数的话，行首补零*/
	LEFTZERO
}


/*
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoOptional.java
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
 * 功能：该枚举用于指明数据域的出现方式
 * @author Tianxiaobo
 */
public enum IsoOptional {
	/**表明该域数据必须出现*/
	M,
	/**表明该域数据在特定条件下才出现*/
	C
}

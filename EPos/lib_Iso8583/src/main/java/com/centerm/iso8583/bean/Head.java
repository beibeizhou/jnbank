/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  Head.java
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
package com.centerm.iso8583.bean;

import java.util.Map;
/**
 * 功能描述：用于存放报文的头信息，主要用于构成ISO8583报文TPDU部分的内容
 * @author Tianxiaobo
 */
public class Head {
	/**存放域ID和对应的域格式控制对象*/
	private Map<Integer,Field> map = null;
	
	public Head(Map<Integer,Field> map){
		this.map = map;
	}
	/**
	 * 功能描述：获取存储有内容编号和格式控制对象的Map集合
	 * @return 存储有内容编号和格式控制对象的Map集合
	 */
	public Map<Integer, Field> getMap() {
		return map;
	}
	/**
	 * 功能描述：设置存储有内容编号和格式控制对象的Map集合
	 * @param map 存储有内容编号和格式控制对象的Map集合
	 */
	public void setMap(Map<Integer, Field> map) {
		this.map = map;
	}
	
}

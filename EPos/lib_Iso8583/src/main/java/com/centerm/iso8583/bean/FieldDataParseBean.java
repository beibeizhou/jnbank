/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  FieldDataParseBean.java
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
/**
 * 功能描述：用于描述每个数据域的内容被解析之后的相关信息
 * @author Xiaobo Tian
 */
public class FieldDataParseBean {
	/**用于存储从field对象中获取的Key值，方便数据填入HashMap中*/
	private String key;			//用于存储从field对象中获取的Key值，方便数据填入HashMap中
	/**用于存储从字节数组中解析之后每个域的内容*/
	private String content;		//用于存储从字节数组中解析之后每个域的内容
	/**用于存储该域数据在数组中所占的实际长度*/
	private int length;			//用于存储该域数据在数组中所占的实际长度
	
	public  FieldDataParseBean(String key,String content,int length){
		this.key = key;
		this.content = content;
		this.length = length;
	}
	/**
	 * 功能描述：获取key值
	 * @return key值
	 */
	public String getKey() {
		return key;
	}
	/**
	 * 功能描述：设置key值
	 * @param key key值
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * 功能描述：获取解析后的内容
	 * @return 解析后的内容
	 */
	public String getContent() {
		return content;
	}
	/**
	 * 功能描述：设置数据域内容
	 * @param content  数据域内容
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * 功能描述：获取数据域内容长度
	 * @return 数据域内容长度
	 */
	public int getLength() {
		return length;
	}
	/**
	 * 功能描述：设置数据域长度内容
	 * @param length 数据域长度内容
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
}

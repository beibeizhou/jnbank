/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  FormatInfo.java
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
 * 功能描述：用于描述一个交易报文的格式控制信息
 * @author Tianxiaobo
 */
public class FormatInfo {
	/**交易标识码，在配置文件中process标签中code元素的取值处定义*/
	private String code; // 标志该交易的交易类型
	/**报文头格式控制信息，主要包含TPDU部分内容*/
	private Head head; // 报文头内容
	/**报文体格式控制信息，主要包含报文类型和数据域组成内容等*/
	private Body body; // 报文体内容

	public FormatInfo(String code, Head head, Body body) {
		this.code = code;
		this.head = head;
		this.body = body;
	}
	/**
	 * 功能描述：获取交易标识码
	 * @return 返回交易标识码
	 */
	public String getCode() {
		return code;
	}
	/**
	 * 功能描述：设置交易标识码
	 * @param code 交易标识码
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * 功能描述：获取报文头控制信息
	 * @return 报文头格式控制对象
	 */
	public Head getHead() {
		return head;
	}
	/**
	 * 功能描述：设置报文头控制信息
	 * @param head 报文头格式控制对象
	 */
	public void setHead(Head head) {
		this.head = head;
	}
	/**
	 * 功能描述：获取报文体控制信息
	 * @return 报文体控制对象
	 */
	public Body getBody() {
		return body;
	}
	/**
	 * 功能描述：设置报文体控制信息
	 * @param body 报文体格式控制对象
	 */
	public void setBody(Body body) {
		this.body = body;
	}
}

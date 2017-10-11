/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  FieldDataBean.java
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
 * 功能描述：该bean的实例中描述了每个数据域的数据内容
 * @author XiaoBo Tian
 */
public class FieldDataBean {
	/**在head标签中对应head组成内容编号，在body标签中对应位域编号*/
	private int fieldId;		//在head标签中表示每个head组成的内容，在body标签中对应位域
	/**以字节数组方式表示每个域经过格式化后的数据内容*/
	private byte[] fieldData;	//表示每个域经过格式化后的数据内容
	/**表示每个域数据对应的字节数组长度，即fieldData的长度*/
	private int fieldDataLength;	//表示每个数据域字节数组的长度，即fieldData的数据长度
	
	public FieldDataBean(int fieldId,byte[] fieldData,int fieldDataLength){
		this.fieldId = fieldId;
		this.fieldData = fieldData;
		this.fieldDataLength = fieldDataLength;
	}
	/**
	 * 功能描述：获取fieldId
	 * @return 域编号
	 */
	public int getFieldId() {
		return fieldId;
	}
	/**
	 * 功能描述：设置fieldId
	 * @param fieldId 域编号
	 */
	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}
	/**
	 * 功能描述：获取域的数据内容
	 * @return 以字节数组方式返回域数据内容
	 */
	public byte[] getFieldData() {
		return fieldData;
	}
	/**
	 * 功能描述：设置数据域内容
	 * @param fieldData 数据域内容
	 */
	public void setFieldData(byte[] fieldData) {
		this.fieldData = fieldData;
	}
	/**
	 * 功能描述：获取数据域内容长度
	 * @return 数据域内容长度
	 */
	public int getFieldDataLength() {
		return fieldDataLength;
	}
	/**
	 * 功能描述：设置数据域内容长度值
	 * @param fieldDataLength 数据域内容长度值
	 */
	public void setFieldDataLength(int fieldDataLength) {
		this.fieldDataLength = fieldDataLength;
	}
}

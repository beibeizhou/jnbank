/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  Field.java
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

import com.centerm.iso8583.enums.IsoCompressMode;
import com.centerm.iso8583.enums.IsoDataMode;
import com.centerm.iso8583.enums.IsoLeanMode;
import com.centerm.iso8583.enums.IsoLengthMode;
import com.centerm.iso8583.enums.IsoLengthType;
import com.centerm.iso8583.enums.IsoOptional;
/**
 * 功能描述：这个类描述了每个数据域的配置信息
 * @author XiaoboTian
 */
public class Field {
	/**域ID*/
	private int fieldId;				//域ID
	/**域数据长度，如果为变长，表示最大长度*/
	private int length;					//长度值
	/**数据长度值类型，取值请参见IsoLengthType枚举取值范围*/
	private IsoLengthType lengthType;	//描述数据长度值的类型
	/**数据编码方式，取值请参见IsoCompressMode枚举取值范围*/
	private IsoCompressMode commode;	//数据内容的存储方式
	/**数据域内容的靠拢方式，取值请参见IsoLeanMode的取值范围*/
	private IsoLeanMode leanmode;		//数据的靠拢方式
	/**数据域内容的出现方式，取值请参见IsoOptional的取值范围*/
	private IsoOptional	optional;		//数据的出现方式
	/**数据内容的获取与输出方式，取值请参见IsoDataMode的取值范围*/
	private IsoDataMode dataMode;		//数据获取方式
	/**用来存储xml配置文件中field标签节点存储的值*/
	private String value;				//存储xml节点存储的值
	/**数据域长度计算方式*/
	private IsoLengthMode lengthMode;
	
	public Field(int fieldId, int length, IsoLengthType lengthType,
			IsoCompressMode commode, IsoLeanMode leanmode,
			IsoOptional optional, IsoDataMode dataMode,IsoLengthMode lengthMode,String value) {
		this.fieldId = fieldId;
		this.length = length;
		this.lengthType = lengthType;
		this.commode = commode;
		this.leanmode = leanmode;
		this.optional = optional;
		this.dataMode = dataMode;
		this.lengthMode = lengthMode;
		this.value = value;

	}
	/**
	 * 获取数据域长度计算方式
	 * @return 数据域长度计算方式
	 * @createtor：Administrator
	 * @date:2014-8-19 上午10:39:15
	 */
	public IsoLengthMode getLengthMode() {
		return lengthMode;
	}
	/**
	 * 设置数据域长度计算方式
	 * @param lengthMode
	 * @createtor：Administrator
	 * @date:2014-8-19 上午10:39:32
	 */
	public void setLengthMode(IsoLengthMode lengthMode) {
		this.lengthMode = lengthMode;
	}

	/**
	 * 功能描述：获取FieldId
	 * @return 返回FieldId的值
	 */
	public int getFieldId() {
		return fieldId;
	}
	/**
	 * 功能描述：设置fieldId的值
	 * @param fieldId 要设置的fieldId
	 */
	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}
	/**
	 * 功能描述：获取数据域数据内容长度信息
	 * @return 返回数据内容长度信息
	 */
	public int getLength() {
		return length;
	}
	/**
	 * 功能描述：设置数据域数据内容长度信息
	 * @param length 数据内容的长度
	 */
	public void setLength(int length) {
		this.length = length;
	}
	/**
	 * 功能描述：获取数据域数据内容的长度类型
	 * @return 数据内容的长度类型
	 */
	public IsoLengthType getLengthType() {
		return lengthType;
	}
	/**
	 * 功能描述：设置数据域数据内容的长度类型
	 * @param lengthType 数据内容的长度类型
	 */
	public void setLengthType(IsoLengthType lengthType) {
		this.lengthType = lengthType;
	}
	/**
	 * 功能描述：获取数据域数据内容的编码方式
	 * @return 数据内容编码方式
	 */
	public IsoCompressMode getCommode() {
		return commode;
	}
	/**
	 * 功能描述：设置数据域内容存储编码方式
	 * @param commode 数据域内容的存储方式
	 */
	public void setCommode(IsoCompressMode commode) {
		this.commode = commode;
	}
	/**
	 * 功能描述：获取数据域内容的靠拢方式
	 * @return 数据域内容靠拢方式
	 */
	public IsoLeanMode getLeanmode() {
		return leanmode;
	}
	/**
	 * 功能描述：设置数据域内容的靠拢方式
	 * @param leanmode 数据域内容靠拢方式
	 */
	public void setLeanmode(IsoLeanMode leanmode) {
		this.leanmode = leanmode;
	}
	/**
	 * 功能描述：获取数据域内容出现方式
	 * @return 数据域内容出现方式
	 */
	public IsoOptional getOptional() {
		return optional;
	}
	/**
	 * 功能描述：设置数据域内容出现方式
	 * @param optional 数据域内容出现方式
	 */
	public void setOptional(IsoOptional optional) {
		this.optional = optional;
	}
	/**
	 * 功能描述：获取数据域内容的获取方式
	 * @return 数据域内容的获取方式
	 */
	public IsoDataMode getDataMode() {
		return dataMode;
	}
	/**
	 * 功能描述：设置数据域内容的获取方式
	 * @param dataMode 数据域内容的获取方式
	 */
	public void setDataMode(IsoDataMode dataMode) {
		this.dataMode = dataMode;
	}
	/**
	 * 功能描述：获取field节点的值
	 * @return 以字符串方式返回field节点的值
	 */
	public String getValue() {
		return value;
	}
	/**
	 * 功能描述：设置field节点的值
	 * @param value field节点的值
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Field{" +
				"fieldId=" + fieldId +
				", length=" + length +
				", lengthType=" + lengthType +
				", commode=" + commode +
				", leanmode=" + leanmode +
				", optional=" + optional +
				", dataMode=" + dataMode +
				", value='" + value + '\'' +
				", lengthMode=" + lengthMode +
				'}';
	}
}

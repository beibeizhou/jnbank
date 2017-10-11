/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IExchange.java
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
package com.centerm.iso8583.inf;

import java.util.Map;

import com.centerm.iso8583.IsoMessage;
import com.centerm.iso8583.bean.FormatInfo;
/**
 * 功能描述：该接口中规定了解包和组包所需的接口信息
 * @author Tianxiaobo
 */
public interface IExchange {
	/**
	 * 功能描述：解包函数
	 * @param msg Iso8583报文
	 * @param formatInfo 解包格式控制对象
	 * @return 存放有数据域标签和内容的Map集合，例如[["msg_tp","0200"]["pri_act_no","622848291030"]]
	 */
	public Map<String, String> unPackTrns(byte[] msg, FormatInfo formatInfo) throws Exception;			//解包方法
	/**
	 * 功能描述：组包函数
	 * @param map 存放有数据标签和内容的map集合
	 * @param formatInfo 组包格式控制对象
	 * @return 进行组装好的Iso8583报文对象
	 * @throws Exception 
	 */
	public IsoMessage packTrns(Map<String, String> map, FormatInfo formatInfo) throws Exception;			//组包方法
	
	public Map<Integer,byte[]> getFieldMap();
}

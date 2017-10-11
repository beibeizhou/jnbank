/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  FormatInfoFactory.java
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

import com.centerm.iso8583.enums.IsoMessageMode;
/**
 * 功能描述：该类中存放着整个配置文件的格式化控制信息
 * @author Tianxiaobo
 */
public class FormatInfoFactory {
	/**存放交易标志码和交易报文*/
	private Map<String,FormatInfo> map = null;
	/**存放交易标识码和mac校验信息*/
	private Map<String,String> mabMap = null;
	
	public FormatInfoFactory(){
	}
	/**
	 * 功能描述:FormatInfoFactory类的构造函数
	 * @param map	这个map集合中存放了交易标识码和与之对应的交易报文格式控制对象formatInfo
	 * @param mabMap 这个Map集合中存放了交易标识码和与之对应的MAC校验信息域组成字符串，例如<br/>
	 * 2|3|11|14这样的字符串，用于说明构成MAC信息的数据域来源
	 */
	public FormatInfoFactory(Map<String,FormatInfo> map,Map<String,String> mabMap){
		this.map = map;
		this.mabMap = mabMap;
	}
	/**
	 * 功能描述：获取存放交易标识码和格式控制对象的Map集合
	 * @return 返回存储有交易标识码和格式控制对象的map集合
	 */
	public Map<String, FormatInfo> getMap() {
		return map;
	}
	/**
	 * 功能描述：设置存放交易标识码和格式控制对象的Map集合
	 * @param map 返回存储有交易标识码和格式控制对象的map集合
	 */
	public void setMap(Map<String, FormatInfo> map) {
		this.map = map;
	}
	/**
	 * 功能描述：根据交易类型标志码和报文组解包方向返回一个交易的格式化操作对象
	 * @param code	交易标识码，唯一标识一个交易
	 * @return	返回一个交易的格式化控制对象FormatInfo
	 */
	public FormatInfo getFormatInfo(String code,IsoMessageMode msgmode){
		String joinKey = code + msgmode;		//拼接key值
		return this.map.get(joinKey);
	}
	/**
	 * 功能描述：根据交易类型标识码返回一个交易的格式化操作对象
	 * @param code 交易标识码，唯一标识一个交易
	 * @return 返回一个交易的格式化控制对象FormatInfo
	 */
	public FormatInfo getFormatInfo(String code){
		return this.map.get(code);
	}
	/**
	 * 功能描述：获取存储有交易标识码和MAC校验过滤字符串的map集合
	 * @return	存储有交易标识码和MAC校验过滤字符串的map集合
	 */
	public Map<String, String> getMabMap() {
		return mabMap;
	}
	/**
	 * 功能描述：设置存储有交易标识码和MAC校验过滤字符串的map集合
	 * @param mabMap 存储有交易标识码和MAC校验过滤字符串的map集合
	 */
	public void setMabMap(Map<String, String> mabMap) {
		this.mabMap = mabMap;
	}
	/**
	 *功能描述：根据交易类型标识码和报文组解包方向返回一个交易的MAC码过滤字符串
	 * @param code 交易标识码，唯一标识一个交易
	 * @param msgmode 报文的组解包方向，PACK为组包，UNPACK为解包
	 * @return mac计算数据域来源过滤字符串
	 */
	public String getMabInfo(String code,IsoMessageMode msgmode){
		String joinKey = code + msgmode;		//拼接key值
		return this.mabMap.get(joinKey);
	}
	/**
	 * 功能描述：根据交易类型标识码返回一个交易的MAC码过滤字符串
	 * @param code 交易类型标识码
	 * @return MAC计算数据域来源过滤字符串
	 */
	public String getMabInfo(String code){
		return this.mabMap.get(code);
	}
}

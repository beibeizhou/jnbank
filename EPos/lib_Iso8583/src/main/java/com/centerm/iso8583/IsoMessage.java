/**
 *  copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoMessage.java
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

package com.centerm.iso8583;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.centerm.iso8583.bean.FieldDataBean;
import com.centerm.iso8583.util.DataConverter;
/**
 * 功能:用于描述一个Iso8583报文
 * @author Tianxiaobo
 */
public class IsoMessage {
	/**byte[] header 表示报文头，主要包括TPDU部分内容*/
	private byte[]	header = null;		//表示报文头
	/**表示报文类型，压缩的话2个字节，ASCII码表示的话4个字节*/
	private byte[]	msg_tp = null;		//表示报文类型
	/**表示报文的位域，二进制表示，通常为8个字节数组，或16字节数组*/
	private byte[]	bitMap = null;		//表示位域
	/**存储了报文域ID和对应的报文域内容*/
	private Map<Integer,byte[]> fieldMap = new HashMap<Integer,byte[]>();		//表示每个域的数据内容
	
	public IsoMessage(byte[] header,byte[] msg_tp,byte[] bitMap,Map<Integer,byte[]> fieldMap){
		this.header = header;
		this.msg_tp = msg_tp;
		this.bitMap = bitMap;
		this.fieldMap = fieldMap;
	}
	
	public IsoMessage(){
	}
	/**
	 * 功能描述：获取报文头信息
	 * @return 以字节数组方式返回报文头信息
	 */
	public byte[] getHeader() {
		return header;
	}
	/**
	 * 功能描述：设置报文头信息
	 * @param header 字节数组表示的报文头信息
	 */
	public void setHeader(byte[] header) {
		this.header = header;
	}
	/**
	 * 功能描述：获取报文类型
	 * @return 以字节数组方式返回报文类型数据
	 */
	public byte[] getMsg_tp() {
		return msg_tp;
	}
	/**
	 * 功能描述：设置报文类型数据
	 * @param msg_tp 报文类型数据
	 */
	public void setMsg_tp(byte[] msg_tp) {
		this.msg_tp = msg_tp;
	}
	/**
	 * 功能描述：获取报文bitMap数据
	 * @return 以字节数组方式返回bitMap内容
	 */
	public byte[] getBitMap() {
		return bitMap;
	}
	/**
	 * 功能描述：设置报文bitMap内容
	 * @param bitMap 以字节数组表示的报文bitMap内容
	 */
	public void setBitMap(byte[] bitMap) {
		this.bitMap = bitMap;
	}
	/**
	 * 功能描述：返回报文域的数据集合Map
	 * @return 以map集合的方式返回报文域的内容，域ID与域数据一一对应
	 */
	public Map<Integer, byte[]> getFieldMap() {
		return fieldMap;
	}
	/**
	 * 功能描述：设置报文域内容
	 * @param fieldMap 存储报文域ID和域数据的Map集合
	 */
	public void setFieldMap(Map<Integer, byte[]> fieldMap) {
		this.fieldMap = fieldMap;
	}
	/**
	 * 功能描述：根据域ID集合，计算报文的BitMap内容
	 * @return 以字节数组的方式返回bitMap的内容
	 */
	public byte[] getBitMapData(){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Map<Integer,byte[]> map = this.fieldMap;
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(map.keySet());
		Collections.sort(list);
		BitSet bs = new BitSet(64);
    	for (Integer i : list) {	// BitSet可以自动扩展大小
    		bs.set(i - 1, true);
    	}
    	//Extend to 128 if needed
    	if (bs.length() > 64) {
    		BitSet b2 = new BitSet(128);
    		b2.or(bs);	// 得到位图(根据域的个数，可能自动扩展)
    		bs = b2;
    		bs.set(0, true);
    	}
    	//Write bitmap into stream
		int pos = 128; 	// 用来做位运算： -- 1000 0000（初值最高位为1，然后右移一位，等等）
		int b = 0; 	// 用来做位运算：初值二进制位全0
		for (int i = 0; i < bs.size(); i++) {
			if (bs.get(i)) {
				b |= pos;
			}
			pos >>= 1;
			if (pos == 0) { // 到一个字节时（8位），就写入
				bos.write(b);
				pos = 128;
				b = 0;
			}
		}
		return bos.toByteArray();
	}
	/**
	 * 功能描述：获取8583报文所有域的内容
	 * @return 以字节数组的方式返回报文域的所有内容
	 */
	public byte[] getAllFieldData(){
		Map<Integer,byte[]> map = this.fieldMap;
		List<Integer> list = new ArrayList<Integer>();
		List<FieldDataBean> fieldBean = new ArrayList<FieldDataBean>();
		list.addAll(map.keySet());
		Collections.sort(list);
		for(Integer key:list){
			FieldDataBean fieldData = new FieldDataBean(key,map.get(key),map.get(key).length);
			fieldBean.add(fieldData);
		}
		return byteArrayCollapse(fieldBean);
	}
	/**
	 * 功能描述：将message对象中的所有属性转换成符合ISO8583报文格式的字节数组返回
	 * @return 以字节数组的形式返回符合ISO8583要求的报文数据
	 */
	public byte[] getAllMessageByteData(){
		byte[] message = null;
		int pos = 0;
		int headLength = this.getHeader().length;		//获取报文头长度信息
		int msgTypeidLength = this.getMsg_tp().length;	//获取报文类型长度信息
		int bitMapLength = this.getBitMap().length;		//获取位域长度信息
		int allFieldDataLength = this.getAllFieldData().length;	//获取报文长度信息
		int allLength =  headLength + msgTypeidLength + bitMapLength + allFieldDataLength;
		message = new byte[allLength];		//初始化数组
		System.arraycopy(this.getHeader(), 0, message, pos, headLength);
		pos += headLength;
		System.arraycopy(this.getMsg_tp(), 0, message, pos, msgTypeidLength);
		pos += msgTypeidLength;
		System.arraycopy(this.getBitMap(), 0, message, pos, bitMapLength);
		pos += bitMapLength;
		System.arraycopy(this.getAllFieldData(), 0, message, pos, allFieldDataLength);
		return message;
	}
	/**
	 * 功能描述：根据mab过滤条件和规则，获取macBlock信息
	 * @param mabStr mab过滤字符串在配置文件中mab-filter标签下配置
	 * @return 处理好的macBlock信息
	 */
	public String  getMacBlock(String mabStr){
		String mabInfo = "";
		try {
			if(this.getMsg_tp()!=null){
				mabInfo += new String(this.getMsg_tp(),"GBK");		//将报文类型添加进报文域中
			}
			//mabInfo += DataConverter.bytesToHexString(this.getBitMap());		//获取位图域
			String[] mabField = mabStr.split("\\|");		//根据过滤条件获取拆分后的域内容
			for (int i = 0; i < mabField.length; i++) {
				if(this.getFieldMap().get(Integer.parseInt(mabField[i])) != null){
					byte[] fieldData = this.getFieldMap().get(Integer.parseInt(mabField[i]));
					if("90".trim().equals(mabField[i])) {
						byte[] field90 = new byte[20];
						System.arraycopy(fieldData, 0, field90, 0, 20);
						mabInfo += " "+new String(field90,"GBK");
					}
					else{
						mabInfo += " "+new String(fieldData,"GBK");
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return mabInfo.trim();
	}
	/**
	 * 功能描述：该方法用于把fieldDataListt集合中的多个数组合并为一个数组
	 * @param fieldDataList 需要进行合并的list集合
	 * @return 把集合中对象的数组属性内容进行合并，以字节数组形式返回
	 */
	public   byte[] byteArrayCollapse(List<FieldDataBean> fieldDataList){
		byte[] data = null;
		int allLength = 0;
		for(FieldDataBean fieldDataBean:fieldDataList){		//循环遍历list集合
			allLength += fieldDataBean.getFieldDataLength();	//获取每个数组的长度和
		}
		data = new byte[allLength];		//初始化字节数组用于存放所有单个字节的内容
		int pos = 0;
		for(FieldDataBean fieldDataBean:fieldDataList){
			byte[] temp = fieldDataBean.getFieldData();
			int length = fieldDataBean.getFieldDataLength();
			System.arraycopy(temp, 0, data, pos, length);
			pos += length;
		}
		return data;
	}
}

/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  IsoConfigParser.java
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
package com.centerm.iso8583.parse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.centerm.iso8583.ISOConfig;
import com.centerm.iso8583.bean.Body;
import com.centerm.iso8583.bean.Field;
import com.centerm.iso8583.bean.FormatInfo;
import com.centerm.iso8583.bean.FormatInfoFactory;
import com.centerm.iso8583.bean.Head;
import com.centerm.iso8583.enums.IsoCompressMode;
import com.centerm.iso8583.enums.IsoDataMode;
import com.centerm.iso8583.enums.IsoLeanMode;
import com.centerm.iso8583.enums.IsoLengthMode;
import com.centerm.iso8583.enums.IsoLengthType;
import com.centerm.iso8583.enums.IsoMessageMode;
import com.centerm.iso8583.enums.IsoOptional;

/**
 * 功能描述：该类用于对配置文件进行解析，并转换为对象
 * 
 * @author Tianxiaobo
 */
public class IsoConfigParser {
	private static FormatInfoFactory formatInfoFactory = new FormatInfoFactory();
	private Field field = null;
	private Head head = null;
	private Body body = null;
	private FormatInfo formatInfo = null;

	public IsoConfigParser() {

	}

	/**
	 * 功能描述：根据配置的IsoConfig.xml配置文件，将配置信息转化为FormatInfoFactory对象
	 * 
	 * @param filepath
	 *            配置文件所在路径
	 * @return FormatInfoFactory对象
	 * @throws Exception
	 * @throws IOException
	 *             抛出IOException异常
	 */
	public FormatInfoFactory parseFromXMLConfigFile(String filepath)
			throws Exception {
		InputStream ins;
		try {
			ins = new FileInputStream(filepath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new FileNotFoundException(filepath + "文件不存在");
		}
		if (ins != null) {
			ISOConfig.log("parsing config from xml file: [" + filepath + "]");
			try {
				try {
					parse(formatInfoFactory, ins);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new IllegalArgumentException(
							"请检查配置文件中的标签元素取值是否超出枚举范围");
				}
			} finally {
				ins.close();
			}
		}
		return formatInfoFactory;
	}

	/**
	 * 功能描述：根据传入的xml文件的输入流进行解析
	 * 
	 * @param ins
	 *            xml文件的输入流
	 * @return 返回FormatInfoFactory对象
	 * @throws IOException
	 *             抛出IOException
	 */
	public FormatInfoFactory parseFromInputStream(InputStream ins)
			throws Exception {
		if (ins != null) {
			try {
				try {
					parse(formatInfoFactory, ins);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new IllegalArgumentException(
							"请检查配置文件中的标签元素取值是否超出枚举范围");
				}
			} finally {
				ins.close();
			}
		} else {
			throw new IllegalArgumentException("传入输入流无法按照xml文件格式进行解析");
		}
		return formatInfoFactory;
	}

	/**
	 * 功能描述：根据配置文件所在的URL地址将配置文件信息转换为FormatInfo对象
	 * 
	 * @param url
	 *            配置文件所在的URL地址
	 * @return FormatInfoFacotry对象
	 * @throws IOException
	 *             抛出IOException异常
	 */
	public FormatInfoFactory parseFromUrl(URL url) throws Exception {
		InputStream stream = url.openStream();
		try {
			parse(formatInfoFactory, stream);
		} finally {
			stream.close();
		}
		return formatInfoFactory;
	}

	/**
	 * 功能描述：读取XML配置文件的信息，并将其转化为FormatInfoFactory对象，用于为后续组解包提供数据格式化规则
	 * 
	 * @param formatInfoFactory
	 *            包含了配置文件中有关每个交易组解包相关配置信息
	 * @param stream
	 *            XML配置文件的输入流
	 */
	protected void parse(FormatInfoFactory formatInfoFactory, InputStream stream)
			throws IOException {
		final DocumentBuilderFactory docfact = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docb = null;
		Document doc = null;
		try {
			docb = docfact.newDocumentBuilder();
			doc = docb.parse(stream);
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
			return;
		} catch (SAXException ex) {
			ex.printStackTrace();
			return;
		}
		final Element root = doc.getDocumentElement();

		// Read the iso8583 message configure PROCESS
		NodeList nodes = root.getElementsByTagName("process");
		Element elem = null;
		Map<String, FormatInfo> formatFactoryMap = new HashMap<String, FormatInfo>();
		Map<String, String> mabMap = new HashMap<String, String>(); // 这个Map用于存储每个交易对应的MAC校验码
		IsoMessageMode msgmode = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			elem = (Element) nodes.item(i);
			String code = elem.getAttribute("code"); // 获取交易码code
			if (code == null || "".equals(code)) {
				throw new IllegalArgumentException("process标签中的code元素的值不能为空");
			}
			if (elem.getAttribute("type").length() > 0) { // 如果type值不为空就获取
				msgmode = IsoMessageMode.valueOf(elem.getAttribute("type")); // 获取组解包类型
			}
			if (elem.getChildNodes() == null
					|| elem.getChildNodes().getLength() == 0) {
				throw new IllegalArgumentException(
						"请检查xml配置文件，是否含有无效的process配置节点信息");
			}
			NodeList headInfo = elem.getElementsByTagName("head"); // 获取head节点下的信息
			Map<Integer, Field> headMap = new HashMap<Integer, Field>();
			Element headField = null;
			for (int j = 0; j < headInfo.getLength(); j++) {
				headField = (Element) headInfo.item(j);
				int id = 0;
				NodeList fieldsInfo = headField.getElementsByTagName("field"); // 获取HEAD节点下的Field信息
				for (int m = 0; m < fieldsInfo.getLength(); m++) {
					headField = (Element) fieldsInfo.item(m);
					id = Integer.parseInt(headField.getAttribute("id")); // 获取id值
					int length = Integer.parseInt(headField
							.getAttribute("length"));
					IsoLengthType lengthType = IsoLengthType.valueOf(headField
							.getAttribute("length-type")); // 获取长度表示类型
					IsoCompressMode commode = IsoCompressMode.valueOf(headField
							.getAttribute("compress-mode")); // 获取数据压缩方式
					IsoLeanMode lean_mode = IsoLeanMode.valueOf(headField
							.getAttribute("lean-mode")); // 获取靠拢方式
					IsoOptional optional = IsoOptional.valueOf(headField
							.getAttribute("optional")); // 获取出现方式
					IsoDataMode dataMode = IsoDataMode.valueOf(headField
							.getAttribute("data-mode")); // 获取数据来源方式
					String value = null;
					String lengmodestr = headField.getAttribute("length-mode");
					IsoLengthMode lengthMode = null;
					if("".equals(lengmodestr)){
						lengthMode = IsoLengthMode.valueOf("CHARLEN");
					}else{
						lengthMode = IsoLengthMode.valueOf(headField
							.getAttribute("length-mode"));
					}
					
					if (headField.getChildNodes() != null
							|| headField.getChildNodes().getLength() != 0) { // 如果
						value = headField.getChildNodes().item(0)
								.getNodeValue();
					} else {
						throw new IllegalArgumentException(
								"配置文件出错，Head中的Field节点中的值不能为空");
					}
					field = new Field(id, length, lengthType, commode,
							lean_mode, optional, dataMode, lengthMode, value);
					headMap.put(id, field);
				}
			}
			// head.setMap(headMap); // head节点解析完成之后完成head对象的初始化
			head = new Head(headMap);
			NodeList bodyInfo = elem.getElementsByTagName("body"); // 获取body节点下的信息
			Element bodyNode = null;
			Map<Integer, Field> bodyMap = new HashMap<Integer, Field>();
			for (int j = 0; j < bodyInfo.getLength(); j++) {
				bodyNode = (Element) bodyInfo.item(j);
				int id = 0;
				NodeList fieldsInfo = bodyNode.getElementsByTagName("field"); // 获取HEAD节点下的Field信息
				for (int m = 0; m < fieldsInfo.getLength(); m++) {
					bodyNode = (Element) fieldsInfo.item(m);
					id = Integer.parseInt(bodyNode.getAttribute("id")); // 获取id值
					int length = Integer.parseInt(bodyNode
							.getAttribute("length"));
					IsoLengthType lengthType = IsoLengthType.valueOf(bodyNode
							.getAttribute("length-type")); // 获取长度表示类型
					IsoCompressMode commode = IsoCompressMode.valueOf(bodyNode
							.getAttribute("compress-mode")); // 获取数据压缩方式
					IsoLeanMode lean_mode = IsoLeanMode.valueOf(bodyNode
							.getAttribute("lean-mode")); // 获取靠拢方式
					IsoOptional optional = IsoOptional.valueOf(bodyNode
							.getAttribute("optional")); // 获取出现方式
					IsoDataMode dataMode = IsoDataMode.valueOf(bodyNode
							.getAttribute("data-mode")); // 获取数据来源方式
//					IsoLengthMode lengthMode = IsoLengthMode.valueOf(bodyNode
//							.getAttribute("length-mode")); // 获取数据来源方式
					String value = null;
					String lengmodestr = bodyNode.getAttribute("length-mode");
					IsoLengthMode lengthMode = null;
					lengthMode = null;
					if("".equals(lengmodestr)){
						lengthMode = IsoLengthMode.valueOf("CHARLEN");
					}else{
						lengthMode = IsoLengthMode.valueOf(bodyNode
							.getAttribute("length-mode"));
					}
					if (bodyNode.getChildNodes() != null
							|| bodyNode.getChildNodes().getLength() != 0) { // 如果
						value = bodyNode.getChildNodes().item(0).getNodeValue();
					} else {
						throw new IllegalArgumentException(
								"配置文件出错，Body中的Field节点中的值不能为空");
					}
					field = new Field(id, length, lengthType, commode,
							lean_mode, optional, dataMode, lengthMode, value);
					bodyMap.put(id, field);
				}
			}
			body = new Body(bodyMap);
			if (msgmode != null) {
				code = code + msgmode;
			}
			formatInfo = new FormatInfo(code, head, body);
			NodeList mabInfo = elem.getElementsByTagName("mab-filter"); // 解析mab-filter下的内容
			Element mabNode = null;
			String mabFilterStr = null;
			for (int p = 0; p < mabInfo.getLength(); p++) {
				mabNode = (Element) mabInfo.item(p);
				if (mabNode.getChildNodes().getLength() > 0
						|| mabNode.getChildNodes() != null) {
					mabFilterStr = mabNode.getChildNodes().item(0)
							.getNodeValue(); // 获取节点值
				}
			}
			formatFactoryMap.put(code, formatInfo); // 每循环一次就增加一个
			mabMap.put(code, mabFilterStr);
		}
		formatInfoFactory.setMap(formatFactoryMap);
		formatInfoFactory.setMabMap(mabMap);
	}
}

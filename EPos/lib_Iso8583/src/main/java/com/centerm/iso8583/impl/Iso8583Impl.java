/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  Iso8583Impl.java
 * @Version : 1.0
 * @Create on:  2013-01-18
 * @Author :  Xiaobo Tian
 * @ChangeList ---------------------------------------------------
 * Date         Editor              ChangeReasons
 */
package com.centerm.iso8583.impl;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.centerm.iso8583.ISOConfig;
import com.centerm.iso8583.IsoMessage;
import com.centerm.iso8583.bean.Field;
import com.centerm.iso8583.bean.FieldDataBean;
import com.centerm.iso8583.bean.FieldDataParseBean;
import com.centerm.iso8583.bean.FormatInfo;
import com.centerm.iso8583.bean.Head;
import com.centerm.iso8583.enums.IsoCompressMode;
import com.centerm.iso8583.enums.IsoDataMode;
import com.centerm.iso8583.enums.IsoLeanMode;
import com.centerm.iso8583.enums.IsoLengthMode;
import com.centerm.iso8583.enums.IsoLengthType;
import com.centerm.iso8583.enums.IsoOptional;
import com.centerm.iso8583.inf.IExchange;
import com.centerm.iso8583.util.BCDUtil;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.iso8583.util.IPTimeStamp;
import com.centerm.iso8583.util.ISOString;
import com.centerm.iso8583.util.M3HexUtil;

/**
 * 功能：用于实现ISO8583组包和解包功能
 * @author Tianxiaobo
 */
public class Iso8583Impl implements IExchange {

    private Map<Integer, byte[]> fieldMap = new HashMap<Integer, byte[]>();        //表示每个域的数据内容

    public Map<Integer, byte[]> getFieldMap() {
        return fieldMap;
    }

    public Iso8583Impl() {        //构造方法
    }
    //====================================组包功能函数存放区域开始==========================//

    /**
     * 功能说明：该函数为组包函数，用于根据数据源和格式控制对象组8583报文
     * @param map 存储了组包所需的数据源内容
     * @param formatInfo 存放了对数据域内容进行约束的格式控制对象
     * @return IsoMessage对象
     * @throws Exception
     */
    public IsoMessage packTrns(Map<String, String> map, FormatInfo formatInfo) throws Exception {
        IsoMessage message = new IsoMessage();        //表示一个ISO8583报文对象
        byte[] headerData = null;        //表示报文头
        byte[] msgTypeid = null;        //表示报文类型
        byte[] fieldData = null;        //表示单个域数据
        FieldDataBean bean = null;        //用来存放每个数据域的内容
        Field field = null;                //定义一个Field对象

        String code = formatInfo.getCode();        //获取交易码
        Map<Integer, Field> headMap = formatInfo.getHead().getMap();            //获取报文头格式控制信息
        Set<Integer> headMapKeySet = headMap.keySet();    //获取报文头map中的键集合
        List<Integer> list = new ArrayList<Integer>();
        list.addAll(headMapKeySet);
        Collections.sort(list);        //对headMap中的内容进行排序
        List<FieldDataBean> headerBean = new ArrayList<FieldDataBean>();    //定义一个list存放报文头信息
        for (Object headKey : list) {        //遍历headMap，处理报文头
            field = (Field) headMap.get(headKey);        //获取单个域对象
            String realValue = null;
            String nodeValue = field.getValue();        //获取节点的额值
            IsoDataMode datamode = field.getDataMode();        //获取dataMode的值
            IsoOptional optional = field.getOptional();        //获取Optionla的值
            if (datamode.equals(IsoDataMode.Key)) {
                realValue = (String) map.get(nodeValue);        //从HashMap中获取数据
            } else if (datamode.equals(IsoDataMode.Value)) {
                realValue = nodeValue;            //从配置文件获取内容
            }
            ISOConfig.log("nodeValue = " + nodeValue);
            ISOConfig.log("realValue = " + realValue);
            //开始判断Optional
            if ((realValue == null || "".equals(realValue)) && optional.equals(IsoOptional.C)) {
                continue;        //继续解析下一条数据
            } else if ((realValue == null || "".equals(realValue)) && optional.equals(IsoOptional.M)) {
                throw new IllegalArgumentException("交易类型码为" + code + "的交易中，head标签下Id为" + field.getFieldId() + "的域数据是必须的，但是没有获取到该值");
            } else {
                fieldData = fieldDataDealInPack(field, realValue);            //把控制格式对象和数据内容交给FieldDataDeal方法处理
            }
            bean = new FieldDataBean(field.getFieldId(), fieldData, fieldData.length);
            headerBean.add(bean);        //将bean添加进list中
        }
        headerData = byteArrayCollapse(headerBean);        //计算好了报文头，赋值给headData
        message.setHeader(headerData);        //设置报文头

        Map bodyMap = formatInfo.getBody().getMap();
        Set bodyMapKeySet = bodyMap.keySet();            //获取报文体格式控制信息
        list.clear();        //清空list
        list.addAll(bodyMapKeySet);
        Collections.sort(list);            //对list中的内容进行排序
        for (Object bodyKey : list) {
            field = (Field) bodyMap.get(bodyKey);
            String realValue = null;
            String nodeValue = field.getValue();        //获取节点的额值
            IsoOptional optional = field.getOptional();        //获取Optional的值
            IsoDataMode datamode = field.getDataMode();        //获取的值
            if (datamode.equals(IsoDataMode.Key)) {
                realValue = (String) map.get(nodeValue);        //从HashMap中获取数据
            } else if (datamode.equals(IsoDataMode.Value)) {
                realValue = nodeValue;            //从配置文件获取内容
            }
            //开始判断Optional
            if ((null == realValue || realValue.equals("")) && optional.equals(IsoOptional.C)) {
                continue;        //继续解析下一条数据
            } else if ((realValue == null || "".equals(realValue)) && optional.equals(IsoOptional.M)) {
                throw new IllegalArgumentException("交易类型码为" + code + "的交易中，body标签下Id为" + field.getFieldId() + "的域数据是必须的，但是没有获取到该值");
            } else {
                System.out.println("realValue: " + realValue + "   filedid:" + field.getFieldId());
                fieldData = fieldDataDealInPack(field, realValue);            //把控制格式对象和数据内容交给FieldDataDeal方法处理

            }
            if (field.getFieldId() == 0) {            //报文类型，单独处理
                msgTypeid = fieldDataDealInPack(field, realValue);
                message.setMsg_tp(msgTypeid);        //设置报文体
            } else {
                message.getFieldMap().put(field.getFieldId(), fieldData);    //将每一个数据放入message的集合中。
            }
        }
        message.setBitMap(message.getBitMapData());
        //打印报文头
        //ISOConfig.log("组包报文头为:" + DataConverter.bytesToHexStringForPrint(message.getHeader()));
        //打印消息类型
        //ISOConfig.log("组包消息类型为:" + DataConverter.bytesToHexStringForPrint(message.getMsg_tp()));
        //打印位图
        //ISOConfig.log("位图为:" + DataConverter.bytesToHexStringForPrint(message.getBitMapData()));
        //打印报文提
        //ISOConfig.log("报文体为:" + DataConverter.bytesToHexStringForPrint(message.getAllFieldData()));
        //打印整个报文信息
        //ISOConfig.log("报文信息为:" + DataConverter.bytesToHexStringForPrint(message.getAllMessageByteData()));
        return message;
    }

    /**
     * 功能描述：组包过程中处理单个Field对象的格式和其对应的数据
     * @param field 单个域的格式控制对象
     * @param realValue 数据域的数据内容
     * @return 以字节数组的形式返回格式化后的数据内容
     * @throws Exception
     */
    public byte[] fieldDataDealInPack(Field field, String realValue) throws Exception {
        byte data[] = null;
        IsoLengthType lengthType = field.getLengthType();        //获取长度类型
        switch (lengthType) {        //进行lengthType的判断
            case FIX:    //长度类型为定长
                data = dealFixInPack(field, realValue);
                break;        //Fix的break
            case LLASC:    //长度类型为二位变长，并且长度使用ASC码
                data = dealLLASCInPack(field, realValue);
                break;        //LLASC的break
            case LLBCD:    //长度类型为二位变长，并且使用BCD
                data = dealLLBCDInPack(field, realValue);
                break; //LLBCD的break
            case LLLASC: //长度类型为三位变长，并且使用ASC码
                data = dealLLLASCInPack(field, realValue);
                break;//LLLVAR的break
            case LLLBCD:    //长度类型为三位变长，并且使用BCD压缩
                data = dealLLLBCDInPack(field, realValue);
                break;    //LLLBCD的break
        }
        return data;
    }

    /**
     * 功能描述：组包过程中处理lengthtype属性为FIX类型的数据域对象
     * @param field 单个域的格式控制对象
     * @param realValue 数据域的数据内容
     * @return 以字节数组的形式返回格式化后的数据内容
     * @throws Exception
     */
    public byte[] dealFixInPack(Field field, String realValue) throws Exception {
        byte data[] = null;
        int id = field.getFieldId();        //获取对应的ID
        int length = field.getLength();        //获取长度，变长数据为最大长度
        int realValueLength = ISOString.length(realValue);        //获取实际获取数值的长度
        //System.out.println("["+id+"]"+"["+realValueLength+"]["+realValue+"]");
        IsoLengthType lengthType = field.getLengthType();        //获取长度类型
        IsoCompressMode commode = field.getCommode();        //获取编码方式
        //mod by txb 20140819 二进制处理不采用0和1字符串传入，直接传入16进制
        if (commode.equals(IsoCompressMode.BIN)) {
            length = length * 2;
        }

        IsoLeanMode leanmode = field.getLeanmode();        //获取靠拢方式
        IsoOptional optional = field.getOptional();        //获取出现方式
        IsoDataMode datamode = field.getDataMode();        //获取数据来源
        if (realValueLength > length || realValueLength == length) {    //实际获取的内容长度值比预定义长度大，进行截断操作
            //realValue = realValueLength > length ?realValue.substring(0,length):realValue;	//进行数据截断操作
            realValue = realValueLength > length ? ISOString.substring(realValue, 0, length) : realValue;
            //System.out.println("["+id+"]"+"["+realValueLength+"]["+realValue+"]");
            switch (commode) {
                case ASC:
                    //mod by txb 20140819 增加字符集
                    data = realValue.getBytes(ISOConfig.charSet);
                    System.out.println("data.length=" + data.length);
                    break;
                case BCD:
                    if (length % 2 == 0) {
                        data = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                    } else {
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            data = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            data = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                    break;
                case BIN:        //如果为二进制存储
                    data = DataConverter.hexStringToByte(realValue);    //将十六进制字符串转化为对应的字节数组
                    break;
            }
        }
        if (realValueLength < length) {        //实际定义的长度值比预定义长度小，进行补位操作
            switch (commode) {
                case ASC:
                    if (leanmode == IsoLeanMode.LEFTZERO) {
                        realValue = IPTimeStamp.addZeroLeft(realValue, length);
                        data = realValue.getBytes(ISOConfig.charSet);
                    }
                    if (leanmode == IsoLeanMode.LEFTSPACE) {
                        realValue = IPTimeStamp.addSpaceLeft(realValue, length);
                        data = realValue.getBytes(ISOConfig.charSet);
                    }
                    if (leanmode == IsoLeanMode.RIGHTSPACE) {
                        realValue = IPTimeStamp.addSpaceRight(realValue, length);
                        data = realValue.getBytes(ISOConfig.charSet);
                    }
                    if (leanmode == IsoLeanMode.RIGHTZERO) {
                        realValue = IPTimeStamp.addZeroRight(realValue, length);
                        data = realValue.getBytes(ISOConfig.charSet);
                    }
                    break;
                case BCD:
                    if (leanmode == IsoLeanMode.LEFTZERO) {
                        realValue = IPTimeStamp.addZeroLeft(realValue, length);
                    }
                    if (leanmode == IsoLeanMode.RIGHTZERO) {
                        realValue = IPTimeStamp.addZeroRight(realValue, length);
                    }
                    if (leanmode == IsoLeanMode.RIGHTSPACE || leanmode == IsoLeanMode.LEFTSPACE) {
                        throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                    }
                    if (length % 2 == 0) {
                        data = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                    } else {
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            data = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            data = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                    break;
                case BIN:        //二进制字符串位数不够，报错处理
                    throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BIN，且为定长类型，但是传入的实际值不够" + (length / 2) + "个字节");
            }
        }
        return data;
    }

    /**
     * 功能描述：组包过程中处理lengthtype属性为LLASC的数据域内容
     * @param field 单个域的格式控制对象
     * @param realValue 数据域的数据内容
     * @return 以字节数组的形式返回格式化后的数据内容
     * @throws Exception
     */
    public byte[] dealLLASCInPack(Field field, String realValue) throws Exception {
        byte data[] = null;
        int id = field.getFieldId();        //获取对应的ID
        int length = field.getLength();        //获取长度，变长数据为最大长度
        int realValueLength = ISOString.length(realValue);        //获取实际获取数值的长度
        IsoLengthType lengthType = field.getLengthType();        //获取长度类型
        IsoCompressMode commode = field.getCommode();        //获取编码方式
        //mod by txb 20140819 二级制不采用0和1字符串，直接采用16进制字符串
        if (commode.equals(IsoCompressMode.BIN)) {
            length = length * 2;
        }
        IsoLeanMode leanmode = field.getLeanmode();            //获取靠拢方式
        IsoOptional optional = field.getOptional();        //获取出现方式
        IsoDataMode datamode = field.getDataMode();        //获取数据来源
        IsoLengthMode lengtgmode = field.getLengthMode();    //获取长度属性
        if (realValueLength > length || realValueLength == length) {    //实际获取的内容长度值比预定义的长度值大，进行截断操作
            //realValue = realValueLength > length ?realValue.substring(0,length):realValue;	//进行数据截断操作
            realValue = realValueLength > length ? ISOString.substring(realValue, 0, length) : realValue;
            byte[] LLVAR = null;    //存储变长数据的长度
            if (length < 10) {
                LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            switch (commode) {
                case ASC:        //数据内容为ASC编码
                    data = new byte[length + 2];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);    //将长度信息拷贝进data数组
                    System.arraycopy(realValue.getBytes(ISOConfig.charSet), 0, data, 2, length);
                    break;
                case BCD:
                    if (length % 2 == 0) {
                        byte[] content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        data = new byte[content.length + 2];
                        System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                        System.arraycopy(content, 0, data, 2, content.length);
                    } else {
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            byte[] content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 2];
                            System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                            System.arraycopy(content, 0, data, 2, content.length);
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            byte[] content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 2];
                            System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                            System.arraycopy(content, 0, data, 2, content.length);
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，leanmode属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                    break;
                case BIN://进行二进制处理
                    length = length / 2;
                    if (length < 10) {
                        LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    byte[] content = DataConverter.hexStringToByte(realValue);
                    data = new byte[content.length + 2];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 2, content.length);
                    break;
            }
        }
        if (realValueLength < length) {    //实际获取的内容长度值比实际值小，进行长度修订操作
            byte LLVAR[] = null;
            byte content[] = null;
            length = realValueLength;    //重新修改Length的值
            if (length < 10) {
                LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            switch (commode) {
                case ASC:
                    content = realValue.getBytes(ISOConfig.charSet);
                    data = new byte[2 + content.length];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 2, content.length);
                    break;
                case BCD:
                    if (length % 2 == 0) {
                        content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                    } else {
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                    data = new byte[content.length + 2];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 2, content.length);
                    break;
                case BIN:        //进行二进制处理
                    length = realValue.length() / 2;
                    if (length < 10) {
                        LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    content = DataConverter.hexStringToByte(realValue);
                    data = new byte[content.length + 2];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 2, content.length);
                    break;
            }
        }
        return data;
    }

    /**
     * 功能描述：组包过程中处理lengthtype属性为LLBCD数据域的内容
     * @param field 单个域的格式控制对象
     * @param realValue 数据域的数据内容
     * @return 以字节数组的形式返回格式化后的数据内容
     * @throws UnsupportedEncodingException
     */
    public byte[] dealLLBCDInPack(Field field, String realValue) throws Exception {
        byte data[] = null;
        int id = field.getFieldId();        //获取对应的ID
        int length = field.getLength();        //获取长度，变长数据为最大长度
        int realValueLength = ISOString.length(realValue);        //获取实际获取数值的长度
        IsoLengthType lengthType = field.getLengthType();        //获取长度类型
        IsoCompressMode commode = field.getCommode();        //获取编码方式
        if (commode.equals(IsoCompressMode.BIN)) {
            length = length * 2;
        }
        IsoLeanMode leanmode = field.getLeanmode();            //获取靠拢方式
        IsoOptional optional = field.getOptional();        //获取出现方式
        IsoDataMode datamode = field.getDataMode();        //获取数据来源
        IsoLengthMode lengthMode = field.getLengthMode();    //获取数据域长度计算方式
        if (realValueLength > length || realValueLength == length) {    //实际获取的内容长度值比预定义的长度值大，进行截断操作
            //realValue = realValueLength > length ?realValue.substring(0,length):realValue;	//进行数据截断操作
            realValue = realValueLength > length ? ISOString.substring(realValue, 0, length) : realValue;
            byte[] LLVAR = null;    //存储变长数据的长度
            byte[] content = null;    //存储内容
            if (length < 10) {
                LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            LLVAR = BCDUtil.doBCD(LLVAR);    //对于长度数据进行BCD压缩
            switch (commode) {
                case ASC:        //数据内容为ASC编码
                    data = new byte[length + 1];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);    //将长度信息拷贝进data数组
                    System.arraycopy(realValue.getBytes(ISOConfig.charSet), 0, data, 1, length);
                    break;
                case BCD:
                    if (lengthMode.equals(IsoLengthMode.BYTELEN)) {    //改数据域长度采用字节方式计算
                        if (length % 2 != 0) {    //不是偶数个字符，报错
                            throw new IllegalArgumentException("id为" + field.getFieldId() + "的字段长度使用字节计数，数据长度值必须为偶数，请检查");
                        }
                        length = length / 2;    //长度折半后参与计算
                        if (length < 10) {
                            LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                        } else {
                            LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                        }
                        LLVAR = BCDUtil.doBCD(LLVAR);    //对于长度数据进行BCD压缩
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 1];
                            System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                            System.arraycopy(content, 0, data, 1, content.length);
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 1];
                            System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                            System.arraycopy(content, 0, data, 1, content.length);
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，leanmode属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    } else {
                        if (length % 2 == 0) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 1];
                            System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                            System.arraycopy(content, 0, data, 1, content.length);
                        } else {
                            if (leanmode == IsoLeanMode.LEFTZERO) {
                                content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                                data = new byte[content.length + 1];
                                System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                                System.arraycopy(content, 0, data, 1, content.length);
                            }
                            if (leanmode == IsoLeanMode.RIGHTZERO) {
                                content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                                data = new byte[content.length + 1];
                                System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                                System.arraycopy(content, 0, data, 1, content.length);
                            }
                            if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                                throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，leanmode属性不能为LEFTSPACE和RIGHTSPACE");
                            }
                        }
                    }
                    break;
                case BIN://需要进行长度恢复操作
                    length = length / 2;
                    if (length < 10) {
                        LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    LLVAR = BCDUtil.doBCD(LLVAR);    //对于长度数据进行BCD压缩
                    content = DataConverter.hexStringToByte(realValue);
                    data = new byte[content.length + 1];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 1, content.length);
                    break;
            }
        }
        if (realValueLength < length) {    //实际获取的内容长度值比实际值小，进行长度修订操作
            byte LLVAR[] = null;
            byte content[] = null;
            length = realValueLength;    //重新修改Length的值
            if (length < 10) {
                LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            LLVAR = BCDUtil.doBCD(LLVAR);        //将两个字节的长度压缩为1个字节
            switch (commode) {
                case ASC:
                    content = realValue.getBytes(ISOConfig.charSet);
                    data = new byte[1 + content.length];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 1, content.length);
                    break;
                case BCD:
                    if (lengthMode.equals(IsoLengthMode.BYTELEN)) {    //改数据域长度采用字节方式计算
                        if (length % 2 != 0) {    //不是偶数个字符，报错
                            throw new IllegalArgumentException("id为" + field.getFieldId() + "的字段长度使用字节计数，数据长度值必须为偶数，请检查");
                        }
                        length = length / 2;    //长度折半后参与计算
                        if (length < 10) {
                            LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                        } else {
                            LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                        }
                        LLVAR = BCDUtil.doBCD(LLVAR);    //对于长度数据进行BCD压缩
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 1];
                            System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                            System.arraycopy(content, 0, data, 1, content.length);
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 1];
                            System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                            System.arraycopy(content, 0, data, 1, content.length);
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，leanmode属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    } else {
                        if (length % 2 == 0) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        } else {
                            if (leanmode == IsoLeanMode.LEFTZERO) {
                                content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            }
                            if (leanmode == IsoLeanMode.RIGHTZERO) {
                                content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                            }
                            if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                                throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                            }
                        }
                    }
                    data = new byte[content.length + 1];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 1, content.length);
                    break;
                case BIN:        //需要特殊处理
                    length = realValue.length() / 2;        //进行长度恢复操作
                    if (length < 10) {
                        LLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    LLVAR = BCDUtil.doBCD(LLVAR);    //对于长度数据进行BCD压缩
                    content = DataConverter.hexStringToByte(realValue);
                    data = new byte[content.length + 1];
                    System.arraycopy(LLVAR, 0, data, 0, LLVAR.length);
                    System.arraycopy(content, 0, data, 1, content.length);
                    break;
            }
        }
        return data;
    }

    /**
     * 功能描述：组包过程中处理lengthtype属性为LLLASC属性域对应的值
     * @param field 单个域的格式控制对象
     * @param realValue 数据域的数据内容
     * @return 以字节数组的形式返回格式化后的数据内容
     * @throws Exception
     */
    public byte[] dealLLLASCInPack(Field field, String realValue) throws Exception {
        byte data[] = null;
        int id = field.getFieldId();        //获取对应的ID
        int length = field.getLength();        //获取长度，变长数据为最大长度
        int realValueLength = ISOString.length(realValue);        //获取实际获取数值的长度
        IsoLengthType lengthType = field.getLengthType();        //获取长度类型
        IsoCompressMode commode = field.getCommode();        //获取编码方式
        if (commode.equals(IsoCompressMode.BIN)) {
            length = length * 2;
        }
        IsoLeanMode leanmode = field.getLeanmode();            //获取靠拢方式
        IsoOptional optional = field.getOptional();        //获取出现方式
        IsoDataMode datamode = field.getDataMode();        //获取数据来源
        if (realValueLength > length || realValueLength == length) {    //实际获取的内容长度值比预定义的长度值大，进行截断操作
            //realValue = realValueLength > length ?realValue.substring(0,length):realValue;	//进行数据截断操作
            realValue = realValueLength > length ? ISOString.substring(realValue, 0, length) : realValue;
            byte[] LLLVAR = null;    //存储变长数据的长度
            byte[] content = null;    //定义内容存储区域
            if (length < 10) {
                LLLVAR = ("00" + length).getBytes(ISOConfig.charSet);
            } else if (length < 100) {
                LLLVAR = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            switch (commode) {
                case ASC:        //数据内容为ASC编码
                    data = new byte[length + 3];
                    System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);    //将长度信息拷贝进data数组
                    System.arraycopy(realValue.getBytes(ISOConfig.charSet), 0, data, 3, length);
                    break;
                case BCD:
                    if (length % 2 == 0) {
                        content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        data = new byte[content.length + 3];
                        System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);
                        System.arraycopy(content, 0, data, 3, content.length);
                    } else {
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 3];
                            System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);
                            System.arraycopy(content, 0, data, 3, content.length);
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 3];
                            System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);
                            System.arraycopy(content, 0, data, 3, content.length);
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，leanmode属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                    break;
                case BIN://需要特殊处理
                    length = length / 2;
                    if (length < 10) {
                        LLLVAR = ("00" + length).getBytes(ISOConfig.charSet);
                    } else if (length < 100) {
                        LLLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    content = DataConverter.hexStringToByte(realValue);    //将二进制字符串转化为对应的字节数组
                    data = new byte[content.length + 3];
                    System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);
                    System.arraycopy(content, 0, data, 3, content.length);
                    break;
            }
        }
        if (realValueLength < length) {    //实际获取的内容长度值比实际值小，进行长度修订操作
            byte LLLVAR[] = null;
            byte content[] = null;
            //length = realValue.length();	//重新修改Length的值
            length = realValueLength;
            if (length < 10) {
                LLLVAR = ("00" + length).getBytes(ISOConfig.charSet);
            } else if (length < 100) {
                LLLVAR = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            switch (commode) {
                case ASC:
                    content = realValue.getBytes(ISOConfig.charSet);
                    data = new byte[3 + content.length];
                    System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);
                    System.arraycopy(content, 0, data, 3, content.length);
                    break;
                case BCD:
                    if (length % 2 == 0) {
                        content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                    } else {
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new IllegalArgumentException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                    data = new byte[content.length + 3];
                    System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);
                    System.arraycopy(content, 0, data, 3, content.length);
                    break;
                case BIN:        //二进制处理
                    length = realValue.length() / 2;
                    if (length < 10) {
                        LLLVAR = ("00" + length).getBytes(ISOConfig.charSet);
                    } else if (length < 100) {
                        LLLVAR = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLLVAR = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    content = DataConverter.hexStringToByte(realValue);    //将二进制字符串转化为对应的字节数组
                    data = new byte[content.length + 3];
                    System.arraycopy(LLLVAR, 0, data, 0, LLLVAR.length);
                    System.arraycopy(content, 0, data, 3, content.length);
                    break;
            }
        }
        return data;
    }

    /**
     * 功能描述：组包过程中处理lengthType为LLLBCD的属性域内容
     * @param field 单个域的格式控制对象
     * @param realValue 数据域的数据内容
     * @return 以字节数组的形式返回格式化后的数据内容
     * @throws Exception
     */
    public byte[] dealLLLBCDInPack(Field field, String realValue) throws Exception {
        byte data[] = null;
        int id = field.getFieldId();        //获取对应的ID
        int length = field.getLength();        //获取长度，变长数据为最大长度
        int realValueLength = ISOString.length(realValue);        //获取实际获取数值的长度
        IsoLengthType lengthType = field.getLengthType();        //获取长度类型
        IsoCompressMode commode = field.getCommode();        //获取编码方式
        if (commode.equals(IsoCompressMode.BIN)) {
            length = length * 2;
        }
        IsoLeanMode leanmode = field.getLeanmode();            //获取靠拢方式
        IsoOptional optional = field.getOptional();        //获取出现方式
        IsoDataMode datamode = field.getDataMode();        //获取数据来源
        IsoLengthMode lengthMode = field.getLengthMode();    //获取lengthmode
        if (realValueLength > length || realValueLength == length) {    //实际获取的内容长度值比预定义的长度值大，进行截断操作
            //realValue = realValueLength > length ?realValue.substring(0,length):realValue;	//进行数据截断操作
            realValue = realValueLength > length ? ISOString.substring(realValue, 0, length) : realValue;
            byte[] LLLBCD = null;    //存储变长数据的长度
            byte[] content = null;    //存储内容
            if (length < 10) {
                LLLBCD = ("00" + length).getBytes(ISOConfig.charSet);
            } else if (length < 100) {
                LLLBCD = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLLBCD = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            LLLBCD = BCDUtil.doBCD(LLLBCD);    //对长度进行压缩
            switch (commode) {
                case ASC:        //数据内容为ASC编码
                    data = new byte[length + 2];
                    System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);    //将长度信息拷贝进data数组
                    System.arraycopy(realValue.getBytes(ISOConfig.charSet), 0, data, 2, length);
                    break;
                case BCD:
                    if (lengthMode.equals(IsoLengthMode.BYTELEN)) {
                        if (length % 2 != 0) {
                            throw new IllegalArgumentException("id为" + field.getFieldId() + "的字段长度使用字节计数，数据长度值必须为偶数，请检查");
                        }
                        length = length / 2;
                        if (length < 10) {
                            LLLBCD = ("00" + length).getBytes(ISOConfig.charSet);
                        } else if (length < 100) {
                            LLLBCD = ("0" + length).getBytes(ISOConfig.charSet);
                        } else {
                            LLLBCD = String.valueOf(length).getBytes(ISOConfig.charSet);
                        }
                        LLLBCD = BCDUtil.doBCD(LLLBCD);    //对长度进行压缩
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 2];
                            System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                            System.arraycopy(content, 0, data, 2, content.length);
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 2];
                            System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                            System.arraycopy(content, 0, data, 2, content.length);
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new RuntimeException("Id为" + id + "的数据行压缩编码使用了BCD，leanmode属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    } else {
                        if (length % 2 == 0) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            data = new byte[content.length + 2];
                            System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                            System.arraycopy(content, 0, data, 2, content.length);
                        } else {
                            if (leanmode == IsoLeanMode.LEFTZERO) {
                                content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                                data = new byte[content.length + 2];
                                System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                                System.arraycopy(content, 0, data, 2, content.length);
                            }
                            if (leanmode == IsoLeanMode.RIGHTZERO) {
                                content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                                data = new byte[content.length + 2];
                                System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                                System.arraycopy(content, 0, data, 2, content.length);
                            }
                            if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                                throw new RuntimeException("Id为" + id + "的数据行压缩编码使用了BCD，leanmode属性不能为LEFTSPACE和RIGHTSPACE");
                            }
                        }
                    }
                    break;
                case BIN://进行二进制处理
                    length = length / 2;    //进行长度恢复
                    if (length < 10) {
                        LLLBCD = ("00" + length).getBytes(ISOConfig.charSet);
                    } else if (length < 100) {
                        LLLBCD = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLLBCD = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    LLLBCD = BCDUtil.doBCD(LLLBCD);    //对长度进行压缩
                    content = DataConverter.hexStringToByte(realValue);
                    data = new byte[content.length + 2];
                    System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                    System.arraycopy(content, 0, data, 2, content.length);
                    break;
            }
        }
        if (realValueLength < length) {    //实际获取的内容长度值比实际值小，进行长度修订操作
            byte LLLBCD[] = null;
            byte content[] = null;
            length = realValueLength;    //重新修改Length的值
            if (field.getLengthMode() != null && field.getLengthMode().equals("T")) {
                length = length / 2;
            }
            if (length < 10) {
                LLLBCD = ("00" + length).getBytes(ISOConfig.charSet);
            } else if (length < 100) {
                LLLBCD = ("0" + length).getBytes(ISOConfig.charSet);
            } else {
                LLLBCD = String.valueOf(length).getBytes(ISOConfig.charSet);
            }
            LLLBCD = BCDUtil.doBCD(LLLBCD);
            switch (commode) {
                case ASC:
                    content = realValue.getBytes(ISOConfig.charSet);
                    data = new byte[2 + content.length];
                    System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                    System.arraycopy(content, 0, data, 2, content.length);
                    break;
                case BCD:
                    if (lengthMode.equals(IsoLengthMode.BYTELEN)) {    //以字节方式计算长度
                        if (length % 2 != 0) {
                            throw new IllegalArgumentException("id为" + field.getFieldId() + "的字段长度使用字节计数，数据长度值必须为偶数，请检查");
                        }
                        length = length / 2;
                        if (length < 10) {
                            LLLBCD = ("00" + length).getBytes(ISOConfig.charSet);
                        } else if (length < 100) {
                            LLLBCD = ("0" + length).getBytes(ISOConfig.charSet);
                        } else {
                            LLLBCD = String.valueOf(length).getBytes(ISOConfig.charSet);
                        }
                        LLLBCD = BCDUtil.doBCD(LLLBCD);    //对长度进行压缩
                        if (leanmode == IsoLeanMode.LEFTZERO) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.RIGHTZERO) {
                            content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                        }
                        if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                            throw new RuntimeException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                        data = new byte[content.length + 2];
                        System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                        System.arraycopy(content, 0, data, 2, content.length);
                    } else {
                        if (length % 2 == 0) {
                            content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                        } else {
                            if (leanmode == IsoLeanMode.LEFTZERO) {
                                content = BCDUtil.doBCD(realValue.getBytes(ISOConfig.charSet));
                            }
                            if (leanmode == IsoLeanMode.RIGHTZERO) {
                                content = BCDUtil.doBCDLEFT(realValue.getBytes(ISOConfig.charSet));
                            }
                            if (leanmode == IsoLeanMode.LEFTSPACE || leanmode == IsoLeanMode.RIGHTSPACE) {
                                throw new RuntimeException("Id为" + id + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                            }
                        }
                        data = new byte[content.length + 2];
                        System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                        System.arraycopy(content, 0, data, 2, content.length);
                    }
                    break;
                case BIN:        //进行二进制处理
                    length = realValue.length() / 2;    //进行长度恢复
                    if (length < 10) {
                        LLLBCD = ("00" + length).getBytes(ISOConfig.charSet);
                    } else if (length < 100) {
                        LLLBCD = ("0" + length).getBytes(ISOConfig.charSet);
                    } else {
                        LLLBCD = String.valueOf(length).getBytes(ISOConfig.charSet);
                    }
                    LLLBCD = BCDUtil.doBCD(LLLBCD);    //对长度进行压缩
                    content = DataConverter.hexStringToByte(realValue);

                    data = new byte[content.length + 2];
                    System.arraycopy(LLLBCD, 0, data, 0, LLLBCD.length);
                    System.arraycopy(content, 0, data, 2, content.length);
                    System.out.println("=============" + DataConverter.bytesToHexString(data));
                    break;
            }
        }
        return data;
    }
    //==========================组包功能函数存放区域结束===============================//
    //==========================解包功能函数存放区域开始===============================//
    /*
	 * <p>进行ISO8583解包操作函数</p>
	 * @see com.centerm.iso8583.isointerface.IExchange#unPackTrns(byte[], com.centerm.iso8583.bean.FormatInfo)
	 */

    /**
     * 功能描述：进行Iso8583报文的解包操作
     * @param msg 用字节数组方式表示的Iso8583报文内容
     * @param formatInfo 指定报文解析的格式控制对象
     * @return map集合，里面存放了对应数据标签的数据内容，例如("msg_tp","0200")
     * @throws Exception
     */
    public Map<String, String> unPackTrns(byte[] msg, FormatInfo formatInfo) throws Exception {
        Map<String, String> messageInfo = new HashMap<String, String>();    //初始化一个Map对象用于存储解析的报文内容
        byte[] message = msg;    //接收传递进来的报文信息
        BitSet bs = null;    //用于存储位域
        FieldDataParseBean fieldDataParseBean = null;
        Field field = null;
        int pos = 0;        //定义一个用于进行数组截取的偏移量存储变量
        int msgHeaderLength = 0;    //存储报文头长度
        //==================================Head信息处理开始============================
        Head head = formatInfo.getHead();
        Map headMap = head.getMap();
        Set headKeySet = head.getMap().keySet();
        List list = new ArrayList(headKeySet);
        Collections.sort(list);
        for (Object elem : list) {
            field = (Field) headMap.get(elem);
            fieldDataParseBean = fieldDataDealInUnpack(message, pos, field);
            if (fieldDataParseBean == null) {        //表示该域内容用户不希望进行保存，继续解析下一组数据
                continue;
            } else {
                String content = fieldDataParseBean.getContent();        //获取解析后的内容
                String key = fieldDataParseBean.getKey();                //获取解析得到的key值
                pos += fieldDataParseBean.getLength();                //修改length的偏移量
                messageInfo.put(key, content);            //将解析的内容存放在Map集合中
                System.out.println("key = " + key + ",content = " + content + ",pos = " + pos);
            }
        }
        //==================================Body信息处理开始===========================
        Map bodyMap = formatInfo.getBody().getMap();
        field = (Field) bodyMap.get(0);        //获取ID号为零的配置信息，表示msg_tp
//		Log.w("lwl",field.toString());
        fieldDataParseBean = fieldDataDealInUnpack(message, pos, field);
        String content = fieldDataParseBean.getContent();
        String key = fieldDataParseBean.getKey();
        pos += fieldDataParseBean.getLength();        //调整pos的位置
        System.out.println("key = " + key + ",content = " + content + ",pos = " + pos);
        messageInfo.put(key, content);        //把报文类型信息添加进Map集合中
        bs = new BitSet(64);
        int k = 0;
        for (int i = pos; i < pos + 8; i++) {
            int bit = 128;
            for (int b = 0; b < 8; b++) {
                bs.set(k++, (message[i] & bit) != 0);    //k先参与set赋值，而后加1
                bit >>= 1;
            }
        }
        // 如果有64--128域的内容，调整bitMap
        if (bs.get(0)) {
            for (int i = pos + 8; i < pos + 16; i++) {
                int bit = 128;
                for (int b = 0; b < 8; b++) {
                    bs.set(k++, (message[i] & bit) != 0);
                    bit >>= 1;
                }
            }
            pos += 16;        //此处对k进行了重新赋值操作
        } else {
            pos += 8;
        }
        // 将BitSet中的位图格式化成二进制形式和域ID数字形式
        //每8个二进制位串之间用2个空格隔开，如果为64位的话就是64+2*7=78，如果为128位的话就是128+2*15
        int bit_str_length = bs.length() <= 64 ? 64 : 128;
        StringBuffer bitmap_binstr = new StringBuffer(bit_str_length); // 每8个二进制位串之间用2个空格隔开
        StringBuffer bitmap_str = new StringBuffer("{");
        for (int i = 0; i < bit_str_length; i++) {
            if (bs.get(i)) {
                bitmap_binstr.append("1");
                bitmap_str.append(i + 1).append(", ");
            } else
                bitmap_binstr.append("0");
        }
        //该for循环用于将每8个二进制位串用两个空格隔开
        for (int i = 0; i < bitmap_binstr.length(); i++) {
            if (i % 10 == 7 && i < bitmap_binstr.length()) {
                bitmap_binstr.insert(i + 1, "  ");
            }
        }
        if (bitmap_binstr.substring(bitmap_binstr.length() - 2).equals("  ")) {
            bitmap_binstr.delete(bitmap_binstr.length() - 2, bitmap_binstr.length());
        }
        if (bitmap_str.substring(bitmap_str.length() - 2).equals(", "))
            bitmap_str.delete(bitmap_str.length() - 2, bitmap_str.length());
        bitmap_str.append("}");
        ISOConfig.log("bitmap data(binary format): [" + bitmap_binstr + "]");
        if (bs.size() > 64) {
            ISOConfig.log("parsed bitmap(1-128):  [" + bitmap_str + "]");
        } else {
            ISOConfig.log("parsed bitmap(1-64): [" + bitmap_str + "]");
        }
        for (int i = 1; i < bs.size(); i++) {
            if (!bs.get(i)) {
                continue;
            }
            //ISOConfig.log("进行下一次解析前pos的值为：" + pos);
            field = (Field) bodyMap.get(i + 1);
            fieldDataParseBean = fieldDataDealInUnpack(message, pos, field);
            content = fieldDataParseBean.getContent();        //获取content
            key = fieldDataParseBean.getKey();        //获取key值
            messageInfo.put(key, content);
            pos += fieldDataParseBean.getLength();        //重新调整pos的值
            //ISOConfig.log("i+1 = " + (i+1) + ",key = " + key + ",content = " + content + ",pos = " + pos);
        }
//		ISOConfig.log("解包信息为：" + messageInfo);
        Set<String> keySet = messageInfo.keySet();
        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext(); ) {
            String kk = iterator.next();
            ISOConfig.log("解包==>" + kk + "==>" + messageInfo.get(kk));
        }

        return messageInfo;
    }

    /**
     * 功能描述：根据字节报文内容和控制对象还有数据偏移量，进行报文解析操作
     * @param message 报文内容
     * @param offset 数组偏移量，用于截取报文内容
     * @param field 格式控制对象
     * @return FieldDataParaseBean对象
     * @throws Exception
     */
    public FieldDataParseBean fieldDataDealInUnpack(byte[] message, int offset, Field field) throws Exception {
        FieldDataParseBean fieldDataParseBean = null;
        IsoLengthType lengthtype = field.getLengthType();        //获取lengthType的值
        switch (lengthtype) {
            case FIX:
                fieldDataParseBean = dealFIXInUnpack(message, offset, field);        //处理定长类型数据
                break;
            case LLASC:
                fieldDataParseBean = dealLLASCInUnpack(message, offset, field);    //处理LLASC长度类型的数据
                break;
            case LLBCD:
                fieldDataParseBean = dealLLBCDInUnpack(message, offset, field);    //处理LLBCD长度类型的数据
                break;
            case LLLASC:
                fieldDataParseBean = dealLLLASCInUnpack(message, offset, field);    //处理LLLASC长度类型的数据
                break;
            case LLLBCD:
                fieldDataParseBean = dealLLLBCDInUnpack(message, offset, field);    //处理LLLBCD长度类型的数据
                break;
        }
        return fieldDataParseBean;
    }

    /**
     * 功能描述：解包过程中处理长度类型为FIX的数据域
     * @param message    整个ISO8583报文内容
     * @param offset    该域数据在报文字节数组中的开始位置
     * @param field        该域的格式控制对象
     * @return FieldDataParseBean对象
     * @throws Exception
     */
    public FieldDataParseBean dealFIXInUnpack(byte[] message, int offset, Field field) throws Exception {
        FieldDataParseBean fieldDataParseBean = null;
        int fieldId = field.getFieldId();        //获取fieldId编号
        int length = field.getLength();    //获取配置文件
        IsoDataMode datamode = field.getDataMode();    //获取data-mode元素的值
        IsoCompressMode commode = field.getCommode();    //获取压缩编码方式
        switch (commode) {
            case ASC:
                byte[] fieldData = new byte[length];
                System.arraycopy(message, offset, fieldData, 0, length);
                String content = null;
                content = new String(fieldData, ISOConfig.charSet);
                fieldMap.put(fieldId, fieldData);
                String key = null;
                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();    //获取key的值
                    fieldDataParseBean = new FieldDataParseBean(key, content, length);
                }
                break;
            case BCD:
                int realLength = length % 2 == 0 ? length / 2 : (length + 1) / 2;
                fieldData = new byte[realLength];
                IsoLeanMode leanmode = field.getLeanmode();    //获取该域内容的靠拢方式
                System.arraycopy(message, offset, fieldData, 0, realLength);        //截取指定长度的内容
                fieldMap.put(fieldId, fieldData);
                content = null;
                key = null;
                if (length % 2 == 0) {    //如果长度为偶数
                    content = BCDUtil.bcd2Str(fieldData);        //对压缩的BCD编码内容进行解压
                    if (datamode.equals(IsoDataMode.Key)) {
                        key = field.getValue();        //获取key的值
                        fieldDataParseBean = new FieldDataParseBean(key, content, realLength);
                    }
                } else {    //如果长度为奇数
                    if (leanmode.equals(IsoLeanMode.LEFTZERO)) {
                        content = BCDUtil.bcd2Str(fieldData);    //对内容进行解压缩
                        content = content.substring(1);        //截取最左边的零
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key值
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength);
                        }
                    } else if (leanmode.equals(IsoLeanMode.RIGHTZERO)) {
                        content = BCDUtil.bcd2Str(fieldData);        //对内容进行解压缩
                        content = content.substring(0, content.length() - 1);
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key值
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength);
                        }
                    } else if (leanmode.equals(IsoLeanMode.LEFTSPACE) || leanmode.equals(IsoLeanMode.RIGHTSPACE)) {
                        throw new IllegalArgumentException("Id为" + field.getFieldId() + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                    }
                }
                break;
            case BIN:
                fieldData = new byte[length];
                System.arraycopy(message, offset, fieldData, 0, length);
                content = DataConverter.bytesToHexString(fieldData);        //将字节数组转化为对应的二进制字符串
                fieldMap.put(fieldId, fieldData);
                key = null;
                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();    //获取key的值
                    fieldDataParseBean = new FieldDataParseBean(key, content, length);
                }
                break;
        }
        return fieldDataParseBean;
    }

    /**
     * 功能：解包过程中处理长度类型为LLASC的数据域
     * @param message    整个ISO8583报文内容
     * @param offset    该域数据在报文字节数组中的开始位置
     * @param field        该域的格式控制对象
     * @return FieldDataParseBean对象
     * @throws Exception
     */
    public FieldDataParseBean dealLLASCInUnpack(byte[] message, int offset, Field field) throws Exception {
        FieldDataParseBean fieldDataParseBean = null;
        IsoDataMode datamode = field.getDataMode();
        IsoLeanMode leanmode = field.getLeanmode();
        IsoCompressMode commode = field.getCommode();        //获取内容压缩编码方式
        int length = 0;
//		Log.i("lwl","@@"+field.toString());
        switch (commode) {
            case ASC:
                String content = "";
                String key = "";
                byte[] lengthData = new byte[2];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);    //截取2个字节的长度内容
                length = Integer.parseInt(new String(lengthData, ISOConfig.charSet));            //获取长度值
                byte[] fieldData = new byte[length];        //定义内容存放缓冲区
                offset += 2;
                System.arraycopy(message, offset, fieldData, 0, length);
                content = new String(fieldData, ISOConfig.charSet);

                byte[] datasc = new byte[length + 2];
                System.arraycopy(message, offset - 2, datasc, 0, length + 2);
                fieldMap.put(field.getFieldId(), datasc);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();        //获取key值
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 2);
                }
                break;
            case BCD:
                content = "";
                key = "";
                lengthData = new byte[2];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);    //截取2个字节的长度内容
                length = Integer.parseInt(new String(lengthData, ISOConfig.charSet));            //获取长度值
                int realLength = length % 2 == 0 ? length / 2 : (length + 1) / 2;
                fieldData = new byte[realLength];    //存放域内容
                offset += 2;
                System.arraycopy(message, offset, fieldData, 0, realLength);    //取出内容

                byte[] databcd = new byte[realLength + 2];
                System.arraycopy(message, offset - 2, databcd, 0, realLength + 2);
                fieldMap.put(field.getFieldId(), databcd);

                if (length % 2 == 0) {
                    content = BCDUtil.bcd2Str(fieldData);        //对压缩的BCD编码内容进行解压
                    if (datamode.equals(IsoDataMode.Key)) {
                        key = field.getValue();        //获取key的值
                        fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 2);
                    }
                } else {
                    if (leanmode.equals(IsoLeanMode.LEFTZERO)) {
                        content = BCDUtil.bcd2Str(fieldData);    //对内容进行解压缩
                        content = content.substring(1);        //截取最左边的零
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key值
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 2);
                        }
                    } else if (leanmode.equals(IsoLeanMode.RIGHTZERO)) {
                        content = BCDUtil.bcd2Str(fieldData);        //对内容进行解压缩
                        content = content.substring(0, content.length() - 1);
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key值
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 2);
                        }
                    } else if (leanmode.equals(IsoLeanMode.LEFTSPACE) || leanmode.equals(IsoLeanMode.RIGHTSPACE)) {
                        throw new RuntimeException("Id为" + field.getFieldId() + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                    }
                }
                break;
            case BIN:
                content = "";
                key = "";
                lengthData = new byte[2];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);    //截取2个字节的长度内容
                length = Integer.parseInt(new String(lengthData, ISOConfig.charSet));            //获取长度值
                fieldData = new byte[length];    //存放域内容
                offset += 2;
                System.arraycopy(message, offset, fieldData, 0, length);    //取出内容
                content = DataConverter.bytesToHexString(fieldData);        //转化为01字符串

                byte[] databin = new byte[length + 2];
                System.arraycopy(message, offset - 2, databin, 0, length + 2);
                fieldMap.put(field.getFieldId(), databin);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 2);
                }
                break;
        }
        return fieldDataParseBean;
    }

    /**
     * 功能：解包过程中处理长度类型为LLBCD的数据域
     * @param message    整个ISO8583报文内容
     * @param offset    该域数据在报文字节数组中的开始位置
     * @param field        该域的格式控制对象
     * @return FieldDataParseBean对象
     */
    public FieldDataParseBean dealLLBCDInUnpack(byte[] message, int offset, Field field) throws Exception {
        FieldDataParseBean fieldDataParseBean = null;
        IsoDataMode datamode = field.getDataMode();        //获取datamode
        IsoCompressMode commode = field.getCommode();    //获取内容编码类型
        IsoLeanMode leanmode = field.getLeanmode();        //获取靠拢类型
        IsoLengthMode lengthMode = field.getLengthMode();    //获取内容长度计数方式
        switch (commode) {
            case ASC:
                String content = "";
                String key = "";
                byte[] lengthData = new byte[1];
                System.arraycopy(message, offset, lengthData, 0, 1);        //从报文中截取长度信息的值
                int length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));    //将长度内容转化为整型
                byte[] contentData = new byte[length];    //构造一个字节数组用于存放长度类型的值
                offset += 1;        //标志量加1，跳过长度标识
                System.arraycopy(message, offset, contentData, 0, length);
                content = new String(contentData, ISOConfig.charSet);

                byte[] datasc = new byte[length + 1];
                System.arraycopy(message, offset - 1, datasc, 0, length + 1);
                fieldMap.put(field.getFieldId(), datasc);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 1);
                }
                break;
            case BCD:
                content = "";
                key = "";
                if (lengthMode.equals(IsoLengthMode.BYTELEN)) {    //第一个字节表示字节的长度
                    lengthData = new byte[1];
                    System.arraycopy(message, offset, lengthData, 0, 1);        //截取报文长度信息的值
                    length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));        //将报文长度内容转化为整型
                    int realLength = length;    //实际长度就是最前端长度
                    contentData = new byte[realLength];
                    offset += 1;    //调整偏移量
                    System.arraycopy(message, offset, contentData, 0, realLength);        //获取实际字节内容
                    content = BCDUtil.bcd2Str(contentData);

                    byte[] datbcd = new byte[realLength + 1];
                    System.arraycopy(message, offset - 1, datbcd, 0, realLength + 1);
                    fieldMap.put(field.getFieldId(), datbcd);

                    //ISOConfig.log("content = " + content);
                    if (datamode.equals(IsoDataMode.Key)) {
                        key = field.getValue();        //获取key的值
                    }
                    fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 1);
                } else {
                    lengthData = new byte[1];
                    System.arraycopy(message, offset, lengthData, 0, 1);        //截取报文长度信息的值
                    length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));        //将报文长度内容转化为整型
                    int realLength = length % 2 == 0 ? length / 2 : (length + 1) / 2;        //获取实际占用的长度
                    contentData = new byte[realLength];
                    offset += 1;    //调整偏移量
                    System.arraycopy(message, offset, contentData, 0, realLength);        //获取实际字节内容

                    byte[] datbcd = new byte[realLength + 1];
                    System.arraycopy(message, offset - 1, datbcd, 0, realLength + 1);
                    fieldMap.put(field.getFieldId(), datbcd);

                    if (length % 2 == 0) {
                        content = BCDUtil.bcd2Str(contentData);
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key的值
                        }
                        fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 1);
                    } else {
                        if (leanmode.equals(IsoLeanMode.LEFTZERO)) {
                            content = BCDUtil.bcd2Str(contentData).substring(1);        //去掉最前端补的零
                            if (datamode.equals(IsoDataMode.Key)) {
                                key = field.getValue();        //获取key的值
                            }
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 1);
                        } else if (leanmode.equals(IsoLeanMode.RIGHTZERO)) {
                            content = BCDUtil.bcd2Str(contentData);
                            content = content.substring(0, content.length() - 1);
                            if (datamode.equals(IsoDataMode.Key)) {
                                key = field.getValue();    //获取key的值
                            }
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 1);
                        } else if (leanmode.equals(IsoLeanMode.LEFTSPACE) || leanmode.equals(IsoLeanMode.RIGHTSPACE)) {
                            throw new IllegalArgumentException("解包配置文件中，Id为" + field.getFieldId() + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                }
                break;
            case BIN:
                content = "";
                key = "";
                lengthData = new byte[1];
                System.arraycopy(message, offset, lengthData, 0, 1);        //截取报文长度信息的值
                length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));        //将报文长度内容转化为整型
                offset += 1;        //偏移量加1
                contentData = new byte[length];
                System.arraycopy(message, offset, contentData, 0, length);    //截取报文信息内容
                content = DataConverter.bytesToHexString(contentData);    //将二进制内容转化为01字串

                byte[] datbin = new byte[length + 1];
                System.arraycopy(message, offset - 1, datbin, 0, length + 1);
                fieldMap.put(field.getFieldId(), datbin);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();    //获取key的值
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 1);
                }
                break;
        }
        return fieldDataParseBean;
    }

    /**
     * 功能：解包过程中处理长度类型为LLLASC的数据域
     * @param message    整个ISO8583报文内容
     * @param offset    该域数据在报文字节数组中的开始位置
     * @param field        该域的格式控制对象
     * @return FieldDataParseBean对象
     * @throws Exception
     */
    public FieldDataParseBean dealLLLASCInUnpack(byte[] message, int offset, Field field) throws Exception {
        FieldDataParseBean fieldDataParseBean = null;
        IsoDataMode datamode = field.getDataMode();
        IsoLeanMode leanmode = field.getLeanmode();
        IsoCompressMode commode = field.getCommode();        //获取内容压缩编码方式
        int length = 0;
        switch (commode) {
            case ASC:
                String content = "";
                String key = "";
                byte[] lengthData = new byte[3];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);    //截取2个字节的长度内容
                length = Integer.parseInt(new String(lengthData, ISOConfig.charSet));            //获取长度值
                byte[] fieldData = new byte[length];        //定义内容存放缓冲区
                offset += 3;
                System.arraycopy(message, offset, fieldData, 0, length);
                //content = new String(fieldData,ISOConfig.charSet);
                //content = BCDUtil.bcd2Str(fieldData);


                byte[] datasc = new byte[length + 3];
                System.arraycopy(message, offset - 3, datasc, 0, length + 3);
                content = new String(datasc, ISOConfig.charSet);
                fieldMap.put(field.getFieldId(), datasc);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();        //获取key值
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 3);
                }
                break;
            case BCD:
                content = "";
                key = "";
                lengthData = new byte[3];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);    //截取2个字节的长度内容
                length = Integer.parseInt(new String(lengthData, ISOConfig.charSet));            //获取长度值
                int realLength = length % 2 == 0 ? length / 2 : (length + 1) / 2;
                fieldData = new byte[realLength];    //存放域内容
                offset += 3;
                System.arraycopy(message, offset, fieldData, 0, realLength);    //取出内容

                byte[] datbcd = new byte[realLength + 3];
                System.arraycopy(message, offset - 3, datbcd, 0, realLength + 3);
                fieldMap.put(field.getFieldId(), datbcd);

                if (length % 2 == 0) {
                    content = BCDUtil.bcd2Str(fieldData);        //对压缩的BCD编码内容进行解压
                    if (datamode.equals(IsoDataMode.Key)) {
                        key = field.getValue();        //获取key的值
                        fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 3);
                    }
                } else {
                    if (leanmode.equals(IsoLeanMode.LEFTZERO)) {
                        content = BCDUtil.bcd2Str(fieldData);    //对内容进行解压缩
                        content = content.substring(1);        //截取最左边的零
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key值
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 3);
                        }
                    } else if (leanmode.equals(IsoLeanMode.RIGHTZERO)) {
                        content = BCDUtil.bcd2Str(fieldData);        //对内容进行解压缩
                        content = content.substring(0, content.length() - 1);
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key值
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 3);
                        }
                    } else if (leanmode.equals(IsoLeanMode.LEFTSPACE) || leanmode.equals(IsoLeanMode.RIGHTSPACE)) {
                        throw new RuntimeException("Id为" + field.getFieldId() + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                    }
                }
                break;
            case BIN:
                content = "";
                key = "";
                lengthData = new byte[3];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);    //截取2个字节的长度内容
                length = Integer.parseInt(new String(lengthData, ISOConfig.charSet));            //获取长度值
                fieldData = new byte[length];    //存放域内容
                offset += 3;
                System.arraycopy(message, offset, fieldData, 0, length);    //取出内容
                content = DataConverter.bytesToHexString(fieldData);        //转化为01字符串

                byte[] datbin = new byte[length + 3];
                System.arraycopy(message, offset - 3, datbin, 0, length + 3);
                fieldMap.put(field.getFieldId(), datbin);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 3);
                }
                break;
        }
        return fieldDataParseBean;
    }

    /**
     * 功能：解包过程中处理长度类型为LLLBCD的数据域
     * @param message    整个ISO8583报文内容
     * @param offset    该域数据在报文字节数组中的开始位置
     * @param field        该域的格式控制对象
     * @return FieldDataParseBean对象
     * @throws Exception
     */
    public FieldDataParseBean dealLLLBCDInUnpack(byte[] message, int offset, Field field) throws Exception {
        FieldDataParseBean fieldDataParseBean = null;
        IsoDataMode datamode = field.getDataMode();        //获取datamode
        IsoCompressMode commode = field.getCommode();    //获取内容编码类型
        IsoLeanMode leanmode = field.getLeanmode();        //获取靠拢类型
        IsoLengthMode lengthMode = field.getLengthMode();    //获取数据内容长度计算模式
        switch (commode) {
            case ASC:
                String content = "";
                String key = "";
                byte[] lengthData = new byte[2];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);        //从报文中截取长度信息的值
                int length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));    //将长度内容转化为整型
                byte[] contentData = new byte[length];    //构造一个字节数组用于存放长度类型的值
                offset += 2;        //标志量加1，跳过长度标识
                System.arraycopy(message, offset, contentData, 0, length);
                content = new String(contentData, ISOConfig.charSet);

                byte[] datasc = new byte[length + 2];
                System.arraycopy(message, offset - 2, datasc, 0, length + 2);
                fieldMap.put(field.getFieldId(), datasc);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 2);
                }
                break;
            case BCD:
                content = "";
                key = "";
                if (lengthMode.equals(IsoLengthMode.BYTELEN)) {    //字节方式计算
                    lengthData = new byte[2];
                    System.arraycopy(message, offset, lengthData, 0, lengthData.length);        //截取报文长度信息的值
                    length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));        //将报文长度内容转化为整型
                    int realLength = length;        //获取实际占用的长度
                    contentData = new byte[realLength];
                    offset += 2;    //调整偏移量
                    System.arraycopy(message, offset, contentData, 0, realLength);        //获取实际字节内容
                    content = BCDUtil.bcd2Str(contentData);

                    byte[] datbcd = new byte[realLength + 2];
                    System.arraycopy(message, offset - 2, datbcd, 0, realLength + 2);
                    fieldMap.put(field.getFieldId(), datbcd);

                    ISOConfig.log("content = " + content);
                    if (datamode.equals(IsoDataMode.Key)) {
                        key = field.getValue();        //获取key的值
                    }
                    fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 2);
                } else {
                    lengthData = new byte[2];
                    System.arraycopy(message, offset, lengthData, 0, lengthData.length);        //截取报文长度信息的值
                    length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));        //将报文长度内容转化为整型
                    int realLength = length % 2 == 0 ? length / 2 : (length + 1) / 2;        //获取实际占用的长度
                    contentData = new byte[realLength];
                    offset += 2;    //调整偏移量
                    System.arraycopy(message, offset, contentData, 0, realLength);        //获取实际字节内容

                    byte[] datbcd = new byte[realLength + 2];
                    System.arraycopy(message, offset - 2, datbcd, 0, realLength + 2);
                    fieldMap.put(field.getFieldId(), datbcd);

                    if (length % 2 == 0) {
                        content = BCDUtil.bcd2Str(contentData);
                        if (datamode.equals(IsoDataMode.Key)) {
                            key = field.getValue();        //获取key的值
                        }
                        fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 2);
                    } else {
                        if (leanmode.equals(IsoLeanMode.LEFTZERO)) {
                            content = BCDUtil.bcd2Str(contentData).substring(1);        //去掉最前端补的零
                            if (datamode.equals(IsoDataMode.Key)) {
                                key = field.getValue();        //获取key的值
                            }
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 2);
                        } else if (leanmode.equals(IsoLeanMode.RIGHTZERO)) {
                            content = BCDUtil.bcd2Str(contentData);
                            content = content.substring(0, content.length() - 1);
                            if (datamode.equals(IsoDataMode.Key)) {
                                key = field.getValue();    //获取key的值
                            }
                            fieldDataParseBean = new FieldDataParseBean(key, content, realLength + 2);
                        } else if (leanmode.equals(IsoLeanMode.LEFTSPACE) || leanmode.equals(IsoLeanMode.RIGHTSPACE)) {
                            throw new RuntimeException("解包配置文件中，Id为" + field.getFieldId() + "的数据行压缩编码使用了BCD，靠拢方式属性不能为LEFTSPACE和RIGHTSPACE");
                        }
                    }
                }
                break;
            case BIN:
                content = "";
                key = "";
                lengthData = new byte[2];
                System.arraycopy(message, offset, lengthData, 0, lengthData.length);        //截取报文长度信息的值
                length = Integer.parseInt(BCDUtil.bcd2Str(lengthData));        //将报文长度内容转化为整型
                offset += 2;        //偏移量加1
                contentData = new byte[length];
                System.arraycopy(message, offset, contentData, 0, length);    //截取报文信息内容
                content = DataConverter.bytesToHexString(contentData);    //将二进制内容转化为01字串

                byte[] datbin = new byte[length + 2];
                System.arraycopy(message, offset - 2, datbin, 0, length + 2);
                fieldMap.put(field.getFieldId(), datbin);

                if (datamode.equals(IsoDataMode.Key)) {
                    key = field.getValue();    //获取key的值
                    fieldDataParseBean = new FieldDataParseBean(key, content, length + 2);
                }
                break;
        }
        return fieldDataParseBean;
    }
    //========================================解包函数功能存放区域结束============================//

    /**
     * 功能描述：该方法用于把fieldDataListt集合中的多个数组合并为一个数组
     * @param fieldDataList 存储有FieldDataBean对象的对象数组
     * @return 以byte数组返回集合中对象数组属性合并后的内容
     */
    public byte[] byteArrayCollapse(List<FieldDataBean> fieldDataList) {
        byte[] data = null;
        int allLength = 0;
        for (FieldDataBean fieldDataBean : fieldDataList) {        //循环遍历list集合
            allLength += fieldDataBean.getFieldDataLength();    //获取每个数组的长度和
        }
        data = new byte[allLength];        //初始化字节数组用于存放所有单个字节的内容
        int pos = 0;
        for (FieldDataBean fieldDataBean : fieldDataList) {
            byte[] temp = fieldDataBean.getFieldData();
            int length = fieldDataBean.getFieldDataLength();
            System.arraycopy(temp, 0, data, pos, length);
            pos += length;
        }
        return data;
    }
}

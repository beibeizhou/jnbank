package com.centerm.jnbank.msg;

import android.content.Context;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumMacType;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.iso8583.IsoMessage;
import com.centerm.iso8583.bean.FormatInfo;
import com.centerm.iso8583.bean.FormatInfoFactory;
import com.centerm.iso8583.enums.IsoMessageMode;
import com.centerm.iso8583.parse.IsoConfigParser;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2016/10/28</br>
 */

public abstract class BaseFactory {

    protected Logger logger = Logger.getLogger(getClass());

    public abstract Object pack(Context context, String transCode, Map<String, String> data);

    public abstract Map<String, String> unPack(Context context, String transCode, Object oriMsg);


    protected byte[] mapToIso8583(Context context, String mctFile, String transCode, Map<String, String> data) {
        FormatInfoFactory factory;
        IsoConfigParser parser = new IsoConfigParser();
        String code = transCode;
        if (TransCode.DOWNLOAD_AID.equals(transCode)
                || TransCode.DOWNLOAD_CAPK.equals(transCode)
                || TransCode.DOWNLOAD_QPS_PARAMS.equals(transCode)) {
//            code = TransCode.DOWNLOAD_PARAMS;
        }
        try {
            factory = parser.parseFromInputStream(context.getAssets().open(mctFile));
            FormatInfo info = factory.getFormatInfo(code, IsoMessageMode.PACK);
            IsoMessage message = com.centerm.iso8583.MessageFactory.getIso8583Message().packTrns(data, info);
            byte[] messageData = message.getAllMessageByteData();
            if (!TransCode.NO_MAC_SETS.contains(code)) {
                //非管理类交易需要添加MAC域
                IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
                if (pinPadDev != null) {
                    //CUP_ECB,,,,,BOC_EXTENED,,,XOR_3DES,X919_MP,X919_00,X919
                    byte[] macData = HexUtils.hexStringToByte(pinPadDev.getMac(EnumMacType.X9_9, HexUtils.bytesToHexString(splitMacBlock(messageData)), null));
                    String mac = HexUtils.bytesToHexString(macData);
                    data.put(TransDataKey.iso_f64, mac);
                    logger.info(code + "==>MAC计算结果==>" + mac);
                    System.arraycopy(macData, 0, messageData, messageData.length - 8, 8);
                } else {
                    logger.warn(code + "==>MAC计算失败");
                }
            }
            return messageData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Map<String, String> iso8583ToMap(Context context, String mctFile, String transCode, byte[] iso8583) {
        IsoConfigParser xmlParser = new IsoConfigParser();
        FormatInfoFactory formatInfoFactory;
        Map<String, String> map = null;
        String code = transCode;
        if (TransCode.DOWNLOAD_AID.equals(transCode)
                || TransCode.DOWNLOAD_CAPK.equals(transCode)
                || TransCode.DOWNLOAD_QPS_PARAMS.equals(transCode)
                ) {
//            code = TransCode.DOWNLOAD_PARAMS;
        }
        try {
            if (null==iso8583||iso8583.length==0) {
                return null;
            }
            byte[] macBlock = splitMacBlock(iso8583);
            IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
            if (!TransCode.NO_MAC_SETS.contains(code)) {
                //非管理类交易需要验证MAC
                if (pinPadDev != null) {
                    String macData = pinPadDev.getMac(EnumMacType.X9_9, HexUtils.bytesToHexString(macBlock), null);
                    logger.info(transCode + "==>Local MAC==>" + macData);
                    byte[] respMac = new byte[8];
                    if (iso8583.length < 8) {
                        System.arraycopy(iso8583, 0, respMac, 0, iso8583.length);
                    } else {
                        System.arraycopy(iso8583, iso8583.length - 8, respMac, 0, 8);
                    }
                    if (macData.equals(HexUtils.bcd2str(respMac))) {
                        formatInfoFactory = xmlParser.parseFromInputStream(context
                                .getAssets().open(mctFile));
                        FormatInfo formatInfo = formatInfoFactory.getFormatInfo(code, IsoMessageMode.UNPACK);
                        map = com.centerm.iso8583.MessageFactory.getIso8583Message().unPackTrns(iso8583, formatInfo);
                    } else {
                        map = new HashMap<>();
                    }
                    map.put(TransDataKey.keyLocalMac, macData);
                    map.put(TransDataKey.iso_f64, HexUtils.bytesToHexString(respMac));
                } else {
                    logger.warn(code + "==>计算本地MAC失败==>pinPadDev is null");
                    map.put(TransDataKey.keyLocalMac, "");
                }
            } else {
                logger.warn(code + "==>该交易类型无需计算MAC");
                formatInfoFactory = xmlParser.parseFromInputStream(context
                        .getAssets().open(mctFile));
                FormatInfo formatInfo = formatInfoFactory.getFormatInfo(code, IsoMessageMode.UNPACK);
                map = com.centerm.iso8583.MessageFactory.getIso8583Message().unPackTrns(iso8583, formatInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 分离需要计算MAC的数据域
     *
     * @param returnData 后台返回数据
     * @return 需要计算MAC的数据域
     */
    private byte[] splitMacBlock(byte[] returnData) {
        int len1 = 11;//TPDU+报文头，压缩后11字节
//        int len2 = 2;//消息类型，压缩后2字节
//        int len3 = 8;//位图，压缩后8字节
        int len4 = 8;//mac域，8字节
        int len = returnData.length;
        if (len < len1 + len4) {
            logger.warn("报文长度非法，无法计算MAC");
            return returnData;
        }
        byte[] macBlock = new byte[len - len1 - len4];
//        logger.debug("未截取前的数据==> " + HexUtils.bytesToHexString(returnData));
        System.arraycopy(returnData, len1, macBlock, 0, macBlock.length);
        logger.debug("截取需要计算MAC的数据段==> " + HexUtils.bytesToHexString(macBlock));
        return macBlock;
    }


/*    protected String combineTerminalSn() {
        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            String tag = "Sequence No";
            String sn = service.getTerminalSn();
            int len = (BusinessConfig.NET_LISCENSE_NO + sn).length();
            String str = tag + len + sn;
            logger.warn("Sn TLV == " + str);
            String str1 = HexUtils.bytesToHexString(str.getBytes());
            logger.warn("转成16进制：" + str1);
            return str1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /**
     * 添加两字节报文长度
     *
     * @param message 原报文
     * @return 新报文
     */
    public byte[] addMessageLen(byte[] message) {
        int iLen = message.length;
        byte[] targets = new byte[]{(byte) (iLen / 256), (byte) (iLen % 256)};
        byte[] msg = new byte[iLen + 2];

        System.arraycopy(targets, 0, msg, 0, 2); // 拷贝长度
        System.arraycopy(message, 0, msg, 2, iLen); // 拷贝报文
        return msg;
    }

    /**
     * 去除两字节报文长度
     *
     * @param message 原报文
     * @return 新报文
     */
    public byte[] removeMessaageLen(byte[] message) {
        if (message == null || message.length < 2) {
            return message;
        }
        byte[] msg = new byte[message.length - 2];
        System.arraycopy(message, 2, msg, 0, msg.length);
        return msg;
    }

}

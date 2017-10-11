package com.centerm.jnbank.task;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.bean.iso.Iso62Qps;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.utils.ViewUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;

/**
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public class AsyncDownloadParamTask extends AsyncMultiRequestTask {
    private String transCode = TransCode.LOAD_PARAM;
    private CommonDao<Iso62Qps> dao;
    public AsyncDownloadParamTask(Context context, Map<String, String> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(Iso62Qps.class,new DbHelper(context));
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(LONG_SLEEP);
        Object msgPkg = factory.pack(transCode, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, String> mapData = factory.unpack(TransCode.LOAD_PARAM, data);
                if (null != mapData) {
                    String respCode = mapData.get(iso_f39);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    taskResult[0] = isoCode.getCode();
                    taskResult[1] = context.getString(isoCode.getResId());
                    if ("00".equals(respCode)) {
                        String param = mapData.get(iso_f62);
                        param = ViewUtils.gb2312ToUtf8(param);

                        if(!TextUtils.isEmpty(param)) {
                            logger.info("参数不为空，保存参数");
                            boolean res = loadParamNew(param);
                            if (res) {
                                BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.FLAG_NEED_UPDATE_PARAM, "0");
                                Intent intent = new Intent();
                                intent.setAction("refresh");
                                context.sendBroadcast(intent);
                            }
                            taskResult[2] = res +"";
                        }
                    }
                } else {
                    ISORespCode isoCode = ISORespCode.codeMap("E111");
                    taskResult[0] = isoCode.getCode();
                    taskResult[1] = context.getString(isoCode.getResId());
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0] = code;
                taskResult[1] = msg;
            }
        };
        client.syncSendData((byte[]) msgPkg, handler,transCode);
        return taskResult;
    }



    public boolean loadParamNew(String param) {
        boolean isUpdate = false;
        BusinessConfig config = BusinessConfig.getInstance();
        byte[] bytesParam = HexUtils.hexStringToByte(param);
        int offset = 1;
        byte tag ;
        int len,lenType;
        String data = "";
        Iso62Qps iso62Qps = new Iso62Qps();
        while (offset < bytesParam.length) {
            tag = bytesParam[offset++];
            lenType = bytesParam[offset++];//01表示bcd，02表示ASC，03表示BCD*2（例：长度为2，数据为FFC0，一般为位图开关类）
            len  = bytesParam[offset++]&0xFF;//长度为1字节16进制
            switch (lenType){
                case 0x01:
                    data = HexUtils.bcd2str(Arrays.copyOfRange(bytesParam,offset,offset+(len+1)/2));
                    data = data.substring(0, len);
                    offset += (len+1)/2;
                    break;
                case 0x02:
                    data = HexUtils.bytesToHexString(Arrays.copyOfRange(bytesParam,offset,offset+len));
                    try {
                        data = new String(HexUtils.hexStringToByte(data),"gbk");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    offset += len;
                    break;
                case 0x03:
                    data = HexUtils.bcd2str(Arrays.copyOfRange(bytesParam,offset,offset+len));
                    offset += len;
                    break;
            }
            switch (tag) {
                //参数版本
                case 0x00:
                    //如果参数版本号一样则不更新
                    if (config.getParamVersion(context).equals(data)) {
                        isUpdate = false;
                    } else {
                        isUpdate = true;
                        config.setParamVersion(context,data);
                    }

                    break;
                //商户编号
                case 0x01:
                    logger.info("商户编号"+data);
                    //config.setParam(context, BusinessConfig.Key.PRESET_MERCHANT_CD,data);
                    break;
                //终端编号
                case 0x02:
                    logger.info("终端编号"+data);
                    //config.setParam(context, BusinessConfig.Key.PRESET_TERMINAL_CD,data);
                    break;
                //安全密码
                case 0x03:
                    logger.info("安全密码"+data);
                    if (!StringUtils.isStrNull(data)) {
                        try {
                            CommonDao operDao = new CommonDao<>(Employee.class, new DbHelper(context));
                            Employee e = (Employee) operDao.queryForId(Config.DEFAULT_MSN_ACCOUNT);
                            e.setPassword(data);
                            operDao.update(e);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    //config.setParam(context, BusinessConfig.Key.PARAM_PASSWARD,data);
                    break;
                //商户名称
                case 0x04:
                    logger.info("商户名称"+data);
                    config.setValue(context, BusinessConfig.Key.PRESET_MERCHANT_NAME, data);
                    break;
                //当前时间
                case 0x05:
                    logger.info("当前时间"+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_CURRENT_TIME,data);
                    break;
                //流水号
                case 0x06:
                    logger.info("流水号"+data);
                    config.setPosSerial(context,data);
                    break;
                //批次号
                case 0x07:
                    logger.info("批次号"+data);
                    config.setBatchNo(context,data);
                    break;
                //最大退货金额
                case 0x08:
                    logger.info("最大退货金额"+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_MOST_REFUND,data);
                    break;
                //开关位图
                case 0x09:
                    logger.info("开关位图data"+data);
                    String temp2 = Integer.valueOf(data, 16).toString();
                    int i2 = Integer.parseInt(temp2);
                    String result2 = Integer.toBinaryString(i2);
                    logger.info("开关位图"+result2);
                    char[] chars1 = result2.toCharArray();
                    char[] chars2 = {'0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0'};
                    System.arraycopy(chars1,0,chars2,chars2.length-chars1.length,chars1.length);
                    logger.info("开关位图0"+chars2[0]);
                    logger.info("开关位图1"+chars2[1]);
                    logger.info("开关位图2"+chars2[2]);
                    logger.info("开关位图3"+chars2[3]);
                    logger.info("开关位图4"+chars2[4]);
                    logger.info("开关位图5"+chars2[5]);
                    logger.info("开关位图6"+chars2[6]);
                    logger.info("开关位图7"+chars2[7]);
                    logger.info("开关位图8"+chars2[8]);
                    logger.info("开关位图9"+chars2[9]);
                    logger.info("开关位图10"+chars2[10]);
                    logger.info("开关位图11"+chars2[11]);
                    config.setParam(context, BusinessConfig.Key.FLAG_TIP_PRINT_DETAIL,chars2[0]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_PRINT_ENGLISH,chars2[1]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_PRINT_PAPER,chars2[2]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_VOID_CARD,chars2[4]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_VOID_PSW,chars2[5]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_CARD,chars2[6]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_PSW,chars2[7]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_CANCEL_PSW,chars2[8]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_PSW,chars2[9]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_AUTH_HAND_CARD,chars2[11]+"");
                    break;
                //服务器IP端口 24个Hex（ip1+prot1+ip2+port2）（8+4+8+4）
                case 0x0A:
//                    config.setParam(context, BusinessConfig.Key.PARAM_IP_PORT,data);
                    logger.info("参数下载获取IP数据："+data);
                    if(data.length()>=24){
                        String IP1 = getIPFromHex(data.substring(0,8));
                        int port1 = getPortFromHex(data.substring(8,12));
                        String IP2 = getIPFromHex(data.substring(12,20));
                        int port2 = getPortFromHex(data.substring(20,24));
                        logger.info("ip1>>"+IP1+" port1>>"+port1+" IP2>>"+IP2+" port2>>"+port2);
                        Settings.setParam(context, Settings.KEY.COMMON_IP1,IP1);
                        Settings.setCommonPort1(context, port1);
                        Settings.setParam(context, Settings.KEY.COMMON_IP2,IP2);
                        Settings.setCommonPort2(context, port2);
                    }
                    break;
                //TPDU
                case 0x0B:
                    logger.info("TPDU："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_TPDU,data);
                    break;
                //是否预拨号
                case 0x0C:
                    logger.info("是否预拨号："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_SWITCH_DIALING,data);
                    break;
                //交易超时时间
                case 0x0D:
                    logger.info("交易超时时间："+data);
                    try {
                        Settings.setRespTimeout(context,Integer.parseInt(data));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                //交易重拔次数
                case 0x0E:
                    logger.info("交易重拔次数："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_COUNT_RETRY,data);
                    break;
                //外线号码
                case 0x0F:
                    logger.info("外线号码："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_OUTLINE,data);
                    break;
                //中心交易号码1
                case 0x10:
                    logger.info("中心交易号码1："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_CENTERM_NUM1,data);
                    break;
                //中心交易号码2
                case 0x11:
                    logger.info("中心交易号码2："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_CENTERM_NUM2,data);
                    break;
                //中心交易号码3
                case 0x12:
                    logger.info("中心交易号码3："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_CENTERM_NUM3,data);
                    break;
                //屏蔽设置
                case 0x13:
                    char[] chars = null;
                    logger.info("屏蔽设置："+data);
                    String temp1 = Integer.valueOf(data, 16).toString();
                    int i1 = Integer.parseInt(temp1);
                    String result1 = Integer.toBinaryString(i1);
                    char[] charsTemp = result1.toCharArray();
                    if (data.length() == 4) {
                         chars = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
                    } else {
                         chars = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                                 '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
                    }
                    System.arraycopy(charsTemp,0,chars,chars.length-charsTemp.length,charsTemp.length);
                    config.setParam(context, BusinessConfig.Key.FLAG_BALANCE_SWITH,chars[0]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_SALE_SWITH,chars[1]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_VOID_SWITH,chars[2]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_AUTH_SWITH,chars[3]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_CANCEL_SWITH,chars[4]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_SWITH,chars[6]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_SWITH,chars[7]+"");
                    config.setParam(context, BusinessConfig.Key.FLAG_REFUND_SWITH,chars[10]+"");
                    break;
                //冲正重发次数
                case 0x14:
                    logger.info("冲正重发次数："+data);
                    config.setNumber(context,BusinessConfig.Key.PARAM_REVERSE_COUNT,Integer.parseInt(data));
                    break;
                //打印张数
                case 0x15:
                    logger.info("打印张数："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_PRINT_COUNT,data);
                    break;
                //最大交易笔数
                case 0x16:
                    logger.info("最大交易笔数："+data);
                    if(Integer.parseInt(data)>500){
                        //超过500自动改为500
                        data = "500";
                    }
                    BusinessConfig.getInstance().setNumber(context,BusinessConfig.Key.PARAM_MOST_TRANS,Integer.parseInt(data));
                    break;
                //签购单备注
                case 0x17:
                    logger.info("签购单备注："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_PRINT_REMARK,data);
                    break;
                //电子现金笔数
                case 0x18:
                    logger.info("电子现金笔数："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_ELC_COUNT,data);
                    break;
                //是否显示XXXlogo
                case 0x19:
                    config.setParam(context, BusinessConfig.Key.FLAG_SWITCH_LOGO,data);
                    break;
                //原样打印在二维码上方的文字
                case 0x1A:
//                    config.setParam(context, BusinessConfig.Key.PARAM_QRCODE_UP,data);
                    break;
                //商户名称
                case 0x1B:
//                    config.setParam(context, BusinessConfig.Key.PARAM_MERCHANT_NUM,data);
                    break;
                //商户名称
                case 0x1C:
//                    config.setParam(context, BusinessConfig.Key.PARAM_MERCHANT_NUM,data);
                    break;
                //商户名称
                case 0x1D:
//                    config.setParam(context, BusinessConfig.Key.PARAM_MERCHANT_NUM,data);
                    break;
                //商户名称
                case 0x1E:
//                    config.setParam(context, BusinessConfig.Key.PARAM_MERCHANT_NUM,data);
                    break;
                //商户名称
                case 0x1F:
//                    config.setParam(context, BusinessConfig.Key.PARAM_MERCHANT_NUM,data);
                    break;
                //原样打印在二维码上方的文字
                case 0x20:
                    logger.info("原样打印在二维码上方的文字："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_QRCODE_UP,data);
                    break;
                //二维码链接内容转换为二维码打印
                case 0x21:
                    logger.info("二维码链接内容转换为二维码打印："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_QRCODE,data);
                    break;
                //原样打印在二维码下方的文字
                case 0x22:
                    logger.info("原样打印在二维码下方的文字："+data);
                    config.setParam(context, BusinessConfig.Key.PARAM_QRCODE_DOWN,data);
                    break;
                case 0x23:
                    logger.info("交易控制-主消费流程含插卡："+data);
                    //config.setParam(context, BusinessConfig.Key.PARAM_QRCODE_DOWN,data);
                    break;
                case 0x24:
                    break;
                case 0x25:
                    logger.info("非接快速业务（QPS）免密限额："+data);
                    iso62Qps.setFF8058(data);
                    break;
                case 0x26:
                    logger.info("免签限额："+data);
                    iso62Qps.setFF8059(data);
                    break;
                case 0x27:
                    logger.info("免签免密功能控制参数："+data);
                    char[] qrChars = null;
                    String temp = Integer.valueOf(data, 16).toString();
                    int i3 = Integer.parseInt(temp);
                    String result = Integer.toBinaryString(i3);
                    char[] qrCharsTemp = result.toCharArray();
                    if (data.length() == 4) {
                        qrChars = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
                    } else {
                        qrChars = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
                    }
                    System.arraycopy(qrCharsTemp,0,qrChars,qrChars.length-qrCharsTemp.length,qrCharsTemp.length);
                    iso62Qps.setFF8054(qrChars[0]+"");//非接快速业务标识
                    iso62Qps.setFF8055(qrChars[1]+"");//BIN表A标识
                    iso62Qps.setFF8056(qrChars[2]+"");//BIN表B标识
                    iso62Qps.setFF8057(qrChars[3]+"");//CDCVM标识
                    iso62Qps.setFF805A(qrChars[4]+"");//免签标识
                    logger.info("非接快速业务标识："+qrChars[0]+"");
                    logger.info("BIN表A标识："+qrChars[1]+"");
                    logger.info("BIN表B标识："+qrChars[2]+"");
                    logger.info("CDCVM标识："+qrChars[3]+"");
                    logger.info("免签标识："+qrChars[4]+"");
                    break;
            }
        }
        boolean isDelete = dao.deleteByWhere("id IS NOT NULL");
        logger.debug("小额免密免签历史记录删除："+isDelete);
        boolean res = isDelete && dao.save(iso62Qps);
        if (res) {
            BaseTradeActivity.nullQpsParams();
            logger.debug("更新小额免密免签参数成功："+res+"参数为："+iso62Qps.toString());
        } else {
            logger.warn("更新小额免密免签参数失败");
        }
        return isUpdate;
    }


    String getIPFromHex(String data){
        String ip="";
        if(data.length() == 8){
            byte[] bytesParam = HexUtils.hexStringToByte(data);
            ip += bytesParam[0]&0xFF;
            ip += ".";
            ip += bytesParam[1]&0xFF;
            ip += ".";
            ip += bytesParam[2]&0xFF;
            ip += ".";
            ip += bytesParam[3]&0xFF;
        }
        return ip;
    }
    int getPortFromHex(String data){
        int port = 0;
        if(data.length() == 4){
            byte[] bytesParam = HexUtils.hexStringToByte(data);
            port = HexUtils.bytes2short(bytesParam);
        }
        return port;
    }
}

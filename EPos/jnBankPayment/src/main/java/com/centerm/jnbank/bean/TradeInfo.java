package com.centerm.jnbank.bean;

import com.centerm.jnbank.common.TransDataKey;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 交易流水。
 * author:wanliang527</br>
 * date:2016/11/2</br>
 */

@DatabaseTable(tableName = "tb_trade_serial")
public class TradeInfo implements Serializable {

    @DatabaseField
    private String transCode;//交易类型
    @DatabaseField
    private String iso_f2;//主账号
    @DatabaseField
    private String iso_f3;//交易处理码
    @DatabaseField
    private String iso_f4;//交易金额
    @DatabaseField
    private String iso_f5;//
    @DatabaseField
    private String iso_f6;//
    @DatabaseField
    private String iso_f7;//
    @DatabaseField
    private String iso_f8;//
    @DatabaseField
    private String iso_f9;//
    @DatabaseField
    private String iso_f10;//
    @DatabaseField(id = true)//作为主键
    private String iso_f11;//受卡方系统跟踪号（终端流水号）
    @DatabaseField
    private String iso_f12;//受卡方所在地时间
    @DatabaseField
    private String iso_f13;//受卡方所在地日期
    @DatabaseField
    private String iso_f14;//卡有效期
    @DatabaseField
    private String iso_f14_forvoid;//卡有效期
    @DatabaseField
    private String iso_f15;//清算日期
    @DatabaseField
    private String iso_f16;//
    @DatabaseField
    private String iso_f17;//
    @DatabaseField
    private String iso_f18;//
    @DatabaseField
    private String iso_f19;//
    @DatabaseField
    private String iso_f20;//
    @DatabaseField
    private String iso_f21;//
    @DatabaseField
    private String iso_f22;//服务点输入方式码
    @DatabaseField
    private String iso_f23;//卡序列号
    @DatabaseField
    private String iso_f24;//
    @DatabaseField
    private String iso_f25;//服务点条件码
    @DatabaseField
    private String iso_f26;//服务点PIN获取码
    @DatabaseField
    private String iso_f27;//
    @DatabaseField
    private String iso_f28;//
    @DatabaseField
    private String iso_f29;//
    @DatabaseField
    private String iso_f30;//
    @DatabaseField
    private String iso_f31;//
    @DatabaseField
    private String iso_f32;//受理方标志码
    @DatabaseField
    private String iso_f33;//
    @DatabaseField
    private String iso_f34;//
    @DatabaseField
    private String iso_track2;//2磁数据
    @DatabaseField
    private String iso_track3;//3磁数据
    @DatabaseField
    private String iso_f35;//2磁数据
    @DatabaseField
    private String iso_f36;//3磁数据
    @DatabaseField
    private String iso_f37;//检索参考号
    @DatabaseField
    private String iso_f38;//授权码
    @DatabaseField
    private String iso_f39;//应答码
    @DatabaseField
    private String iso_f40;//
    @DatabaseField
    private String iso_f41;//受卡机终端标识码
    @DatabaseField
    private String iso_f42;//受卡方标识码
    @DatabaseField
    private String iso_f43;//
    @DatabaseField
    private String iso_f44;//
    @DatabaseField
    private String iso_f45;//
    @DatabaseField
    private String iso_f46;//
    @DatabaseField
    private String iso_f47;//
    @DatabaseField
    private String iso_f48;//
    @DatabaseField
    private String iso_f49;//交易货币代码
    @DatabaseField
    private String iso_f50;//
    @DatabaseField
    private String iso_f51;//
    @DatabaseField
    private String iso_f52;//个人标识码数据
    @DatabaseField
    private String iso_f53;//安全控制信息
    @DatabaseField
    private String iso_f54;//
    @DatabaseField
    private String iso_f55;//IC卡数据域
    @DatabaseField
    private String iso_f56;//
    @DatabaseField
    private String iso_f57;//
    @DatabaseField
    private String iso_f58;//
    @DatabaseField
    private String iso_f59;//钱宝自定义，用于签购单控制
    @DatabaseField
    private String iso_f60;//自定义域（交易类型码+网络管理码+终端读取能力+IC卡条件代码）
    @DatabaseField
    private String iso_f61;//
    @DatabaseField
    private String iso_f62;//自定义域
    @DatabaseField
    private String iso_f63;//自定义域
    @DatabaseField
    private String iso_f64;//MAC值
    @DatabaseField
    private String iso_f55_send;//请求报文中的55域
    @DatabaseField
    private String iso_f59_send;//请求报文中的59域
    @DatabaseField
    private String iso_f60_send;//请求报文中的60域
    @DatabaseField
    private String iso_f64_send;//请求报文中的64域
    @DatabaseField
    private int flag;//该笔流水状态标识，1-交易成功；2-已撤销；3-已冲正；4-已退货；5-已完成(针对预授权类交易)
    @DatabaseField
    private boolean noPinFlag;//免密标志
    @DatabaseField
    private boolean noSignFlag;//免签标志
    @DatabaseField
    private String transTime;//本地交易时间
    @DatabaseField(defaultValue = "0")
    private int sendCount;//上送次数   最多3次   99说明平台返回该笔订单失败
    @DatabaseField(defaultValue = "false")
    private boolean isBatchSuccess;//是否批上送成功 true表示批上送成功
    @DatabaseField
    private String icData;//IC卡数据，用于打印取值
    @DatabaseField
    private int scriptResult;//脚本执行结果，0-无脚本（非成功交易、非IC卡交易、非消费、预授权交易均无脚本）；1-脚本执行成功；2-脚本执行失败；3-脚本上送完成
    @DatabaseField
    private String holderName;//持卡人姓名
    @DatabaseField
    public String iso_f11_origin = "iso_f11_origin";//原交易的11域
    @DatabaseField
    public String key_oriTransTime = "key_oriTransTime";
    @DatabaseField
    public String key_oriAuthCode = "key_oriAuthCode";
    @DatabaseField
    public String key_oriReference = "key_oriReference";
    @DatabaseField
    public String key_trans_code = "key_trans_code";//交易类型码
    @DatabaseField
    public String key_bak_iso_f36 = "key_bak_iso_f36";//交易类型码
    public TradeInfo() {
    }

    /**
     * 构造函数。
     * 对于该应用框架而言，该构造函数用于交易请求时，通过将交易码和交易参数传入，构建原始的数据模型存入数据库；
     * 待返回报文到达时，通过{@link #update(Map)}方法，来更新此数据模型，同时更新到数据库；以此来保证一条记录能完整的包含请求参数和返回参数。
     *
     * @param transCode 自定义的交易码
     * @param dataMap   请求参数集合
     */
    public TradeInfo(String transCode, Map<String, String> dataMap) {
        this.transCode = transCode;
        initValue(dataMap);

        iso_f55_send = iso_f55;
        iso_f59_send = iso_f59;
        iso_f60_send = iso_f60;
        iso_f64_send = iso_f64;
    }

    private void initValue(Map<String, String> mapData) {
        iso_f2 = mapData.get(TransDataKey.iso_f2);
        iso_f3 = mapData.get(TransDataKey.iso_f3);
        iso_f4 = mapData.get(TransDataKey.iso_f4);
        iso_f5 = mapData.get(TransDataKey.iso_f5);
        iso_f6 = mapData.get(TransDataKey.iso_f6);
        iso_f7 = mapData.get(TransDataKey.iso_f7);
        iso_f8 = mapData.get(TransDataKey.iso_f8);
        iso_f9 = mapData.get(TransDataKey.iso_f9);
        iso_f10 = mapData.get(TransDataKey.iso_f10);
        iso_f11 = mapData.get(TransDataKey.iso_f11);
        iso_f12 = mapData.get(TransDataKey.iso_f12);
        iso_f13 = mapData.get(TransDataKey.iso_f13);
        iso_f14 = mapData.get(TransDataKey.iso_f14);
        iso_f14_forvoid = mapData.get(TransDataKey.iso_f14_forvoid);
        iso_f15 = mapData.get(TransDataKey.iso_f15);
        iso_f16 = mapData.get(TransDataKey.iso_f16);
        iso_f17 = mapData.get(TransDataKey.iso_f17);
        iso_f18 = mapData.get(TransDataKey.iso_f18);
        iso_f19 = mapData.get(TransDataKey.iso_f19);
        iso_f20 = mapData.get(TransDataKey.iso_f20);
        iso_f21 = mapData.get(TransDataKey.iso_f21);
        iso_f22 = mapData.get(TransDataKey.iso_f22);
        iso_f23 = mapData.get(TransDataKey.iso_f23);
        iso_f24 = mapData.get(TransDataKey.iso_f24);
        iso_f25 = mapData.get(TransDataKey.iso_f25);
        iso_f26 = mapData.get(TransDataKey.iso_f26);
        iso_f27 = mapData.get(TransDataKey.iso_f27);
        iso_f28 = mapData.get(TransDataKey.iso_f28);
        iso_f29 = mapData.get(TransDataKey.iso_f29);
        iso_f30 = mapData.get(TransDataKey.iso_f30);
        iso_f31 = mapData.get(TransDataKey.iso_f31);
        iso_f32 = mapData.get(TransDataKey.iso_f32);
        iso_f33 = mapData.get(TransDataKey.iso_f33);
        iso_f34 = mapData.get(TransDataKey.iso_f34);
        iso_f35 = mapData.get(TransDataKey.iso_f35);
        iso_track2 = mapData.get(TransDataKey.iso_track2);
        iso_track3 = mapData.get(TransDataKey.iso_track3);
        iso_f36 = mapData.get(TransDataKey.iso_f36);
        iso_f37 = mapData.get(TransDataKey.iso_f37);
        iso_f38 = mapData.get(TransDataKey.iso_f38);
        iso_f39 = mapData.get(TransDataKey.iso_f39);
        iso_f40 = mapData.get(TransDataKey.iso_f40);
        iso_f41 = mapData.get(TransDataKey.iso_f41);
        iso_f42 = mapData.get(TransDataKey.iso_f42);
        iso_f43 = mapData.get(TransDataKey.iso_f43);
        iso_f44 = mapData.get(TransDataKey.iso_f44);
        iso_f45 = mapData.get(TransDataKey.iso_f45);
        iso_f46 = mapData.get(TransDataKey.iso_f46);
        iso_f47 = mapData.get(TransDataKey.iso_f47);
        iso_f48 = mapData.get(TransDataKey.iso_f48);
        iso_f49 = mapData.get(TransDataKey.iso_f49);
        iso_f50 = mapData.get(TransDataKey.iso_f50);
        iso_f51 = mapData.get(TransDataKey.iso_f51);
        iso_f52 = mapData.get(TransDataKey.iso_f52);
        iso_f53 = mapData.get(TransDataKey.iso_f53);
        iso_f54 = mapData.get(TransDataKey.iso_f54);
        iso_f55 = mapData.get(TransDataKey.iso_f55);
        iso_f56 = mapData.get(TransDataKey.iso_f56);
        iso_f57 = mapData.get(TransDataKey.iso_f57);
        iso_f58 = mapData.get(TransDataKey.iso_f58);
        iso_f59 = mapData.get(TransDataKey.iso_f59);
        iso_f60 = mapData.get(TransDataKey.iso_f60);
        iso_f61 = mapData.get(TransDataKey.iso_f61);
        iso_f62 = mapData.get(TransDataKey.iso_f62);
        iso_f63 = mapData.get(TransDataKey.iso_f63);
        iso_f64 = mapData.get(TransDataKey.iso_f64);

        noPinFlag = "true".equals(mapData.get(TransDataKey.key_noPinFlag));
        noSignFlag = "true".equals(mapData.get(TransDataKey.key_noSignFlag));
        icData = mapData.get(TransDataKey.KEY_IC_DATA_PRINT);
        transTime = mapData.get(TransDataKey.KEY_TRANS_TIME);
        String r = mapData.get(TransDataKey.KEY_IC_SCRIPT_RESULT);
        if ("true".equals(r)) {
            scriptResult = 1;
        } else if ("false".equals(r)) {
            scriptResult = 2;
        }
        holderName = mapData.get(TransDataKey.KEY_HOLDER_NAME);
        iso_f11_origin = mapData.get(TransDataKey.iso_f11_origin);
        key_oriTransTime = mapData.get(TransDataKey.key_oriTransTime);
        key_oriAuthCode = mapData.get(TransDataKey.key_oriAuthCode);
        key_oriReference = mapData.get(TransDataKey.key_oriReference);
        key_trans_code = mapData.get(TransDataKey.key_trans_code);
    }


    /**
     * 数据更新
     *
     * @param returnData 平台返回参数集合
     * @return 更新后的交易流水，可以直接用于更新数据库
     */
    public TradeInfo update(Map<String, String> returnData) {
        Map<String, String> map = convert2Map();
        map.putAll(returnData);
        initValue(map);
        return this;
    }

    public String getKey_bak_iso_f36() {
        return key_bak_iso_f36;
    }

    public void setKey_bak_iso_f36(String key_bak_iso_f36) {
        this.key_bak_iso_f36 = key_bak_iso_f36;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public boolean isNoSignFlag() {
        return noSignFlag;
    }

    public void setNoSignFlag(boolean noSignFlag) {
        this.noSignFlag = noSignFlag;
    }

    public boolean isNoPinFlag() {
        return noPinFlag;
    }

    public void setNoPinFlag(boolean noPinFlag) {
        this.noPinFlag = noPinFlag;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public int getScriptResult() {
        return scriptResult;
    }

    public void setScriptResult(int scriptResult) {
        this.scriptResult = scriptResult;
    }

    /**
     * 获取批次号，批次号存在于60.2域中
     */
    public String getBatchNo() {
        if (iso_f60 != null && iso_f60.length() >= 8) {
            return iso_f60.substring(2, 8);
        }
        return null;
    }

    //**********************************************//
    public String getIso_f2() {
        return iso_f2;
    }

    public void setIso_f2(String iso_f2) {
        this.iso_f2 = iso_f2;
    }

    public String getIso_f3() {
        return iso_f3;
    }

    public void setIso_f3(String iso_f3) {
        this.iso_f3 = iso_f3;
    }

    public String getIso_f4() {
        return iso_f4;
    }

    public void setIso_f4(String iso_f4) {
        this.iso_f4 = iso_f4;
    }

    public String getIso_f11() {
        return iso_f11;
    }

    public void setIso_f11(String iso_f11) {
        this.iso_f11 = iso_f11;
    }

    public String getIso_f14() {
        return iso_f14;
    }

    public void setIso_f14(String iso_f14) {
        this.iso_f14 = iso_f14;
    }

    public String getIso_f14_forvoid() {
        return iso_f14_forvoid;
    }

    public void setIso_f14_forvoid(String iso_f14_forvoid) {
        this.iso_f14_forvoid = iso_f14_forvoid;
    }

    public String getIso_f22() {
        return iso_f22;
    }

    public void setIso_f22(String iso_f22) {
        this.iso_f22 = iso_f22;
    }

    public String getIso_f23() {
        return iso_f23;
    }

    public void setIso_f23(String iso_f23) {
        this.iso_f23 = iso_f23;
    }

    public String getIso_f25() {
        return iso_f25;
    }

    public void setIso_f25(String iso_f25) {
        this.iso_f25 = iso_f25;
    }

    public String getIso_f26() {
        return iso_f26;
    }

    public void setIso_f26(String iso_f26) {
        this.iso_f26 = iso_f26;
    }

    public String getIso_f35() {
        return iso_f35;
    }

    public void setIso_f35(String iso_f35) {
        this.iso_f35 = iso_f35;
    }

    public String getIso_f36() {
        return iso_f36;
    }

    public void setIso_f36(String iso_f36) {
        this.iso_f36 = iso_f36;
    }

    public String getIso_f39() {
        return iso_f39;
    }

    public void setIso_f39(String iso_f39) {
        this.iso_f39 = iso_f39;
    }

    public String getIso_f41() {
        return iso_f41;
    }

    public void setIso_f41(String iso_f41) {
        this.iso_f41 = iso_f41;
    }

    public String getIso_f42() {
        return iso_f42;
    }

    public void setIso_f42(String iso_f42) {
        this.iso_f42 = iso_f42;
    }

    public String getIso_f49() {
        return iso_f49;
    }

    public void setIso_f49(String iso_f49) {
        this.iso_f49 = iso_f49;
    }

    public String getIso_f52() {
        return iso_f52;
    }

    public void setIso_f52(String iso_f52) {
        this.iso_f52 = iso_f52;
    }

    public String getIso_f53() {
        return iso_f53;
    }

    public void setIso_f53(String iso_f53) {
        this.iso_f53 = iso_f53;
    }

    public String getIso_f55() {
        return iso_f55;
    }

    public void setIso_f55(String iso_f55) {
        this.iso_f55 = iso_f55;
    }

    public String getIso_f59() {
        return iso_f59;
    }

    public void setIso_f59(String iso_f59) {
        this.iso_f59 = iso_f59;
    }

    public String getIso_f60() {
        return iso_f60;
    }

    public void setIso_f60(String iso_f60) {
        this.iso_f60 = iso_f60;
    }

    public String getIso_f62() {
        return iso_f62;
    }

    public void setIso_f62(String iso_f62) {
        this.iso_f62 = iso_f62;
    }

    public String getIso_f63() {
        return iso_f63;
    }

    public void setIso_f63(String iso_f63) {
        this.iso_f63 = iso_f63;
    }

    public String getIso_f64() {
        return iso_f64;
    }

    public void setIso_f64(String iso_f64) {
        this.iso_f64 = iso_f64;
    }

    public String getIso_f5() {
        return iso_f5;
    }

    public void setIso_f5(String iso_f5) {
        this.iso_f5 = iso_f5;
    }

    public String getIso_f6() {
        return iso_f6;
    }

    public void setIso_f6(String iso_f6) {
        this.iso_f6 = iso_f6;
    }

    public String getIso_f7() {
        return iso_f7;
    }

    public void setIso_f7(String iso_f7) {
        this.iso_f7 = iso_f7;
    }

    public String getIso_f8() {
        return iso_f8;
    }

    public void setIso_f8(String iso_f8) {
        this.iso_f8 = iso_f8;
    }

    public String getIso_f9() {
        return iso_f9;
    }

    public void setIso_f9(String iso_f9) {
        this.iso_f9 = iso_f9;
    }

    public String getIso_f10() {
        return iso_f10;
    }

    public void setIso_f10(String iso_f10) {
        this.iso_f10 = iso_f10;
    }

    public String getIso_f12() {
        return iso_f12;
    }

    public void setIso_f12(String iso_f12) {
        this.iso_f12 = iso_f12;
    }

    public String getIso_f13() {
        return iso_f13;
    }

    public void setIso_f13(String iso_f13) {
        this.iso_f13 = iso_f13;
    }

    public String getIso_f15() {
        return iso_f15;
    }

    public void setIso_f15(String iso_f15) {
        this.iso_f15 = iso_f15;
    }

    public String getIso_f16() {
        return iso_f16;
    }

    public void setIso_f16(String iso_f16) {
        this.iso_f16 = iso_f16;
    }

    public String getIso_f17() {
        return iso_f17;
    }

    public void setIso_f17(String iso_f17) {
        this.iso_f17 = iso_f17;
    }

    public String getIso_f18() {
        return iso_f18;
    }

    public void setIso_f18(String iso_f18) {
        this.iso_f18 = iso_f18;
    }

    public String getIso_f19() {
        return iso_f19;
    }

    public void setIso_f19(String iso_f19) {
        this.iso_f19 = iso_f19;
    }

    public String getIso_f20() {
        return iso_f20;
    }

    public void setIso_f20(String iso_f20) {
        this.iso_f20 = iso_f20;
    }

    public String getIso_f21() {
        return iso_f21;
    }

    public void setIso_f21(String iso_f21) {
        this.iso_f21 = iso_f21;
    }

    public String getIso_f24() {
        return iso_f24;
    }

    public void setIso_f24(String iso_f24) {
        this.iso_f24 = iso_f24;
    }

    public String getIso_f27() {
        return iso_f27;
    }

    public void setIso_f27(String iso_f27) {
        this.iso_f27 = iso_f27;
    }

    public String getIso_f28() {
        return iso_f28;
    }

    public void setIso_f28(String iso_f28) {
        this.iso_f28 = iso_f28;
    }

    public String getIso_f29() {
        return iso_f29;
    }

    public void setIso_f29(String iso_f29) {
        this.iso_f29 = iso_f29;
    }

    public String getIso_f30() {
        return iso_f30;
    }

    public void setIso_f30(String iso_f30) {
        this.iso_f30 = iso_f30;
    }

    public String getIso_f31() {
        return iso_f31;
    }

    public void setIso_f31(String iso_f31) {
        this.iso_f31 = iso_f31;
    }

    public String getIso_f32() {
        return iso_f32;
    }

    public void setIso_f32(String iso_f32) {
        this.iso_f32 = iso_f32;
    }

    public String getIso_f33() {
        return iso_f33;
    }

    public void setIso_f33(String iso_f33) {
        this.iso_f33 = iso_f33;
    }

    public String getIso_f34() {
        return iso_f34;
    }

    public void setIso_f34(String iso_f34) {
        this.iso_f34 = iso_f34;
    }

    public String getIso_track2() {
        return iso_track2;
    }

    public void setIso_track2(String iso_track2) {
        this.iso_track2 = iso_track2;
    }

    public String getIso_track3() {
        return iso_track3;
    }

    public void setIso_track3(String iso_track3) {
        this.iso_track3 = iso_track3;
    }

    public String getIso_f37() {
        return iso_f37;
    }

    public void setIso_f37(String iso_f37) {
        this.iso_f37 = iso_f37;
    }

    public String getIso_f38() {
        return iso_f38;
    }

    public void setIso_f38(String iso_f38) {
        this.iso_f38 = iso_f38;
    }

    public String getIso_f40() {
        return iso_f40;
    }

    public void setIso_f40(String iso_f40) {
        this.iso_f40 = iso_f40;
    }

    public String getIso_f43() {
        return iso_f43;
    }

    public void setIso_f43(String iso_f43) {
        this.iso_f43 = iso_f43;
    }

    public String getIso_f44() {
        return iso_f44;
    }

    public void setIso_f44(String iso_f44) {
        this.iso_f44 = iso_f44;
    }

    public String getIso_f45() {
        return iso_f45;
    }

    public void setIso_f45(String iso_f45) {
        this.iso_f45 = iso_f45;
    }

    public String getIso_f46() {
        return iso_f46;
    }

    public void setIso_f46(String iso_f46) {
        this.iso_f46 = iso_f46;
    }

    public String getIso_f47() {
        return iso_f47;
    }

    public void setIso_f47(String iso_f47) {
        this.iso_f47 = iso_f47;
    }

    public String getIso_f48() {
        return iso_f48;
    }

    public void setIso_f48(String iso_f48) {
        this.iso_f48 = iso_f48;
    }

    public String getIso_f50() {
        return iso_f50;
    }

    public void setIso_f50(String iso_f50) {
        this.iso_f50 = iso_f50;
    }

    public String getIso_f51() {
        return iso_f51;
    }

    public void setIso_f51(String iso_f51) {
        this.iso_f51 = iso_f51;
    }

    public String getIso_f54() {
        return iso_f54;
    }

    public void setIso_f54(String iso_f54) {
        this.iso_f54 = iso_f54;
    }

    public String getIso_f56() {
        return iso_f56;
    }

    public void setIso_f56(String iso_f56) {
        this.iso_f56 = iso_f56;
    }

    public String getIso_f57() {
        return iso_f57;
    }

    public void setIso_f57(String iso_f57) {
        this.iso_f57 = iso_f57;
    }

    public String getIso_f58() {
        return iso_f58;
    }

    public void setIso_f58(String iso_f58) {
        this.iso_f58 = iso_f58;
    }

    public String getIso_f61() {
        return iso_f61;
    }

    public void setIso_f61(String iso_f61) {
        this.iso_f61 = iso_f61;
    }

    public String getIso_f64_send() {
        return iso_f64_send;
    }

    public void setIso_f64_send(String iso_f64_send) {
        this.iso_f64_send = iso_f64_send;
    }

    public String getIso_f55_send() {
        return iso_f55_send;
    }

    public void setIso_f55_send(String iso_f55_send) {
        this.iso_f55_send = iso_f55_send;
    }

    public String getIso_f59_send() {
        return iso_f59_send;
    }

    public void setIso_f59_send(String iso_f59_send) {
        this.iso_f59_send = iso_f59_send;
    }

    public String getIso_f60_send() {
        return iso_f60_send;
    }

    public void setIso_f60_send(String iso_f60_send) {
        this.iso_f60_send = iso_f60_send;
    }

    public String getIcData() {
        return icData;
    }

    public void setIcData(String icData) {
        this.icData = icData;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public boolean isBatchSuccess() {
        return isBatchSuccess;
    }

    public void setBatchSuccess(boolean batchSuccess) {
        isBatchSuccess = batchSuccess;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getIso_f11_origin() {
        return iso_f11_origin;
    }

    public void setIso_f11_origin(String iso_f11_origin) {
        this.iso_f11_origin = iso_f11_origin;
    }

    public String getKey_oriTransTime() {
        return key_oriTransTime;
    }

    public void setKey_oriTransTime(String key_oriTransTime) {
        this.key_oriTransTime = key_oriTransTime;
    }

    public String getKey_oriAuthCode() {
        return key_oriAuthCode;
    }

    public void setKey_oriAuthCode(String key_oriAuthCode) {
        this.key_oriAuthCode = key_oriAuthCode;
    }

    public String getKey_oriReference() {
        return key_oriReference;
    }

    public void setKey_oriReference(String key_oriReference) {
        this.key_oriReference = key_oriReference;
    }

    public String getKey_trans_code() {
        return key_trans_code;
    }

    public void setKey_trans_code(String key_trans_code) {
        this.key_trans_code = key_trans_code;
    }

    public Map<String, String> convert2Map() {
        Map<String, String> map = new HashMap<>();
        map.put(TransDataKey.iso_f2, iso_f2);
        map.put(TransDataKey.iso_f3, iso_f3);
        map.put(TransDataKey.iso_f4, iso_f4);
        map.put(TransDataKey.iso_f5, iso_f5);
        map.put(TransDataKey.iso_f6, iso_f6);
        map.put(TransDataKey.iso_f7, iso_f7);
        map.put(TransDataKey.iso_f8, iso_f8);
        map.put(TransDataKey.iso_f9, iso_f9);
        map.put(TransDataKey.iso_f10, iso_f10);
        map.put(TransDataKey.iso_f11, iso_f11);
        map.put(TransDataKey.iso_f12, iso_f12);
        map.put(TransDataKey.iso_f13, iso_f13);
        map.put(TransDataKey.iso_f14, iso_f14);
        map.put(TransDataKey.iso_f14_forvoid, iso_f14_forvoid);
        map.put(TransDataKey.iso_f15, iso_f15);
        map.put(TransDataKey.iso_f16, iso_f16);
        map.put(TransDataKey.iso_f17, iso_f17);
        map.put(TransDataKey.iso_f18, iso_f18);
        map.put(TransDataKey.iso_f19, iso_f19);
        map.put(TransDataKey.iso_f20, iso_f20);
        map.put(TransDataKey.iso_f21, iso_f21);
        map.put(TransDataKey.iso_f22, iso_f22);
        map.put(TransDataKey.iso_f23, iso_f23);
        map.put(TransDataKey.iso_f24, iso_f24);
        map.put(TransDataKey.iso_f25, iso_f25);
        map.put(TransDataKey.iso_f26, iso_f26);
        map.put(TransDataKey.iso_f27, iso_f27);
        map.put(TransDataKey.iso_f28, iso_f28);
        map.put(TransDataKey.iso_f29, iso_f29);
        map.put(TransDataKey.iso_f30, iso_f30);
        map.put(TransDataKey.iso_f31, iso_f30);
        map.put(TransDataKey.iso_f32, iso_f32);
        map.put(TransDataKey.iso_f33, iso_f33);
        map.put(TransDataKey.iso_f34, iso_f34);
        map.put(TransDataKey.iso_track2, iso_track2);
        map.put(TransDataKey.iso_track3, iso_track3);
        map.put(TransDataKey.iso_f35, iso_f35);
        map.put(TransDataKey.iso_f36, iso_f36);
        map.put(TransDataKey.iso_f37, iso_f37);
        map.put(TransDataKey.iso_f38, iso_f38);
        map.put(TransDataKey.iso_f39, iso_f39);
        map.put(TransDataKey.iso_f40, iso_f40);
        map.put(TransDataKey.iso_f41, iso_f41);
        map.put(TransDataKey.iso_f42, iso_f42);
        map.put(TransDataKey.iso_f43, iso_f43);
        map.put(TransDataKey.iso_f44, iso_f44);
        map.put(TransDataKey.iso_f45, iso_f45);
        map.put(TransDataKey.iso_f46, iso_f46);
        map.put(TransDataKey.iso_f47, iso_f47);
        map.put(TransDataKey.iso_f48, iso_f48);
        map.put(TransDataKey.iso_f49, iso_f49);
        map.put(TransDataKey.iso_f50, iso_f50);
        map.put(TransDataKey.iso_f51, iso_f51);
        map.put(TransDataKey.iso_f52, iso_f52);
        map.put(TransDataKey.iso_f53, iso_f53);
        map.put(TransDataKey.iso_f54, iso_f54);
        map.put(TransDataKey.iso_f55, iso_f55);
        map.put(TransDataKey.iso_f56, iso_f56);
        map.put(TransDataKey.iso_f57, iso_f57);
        map.put(TransDataKey.iso_f58, iso_f58);
        map.put(TransDataKey.iso_f59, iso_f59);
        map.put(TransDataKey.iso_f60, iso_f60);
        map.put(TransDataKey.iso_f61, iso_f61);
        map.put(TransDataKey.iso_f62, iso_f62);
        map.put(TransDataKey.iso_f63, iso_f63);
        map.put(TransDataKey.iso_f64, iso_f64);
        map.put(TransDataKey.key_noPinFlag, "" + noPinFlag);
        map.put(TransDataKey.key_noSignFlag, "" + noSignFlag);
        map.put(TransDataKey.key_flag, "" + flag);
        map.put(TransDataKey.KEY_TRANS_TIME, transTime);
        map.put(TransDataKey.KEY_IC_DATA_PRINT, icData);
        map.put(TransDataKey.KEY_HOLDER_NAME, holderName);
        map.put(TransDataKey.iso_f11_origin, iso_f11_origin);
        map.put(TransDataKey.key_oriTransTime, key_oriTransTime);
        map.put(TransDataKey.key_oriAuthCode, key_oriAuthCode);
        map.put(TransDataKey.key_oriReference, key_oriReference);
        map.put(TransDataKey.key_trans_code, key_trans_code);
        return map;
    }

    @Override
    public String toString() {
        return "TradeInfo{" +
                "transCode='" + transCode + '\'' +
                ", iso_f2='" + iso_f2 + '\'' +
                ", iso_f3='" + iso_f3 + '\'' +
                ", iso_f4='" + iso_f4 + '\'' +
                ", iso_f5='" + iso_f5 + '\'' +
                ", iso_f6='" + iso_f6 + '\'' +
                ", iso_f7='" + iso_f7 + '\'' +
                ", iso_f8='" + iso_f8 + '\'' +
                ", iso_f9='" + iso_f9 + '\'' +
                ", iso_f10='" + iso_f10 + '\'' +
                ", iso_f11='" + iso_f11 + '\'' +
                ", iso_f12='" + iso_f12 + '\'' +
                ", iso_f13='" + iso_f13 + '\'' +
                ", iso_f14='" + iso_f14 + '\'' +
                ", iso_f14_forvoid='" + iso_f14_forvoid + '\'' +
                ", iso_f15='" + iso_f15 + '\'' +
                ", iso_f16='" + iso_f16 + '\'' +
                ", iso_f17='" + iso_f17 + '\'' +
                ", iso_f18='" + iso_f18 + '\'' +
                ", iso_f19='" + iso_f19 + '\'' +
                ", iso_f20='" + iso_f20 + '\'' +
                ", iso_f21='" + iso_f21 + '\'' +
                ", iso_f22='" + iso_f22 + '\'' +
                ", iso_f23='" + iso_f23 + '\'' +
                ", iso_f24='" + iso_f24 + '\'' +
                ", iso_f25='" + iso_f25 + '\'' +
                ", iso_f26='" + iso_f26 + '\'' +
                ", iso_f27='" + iso_f27 + '\'' +
                ", iso_f28='" + iso_f28 + '\'' +
                ", iso_f29='" + iso_f29 + '\'' +
                ", iso_f30='" + iso_f30 + '\'' +
                ", iso_f31='" + iso_f31 + '\'' +
                ", iso_f32='" + iso_f32 + '\'' +
                ", iso_f33='" + iso_f33 + '\'' +
                ", iso_f34='" + iso_f34 + '\'' +
                ", iso_track2='" + iso_track2 + '\'' +
                ", iso_track3='" + iso_track3 + '\'' +
                ", iso_f35='" + iso_f35 + '\'' +
                ", iso_f36='" + iso_f36 + '\'' +
                ", iso_f37='" + iso_f37 + '\'' +
                ", iso_f38='" + iso_f38 + '\'' +
                ", iso_f39='" + iso_f39 + '\'' +
                ", iso_f40='" + iso_f40 + '\'' +
                ", iso_f41='" + iso_f41 + '\'' +
                ", iso_f42='" + iso_f42 + '\'' +
                ", iso_f43='" + iso_f43 + '\'' +
                ", iso_f44='" + iso_f44 + '\'' +
                ", iso_f45='" + iso_f45 + '\'' +
                ", iso_f46='" + iso_f46 + '\'' +
                ", iso_f47='" + iso_f47 + '\'' +
                ", iso_f48='" + iso_f48 + '\'' +
                ", iso_f49='" + iso_f49 + '\'' +
                ", iso_f50='" + iso_f50 + '\'' +
                ", iso_f51='" + iso_f51 + '\'' +
                ", iso_f52='" + iso_f52 + '\'' +
                ", iso_f53='" + iso_f53 + '\'' +
                ", iso_f54='" + iso_f54 + '\'' +
                ", iso_f55='" + iso_f55 + '\'' +
                ", iso_f56='" + iso_f56 + '\'' +
                ", iso_f57='" + iso_f57 + '\'' +
                ", iso_f58='" + iso_f58 + '\'' +
                ", iso_f59='" + iso_f59 + '\'' +
                ", iso_f60='" + iso_f60 + '\'' +
                ", iso_f61='" + iso_f61 + '\'' +
                ", iso_f62='" + iso_f62 + '\'' +
                ", iso_f63='" + iso_f63 + '\'' +
                ", iso_f64='" + iso_f64 + '\'' +
                ", iso_f55_send='" + iso_f55_send + '\'' +
                ", iso_f59_send='" + iso_f59_send + '\'' +
                ", iso_f60_send='" + iso_f60_send + '\'' +
                ", iso_f64_send='" + iso_f64_send + '\'' +
                ", flag=" + flag +
                ", noPinFlag=" + noPinFlag +
                ", noSignFlag=" + noSignFlag +
                ", transTime='" + transTime + '\'' +
                ", icData='" + icData + '\'' +
                ", holderName='" + holderName + '\'' +
                ", iso_f11_origin='" + iso_f11_origin + '\'' +
                ", key_oriTransTime='" + key_oriTransTime + '\'' +
                ", key_oriAuthCode='" + key_oriAuthCode + '\'' +
                ", key_oriReference='" + key_oriReference + '\'' +
                ", key_trans_code='" + key_trans_code + '\'' +
                '}';
    }
}

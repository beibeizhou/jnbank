package com.centerm.jnbank.bean;

import com.centerm.jnbank.common.TransDataKey;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 交易记录。用于冲正使用。
 * 所有发出去的交易都需要先记录在此表中。
 * 如果该笔交易未接收到平台响应，下次发起交易时需要先进行冲正。
 * author:wanliang527</br>
 * date:2016/11/2</br>
 */

@DatabaseTable(tableName = "tb_reverse")
public class ReverseInfo implements Serializable {

    @DatabaseField
    private String transCode;//交易类型
    @DatabaseField
    private String iso_f2;//主账号
    @DatabaseField
    private String iso_f3;//交易处理码
    @DatabaseField
    private String iso_f4;//交易金额
    @DatabaseField(id = true)//作为主键
    private String iso_f11;//受卡方系统跟踪号（终端流水号）
    @DatabaseField
    private String iso_f12;//受卡方所在地时间
    @DatabaseField
    private String iso_f13;//受卡方所在地日期
    @DatabaseField
    private String iso_f14;//卡有效期
    @DatabaseField
    private String iso_f22;//服务点输入方式码
    @DatabaseField
    private String iso_f23;//卡序列号
    @DatabaseField
    private String iso_f25;//服务点条件码
    @DatabaseField
    private String iso_f26;//服务点PIN获取码
    @DatabaseField
    private String iso_f35;//2磁数据
    @DatabaseField
    private String iso_f36;//3磁数据
    @DatabaseField
    private String iso_f39 = "06";//应答码，冲正用来传递冲正原因：98-未收到应答；96-POS机故障；A0-校验MAC出错；06-其它
    @DatabaseField
    private String iso_f41;//受卡机终端标识码
    @DatabaseField
    private String iso_f42;//受卡方标识码
    @DatabaseField
    private String iso_f49;//交易货币代码
    @DatabaseField
    private String iso_f52;//个人标识码数据
    @DatabaseField
    private String iso_f53;//安全控制信息
    @DatabaseField
    private String iso_f55;//IC卡数据域
    @DatabaseField
    private String iso_f59;//钱宝自定义，用于签购单控制
    @DatabaseField
    private String iso_f60;//自定义域（交易类型码+网络管理码+终端读取能力+IC卡条件代码）
    @DatabaseField
    private String iso_f61;//原始信息域
    @DatabaseField
    private String iso_f62;//自定义域
    @DatabaseField
    private String iso_f63;//自定义域
    @DatabaseField
    private String iso_f64;//MAC值
    @DatabaseField
    private int retryTimes;//已重试次数
    @DatabaseField
    private String transTime;//本地交易日期yyyyMMddHHmmss

    public ReverseInfo() {
    }

    public ReverseInfo(String transCode, Map<String, String> mapData) {
        this.transCode = transCode;
        iso_f2 = mapData.get(TransDataKey.iso_f2);
        iso_f3 = mapData.get(TransDataKey.iso_f3);
        iso_f4 = mapData.get(TransDataKey.iso_f4);
        iso_f11 = mapData.get(TransDataKey.iso_f11);
        iso_f12 = mapData.get(TransDataKey.iso_f12);
        iso_f13 = mapData.get(TransDataKey.iso_f13);
        iso_f14 = mapData.get(TransDataKey.iso_f14);
        iso_f22 = mapData.get(TransDataKey.iso_f22);
        iso_f23 = mapData.get(TransDataKey.iso_f23);
        iso_f25 = mapData.get(TransDataKey.iso_f25);
        iso_f26 = mapData.get(TransDataKey.iso_f26);
        iso_f35 = mapData.get(TransDataKey.iso_f35);
        iso_f36 = mapData.get(TransDataKey.iso_f36);
        iso_f39 = mapData.get(TransDataKey.iso_f39);
        iso_f41 = mapData.get(TransDataKey.iso_f41);
        iso_f42 = mapData.get(TransDataKey.iso_f42);
        iso_f49 = mapData.get(TransDataKey.iso_f49);
        iso_f52 = mapData.get(TransDataKey.iso_f52);
        iso_f53 = mapData.get(TransDataKey.iso_f53);
        iso_f55 = mapData.get(TransDataKey.iso_f55_reverse);
        iso_f59 = mapData.get(TransDataKey.iso_f59);
        iso_f60 = mapData.get(TransDataKey.iso_f60);
        iso_f61 = mapData.get(TransDataKey.iso_f61);
        iso_f62 = mapData.get(TransDataKey.iso_f62);
        iso_f63 = mapData.get(TransDataKey.iso_f63);
        iso_f64 = mapData.get(TransDataKey.iso_f64);
        transTime = mapData.get(TransDataKey.KEY_TRANS_TIME);
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

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

    public String getIso_f14() {
        return iso_f14;
    }

    public void setIso_f14(String iso_f14) {
        this.iso_f14 = iso_f14;
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

    public String getIso_f61() {
        return iso_f61;
    }

    public void setIso_f61(String iso_f61) {
        this.iso_f61 = iso_f61;
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

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Map<String, String> convert2Map() {
        Map<String, String> map = new HashMap<>();
        map.put(TransDataKey.iso_f2, iso_f2);
        map.put(TransDataKey.iso_f3, iso_f3);
        map.put(TransDataKey.iso_f4, iso_f4);
        map.put(TransDataKey.iso_f11, iso_f11);
        map.put(TransDataKey.iso_f14, iso_f14);
        map.put(TransDataKey.iso_f22, iso_f22);
        map.put(TransDataKey.iso_f23, iso_f23);
        map.put(TransDataKey.iso_f25, iso_f25);
        map.put(TransDataKey.iso_f26, iso_f26);
        map.put(TransDataKey.iso_f35, iso_f35);
        map.put(TransDataKey.iso_f36, iso_f36);
        map.put(TransDataKey.iso_f39, iso_f39);
        map.put(TransDataKey.iso_f41, iso_f41);
        map.put(TransDataKey.iso_f42, iso_f42);
        map.put(TransDataKey.iso_f49, iso_f49);
        map.put(TransDataKey.iso_f52, iso_f52);
        map.put(TransDataKey.iso_f53, iso_f53);
        map.put(TransDataKey.iso_f55, iso_f55);
        map.put(TransDataKey.iso_f59, iso_f59);
        map.put(TransDataKey.iso_f60, iso_f60);
        map.put(TransDataKey.iso_f61, iso_f61);
        map.put(TransDataKey.iso_f62, iso_f62);
        map.put(TransDataKey.iso_f63, iso_f63);
        map.put(TransDataKey.iso_f64, iso_f64);
        map.put(TransDataKey.KEY_TRANS_TIME, transTime);
        map.put(TransDataKey.key_retryTimes, "" + retryTimes);
        return map;
    }

    @Override
    public String toString() {
        return "ReverseInfo{" +
                "transCode='" + transCode + '\'' +
                ", iso_f2='" + iso_f2 + '\'' +
                ", iso_f3='" + iso_f3 + '\'' +
                ", iso_f4='" + iso_f4 + '\'' +
                ", iso_f11='" + iso_f11 + '\'' +
                ", iso_f14='" + iso_f14 + '\'' +
                ", iso_f22='" + iso_f22 + '\'' +
                ", iso_f23='" + iso_f23 + '\'' +
                ", iso_f25='" + iso_f25 + '\'' +
                ", iso_f26='" + iso_f26 + '\'' +
                ", iso_f35='" + iso_f35 + '\'' +
                ", iso_f36='" + iso_f36 + '\'' +
                ", iso_f39='" + iso_f39 + '\'' +
                ", iso_f41='" + iso_f41 + '\'' +
                ", iso_f42='" + iso_f42 + '\'' +
                ", iso_f49='" + iso_f49 + '\'' +
                ", iso_f52='" + iso_f52 + '\'' +
                ", iso_f53='" + iso_f53 + '\'' +
                ", iso_f55='" + iso_f55 + '\'' +
                ", iso_f59='" + iso_f59 + '\'' +
                ", iso_f60='" + iso_f60 + '\'' +
                ", iso_f61='" + iso_f61 + '\'' +
                ", iso_f62='" + iso_f62 + '\'' +
                ", iso_f63='" + iso_f63 + '\'' +
                ", iso_f64='" + iso_f64 + '\'' +
                ", key_retryTimes=" + retryTimes +
                ", KEY_TRANS_TIME='" + transTime + '\'' +
                '}';
    }
}

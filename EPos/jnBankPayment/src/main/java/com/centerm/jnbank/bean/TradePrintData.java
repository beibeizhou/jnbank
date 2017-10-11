package com.centerm.jnbank.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by ysd on 2016/12/5.
 */
@DatabaseTable(tableName = "tb_reprint")
public class TradePrintData {
    @DatabaseField(id = true)
    private String iso_f11;//终端流水号
    @DatabaseField
    private String arqc;
    @DatabaseField
    private String tvr;
    @DatabaseField
    private String aid;
    @DatabaseField
    private String atc;
    @DatabaseField
    private String tsi;
    @DatabaseField
    private String umpr_num;
    @DatabaseField
    private String aip;
    @DatabaseField
    private String iad;
    @DatabaseField
    private String tc;
    @DatabaseField
    private String appLabel;
    @DatabaseField
    private String appName;
    @DatabaseField
    private boolean noNeedPin;
    @DatabaseField
    private boolean noNeedSign;
    @DatabaseField
    private String amount;//免签免密的金额

    private boolean isRePrint;

    public TradePrintData() {
    }

    public String getIso_f11() {
        return iso_f11;
    }

    public void setIso_f11(String iso_f11) {
        this.iso_f11 = iso_f11;
    }

    public String getArqc() {
        return arqc;
    }

    public void setArqc(String arqc) {
        this.arqc = arqc;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public String getTsi() {
        return tsi;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public String getUmpr_num() {
        return umpr_num;
    }

    public void setUmpr_num(String umpr_num) {
        this.umpr_num = umpr_num;
    }

    public String getIad() {
        return iad;
    }

    public void setIad(String iad) {
        this.iad = iad;
    }

    public String getAip() {
        return aip;
    }

    public void setAip(String aip) {
        this.aip = aip;
    }

    public boolean isNoNeedPin() {
        return noNeedPin;
    }

    public void setNoNeedPin(boolean noNeedPin) {
        this.noNeedPin = noNeedPin;
    }

    public boolean isNoNeedSign() {
        return noNeedSign;
    }

    public void setNoNeedSign(boolean noNeedSign) {
        this.noNeedSign = noNeedSign;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isRePrint() {
        return isRePrint;
    }

    public void setRePrint(boolean rePrint) {
        isRePrint = rePrint;
    }

    public String getTc() {
        return tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}

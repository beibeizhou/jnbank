package com.centerm.jnbank.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by ysd on 2016/11/29.
 */
@DatabaseTable(tableName = "tb_elec_sign")
public class ElecSignInfo {
    @DatabaseField
    private String mchtId;//商户号
    @DatabaseField
    private String termId;//终端号
    @DatabaseField
    private String transDate;//交易日期（yyyymmdd）
    @DatabaseField
    private String transTime;//交易时间
    @DatabaseField
    private String transNum;//交易参考号
    @DatabaseField(id = true)
    private String picName;//图片名称
    @DatabaseField(defaultValue = "0")
    private int retryCount;//重试次数

    public String getMchtId() {
        return mchtId;
    }

    public void setMchtId(String mchtId) {
        this.mchtId = mchtId;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getTransNum() {
        return transNum;
    }

    public void setTransNum(String transNum) {
        this.transNum = transNum;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}

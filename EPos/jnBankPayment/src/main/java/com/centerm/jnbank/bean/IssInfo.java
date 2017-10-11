package com.centerm.jnbank.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 创建日期：2017/9/14 0014 on 17:32
 * 描述:Iss卡信息表
 * 作者:周文正
 */
@DatabaseTable(tableName = "tb_iss_data")
public class IssInfo {
    @DatabaseField(generatedId = true)
    private int id;//自定义ID，自增长
    @DatabaseField
    private String cardIssNo;//卡ISS编号
    @DatabaseField
    private String issName;//发卡行名称

    public IssInfo() {
    }

    public IssInfo( String cardIssNo, String issName) {
        this.cardIssNo = cardIssNo;
        this.issName = issName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCardIssNo() {
        return cardIssNo;
    }

    public void setCardIssNo(String cardIssNo) {
        this.cardIssNo = cardIssNo;
    }

    public String getIssName() {
        return issName;
    }

    public void setIssName(String issName) {
        this.issName = issName;
    }

    @Override
    public String toString() {
        return "IssInfo{" +
                "id=" + id +
                ", cardIssNo='" + cardIssNo + '\'' +
                ", issName='" + issName + '\'' +
                '}';
    }
}

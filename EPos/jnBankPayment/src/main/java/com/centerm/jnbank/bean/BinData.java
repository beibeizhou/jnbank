package com.centerm.jnbank.bean;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysd on 2016/11/29.
 */
@DatabaseTable(tableName = "tb_bin_data")
public class BinData {
    @DatabaseField(generatedId = true)
    private int id;//自定义ID，自增长
    @DatabaseField
    private String cardBinNo;//卡bin编号
    @DatabaseField
    private String cardBinLenth;//卡bin长度
    @DatabaseField
    private String cardBin;//卡bin
    @DatabaseField
    private String cardNumberLenth;//卡长度
    @DatabaseField
    private String cardType;//卡类型
    @DatabaseField
    private String orgNo;//机构号
    @DatabaseField
    private String cardOrg;//卡组织

    public BinData() {
    }

    public BinData(String cardBinNo, String cardBinLenth, String cardBin, String cardNumberLenth, String cardType, String orgNo, String cardOrg) {
        this.cardBinNo = cardBinNo;
        this.cardBinLenth = cardBinLenth;
        this.cardBin = cardBin;
        this.cardNumberLenth = cardNumberLenth;
        this.cardType = cardType;
        this.orgNo = orgNo;
        this.cardOrg = cardOrg;
    }

    public String getCardBinNo() {
        return cardBinNo;
    }

    public void setCardBinNo(String cardBinNo) {
        this.cardBinNo = cardBinNo;
    }

    public String getCardBinLenth() {
        return cardBinLenth;
    }

    public void setCardBinLenth(String cardBinLenth) {
        this.cardBinLenth = cardBinLenth;
    }

    public String getCardBin() {
        return cardBin;
    }

    public void setCardBin(String cardBin) {
        this.cardBin = cardBin;
    }

    public String getCardNumberLenth() {
        return cardNumberLenth;
    }

    public void setCardNumberLenth(String cardNumberLenth) {
        this.cardNumberLenth = cardNumberLenth;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getOrgNo() {
        return orgNo;
    }

    public void setOrgNo(String orgNo) {
        this.orgNo = orgNo;
    }

    public String getCardOrg() {
        return cardOrg;
    }

    public void setCardOrg(String cardOrg) {
        this.cardOrg = cardOrg;
    }

    public static List<BinData> parse(String iso62) {
        List<BinData> binList = new ArrayList<>();
        if (TextUtils.isEmpty(iso62)) {
            return binList;
        }
        int index = 6;
        while (index < iso62.length()) {
            BinData data = new BinData();
            try {
                data.cardBinLenth = iso62.substring(index, index + 2).trim();
                index += 2;
                int len = Integer.valueOf(data.cardBinLenth);
                data.cardBin = iso62.substring(index, index + len).trim();
                index += len;
                data.cardNumberLenth = iso62.substring(index, index + 2).trim();
                index += 2;
                data.cardType = iso62.substring(index, index + 2).trim();
                index += 2;
                data.orgNo = iso62.substring(index, index + 11).trim();
                index += 11;
                data.cardOrg = iso62.substring(index, index + 1).trim();
                index += 1;
                binList.add(data);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return binList;
    }


    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BinData{" +
                "id=" + id +
                ", cardBinNo='" + cardBinNo + '\'' +
                ", cardBinLenth='" + cardBinLenth + '\'' +
                ", cardBin='" + cardBin + '\'' +
                ", cardNumberLenth='" + cardNumberLenth + '\'' +
                ", cardType='" + cardType + '\'' +
                ", orgNo='" + orgNo + '\'' +
                ", cardOrg='" + cardOrg + '\'' +
                '}';
    }
}

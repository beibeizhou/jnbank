package com.centerm.jnbank.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by ysd on 2016/11/1.
 */
@DatabaseTable(tableName = "tb_print_item")
public class PrinterItem {
    @DatabaseField(id = true)
    private String paramId;
    @DatabaseField
    private String paramTip;
    @DatabaseField
    private String paramValue;
    @DatabaseField
    private int printRange;
    @DatabaseField
    private int textSize;

    public String getParamTip() {
        return paramTip;
    }

    public void setParamTip(String paramTip) {
        this.paramTip = paramTip;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public int getPrintRange() {
        return printRange;
    }

    public void setPrintRange(int printRange) {
        this.printRange = printRange;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public PrinterItem(){

    }
    public PrinterItem(String paramTip, String paramValue, String paramId, int textSize, int printRange){
        this.paramTip = paramTip;
        this.paramValue = paramValue;
        this.paramId = paramId;
        this.printRange = printRange;
        this.textSize = textSize;
    }
}

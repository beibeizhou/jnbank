package com.centerm.jnbank.xml.menu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 菜单项（功能入口）
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class MenuItem implements Parcelable {
    protected String entag;//英文标签
    protected String chnTag;//中文标签
    protected String iconResName;//图标资源名称
    protected String textResName;//文本资源名称
    //流程文件名，如果XML文件中不定义该属性，则默认对应与英文标签一致的文件;
    //例如【消费业务】，英文标签为SALE，则默认对应的流程文件为assets/process/SALE
    protected String processFile;
    protected String transCode;//交易码

    public MenuItem() {
    }

    public MenuItem(String iconResName, String textName) {
        this.iconResName = iconResName;
        this.textResName = textName;
    }

    public String getIconResName() {
        return iconResName;
    }

    public void setIconResName(String iconResName) {
        this.iconResName = iconResName;
    }

    public String getTextResName() {
        return textResName;
    }

    public void setTextResName(String textResName) {
        this.textResName = textResName;
    }

    public String getEntag() {
        return entag;
    }

    public void setEntag(String entag) {
        this.entag = entag;
    }

    public String getChnTag() {
        return chnTag;
    }

    public void setChnTag(String chnTag) {
        this.chnTag = chnTag;
    }

    public String getProcessFile() {
        return processFile;
    }

    public void setProcessFile(String processFile) {
        this.processFile = processFile;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "entag='" + entag + '\'' +
                ", chnTag='" + chnTag + '\'' +
                ", iconResName='" + iconResName + '\'' +
                ", textResName='" + textResName + '\'' +
                ", processFile='" + processFile + '\'' +
                ", transCode='" + transCode + '\'' +
                '}';
    }

    public static final Creator<MenuItem> CREATOR = new Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel source) {
            MenuItem item = new MenuItem();
            item.entag = source.readString();
            item.chnTag = source.readString();
            item.iconResName = source.readString();
            item.textResName = source.readString();
            item.processFile = source.readString();
            item.transCode = source.readString();
            return item;
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entag);
        dest.writeString(chnTag);
        dest.writeString(iconResName);
        dest.writeString(textResName);
        dest.writeString(processFile);
        dest.writeString(transCode);
    }
}

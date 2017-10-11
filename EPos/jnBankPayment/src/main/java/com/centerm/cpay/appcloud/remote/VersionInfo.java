package com.centerm.cpay.appcloud.remote;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author: linwanliang</br>
 * date:2016/7/2</br>
 */
public class VersionInfo implements Parcelable {

    private String pkgName;//包名
    private String appName;//应用名称
    private String rate;//星级
    private int downloadCounts;//下载数量
    private String description;//描述
    private String updateInfo;//更新信息
    private String downloadUrl;//下载地址
    private String iconUrl;//图标地址
    private long size;//大小
    private int versionCode;//版本号
    private String versionName;//版本名
    private String developer;//开发者
    private String publicTime;//发布时间
    private String previewImg;//预览图
    private String typeName;//类型
    private String keyword;//关键词
    private String appMd5;//文件MD5
    private String updateType;//是否强制更新   1代表强制，2代表非强制

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public VersionInfo() {
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public int getDownloadCounts() {
        return downloadCounts;
    }

    public void setDownloadCounts(int downloadCounts) {
        this.downloadCounts = downloadCounts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPublicTime() {
        return publicTime;
    }

    public void setPublicTime(String publicTime) {
        this.publicTime = publicTime;
    }

    public String getPreviewImg() {
        return previewImg;
    }

    public void setPreviewImg(String previewImg) {
        this.previewImg = previewImg;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getAppMd5() {
        return appMd5;
    }

    public void setAppMd5(String appMd5) {
        this.appMd5 = appMd5;
    }

    protected VersionInfo(Parcel in) {
        this.pkgName = in.readString();
        this.appName = in.readString();
        this.rate = in.readString();
        this.downloadCounts = in.readInt();
        this.description = in.readString();
        this.updateInfo = in.readString();
        this.downloadUrl = in.readString();
        this.iconUrl = in.readString();
        this.size = in.readLong();
        this.versionName = in.readString();
        this.versionCode = in.readInt();
        this.developer = in.readString();
        this.publicTime = in.readString();
        this.previewImg = in.readString();
        this.typeName = in.readString();
        this.keyword = in.readString();
        this.appMd5 = in.readString();
        this.updateType = in.readString();
    }

    public static final Creator<VersionInfo> CREATOR = new Creator<VersionInfo>() {
        @Override
        public VersionInfo createFromParcel(Parcel in) {
            return new VersionInfo(in);
        }

        @Override
        public VersionInfo[] newArray(int size) {
            return new VersionInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pkgName);
        dest.writeString(appName);
        dest.writeString(rate);
        dest.writeInt(downloadCounts);
        dest.writeString(description);
        dest.writeString(updateInfo);
        dest.writeString(downloadUrl);
        dest.writeString(iconUrl);
        dest.writeLong(size);
        dest.writeString(versionName);
        dest.writeInt(versionCode);
        dest.writeString(developer);
        dest.writeString(publicTime);
        dest.writeString(previewImg);
        dest.writeString(typeName);
        dest.writeString(keyword);
        dest.writeString(appMd5);
        dest.writeString(updateType);
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "pkgName='" + pkgName + '\'' +
                ", appName='" + appName + '\'' +
                ", rate='" + rate + '\'' +
                ", downloadCounts=" + downloadCounts +
                ", description='" + description + '\'' +
                ", updateInfo='" + updateInfo + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", size=" + size +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", developer='" + developer + '\'' +
                ", publicTime='" + publicTime + '\'' +
                ", previewImg='" + previewImg + '\'' +
                ", typeName='" + typeName + '\'' +
                ", keyword='" + keyword + '\'' +
                ", appMd5='" + appMd5 + '\'' +
                ", updateType='" + updateType + '\'' +
                '}';
    }
}


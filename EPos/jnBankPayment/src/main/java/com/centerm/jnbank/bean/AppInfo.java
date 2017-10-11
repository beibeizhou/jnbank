package com.centerm.jnbank.bean;

/**
 * author:wanliang527</br>
 * date:2016/12/5</br>
 */

public class AppInfo {
    private String appName;//应用名称
    private String downloadUrl;//下载地址
    private int id;//ID
    private boolean forceUpdate;//强制升级标识
    private String describe;//版本描述
    private String version;//版本名称
    private String platform;//平台

    public AppInfo(String appName, String downloadUrl, int id, boolean forceUpdate, String describe, String version, String platform) {
        this.appName = appName;
        this.downloadUrl = downloadUrl;
        this.id = id;
        this.forceUpdate = forceUpdate;
        this.describe = describe;
        this.version = version;
        this.platform = platform;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}

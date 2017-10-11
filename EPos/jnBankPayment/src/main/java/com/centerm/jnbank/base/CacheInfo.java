package com.centerm.jnbank.base;

/**
 * 创建日期：2017/9/20 0020 on 14:05
 * 描述:
 * 作者:周文正
 */

public class CacheInfo {

    String timestamp;// 时间戳
    String randomNum;// 随机数
    String codeCmd;// 信息码
    String packagename;// 广告查询时，包名需要存储下来

    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public void setRandomNum(String randomNum){
        this.randomNum = randomNum;
    }

    public void setCodeCmd(String codeCmd){
        this.codeCmd = codeCmd;
    }

    public void setPackname(String packagename){
        this.packagename = packagename;
    }

    public String getCacheInfo() {
        return timestamp+randomNum+codeCmd;
    }

    public String getPackname() {
        return packagename;
    }


}

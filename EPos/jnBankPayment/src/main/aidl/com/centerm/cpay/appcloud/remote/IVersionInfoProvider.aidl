// IVersionProvider.aidl
package com.centerm.cpay.appcloud.remote;

import com.centerm.cpay.appcloud.remote.IVersionInfoCallback;
/**
 * author: linwanliang</br>
 * date:2016/7/2</br>
 */
interface IVersionInfoProvider {

    /**
     * 获取最新的版本信息
     *
     * @param pkgName  包名
     * @param callback 回调对象
     */
    void getLatestVersion(String pkgName, IVersionInfoCallback callback);

}
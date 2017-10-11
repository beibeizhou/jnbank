// IVersionInfoCallback.aidl
package com.centerm.cpay.appcloud.remote;

import com.centerm.cpay.appcloud.remote.VersionInfo;
/**
 * author: linwanliang</br>
 * date:2016/7/2</br>
 */
interface IVersionInfoCallback {
    /**
     * 查询成功
     *
     * @param info 版本信息
     */
    void onSuccess(in VersionInfo info);

    /**
     * 查询失败
     *
     * @param errorCode 错误码
     * @param errorInfo 错误信息
     */
    void onError(int errorCode, String errorInfo);
}

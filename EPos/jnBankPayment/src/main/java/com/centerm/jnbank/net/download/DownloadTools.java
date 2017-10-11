package com.centerm.jnbank.net.download;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;


/**
 * Created by 王玮 on 2016/7/4.
 */
public class DownloadTools {
    private Callback.Cancelable cancelable;

    public Callback.Cancelable download(String md5, String path, ProgressCallBack callBack) {
        RequestParams requestParams = new RequestParams(path);
        final String downloadPath = FileUtils.getSDCardRootPath() + "/JnPayment/JnPaymentNewVersion.apk";
        requestParams.setAutoResume(true);
        requestParams.setSaveFilePath(downloadPath);
        cancelable = x.http().get(requestParams, callBack);
        return cancelable;
    }
}


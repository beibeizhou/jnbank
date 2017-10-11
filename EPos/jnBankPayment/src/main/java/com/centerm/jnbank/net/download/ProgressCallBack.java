package com.centerm.jnbank.net.download;

import org.xutils.common.Callback;

import java.io.File;

/**
 * Created by 王玮 on 2016/7/4.
 */
public abstract class ProgressCallBack implements Callback.ProgressCallback<File> {

    public abstract void onProgress(int progress, boolean isDownloading);

    @Override
    public void onWaiting() {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onLoading(long total, long current, boolean isDownloading) {
        long l = current * 100 / total;
        onProgress((int) l, isDownloading);
    }

    @Override
    public void onSuccess(File result) {

    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {

    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }
}

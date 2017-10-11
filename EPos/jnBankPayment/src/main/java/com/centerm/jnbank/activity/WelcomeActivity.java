package com.centerm.jnbank.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.appcloud.remote.IVersionInfoCallback;
import com.centerm.cpay.appcloud.remote.IVersionInfoProvider;
import com.centerm.cpay.appcloud.remote.VersionInfo;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.net.download.DownloadTools;
import com.centerm.jnbank.net.download.ProgressCallBack;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.FileMD5;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.view.numProgress.NumberProgressBar;
import com.centerm.jnbank.view.numProgress.OnProgressBarListener;

import org.apache.log4j.Logger;
import org.xutils.common.Callback;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import config.BusinessConfig;


public class WelcomeActivity extends BaseActivity {
    /**
     * 日志头信息：类名
     */
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private static final String cs = "JIEPOS";
    private NumberProgressBar progressBar;
    private TextView noticeTv;
    Timer timer;
    String status = "0";
    boolean isDownLoad = false;
    Logger logger = Logger.getLogger(WelcomeActivity.class);
    Callback.Cancelable cancelable;
    private Handler handler;

    @Override
    public int onLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    public void onInitView() {
        initHandler();
        x.view().inject(this);
        progressBar = (NumberProgressBar) findViewById(R.id.launcher_progressbar);
        noticeTv = (TextView) findViewById(R.id.launcher_notice);
        noticeTv.setText("正在初始化...");
        startTimer(165);//应用商店15秒内返回结果
        progressBar.setOnProgressBarListener(new OnProgressBarListener() {
            @Override
            public void onProgressChange(int current, int max) {
                if (!isDownLoad) {
                    if (current >= 95 && current < max) {
                        noticeTv.setText("应用启动中...");
                    }
                    if (current == max) {
                        stopTimer();
                        if (status.equals("1")) {//平台返回异常
                            ViewUtils.showToast(context, "应用启动失败");
                            activityStack.pop();
                            return;
                        } else if (status.equals("2")) {
//                            ViewUtils.showToast(context, "网络异常");
//                            activityStack.pop();
                        } else if (!status.equals("0")) {

                        } else {
                            //防止万一应用商店就是没应答导致返回了主界面又没有提示
//                            ViewUtils.showToast(context, "网络异常");
                            //activityStack.pop();
                        }
                        checkToLogin();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        check();
    }

    public void startTimer(int time) {
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setProgressBar(1);
            }
        }, 0, time);
    }

    public void stopTimer() {
        if (timer != null)
            timer.cancel();
    }

    public void setProgressBar(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.incrementProgressBy(progress);
            }
        });
    }

    /**
     * 校验版本
     */
    public void check() {
        logger.info("检测新版本");
        Intent intent = new Intent();
        intent.setAction("com.centerm.cpay.appcloud.REMOTE_SERVICE");
        intent.setPackage("com.centerm.cpay.applicationshop");
        boolean b = bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (!b) {
            logger.info("服务连接失败");
            checkToLogin();
        }
    }

    public void checkToLogin(){
        BusinessConfig config = BusinessConfig.getInstance();
        String today = new SimpleDateFormat("MMdd").format(new Date());
        String lastSignDate = config.getValue(context, BusinessConfig.Key.KEY_LAST_SIGNIN_DATE);
        logger.warn("上次签到日期：" + lastSignDate);
        //每日强制签到
        if (!today.equals(lastSignDate)) {
            config.setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
            BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.KEY_OPER_ID,null);
        }
        String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if (StringUtils.isStrNull(operId)) {
            Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivityForFirst.class);
            startActivity(loginIntent);
        } else {
            Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(mainIntent);
        }
        activityStack.pop();
    }

    private void initHandler() {
        if (handler == null) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    synchronized (this) {
                        switch (msg.what) {
                            case 0:
                                progressBar.setProgress(95);
                                break;
                            case 1:
                                noticeTv.setText("正在下载新版本...");
                                break;
                            case 2:
                                ViewUtils.showToast(context, "文件校验失败");
                                break;
                            default:
                                break;
                        }
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.info("服务已连接");
            IVersionInfoProvider versionInfoProvider = IVersionInfoProvider.Stub.asInterface(service);
            try {
                versionInfoProvider.getLatestVersion("com.centerm.epos.payment", new IVersionInfoCallback.Stub() {
                    @Override
                    public void onSuccess(VersionInfo info) throws RemoteException {
                        logger.info("【获取成功】" + info.toString());
                        status = "Ver" + info.getVersionCode();
                        final String downloadUrl = info.getDownloadUrl();
                        final VersionInfo versionInfo = info;
                        PackageManager manager = getApplicationContext().getPackageManager();
                        PackageInfo versioninfo = null;
                        try {
                            versioninfo = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        int versionCode = versioninfo.versionCode;
                        logger.info("【新版本：】" + info.getVersionCode() + " 【当前版本：】" + versionCode);
                        if (info.getVersionCode() > versionCode) {
                            if (info.getUpdateType()!=null && "1".equals(info.getUpdateType())){
                                logger.info("强制更新标志："+info.getUpdateType());
                                handler.sendEmptyMessage(1);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            stopTimer();
                                            download(versionInfo.getAppMd5(), downloadUrl);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }else if (info.getUpdateType()!=null&& "2".equals(info.getUpdateType())){
                                logger.info("强制更新标志："+info.getUpdateType());
                                stopTimer();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogFactory.showSelectDialog(context, "提示", "银行卡有新版本是否更新?"
                                                ,new AlertDialog.ButtonClickListener() {
                                                    @Override
                                                    public void onClick(AlertDialog.ButtonType button, View v) {
                                                        switch (button) {
                                                            case POSITIVE:
                                                                handler.sendEmptyMessage(1);
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            stopTimer();
                                                                            download(versionInfo.getAppMd5(), downloadUrl);
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }).start();
                                                                break;
                                                            case NEGATIVE:
                                                                progressBar.setProgress(100);
                                                                String fileName = FileUtils.getSDCardRootPath() + "/JnPayment/JnPaymentNewVersion.apk";
                                                                File file = new File(fileName);
                                                                if (file.exists()) {
                                                                    file.delete();
                                                                }
                                                                break;
                                                        }
                                                    }
                                                },false);
                                    }
                                });

                            }else if (info.getUpdateType()==null){
                                logger.info("强制更新标志："+info.getUpdateType());
                                handler.sendEmptyMessage(1);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            stopTimer();
                                            download(versionInfo.getAppMd5(), downloadUrl);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }

                        } else {
                            handler.sendEmptyMessage(0);
                            String fileName = FileUtils.getSDCardRootPath() + "/JnPayment/JnPaymentNewVersion.apk";
                            File file = new File(fileName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }

                    @Override
                    public void onError(int errorCode, String errorInfo) throws RemoteException {
                        logger.info("【获取失败】错误码：" + errorCode + "  错误信息：" + errorInfo);
                        if(errorCode == 0 && "网络异常".equals(errorInfo)){
                            handler.sendEmptyMessage(0);
                            status = "2";
                        }else {
                            handler.sendEmptyMessage(0);
                            status = "3";
                        }
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.warn("服务已断开");
        }
    };



    public void download(final String md5, final String path) {
        isDownLoad = true;
        String localFile = FileUtils.getSDCardRootPath() + "/JnPayment/";
        File f = new File(localFile);
        if (!f.exists()) {
            f.mkdirs();
        }
        final String localPath = FileUtils.getSDCardRootPath() + "/JnPayment/JnPaymentNewVersion.apk";
        File file = new File(localPath);
        if (file.exists()) {//存在，则直接安装
            try {
                if (md5.equals(FileMD5.getFileMD5String(file.getPath()))) {
                    String fileName = FileUtils.getSDCardRootPath() + "/JnPayment/JnPaymentNewVersion.apk";
                    Uri uri = Uri.fromFile(new File(fileName));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    startActivity(intent);
                    activityStack.pop();
                    return;
                } else {
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        cancelable = new DownloadTools().download(md5, path, new ProgressCallBack() {
            @Override
            public void onWaiting() {
                progressBar.setProgress(0);
            }

            @Override
            public void onProgress(int progress, boolean isDownloading) {
                progressBar.setProgress(progress);
            }

            @Override
            public void onSuccess(File result) {
                super.onSuccess(result);
                File file = new File(localPath);
                try {
                    if (md5.equals(FileMD5.getFileMD5String(localPath))) {
                        Uri uri = Uri.fromFile(new File(localPath));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                        startActivity(intent);
                        activityStack.pop();
                    } else {
                        handler.sendEmptyMessage(2);
                        if (file.exists()) {
                            file.delete();
                        }
                        activityStack.pop();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                super.onError(ex, isOnCallback);
                DialogFactory.showMessageDialog(context, "提示", "下载失败", new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                activityStack.pop();
                                break;
                            case NEGATIVE:
                                activityStack.pop();
                                break;
                        }
                    }
                });
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (isDownLoad) {
            DialogFactory.showSelectDialog(context, "提示", "正在下载，是否退出", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            String fileName = FileUtils.getSDCardRootPath() + "/JnPayment/JnPaymentNewVersion.apk";
                            File file = new File(fileName);
                            if (file.exists()) {
                                file.delete();
                            }
                            activityStack.pop();
                            break;
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        unbindService(connection);
        //DeviceFactory.getInstance().release();
        if (cancelable != null) {
            cancelable.cancel();
        }
        super.onDestroy();
    }
}
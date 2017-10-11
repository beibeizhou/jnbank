package com.centerm.jnbank.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.jnbank.bean.ElecSignInfo;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.net.htttp.DefaultHttpClient;
import com.centerm.jnbank.net.htttp.request.ESignRequest;
import com.centerm.jnbank.utils.ImageUtils;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import config.Config;


/**
 * Created by ysd on 2016/11/29.
 */

public class SendSignService extends Service {
    private Timer timer;
    private Handler handler;
    private List<ElecSignInfo> infos;
    private Logger logger = Logger.getLogger(SendSignService.class);
    private CommonDao<ElecSignInfo> commonDao;

    @Override
    public void onCreate() {
        super.onCreate();
        logger.debug("电子签名服务启动");
        DbHelper dbHelper = new DbHelper(SendSignService.this);
        commonDao = new CommonDao<>(ElecSignInfo.class, dbHelper);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    for (ElecSignInfo info :
                            infos) {
                        sendSignMms(info);
                    }
                }
            }
        };
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    infos = commonDao.queryBuilder().where().lt("retryCount", 3).query();
                    if (null != infos && infos.size() > 0) {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    } else {
                        logger.debug("未找到上传失败的电子签名信息");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, Config.UPLOAD_FAIL_ELEC_SIGN_INTERVAL, Config.UPLOAD_FAIL_ELEC_SIGN_INTERVAL);
    }

    private void sendSignMms(final ElecSignInfo info) {
        logger.debug("进入电子签名上送方法");
        String path = info.getPicName();
        try {
            if (FileUtils.getFileSize(path) > 0) {
                FileInputStream inputStream = new FileInputStream(path);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                String pic = ImageUtils.bitmaptoString(bitmap, 100);
                bitmap.recycle();
                final ESignRequest request = new ESignRequest(info.getMchtId(), info.getTermId(), info.getTransDate(), info.getTransTime(), info.getTransNum(), pic);
                //request.setUrl(Settings.getSlipUploadUrl(SendSignService.this));
                ResponseHandler handler = new ResponseHandler() {
                    @Override
                    public void onSuccess(String statusCode, String msg, byte[] data) {
                        try {
                            JSONObject object = new JSONObject(msg);
                            if (object.has("success") && object.get("success") instanceof Boolean) {
                                boolean result = (boolean) object.get("success");
                                String mms = (String) object.get("msg");
                                logger.debug("签购单上送返回信息：" + mms);
                                if (!result) {
                                    int count = info.getRetryCount();
                                    info.setRetryCount(++count);
                                    commonDao.update(info);
                                } else {
                                    logger.debug("【重新上送】电子签名上送成功");
                                    commonDao.delete(info);
                                }
                            } else {
                                logger.debug("【重新上送】电子签名上送成功");
                                commonDao.delete(info);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(String code, String msg, Throwable error) {
                        int count = info.getRetryCount();
                        info.setRetryCount(++count);
                        commonDao.update(info);
                        logger.debug("【重新上送】电子签名上送失败，订单流水为：" + info.getPicName() + "失败次数为：" + info.getRetryCount());
                    }
                };
                DefaultHttpClient.getInstance().post(SendSignService.this, request, handler);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}

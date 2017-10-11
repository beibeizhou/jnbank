package com.centerm.jnbank.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import config.Config;




/**
 * author:wanliang527</br>
 * date:2016/11/17</br>
 */

public class LaunchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long time1 = System.currentTimeMillis();
        try {
            String path = Config.Path.CODE_PATH;
            File fileDir = new File(path);
            if (!fileDir.exists()) {
                FileUtils.createDirectory(fileDir.toString());
            }
            String fileName = Config.Path.CODE_FILE_NAME;
            File file = new File(fileDir + File.separator + fileName);
            if (!file.exists()) {
                BitmapDrawable code = (BitmapDrawable) context.getResources().getDrawable(R.drawable.erweima);
                Bitmap codeBitmap = code.getBitmap();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                codeBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                bos.flush();
                bos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*boolean isTmkExist = Settings.hasTmk(context);
        String operId = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if (isTmkExist) {
            if (TextUtils.isEmpty(operId)
                    || "99".equals(operId)
                    || "00".equals(operId)) {
                //无普通操作员登录时，跳转到登录界面
                logger.info("已登录用户非普通操作员，跳转登录界面");
                jumpToLogin();
            } else {
                logger.info("普通操作员已登录，跳转主界面");
                jumpToMain();
            }
        } else {
            logger.info("未检测到主密钥，跳转登录界面");
            jumpToLogin();
        }
        long time2 = System.currentTimeMillis();
        Log.d("加载页操作时间：", (time2 - time1)+"");*/
        jumpToMain();
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_launch;
    }

    @Override
    public void onInitView() {
    }

    private void jumpToMain() {
        Intent intent = new Intent(context, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activityStack.remove(LaunchActivity.class);
            }
        }, 300);
        startActivity(intent);
    }

}

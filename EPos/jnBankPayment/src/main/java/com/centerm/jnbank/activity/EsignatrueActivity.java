package com.centerm.jnbank.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.view.HandwrittenPad;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import config.BusinessConfig;
import config.Config;

import static com.centerm.jnbank.common.TransDataKey.iso_f11;

/**
 * 电子签名页面
 * Created by ysd on 2016/11/25.
 */

public class EsignatrueActivity extends BaseTradeActivity implements View.OnClickListener {

    private int resignCount = 0;
    private int maxSignCnt;
    private HandwrittenPad writePad;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openPageTimeout(true);
    }

    @Override
    protected void onPause() {
        writePad.clear();
        super.onPause();
        closePageTimeout();
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_electronic_sign;
    }

    @Override
    public void onInitView() {
        setTitle("电子签名");
        hideBackBtn();
        findViewById(R.id.resign_btn).setOnClickListener(this);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
        writePad = (HandwrittenPad) findViewById(R.id.hand_write_pad);
        writePad.setSignature("请在此区域签名");
        String timeOut = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.PARAM_SIGN_OUT_TIME);
        String maxSignStr = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.PARAM_MAX_RESIGN_CNT);
        maxSignCnt = Integer.parseInt(maxSignStr);
        long timeOutLong = Integer.parseInt(timeOut) * 1000;
        pageTimeout = timeOutLong;
        resignCount = 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resign_btn:
                if (!writePad.isSign()) {
                    return;
                }
                if (++resignCount <= maxSignCnt) {
                    writePad.clear();
                } else {
                    DialogFactory.showMessageDialog(context, "提示", "重签次数超限，请手动签名！", new AlertDialog.ButtonClickListener() {
                        @Override
                        public void onClick(AlertDialog.ButtonType button, View v) {
                            jumpToNext();
                        }
                    });
                }
                break;
            case R.id.confirm_btn:
                if (writePad.isSign()) {
                    String flagTipComfirmSign = BusinessConfig.Key.FLAG_TIP_COMFIRM_SIGN;
//                    boolean isTipComfirm = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.FLAG_TIP_COMFIRM_SIGN).equals("1") ? true : false;
                    boolean isTipComfirm =true;
                    if (isTipComfirm) {
                        DialogFactory.showSelectDialog(context, "提示", "是否确认签名？", new AlertDialog.ButtonClickListener() {
                            @Override
                            public void onClick(AlertDialog.ButtonType button, View v) {
                                switch (button) {
                                    case POSITIVE:
                                        saveESignAndGoNext(writePad.getCachebBitmap());
                                        break;
                                }
                            }
                        });
                    } else {
                        saveESignAndGoNext(writePad.getCachebBitmap());
                    }

                } else {
                    ViewUtils.showToast(context, R.string.tip_sign);
                }
                break;
        }
    }

    private void saveESignAndGoNext(Bitmap bitmap) {
        //保存Bitmap,供下个界面使用
        if (!FileUtils.hasSDCard()) {
            ViewUtils.showToast(context, R.string.tips_nosdcard);
        } else {
            File fileDir = new File(Config.Path.SIGN_PATH);
            if (!fileDir.exists()) {
                FileUtils.createDirectory(fileDir.toString());
            }
            String fileName = BusinessConfig.getInstance().getBatchNo(context) + "_" + dataMap.get(iso_f11) + ".png";
            File file = new File(fileDir + File.separator + fileName);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                ViewUtils.showToast(context, R.string.tips_errorwhilesigning + e.toString());
            }
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap = cQuality(bitmap);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap.recycle();
        }
        jumpToNext();
    }

    private static Bitmap cQuality(Bitmap bitmap) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int beginRate = 20;
        // 第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差 ，第三个参数：保存压缩后的数据的流
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, bOut);
        while (bOut.size() / 1024 / 1024 > 100) { // 如果压缩后大于100Kb，则提高压缩率，重新压缩
            beginRate -= 10;
            bOut.reset();
            bitmap.compress(Bitmap.CompressFormat.PNG, beginRate, bOut);
        }
        ByteArrayInputStream bInt = new ByteArrayInputStream(bOut.toByteArray());
        Bitmap newBitmap = BitmapFactory.decodeStream(bInt);
        if (newBitmap != null) {
            return newBitmap;
        } else {
            return bitmap;
        }
    }


    @Override
    public void onBackPressed() {
        DialogFactory.showMessageDialog(context, null, getString(R.string.tips_please_finish_sign), new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
            }
        });
    }
}

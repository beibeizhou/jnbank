package com.centerm.jnbank.activity.qrcode;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumDeviceType;
import com.centerm.cpay.midsdk.dev.define.IBarCodeScanner;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.utils.ViewUtils;

import static com.centerm.jnbank.common.TransCode.SCAN_PAY_ALI;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_WEI;


/**
 * author:wanliang527</br>
 * date:2017/1/1</br>
 */

public class ScanQrCodeActivity extends BaseTradeActivity {

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_scan_qr_code2;
    }

    @Override
    public void onInitView() {
        openCamera();
    }
    private void openCamera() {
        IBarCodeScanner dev = (IBarCodeScanner) DeviceFactory.getInstance().getDevice(EnumDeviceType
                .BAR_CODE_SCANNER_DEV);
        if (dev == null) {
            return;
        }
        dev.scanBarCode(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //显示扫描到的内容
        if (data == null) {
            activityStack.pop();
            return;
        }
        String resultString = data.getStringExtra("txtResult");
        if (!TextUtils.isEmpty(resultString)) {
        /*    if (content.matches("[0-9]+")) {
                dataMap.put(JsonKey.auth_code, content);
                jumpToNext();
            } else {
                ViewUtils.showToast(context,"只支持数字！");
            }*/
            logger.info("扫码结果:" + resultString);
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
//			bundle.putParcelable("bitmap", barcode);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
            String txnWay = "";
            if (transCode.equals(SCAN_PAY_WEI)) {
                txnWay = "W";
            } else if (transCode.equals(SCAN_PAY_ALI)) {
                txnWay = "A";
            }
//            else if (transCode.equals(SCAN_PAY_SFT)) {
//                txnWay = "SFT01";
//            }
            //47域，扫码支付交易上送通道类型和通道授权码
//            dataMap.put(TransDataKey.iso_f47, "TXNWAY=" + txnWay + "|WXCODE=" + resultString);
            dataMap.put(TransDataKey.iso_f62, txnWay + resultString);
            activityStack.pop();
            jumpToNext();
        } else {
            ViewUtils.showToast(context,"扫码失败！");
        }
    }
}

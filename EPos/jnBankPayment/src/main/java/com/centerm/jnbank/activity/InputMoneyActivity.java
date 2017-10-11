package com.centerm.jnbank.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.base.MenuActivity;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.view.NumberPad;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransCode.REFUND;
import static com.centerm.jnbank.common.TransCode.SALE;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_ALI;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_SFT;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_WEI;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_S;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_W;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_Z;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.key_scan_type;

/**
 * 输入金额界面
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class InputMoneyActivity extends BaseTradeActivity {

    private NumberPad numberPad;
    private TextView amtShow;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_input_money;

    }

    @Override
    public void onInitView() {
        //修改UI表现，将除消费以外的所有银行卡业务放入右键入口
        if(SALE.equals(transCode) || SCAN_PAY_WEI.equals(transCode)
                || SCAN_PAY_ALI.equals(transCode)
                || SCAN_PAY_SFT.equals(transCode))
            showRightButton("其它");
        //添加扫码判断，扫码支付时改为两个button用于选择正扫还是反扫
        if(SCAN_PAY_WEI.equals(transCode)
                || SCAN_PAY_ALI.equals(transCode)
                || SCAN_PAY_SFT.equals(transCode)) {

            Button buttonOri = (Button) findViewById(R.id.positive_btn);
            buttonOri.setVisibility(View.GONE);
            LinearLayout btn_qrcode =  (LinearLayout) findViewById(R.id.btn_qrcode);
            btn_qrcode.setVisibility(View.VISIBLE);

           /* Button buttonPositive = (Button) findViewById(R.id.positive_scan_btn);
            buttonPositive.setVisibility(View.VISIBLE);
            Button buttonNegative = (Button) findViewById(R.id.negative_scan_btn);
            buttonNegative.setVisibility(View.VISIBLE);*/
        }
        numberPad = (NumberPad) findViewById(R.id.number_pad_show);
        amtShow = (TextView) findViewById(R.id.money_show);
        numberPad.bindShowView(amtShow);
        if (entryFlag) {
            String tempAmt = dataMap.get(iso_f4);
            if (!TextUtils.isEmpty(tempAmt)) {
                double amt = DataHelper.parseIsoF4(tempAmt);
                String amtText = DataHelper.formatAmountForShow(amt+"");
                amtShow.setText(amtText);
                amtShow.setEnabled(false);
                numberPad.setPadEnable(false);
                if (TransCode.SALE.equals(transCode)||TransCode.AUTH.equals(transCode)) {
                    jumpToNext();
                    activityStack.pop();
                }
                if (TransCode.SCAN_PAY_SFT.equals(transCode)||TransCode.SCAN_PAY_ALI.equals(transCode)||TransCode.SCAN_PAY_WEI.equals(transCode)) {
                    String scanType = dataMap.get(key_scan_type);
                    if ("1".equals(scanType)) {//被扫
                        jumpToNext("2");
                    } else {//主扫
                        jumpToNext("1");
                    }
                    activityStack.pop();
                }
            }
        }
    }

    private void jumpToMain() {
        Intent intent = new Intent(context, MenuActivity.class);
        int flag = 3;
        if(SCAN_PAY_WEI.equals(transCode))
            flag = 4;
        else if(SCAN_PAY_ALI.equals(transCode))
            flag = 5;
        else if(SCAN_PAY_SFT.equals(transCode))
            flag = 6;
        intent.putExtra(KEY_USER_FLAG, flag);
        activityStack.pop();
        startActivity(intent);
    }

    @Override
    public void onRightButtonClick(View view) {
        jumpToMain();
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        receiver = new MyReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, PbocEventAction.getAllActions());
    }

    @Override
    public void onBackPressed() {
        activityStack.backTo(MainActivity.class);
        if (pbocService != null) {
            pbocService.abortProcess();
        }
    }
//收款
    public void onConfirmClick(View view) {
        if (SCAN_REFUND_W.equals(transCode) || SCAN_REFUND_Z.equals(transCode) || SCAN_REFUND_S.equals(transCode)) {
            if (CommonUtils.isOneFastClick()) {
                logger.debug("==>快速点击事件，不响应！");
                return;
            }
        }
        final String amt = amtShow.getText().toString();
        if (Double.valueOf(amt) == 0) {
            ViewUtils.showToast(context, R.string.tip_input_money2);
            return;
        }
        String mostRefund = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.PARAM_MOST_REFUND);
//        double refundAmt = Double.parseDouble(mostRefund);
        double refundAmt = DataHelper.formatDouble(Double.parseDouble(mostRefund)/100.00);
        if (REFUND.equals(transCode) && Double.valueOf(amt) > refundAmt) {
            ViewUtils.showToast(context, R.string.tip_refund_over_limited);
            return;
        }
        if (REFUND.equals(transCode)) {
            DialogFactory.showSelectDialog(context, "请确认", "退货金额(RMB)：" + amt, new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    if (button.equals(AlertDialog.ButtonType.POSITIVE)) {
                        dataMap.put(iso_f4, amt);
                        jumpToNext();
                    }
                }
            });
        } else {
            dataMap.put(iso_f4, amt);
            /*if(view.getId() == R.id.positive_scan_btn) {
                jumpToNext("1");
            } else if(view.getId() == R.id.negative_scan_btn){
                jumpToNext("2");
            }*/
            if(view.getId() == R.id.btn_wechat) {
                transCode = TransCode.SCAN_PAY_WEI;
                jumpToNext("1");
            }else if(view.getId() == R.id.btn_alipay) {
                transCode = TransCode.SCAN_PAY_ALI;
                jumpToNext("1");
            }
            else {
                BusinessConfig config = BusinessConfig.getInstance();
                boolean is_cancel_psw =  config.getParam(context, BusinessConfig.Key.FLAG_CANCEL_PSW).equals("1")?true:false;
                boolean is_complete_psw =  config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_PSW).equals("1")?true:false;
                if(TransCode.CANCEL.equals(transCode) && !is_cancel_psw) {
                    //消费撤销可能无需输入密码
                    jumpToNext("2");
                } else if (TransCode.AUTH_COMPLETE.equals(transCode) && !is_complete_psw) {
                    //消费撤销可能无需输入密码
                    jumpToNext("2");
                } else {
                    jumpToNext();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openPageTimeout(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closePageTimeout();
    }

    private class MyReceiver extends PbocEventReceiver {

        @Override
        public void onImportAmount() {

        }

        @Override
        public void onImportPin() {

        }

        @Override
        public void onRequestOnline() {

        }

        @Override
        public void onConfirmCardNo(String cardNo) {

        }
    }
}
package com.centerm.jnbank.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.cpay.midsdk.dev.define.pinpad.PinListener;
import com.centerm.cpay.midsdk.dev.define.pinpad.PinParams;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;

import config.BusinessConfig;

import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.getAllActions;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_CARD_CONFIRM_RESULT;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_PIN;
import static com.centerm.jnbank.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.jnbank.common.TransDataKey.FLAG_REQUEST_UNIONCARD;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f52;
import static com.centerm.jnbank.common.TransDataKey.keyFlagNoPin;

/**
 * 输入密码界面
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class   InputPwdActivity extends BaseTradeActivity {

    private CheckBox[] indicatorArr;
    private TextView transType, transAmt, transCardNo;
    private PinParams pinParams;
    private int pinLen;
    private boolean gotTerminatedEvent;
    private int retryTimes;

    private PinListener pinListener = new PinListener() {
        @Override
        public void onReadingPin(int i) {
            pinLen = i;
            changeIndicator(pinLen);
        }

        @Override
        public void onError(int i, String s) {
        }

        @Override
        public void onConfirm(byte[] bytes) {
            if (pinLen != 0 && pinLen < 4) {
                //密码位数不足4位
                if (retryTimes++ < BusinessConfig.PASSWD_RETRY_TIMES) {
                    ViewUtils.showToast(context, R.string.tip_pwd_length_illegal);
                    beginGetPin();
                } else {
                    //输错次数超限，自动返回到主界面
                    ViewUtils.showToast(context, R.string.tip_pwd_times_over_limited);
                    activityStack.backTo(MainActivity.class);
                }
            } else {
                //服务点输入方式码
                String entryMode = dataMap.get(iso_f22);
                if (pinLen == 0) {
                    entryMode += "2";
                    dataMap.put(keyFlagNoPin, "1");
                } else {
                    entryMode += "1";
                    dataMap.put(iso_f52, HexUtils.bytesToHexString(bytes));
                }
                dataMap.put(iso_f22, entryMode);
                if (isICInsertTrade() && !"1".equals(dataMap.get(FLAG_IMPORT_PIN))) {
                    delayJumpToNext();
                } else {
                    jumpToNext();
                }
            }
        }

        private void delayJumpToNext() {
            DialogFactory.showLoadingDialog(context, "处理中，请稍候...");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DialogFactory.hideAll();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    jumpToNext();
                }
            }, 1000);
        }

        @Override
        public void onCanceled() {
            try {
                if (pbocService != null) {
                    pbocService.abortProcess();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!gotTerminatedEvent) {
                activityStack.backTo(MainActivity.class);
            }
            /*switch (transCode) {
                case TransCode.SALE:
                    //消费业务退货到输入金额界面
                    activityStack.backTo(InputMoneyActivity.class);
                    break;
                default:
                    activityStack.backTo(MainActivity.class);
                    break;
            }*/
        }

        @Override
        public void onTimeout() {
            jumpToResultActivity(StatusCode.PIN_TIMEOUT);
        }
    };

/*    *//**
     * 延迟跳转下一个界面，主要是为了解决快速确认密码时发生的卡死在联机界面的问题
     *//*
    private void delayJumpToNext() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gotTerminatedEvent) {
                    jumpToNext();
                } else {
                    logger.warn("内核已终止，密码确认不生效！");
                }
            }
        }, 200);
    }*/

    @Override
    public int onLayoutId() {
        return R.layout.activity_input_pwd;
    }

    @Override
    public void onInitView() {
        hideBackBtn();
        receiver = new MyReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, getAllActions());
        indicatorArr = new CheckBox[]{
                (CheckBox) findViewById(R.id.indicator1),
                (CheckBox) findViewById(R.id.indicator2),
                (CheckBox) findViewById(R.id.indicator3),
                (CheckBox) findViewById(R.id.indicator4),
                (CheckBox) findViewById(R.id.indicator5),
                (CheckBox) findViewById(R.id.indicator6)};
        transType = (TextView) findViewById(R.id.trans_type_show);
        transAmt = (TextView) findViewById(R.id.trans_money_show);
        transCardNo = (TextView) findViewById(R.id.trans_card_show);
        transType.setText(getString(TransCode.codeMapName(transCode)));
        if (!TransCode.BALANCE.equals(transCode)) {
            transAmt.setText(DataHelper.formatAmountForShow(dataMap.get(iso_f4)));
        } else {
            findViewById(R.id.trans_money_block).setVisibility(View.GONE);
        }
        String isShieldCard = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_SHIELD_CARD);
        if (!TransCode.AUTH.equals(transCode)) {
            transCardNo.setText(DataHelper.shieldCardNo(TextUtils.isEmpty(dataMap.get(iso_f2))?dataMap.get(iso_f2_result):dataMap.get(iso_f2)));
        } else {
            if ("1".equals(isShieldCard)) {
                transCardNo.setText(DataHelper.shieldCardNo(TextUtils.isEmpty(dataMap.get(iso_f2))?dataMap.get(iso_f2_result):dataMap.get(iso_f2)));
            } else {
                transCardNo.setText(TextUtils.isEmpty(dataMap.get(iso_f2))?dataMap.get(iso_f2_result):dataMap.get(iso_f2));
            }

        }
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        pinParams = new PinParams();
        if(TextUtils.isEmpty(dataMap.get(iso_f2))){
            logger.info("setPan2:" + dataMap.get(iso_f2_result));
            pinParams.setPan(dataMap.get(iso_f2_result));
        }else {
            pinParams.setPan(dataMap.get(iso_f2));
        }
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        if ("1".equals(dataMap.get(FLAG_IMPORT_AMOUNT))) {
            pbocService.importAmount(dataMap.get(iso_f4));
            dataMap.remove(FLAG_IMPORT_AMOUNT);
        }
        if ("1".equals(dataMap.get(FLAG_IMPORT_CARD_CONFIRM_RESULT))) {
            pbocService.importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
            dataMap.remove(FLAG_IMPORT_CARD_CONFIRM_RESULT);
        }
        delayBeginGetPin();
    }

    private void delayBeginGetPin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gotTerminatedEvent) {
                    beginGetPin();
                } else {
                    logger.warn("交易终止，不进行输PIN操作");
                }
            }
        }, 300);
    }

    private void delayCancelGetPin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
                if (pinPadDev != null) {
                    pinPadDev.cancelGetPin();
                }
            }
        }, 300);
    }

    @Override
    public void onBackPressed() {
        if (pbocService != null) {
            pbocService.abortProcess();
        }
        delayCancelGetPin();
        activityStack.backTo(MainActivity.class);
    }

    private void beginGetPin() {
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        if (pinPadDev != null) {
            pinPadDev.getPin(pinParams, pinListener);
        } else {
            logger.warn("密码键盘获取为空，无法弹出密码键盘");
        }
    }

    public void onPwdBlockClick(View view) {
        beginGetPin();
    }

    public void onConfirmClick(View view) {
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        if (pinPadDev != null) {
            pinPadDev.confirmPin();
        }
        String entryMode = dataMap.get(iso_f22);
        entryMode += "2";
        dataMap.put(keyFlagNoPin, "1");
        dataMap.put(iso_f22, entryMode);
        jumpToNext();
    }

    private void changeIndicator(int pinLen) {
        switch (pinLen) {
            case 0:
                for (int i = 0; i < 6; i++) {
                    indicatorArr[i].setChecked(false);
                }
                break;
            case 1:
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 6:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                break;
        }
    }

    private class MyReceiver extends PbocEventReceiver {

        @Override
        public void onImportAmount() {
            dataMap.put(FLAG_IMPORT_AMOUNT, "1");
        }

        @Override
        public void onImportPin() {
            dataMap.put(FLAG_IMPORT_PIN, "1");
        }

        @Override
        public void onRequestOnline() {
            dataMap.put(FLAG_REQUEST_ONLINE, "1");
        }

        @Override
        public void onConfirmCardNo(String cardNo) {
        }

        @Override
        public boolean onTradeTerminated() {
            //很偶然的情况，内核在导入卡号信息确认结果之后，会上报内核终止事件
            //如果这时候恰好用户点击密码键盘的确认按钮，会造成卡死在联机界面的问题
            //这里采用的解决方法是：在用户点击密码键盘的确认按钮时，延迟跳转到下一个界面
            //如果在此之前已经接收到交易终止的事件，那就不进行跳转
            gotTerminatedEvent = true;
            delayCancelGetPin();
            return false;
        }
    }


/*
    private class PbocEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.debug(InputPwdActivity.this.toString() + "接收到广播：" + action);
            IPbocService pbocService = null;
            try {
                pbocService = DeviceFactory.getInstance().getPbocService();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ACTION_REQUEST_AMOUNT.equals(action)) {
                //请求导入金额
                String amt = dataMap.get(iso_f4);
                pbocService.importAmount(amt);
            } else if (ACTION_REQUEST_TIPS_CONFIRM.equals(action)) {
                //请求提示信息确认
                pbocService.importResult(EnumPbocResultType.MSG_CONFIRM, true);
            } else if (ACTION_REQUEST_CARD_INFO_CONFIRM.equals(action)) {
                //请求卡号信息确认
                pbocService.importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
            } else if (ACTION_REQUEST_USER_AUTH.equals(action)) {
                //请求用户身份认证
                pbocService.importResult(EnumPbocResultType.USER_AUTH, true);
            } else if (ACTION_REQUEST_ONLINE.equals(action)) {
                //请求联机（非接流程会在请求输PIN前出现该事件）
                //此时需要告诉输密界面，内核已经上报过请求联机的事件，输完密码后可直接进行联机，无需等待内核联机事件了
                tempMap.put(KEY_ONLINE_REQUEST_EVENT, "1");
                Map<String, String> cardInfo = pbocService.readKernelData(getCardInfoTags());
                logger.info("IC卡卡片信息读取成功：" + cardInfo.toString());
                String tag57 = cardInfo.get("57");
                dataMap.put(iso_f2, tag57.split("D")[0]);//主账号
                dataMap.put(iso_f35, tag57);//2磁
                String expiry = cardInfo.get("5F24");
                if (expiry != null && expiry.length() == 6) {
                    expiry = expiry.substring(0, 4);
                }
                dataMap.put(iso_f14, expiry);
                String f14 = dataMap.get(iso_f14);
                if (f14 != null && f14.length() >= 4) {
                    dataMap.put(iso_f14, f14.substring(0, 4));
                }
                dataMap.put(iso_f23, cardInfo.get("5F34"));
                String iso55;//55域数据
                String iso55_reserve;//用于冲正的55域数据
                if (TransCode.BALANCE.equals(transCode)
                        || TransCode.SALE.equals(transCode)
                        || TransCode.AUTH.equals(transCode)) {
                    //IC卡交易55域数据用法1，用于余额查询、消费、预授权
                    iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());
                } else {
                    //IC卡交易55域数据用法3，用于消费撤销、消费撤销冲正、退货、预授权撤销、
                    //预授权撤销冲正、预授权完成（请求）、预授权完成（通知）、预授权完成（请求）冲正
                    //预授权完成撤销、预授权完成撤销冲正
                    iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags3());
                }
                if (TransCode.SALE.equals(transCode)
                        || TransCode.AUTH.equals(transCode)) {
                    //IC卡交易55域数据用法2，用于消费冲正、预授权冲正
                    iso55_reserve = pbocService.readTlvKernelData(EmvTag.getF55Tags2());
                } else {
                    iso55_reserve = pbocService.readTlvKernelData(EmvTag.getF55Tags3());
                }
                //退货交易读取到相关信息后，可直接终止PBOC流程
                if (TransCode.REFUND.equals(transCode)) {
                    pbocService.abortProcess();
                }
                dataMap.put(iso_f55, iso55);
                dataMap.put(iso_f55_reverse, iso55_reserve);//读取用于冲正的55域数据
                if (TransCode.VOID.equals(transCode) && BusinessConfig.FLAG_VOID_NEED_PIN) {
                    //消费撤销需要输入密码
                    pbocService.abortProcess();
                    jumpToNext("2");
                } else {
                    jumpToNext();
                }
            } else if (ACTION_REQUEST_PIN.equals(action)) {
                //请求输入密码（插卡交易时会先要求输PIN，导入PIN后再要求联机）
            } else if (ACTION_TRADE_REFUSED.equals(action)) {
                //交易拒绝
                ViewUtils.showToast(context, R.string.tip_trade_refused);
                activityStack.backTo(InputMoneyActivity.class);
                pbocService.stopProcess();
            } else if (ACTION_NEED_CHANGE_READ_CARD_TYPE.equals(action)) {
                //交易降级
                ViewUtils.showToast(context, R.string.tip_trade_fallback);
                activityStack.backTo(InputMoneyActivity.class);
                pbocService.stopProcess();
            } else if (ACTION_ERROR.equals(action)) {
                //内核异常
                ViewUtils.showToast(context, R.string.tip_card_interaction_failed);
                activityStack.backTo(InputMoneyActivity.class);
                pbocService.stopProcess();
            }
        }
    }
*/
}

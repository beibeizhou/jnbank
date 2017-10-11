package com.centerm.jnbank.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.base.MenuActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.printer.ShengPayPrinter;
import com.centerm.jnbank.utils.DataHelper;

import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.StatusCode.PIN_TIMEOUT;
import static com.centerm.jnbank.common.TransCode.DISCOUNT_INTERGRAL;
import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f12;
import static com.centerm.jnbank.common.TransDataKey.iso_f13;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f44;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;
import static com.centerm.jnbank.common.TransDataKey.iso_f61;
import static com.centerm.jnbank.common.TransDataKey.keyBalanceAmt;
import static com.centerm.jnbank.common.TransDataKey.key_oriTransTime;
import static com.centerm.jnbank.common.TransDataKey.key_param_update;


/**
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class ResultActivity extends BaseTradeActivity {

    private boolean isSuccess;
    private LinearLayout itemContainer;
    private ImageView flagIconShow;
    private TextView flagTextShow;
    private Button returnBtn;
    private String respCode, respMsg;
    private CommonDao<TradeInfo> commonDao;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        DbHelper dbHelper = new DbHelper(context);
        commonDao = new CommonDao<>(TradeInfo.class, dbHelper);
        String f39 = tempMap.get(iso_f39);
        respCode = tempMap.get(TransDataKey.key_resp_code);
        if(!respCode.equals(StatusCode.PIN_TIMEOUT.getStatusCode())){
            isSuccess = "00".equals(f39) || "0".equals(f39)|| "10".equals(f39)|| "11".equals(f39)|| "A2".equals(f39)|| "A4".equals(f39)|| "A5".equals(f39)|| "A6".equals(f39) ||"58".equals(f39);
        }
        respMsg = tempMap.get(TransDataKey.key_resp_msg);
        //44域
        StatusCode status = StatusCode.codeMap(respCode);
        //如果报自定义的错误，就不让44域的值覆盖
        if (null == status) {
            if(!TextUtils.isEmpty(tempMap.get(iso_f44))) {
                logger.info("[44域提示信息]:"+tempMap.get(iso_f44));
                respMsg = tempMap.get(iso_f44).replaceAll(" ","");
            }
        }
        //是扫码支付就改一个值，或者支付宝或者微信或者xxx
        if (transCode.equals(TransCode.SCAN_SERCH)) {
            String tempType =tempMap.get(iso_f47).split("=|\\|")[1];
            if(tempType.contains("ZFB01")){
                transCode = TransCode.SCAN_PAY_ALI;
            } else if(tempType.contains("TX01")){
                transCode = TransCode.SCAN_PAY_WEI;
            } else if(tempType.contains("SFT01")){
                transCode = TransCode.SCAN_PAY_SFT;
            }
        }
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_result;
    }

    @Override
    public void onInitView() {
        hideBackBtn();
        itemContainer = (LinearLayout) findViewById(R.id.result_info_block);
        flagIconShow = (ImageView) findViewById(R.id.result_pic_show);
        flagTextShow = (TextView) findViewById(R.id.result_text_show);
        returnBtn = (Button) findViewById(R.id.return_btn);
        if (isSuccess) {
            flagIconShow.setImageResource(R.drawable.pic_success);
            flagTextShow.setText(R.string.tip_trade_success);
            switch (transCode) {
                case TransCode.BALANCE:
                    flagTextShow.setText(R.string.tip_query_success);
                    String cardNum = TextUtils.isEmpty(tempMap.get(iso_f2))?tempMap.get(iso_f2_result):tempMap.get(iso_f2);
                    addItemView(getString(R.string.label_balance), "RMB " + tempMap.get(keyBalanceAmt), true);//可用余额
                    addItemView(getString(R.string.label_card_no), DataHelper.shieldCardNo(cardNum), false);//卡号
                    break;
                case TransCode.SIGN_IN:
                    flagTextShow.setText(R.string.tip_sign_in_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.OBTAIN_TMK:
                    flagTextShow.setText(R.string.tip_load_tmk_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.DOWNLOAD_CAPK:
                    flagTextShow.setText(R.string.tip_load_capk_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.DOWNLOAD_AID:
                    flagTextShow.setText(R.string.tip_load_aid_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.DOWNLOAD_QPS_PARAMS:
                    flagTextShow.setText(R.string.tip_load_clss_params_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.DOWNLOAD_CARD_BIN:
                    flagTextShow.setText(R.string.tip_load_card_bin_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.DOWNLOAD_CARD_BIN_QPS:
                    flagTextShow.setText(R.string.tip_load_card_bin_b_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS:
                    flagTextShow.setText(R.string.tip_load_black_card_bin_success);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.LOAD_PARAM:
                    if ("true".equals(tempMap.get(key_param_update))) {
                        flagTextShow.setText(R.string.tip_load_terminal_param);
                    } else {
                        flagTextShow.setText(R.string.tip_load_terminal_param_null);
                    }
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.INIT_TERMINAL:
                    flagTextShow.setText(R.string.tip_load_terminal_init);
                    itemContainer.setVisibility(View.INVISIBLE);
                    break;
                case TransCode.SCAN_LAST_SERCH:
                    flagTextShow.setText("末笔扫码查询成功！");
                    addItemView("交易结果", "扫码交易成功", true);
                    String transType = tempMap.get(iso_f47);
                    String tempType = "";
                    if(!TextUtils.isEmpty(transType)){
                        if(transType.contains("ZFB01")){
                            tempType = "支付宝";
                        } else if(transType.contains("TX01")){
                            tempType = "微信支付";
                        } else if(transType.contains("SFT01")){
                            tempType = "xxx钱包";
                        } else {
                            tempType = "其它";
                        }
                    }
                    addItemView(getString(R.string.label_trans_type2), tempType, true);
                    transType = tempMap.get(iso_f61);
                    if(!TextUtils.isEmpty(transType) && transType.length()>=12){
                        //原扫码交易凭证号
                        tempType = transType.substring(6,12);
                    }
                    addItemView(getString(R.string.label_trans_amt2), "RMB " + DataHelper.formatIsoF4(tempMap.get(iso_f4)), true);
                    addItemView("原凭证号", tempType, true);
                    logger.info("key_oriTransTime = "+tempMap.get(key_oriTransTime));
                    addItemView(getString(R.string.label_trans_time), DataHelper.formatIsoF12F13(tempMap.get(key_oriTransTime).substring(0,6), tempMap.get(key_oriTransTime).substring(6,10)), false);
                    break;
                case TransCode.SCAN_PAY_WEI:
                case TransCode.SCAN_PAY_ALI:
                case TransCode.SCAN_PAY_SFT:
                case TransCode.SCAN_CANCEL:
                case TransCode.SCAN_REFUND_W:
                case TransCode.SCAN_REFUND_Z:
                case TransCode.SCAN_REFUND_S:
                    addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(transCode)), true);
                    addItemView(getString(R.string.label_trans_amt2), "RMB " + DataHelper.formatIsoF4(tempMap.get(iso_f4)), true);
                    addItemView(getString(R.string.label_pos_serial), tempMap.get(iso_f11), true);
                    addItemView(getString(R.string.label_trans_time), DataHelper.formatIsoF12F13(tempMap.get(iso_f12), tempMap.get(iso_f13)), false);
                    break;
                default:
                    addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(transCode)), true);
                    addItemView(getString(R.string.label_trans_amt2), "RMB " + DataHelper.formatIsoF4(tempMap.get(iso_f4)), true);
                    addItemView(getString(R.string.label_pos_serial), tempMap.get(iso_f11), true);
                    String cardNum2 = TextUtils.isEmpty(tempMap.get(iso_f2))?tempMap.get(iso_f2_result):tempMap.get(iso_f2);
                    String isShieldCard = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_SHIELD_CARD);
                    if (!TransCode.AUTH.equals(transCode)) {
                        addItemView(getString(R.string.label_trans_card2), DataHelper.shieldCardNo(cardNum2), true);
                    } else {
                        if ("1".equals(isShieldCard)) {
                            addItemView(getString(R.string.label_trans_card2), DataHelper.shieldCardNo(cardNum2), true);
                        } else {
                            addItemView(getString(R.string.label_trans_card2), cardNum2, true);
                        }
                    }
                    if (TransCode.AUTH_SETTLEMENT.equals(transCode)) {
                        Log.e("参考号是：",tempMap.get(iso_f37));
                    }
                    addItemView(getString(R.string.label_trans_time), DataHelper.formatIsoF12F13(tempMap.get(iso_f12), tempMap.get(iso_f13)), false);
                    break;
            }
        } else {
            flagIconShow.setImageResource(R.drawable.pic_jingao);
            addItemView(getString(R.string.label_resp_code), respCode, true);//状态码
            addItemView(getString(R.string.label_resp_msg), respMsg, false);//提示信息
            switch (transCode) {
                case TransCode.BALANCE:
                    flagTextShow.setText(R.string.tip_query_failed);
                    break;
                case TransCode.SIGN_IN:
                    flagTextShow.setText(R.string.tip_sign_in_failed);
                    break;
                case TransCode.OBTAIN_TMK:
                    flagTextShow.setText(R.string.tip_load_tmk_failed);
                    break;
                case TransCode.DOWNLOAD_CAPK:
                    flagTextShow.setText(R.string.tip_load_capk_failed);
                    break;
                case TransCode.DOWNLOAD_AID:
                    flagTextShow.setText(R.string.tip_load_aid_failed);
                    break;
                case TransCode.DOWNLOAD_QPS_PARAMS:
                    flagTextShow.setText(R.string.tip_load_aid_failed);
                    break;
                case TransCode.DOWNLOAD_CARD_BIN:
                    flagTextShow.setText(R.string.tip_load_card_bin_failed);
                    break;
                case TransCode.DOWNLOAD_CARD_BIN_QPS:
                    flagTextShow.setText(R.string.tip_load_card_bin_b_failed);
                    break;
                case TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS:
                    flagTextShow.setText(R.string.tip_load_black_card_bin_failed);
                    break;
                case TransCode.SCAN_LAST_SERCH:
                    flagTextShow.setText(R.string.tip_query_failed);
                    break;
                default:
                    flagTextShow.setText(R.string.tip_trade_failed2);
                    break;
            }
            if ("06".equals(respCode) || "18".equals(respCode)) {
                returnBtn.setText(R.string.label_confirm);
            }
        }
        if (isICInsertTrade()) {
            findViewById(R.id.tip_take_out).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        logger.debug("执行afterInitView方法");
        String printPaper = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_PAPER);
        if (printPaper.equals("1")) {
            if (isSuccess && (TransCode.DEBIT_SETS.contains(transCode)
                    || TransCode.CREDIT_SETS.contains(transCode))) {
                ShengPayPrinter shengPayPrinter = ShengPayPrinter.getMenuPrinter();
                shengPayPrinter.init(context);
                shengPayPrinter.printData(tempMap, transCode, false);
            }
            if (isSuccess && TransCode.SCAN_LAST_SERCH.equals(transCode)) {
                String tempType =tempMap.get(iso_f47).split("=|\\|")[1];
                if(tempType.contains("ZFB01")){
                    transCode = TransCode.SCAN_PAY_ALI;
                } else if(tempType.contains("TX01")){
                    transCode = TransCode.SCAN_PAY_WEI;
                } else if(tempType.contains("SFT01")){
                    transCode = TransCode.SCAN_PAY_SFT;
                }
                ShengPayPrinter shengPayPrinter = ShengPayPrinter.getMenuPrinter();
                shengPayPrinter.init(context);
                shengPayPrinter.printData(tempMap, transCode, true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (transCode) {
            case TransCode.BALANCE:
            case TransCode.SIGN_IN:
            case TransCode.OBTAIN_TMK:
            case TransCode.DOWNLOAD_CAPK:
            case TransCode.DOWNLOAD_AID:
            case TransCode.DOWNLOAD_QPS_PARAMS:
            case TransCode.DOWNLOAD_CARD_BIN:
            case TransCode.DOWNLOAD_CARD_BIN_QPS:
            case TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS:
            case TransCode.INIT_TERMINAL:
                openPageTimeout(false);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closePageTimeout();
    }


    public void onConfirmClick(View view) {
        String oper = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if ("99".equals(oper) || "00".equals(oper)) {
            activityStack.backTo(MenuActivity.class);
        } else {
            //activityStack.backTo(MainActivity.class);
            if(entryFlag) {
                jumpToNext("22");
            }else {
                jumpToNext();
            }
        }
        if ("06".equals(respCode)) {
            //重新签到
            jumpToSignIn();
        } else if ("18".equals(respCode)) {
            //重新下载主密钥
            jumpToDownloadTmk();
        }
    }





    private void addItemView(String key, String value, boolean addDivider) {
        View view = getLayoutInflater().inflate(R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -2);
        itemContainer.invalidate();
        if (addDivider) {
            float size = getResources().getDimension(R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(context);
            divider.setBackgroundColor(getResources().getColor(R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }

    @Override
    public void onBackPressed() {
        switch (transCode) {
            case TransCode.BALANCE:
            case TransCode.SIGN_IN:
                activityStack.backTo(MainActivity.class);
                break;
            case TransCode.OBTAIN_TMK:
            case TransCode.DOWNLOAD_CAPK:
            case TransCode.DOWNLOAD_AID:
            case TransCode.DOWNLOAD_QPS_PARAMS:
            case TransCode.DOWNLOAD_CARD_BIN:
            case TransCode.DOWNLOAD_CARD_BIN_QPS:
            case TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS:
            case TransCode.LOAD_PARAM:
            case TransCode.INIT_TERMINAL:
                activityStack.backTo(MenuActivity.class);
                break;
            default:
                activityStack.backTo(MainActivity.class);
                break;
        }
    }
}

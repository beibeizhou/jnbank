package com.centerm.jnbank.activity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.msg.MessageFactory;
import com.centerm.jnbank.msg.Iso_21_62;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.net.SocketClient;
import com.centerm.jnbank.utils.DataHelper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransCode.DISCOUNT_INTERGRAL;
import static com.centerm.jnbank.common.TransCode.SALE;
import static com.centerm.jnbank.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.jnbank.common.TransDataKey.FLAG_REQUEST_UNIONCARD;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;
import static config.BusinessConfig.Key.FLAG_USE_DISCOUNT;
import static config.BusinessConfig.Key.FLAG_USE_INTEGRAL;

/**
 * 创建日期：2017/9/4 0004 on 17:41
 * 描述:工会卡积分和则扣查询结果页面
 * 作者:周文正
 */

public class UnionCardQueryActivity extends BaseTradeActivity {
    private TextView re_amount, en_discount, own_integral, actual_amount, txtvw_title;
    private String in2money;
    private float num;
    private Button positive_btn1;
    private Double d1;//在本界面展示在实际金额数值
    private Double d2;//积分能抵扣的金额
    private String integralBalance;
    private String cardNum;
    private String iso62_21;
    private String integralFlag;
    private String s1;


    @Override
    public int onLayoutId() {
        return R.layout.activity_unioncard_query;
    }

    @Override
    public void onInitView() {
        txtvw_title = ((TextView) findViewById(R.id.txtvw_title));
        txtvw_title.setText(R.string.discount_intergal);
        //实际金额
        actual_amount = ((TextView) findViewById(R.id.actual_amount));
        //应收金额
        re_amount = ((TextView) findViewById(R.id.re_amount));
        //享受折扣
        en_discount = ((TextView) findViewById(R.id.en_discount));
        //拥有积分
        own_integral = ((TextView) findViewById(R.id.own_integral));
        positive_btn1 = ((Button) findViewById(R.id.positive_btn1));
    }

    @Override
    public void afterInitView() {
        Map<String, String> returnData= (Map<String, String>) getIntent().getSerializableExtra(DISCOUNT_INTERGRAL);

        if (returnData != null) {
            cardNum = returnData.get(iso_f2);
            re_amount.setText(DataHelper.formatAmountForShow(returnData.get(iso_f4)));
            String iso_f39 = returnData.get(TransDataKey.iso_f39);
            if (!TextUtils.isEmpty(iso_f39)) {
                String iso_f62 = returnData.get(TransDataKey.iso_f62);
                //折扣费率
                if (TextUtils.isEmpty(iso_f62)) {
                    positive_btn1.setVisibility(View.GONE);
                    jumpToNext("99");
                    return;
                }
                String discountRate = iso_f62.substring(0, 12);
                String sub1 = discountRate.substring(10, 11);
                String sub2 = discountRate.substring(11, 12);
                String sub3 = sub1 + "." + sub2 + "折";
                //折扣后金额
                String preAmount = iso_f62.substring(12, 24);
                //优惠金额
                String disAmount = iso_f62.substring(24, 36);
                //积分查询标志
                integralFlag = iso_f62.substring(36, 37);
                //积分余额
                integralBalance = iso_f62.substring(37);
                s1 = integralBalance(integralBalance);
                logger.info("积分余额:" + s1);
                //积分转成金额
                in2money = math(s1);
                if (iso_f39.equals("00")) {//工会卡有折扣
                    if (BusinessConfig.getInstance().getParam(context, FLAG_USE_DISCOUNT).equals("1") ? true : false) {
                        actual_amount.setText(DataHelper.formatAmountForShow(preAmount));
                        en_discount.setText(sub3);
                    } else {
                        actual_amount.setText(DataHelper.formatAmountForShow(returnData.get(iso_f4)));
                        en_discount.setText("该终端尚未开通折扣功能");
                    }
                    String trim = actual_amount.getText().toString().trim();
                    d1 = Double.valueOf(trim);
                    d2 = Double.valueOf(num);
                    //表示有积分
                    hasInteg();
                    if (d2 > d1) {
                        if (BusinessConfig.getInstance().getParam(context, FLAG_USE_INTEGRAL).equals("1") ? true : false) {

                        } else {
                            own_integral.setText("该终端尚未开通积分抵扣功能");
                        }
                        positive_btn1.setVisibility(View.GONE);
                    } else {
                        if (BusinessConfig.getInstance().getParam(context, FLAG_USE_INTEGRAL).equals("1") ? true : false) {
                            positive_btn1.setVisibility(View.VISIBLE);
                        } else {
                            own_integral.setText("该终端尚未开通积分抵扣功能");
                            positive_btn1.setVisibility(View.GONE);
                        }
                    }
                } else if (iso_f39.equals("58")) {//工会卡无折扣
                    en_discount.setText("无折扣");
                    //表示有积分
                    actual_amount.setText(DataHelper.formatAmountForShow(dataMap.get(iso_f4)));
                    String trim = actual_amount.getText().toString().trim();
                    d1 = Double.valueOf(trim);
                    d2 = Double.valueOf(num);
                    hasInteg();
                    if (d2 > d1) {
                        positive_btn1.setVisibility(View.GONE);
                    } else {
                        if (d2 == 0) {
                            positive_btn1.setVisibility(View.GONE);
                        } else {
                            positive_btn1.setVisibility(View.VISIBLE);
                        }
                    }
                }
                if(TextUtils.isEmpty(s1) || Double.valueOf(s1)==0){
                    positive_btn1.setVisibility(View.GONE);
                }
            }
        }


    }

    /**
     * 判断有无积分
     */
    private void hasInteg() {
        if (!TextUtils.isEmpty(integralFlag) && integralFlag.equals("A")) {
            DecimalFormat df = new DecimalFormat("0.00");
            String format = df.format(d2);
            own_integral.setText(s1 + "  可抵扣" + format + "元");
            positive_btn1.setVisibility(View.VISIBLE);
        } else {
            own_integral.setText("无积分");
            positive_btn1.setVisibility(View.GONE);
        }
    }

    private String math(String str) {
        num = (float) Integer.parseInt(str) / 400;
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(num);
        return format;
    }

    /**
     * 计算积分余额
     */
    private String integralBalance(String string) {
        String trim = string.trim();
        int anInt = Integer.parseInt(trim);
        if (anInt == 0) {
            return "0";
        }
        char[] chars = trim.toCharArray();
        int index = 0;
        for (int i = 0; i < string.length(); i++) {
            if ('0' != chars[i]) {
                index = i;// 找到非零字符串并跳出
                break;
            }
        }
        String substring = trim.substring(index);
        int length1 = substring.length();
        String substring1 = substring.substring(0, length1 - 2);
        return substring1;
    }

    public void onConfirmClick(View view) {
        switch (view.getId()) {
            //不使用积分
            case R.id.positive_btn:
                String trim = actual_amount.getText().toString().trim();
                dataMap.put(iso_f4, DataHelper.formatAmount(Double.parseDouble(trim)));
                transCode = SALE;
                dataMap.put(FLAG_REQUEST_ONLINE, "1");
                dataMap.put(FLAG_REQUEST_UNIONCARD,"1");
                iso62_21 = Iso_21_62.getIso62_21(context, cardNum);
                dataMap.put(iso_f62,"B"+"000000000000000"+ iso62_21);
                BusinessConfig.getInstance().setParam(context, BusinessConfig.USE_INTEGRAL, "B");
                jumpToNext();
                break;
            //使用积分
            case R.id.positive_btn1:
                //实际付款金额要减去积分抵扣的金额
                BigDecimal bl1 = new BigDecimal(d1);
                BigDecimal bl2 = new BigDecimal(d2);
                double v = bl1.subtract(bl2).doubleValue();
                dataMap.put(iso_f4, DataHelper.formatAmount(v));
                transCode = SALE;
                dataMap.put(FLAG_REQUEST_ONLINE, "1");
                iso62_21 = Iso_21_62.getIso62_21(context, cardNum);
                dataMap.put(iso_f62,"A"+integralBalance+ iso62_21);
                BusinessConfig.getInstance().setParam(context, BusinessConfig.USE_INTEGRAL, "A");
                BusinessConfig.getInstance().setParam(context, BusinessConfig.INTEGRAL_BALANCE, integralBalance);
                jumpToNext();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        activityStack.backTo(MainActivity.class);
    }
}

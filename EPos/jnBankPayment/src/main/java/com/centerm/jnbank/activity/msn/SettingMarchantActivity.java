package com.centerm.jnbank.activity.msn;

import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;

import config.BusinessConfig;

/**
 * Created by ysd on 2016/11/30.
 */

public class SettingMarchantActivity extends BaseActivity {

    private EditText marchantNo,termNo,marchantName,marchantName2;

    @Override
    public int onLayoutId() {
        return R.layout.activity_machant_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_marchant);
        marchantNo = (EditText) findViewById(R.id.extxt_marchant_number);
        termNo = (EditText) findViewById(R.id.extxt_term_no);
        marchantName = (EditText) findViewById(R.id.extxt_marchant_name);
//        marchantName2 = (EditText) findViewById(R.id.extxt_marchant_name2);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        BusinessConfig config = BusinessConfig.getInstance();
        String merchantCd = config.getValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD);
        String merchantName =config.getValue(context,BusinessConfig.Key.PRESET_MERCHANT_NAME);
        String termCd = config.getValue(context,BusinessConfig.Key.PRESET_TERMINAL_CD);
        if (null != merchantCd) {
            marchantNo.setText(merchantCd);
        }
        if (null != termCd) {
            termNo.setText(termCd);
        }
        if (null != merchantName) {
            marchantName.setText(merchantName);
//            marchantName2.setText("JiangNanBank");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusinessConfig config = BusinessConfig.getInstance();
        String marchantname = marchantName.getText().toString().trim();
        if(!TextUtils.isEmpty(marchantname)){
            config.setValue(context,BusinessConfig.Key.PRESET_MERCHANT_NAME,marchantname);
        }else {
            config.setValue(context,BusinessConfig.Key.PRESET_MERCHANT_NAME,"");
        }
        String marchantNo = this.marchantNo.getText().toString().trim();
        if(!TextUtils.isEmpty(marchantNo)){
            config.setValue(context,BusinessConfig.Key.PRESET_MERCHANT_CD,marchantNo);
        }else {
            config.setValue(context,BusinessConfig.Key.PRESET_MERCHANT_CD,"");
        }
        String termNo = this.termNo.getText().toString().trim();
        if(!TextUtils.isEmpty(termNo)){
            config.setValue(context,BusinessConfig.Key.PRESET_TERMINAL_CD,termNo);
        }else {
            config.setValue(context,BusinessConfig.Key.PRESET_TERMINAL_CD,"");
        }
    }
}

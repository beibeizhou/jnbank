package com.centerm.jnbank.activity.msn;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.ViewUtils;

import config.BusinessConfig;


/**
 * Created by linwenhui on 2016/11/3.
 */

public class ConfigureCommsActivity extends BaseActivity {

    EditText service1, service2, serviceParam, port1, port2, portParam,paramTpdu,outTime,domainName;
    private BusinessConfig config;
    @Override
    public int onLayoutId() {
        return R.layout.activity_configure_comms;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_configure_comms);
        config = BusinessConfig.getInstance();
        service1 = (EditText) findViewById(R.id.extxt_confiure_comms_service);
        service2 = (EditText) findViewById(R.id.extxt_confiure_comms_service2);
        serviceParam = (EditText) findViewById(R.id.extxt_confiure_param_service);
        port1 = (EditText) findViewById(R.id.extxt_confiure_comms_port);
        port2 = (EditText) findViewById(R.id.extxt_confiure_comms_port2);
        portParam = (EditText) findViewById(R.id.extxt_confiure_comms_port_param);
        domainName = (EditText) findViewById(R.id.extxt_confiure_domain_name);
        paramTpdu = (EditText) findViewById(R.id.param_tpdu);
        outTime = (EditText) findViewById(R.id.out_time);
        service1.addTextChangedListener(new LimitTextWatcher(service1));
        service1.setText(Settings.getCommonIp1(this));
        port1.setText(Settings.getCommonPort1(this) + "");
        service2.addTextChangedListener(new LimitTextWatcher(service2));
        service2.setText(Settings.getCommonIp2(this));
        port2.setText(Settings.getCommonPort2(this) + "");
        serviceParam.addTextChangedListener(new LimitTextWatcher(serviceParam));
        serviceParam.setText(Settings.getParamIp(this));
        portParam.setText(Settings.getCommonPortParam(this)==0?"":Settings.getCommonPortParam(this)+"");
        paramTpdu.setText(config.getParam(context,BusinessConfig.Key.PARAM_TPDU));
        outTime.setText(Settings.getRespTimeout(context)+"");
        domainName.setText(Settings.getDomainName(context)+"");
    }

    public void onSureClick(View v) {
        String tagService = service1.getText().toString().trim().replace(" ", "");
        String tagPort = port1.getText().toString().trim().replace(" ", "");
        String tagService2 = service2.getText().toString().trim().replace(" ", "");
        String tagPort2 = port2.getText().toString().trim().replace(" ", "");
        String tagServiceParam = serviceParam.getText().toString().trim().replace(" ", "");
        String tagPortParam = portParam.getText().toString().trim().replace(" ", "");
        String tagParamTpdu = paramTpdu.getText().toString().trim().replace(" ", "");
        String tagOutTime = outTime.getText().toString().trim().replace(" ", "");
        String domainNameStr = domainName.getText().toString().trim().replace(" ", "");
        //if (TextUtils.isEmpty(tagService)||TextUtils.isEmpty(tagService2)||TextUtils.isEmpty(tagServiceParam)) {
        if (TextUtils.isEmpty(tagService)||TextUtils.isEmpty(tagService2)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_service_empty);
            return;
        }
        if (!CommonUtils.isIp(tagService)||!CommonUtils.isIp(tagService2)||(!TextUtils.isEmpty(tagServiceParam)&&!CommonUtils.isIp(tagServiceParam))) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_service_illegal);
            return;
        }
        //if (TextUtils.isEmpty(tagPort)||TextUtils.isEmpty(tagPort2)||TextUtils.isEmpty(tagPortParam)) {
        if (TextUtils.isEmpty(tagPort)||TextUtils.isEmpty(tagPort2)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_port_empty);
            return;
        }
        if (TextUtils.isEmpty(domainNameStr)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_domain_illegal);
            return;
        }
        int intPort = 0, intPort2 = 0,intPortParam=0;
        try {
            intPort = Integer.parseInt(tagPort);
            intPort2 = Integer.parseInt(tagPort2);
            if (!StringUtils.isStrNull(tagPortParam)) {
                intPortParam = Integer.parseInt(tagPortParam);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (intPort < 0 || intPort > 65535) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_port_illegal);
            return;
        }
        if (tagParamTpdu.length() != 10) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_tpdu_lenth);
            return;
        }
        if (Integer.parseInt(tagOutTime) > 90) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_out_time);
            return;
        }
        Settings.setCommonIp1(this, tagService);
        Settings.setCommonIp2(this, tagService2);
        Settings.setParamIp(this, tagServiceParam);
        Settings.setCommonPort1(this, intPort);
        Settings.setCommonPort2(this, intPort2);
        Settings.setCommonPortParam(this, intPortParam);
        Settings.setCommonPortParam(this, intPortParam);
        config.setValue(context,BusinessConfig.Key.PARAM_TPDU,tagParamTpdu);
        Settings.setRespTimeout(this, Integer.parseInt(tagOutTime));
        Settings.setDomainName(this, domainNameStr);
        activityStack.pop();
    }

    private class LimitTextWatcher implements TextWatcher {
        private EditText service;
        LimitTextWatcher(EditText service){
            this.service = service;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            int index = service.getSelectionStart();
            if (index >= 2) {
                if (text.charAt(index - 1) == text.charAt(index - 2) && text.charAt(index - 1) == '.') {
                    text = text.substring(0, index - 1);
                    service.setText(text);
                    service.setSelection(index - 1);
                } else {
                    int pos = text.length() - text.lastIndexOf(".") - 1;
                    if (pos > 3) {
                        text = text.substring(0, index - 1);
                        service.setText(text);
                        service.setSelection(index - 1);
                    }
                }
            }
        }
    }

}

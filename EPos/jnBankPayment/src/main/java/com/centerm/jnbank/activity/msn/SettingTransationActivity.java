package com.centerm.jnbank.activity.msn;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.ElecSignInfo;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.bean.iso.Iso62Qps;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.ViewUtils;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

import config.BusinessConfig;

import static com.centerm.jnbank.xml.XmlTag.MenuTag.SCAN_PARAM;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.SETTLE_SET;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_CARD;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_OTHER;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_PSW;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_SIGN;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_SWITCH;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.VISA_FREE_PARAM;

/**
 * Created by ysd on 2016/11/30.
 */

public class SettingTransationActivity extends BaseActivity {

    private LinearLayout swith_layout,psw_layout,settle_layout,scan_layout,sign_layout,card_layout, other_layout,visa_free_layout;
    private String type;
    private Button button;
    private EditText max_refund_amt,sign_out_time,reload_sign_count,max_sign_count,max_resign,scan_query_count,scan_wait_time,define_head,sign_limit_et,psw_limit_et;
    private CheckBox sale_swith,void_swith,refund_swith,balance_swith,auth_swith, cancel_swith,complete_swith,completeVoid_swith,
            sft_swith,wei_swith,ali_swith,card_swith,void_psw, complete_void_psw,cancel_psw,complete_psw,viod_card, complete_void_card,auto_sign_out
            ,tip_detail,print_void_detail,print_fail_detail,refund_admin_psw, auth_hand_card,is_feijie,is_show_logo,is_trace_psw,is_sign,tip_comfirm_sign, is_first_feijie,is_query_integral
            ,is_query_discount,qps_swith_cb,bin_a_cb,bin_b_cb,cdcvm_cb,sign_cb;
    private BusinessConfig config = BusinessConfig.getInstance();
    private DecimalFormat formatter = new DecimalFormat("#0.00");
    private RadioGroup radioGroup;
    @Override
    public int onLayoutId() {
        return R.layout.activity_transation_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_transation);
        type = getIntent().getStringExtra("TYPE");
        swith_layout = (LinearLayout) findViewById(R.id.swith_layout);
        psw_layout = (LinearLayout) findViewById(R.id.psw_layout);
        settle_layout = (LinearLayout) findViewById(R.id.settle_layout);
        scan_layout = (LinearLayout) findViewById(R.id.scan_layout);
        sign_layout = (LinearLayout) findViewById(R.id.sign_layout);
        card_layout = (LinearLayout) findViewById(R.id.card_layout);
        other_layout = (LinearLayout) findViewById(R.id.other_layout);
        visa_free_layout = (LinearLayout) findViewById(R.id.visa_free_layout);

        sale_swith = (CheckBox) findViewById(R.id.sale_swith);
        void_swith = (CheckBox) findViewById(R.id.void_swith);
        refund_swith = (CheckBox) findViewById(R.id.refund_swith);
        balance_swith = (CheckBox) findViewById(R.id.balance_swith);
        auth_swith = (CheckBox) findViewById(R.id.auth_swith);
        cancel_swith = (CheckBox) findViewById(R.id.auth_void_swith);
        complete_swith = (CheckBox) findViewById(R.id.complete_swith);
        completeVoid_swith = (CheckBox) findViewById(R.id.complete_void_swith);
        sft_swith = (CheckBox) findViewById(R.id.sft_swith);
        wei_swith = (CheckBox) findViewById(R.id.wei_swith);
        ali_swith = (CheckBox) findViewById(R.id.ali_swith);
        card_swith = (CheckBox) findViewById(R.id.card_swith);

        void_psw = (CheckBox) findViewById(R.id.void_psw);
        complete_void_psw = (CheckBox) findViewById(R.id.complete_void_psw);
        cancel_psw = (CheckBox) findViewById(R.id.cancle_psw);
        complete_psw = (CheckBox) findViewById(R.id.complete_psw);

        viod_card = (CheckBox) findViewById(R.id.viod_card);
        complete_void_card = (CheckBox) findViewById(R.id.complete_void_card);

        auto_sign_out = (CheckBox) findViewById(R.id.auto_sign_out);
        tip_detail = (CheckBox) findViewById(R.id.tip_detail);
        print_void_detail = (CheckBox) findViewById(R.id.print_void_detail);
        print_fail_detail = (CheckBox) findViewById(R.id.print_fail_detail);
        refund_admin_psw = (CheckBox) findViewById(R.id.refund_admin_psw);
        auth_hand_card = (CheckBox) findViewById(R.id.auth_hand_card);
        is_feijie = (CheckBox) findViewById(R.id.is_feijie);
        is_show_logo = (CheckBox) findViewById(R.id.is_show_logo);
        is_trace_psw = (CheckBox) findViewById(R.id.is_trade_psw);
        is_first_feijie = (CheckBox) findViewById(R.id.is_first_feijie);
        is_query_integral=(CheckBox) findViewById(R.id.is_query_integral);
        is_query_discount=(CheckBox) findViewById(R.id.is_query_discount);

        define_head = (EditText) findViewById(R.id.define_head_et);
        scan_query_count = (EditText) findViewById(R.id.scan_query_count);
        scan_wait_time = (EditText) findViewById(R.id.scan_wait_time);

        is_sign = (CheckBox) findViewById(R.id.is_sign);
        tip_comfirm_sign = (CheckBox) findViewById(R.id.tip_comfirm_sign);
        sign_out_time = (EditText) findViewById(R.id.sign_out_time);
        reload_sign_count = (EditText) findViewById(R.id.reload_sign_count);
        max_sign_count = (EditText) findViewById(R.id.max_sign_count);
        max_resign = (EditText) findViewById(R.id.max_resign);

        max_refund_amt = (EditText) findViewById(R.id.max_refund_amt);
        max_refund_amt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    return false;
                }
                inputMoney(keyEvent);
                return true;
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.send_time);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.forwad:
                        config.setParam(context, BusinessConfig.Key.PARAM_WHEN_UPLOAD,"1");
                        break;
                    case R.id.back:
                        config.setParam(context, BusinessConfig.Key.PARAM_WHEN_UPLOAD,"0");
                        break;
                }
            }
        });
        sign_limit_et = (EditText) findViewById(R.id.sign_limit_et);
        psw_limit_et = (EditText) findViewById(R.id.psw_limit_et);
        qps_swith_cb = (CheckBox) findViewById(R.id.qps_swith_cb);
        bin_a_cb = (CheckBox) findViewById(R.id.bin_a_cb);
        bin_b_cb = (CheckBox) findViewById(R.id.bin_b_cb);
        cdcvm_cb = (CheckBox) findViewById(R.id.cdcvm_cb);
        sign_cb = (CheckBox) findViewById(R.id.sign_cb);
        qps_swith_cb.setEnabled(false);
        bin_a_cb.setEnabled(false);
        bin_b_cb.setEnabled(false);
        cdcvm_cb.setEnabled(false);
        sign_cb.setEnabled(false);

        button = (Button) findViewById(R.id.modify_trans);
        if (type == null) {
            return;
        }
        switch (type) {
            case TRANS_SWITCH:
                txtvw.setText("交易开关");
                swith_layout.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
                break;
            case TRANS_PSW:
                txtvw.setText("交易输密");
                psw_layout.setVisibility(View.VISIBLE);
                break;
            case TRANS_CARD:
                txtvw.setText("交易刷卡");
                card_layout.setVisibility(View.VISIBLE);
                break;
            case SETTLE_SET:
                txtvw.setText("结算设置");
                settle_layout.setVisibility(View.VISIBLE);
                break;
            case SCAN_PARAM:
                txtvw.setText("扫码参数");
                scan_layout.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
                break;
            case TRANS_SIGN:
                txtvw.setText("电子签名");
                sign_layout.setVisibility(View.VISIBLE);
                break;
            case TRANS_OTHER:
                txtvw.setText("其他设置");
                other_layout.setVisibility(View.VISIBLE);
                break;
            case VISA_FREE_PARAM:
                txtvw.setText("双免参数");
                CommonDao<Iso62Qps> dao = new CommonDao<>(Iso62Qps.class, new DbHelper(context));
                List<Iso62Qps> qpsList = dao.query();
                if (qpsList != null && qpsList.size() > 0) {
                    Iso62Qps qpsParams = qpsList.get(0);
                    psw_limit_et.setText(StringUtils.isStrNull(qpsParams.getFF8058())?"0.00":formatter.format(DataHelper.parseIsoF4(qpsParams.getFF8058())));
                    sign_limit_et.setText(StringUtils.isStrNull(qpsParams.getFF8059())?"0.00":formatter.format(DataHelper.parseIsoF4(qpsParams.getFF8059())));
                    qps_swith_cb.setChecked("1".equals(qpsParams.getFF8054())?true:false);
                    bin_a_cb.setChecked("1".equals(qpsParams.getFF8055())?true:false);
                    bin_b_cb.setChecked("1".equals(qpsParams.getFF8056())?true:false);
                    cdcvm_cb.setChecked("1".equals(qpsParams.getFF8057())?true:false);
                    sign_cb.setChecked("1".equals(qpsParams.getFF805A())?true:false);
                    logger.warn("查询小额免密免签参数==>" + qpsParams.toString());
                } else {
                    logger.debug("未查询到小额免密免签参数==>可进行参数下载");
                }
                visa_free_layout.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void afterInitView() {
        super.afterInitView();

        String balanceSwitch = config.getParam(context, BusinessConfig.Key.FLAG_BALANCE_SWITH);
        String saleSwitch = config.getParam(context, BusinessConfig.Key.FLAG_SALE_SWITH);
        String voidSwitch = config.getParam(context, BusinessConfig.Key.FLAG_VOID_SWITH);
        String authSwitch = config.getParam(context, BusinessConfig.Key.FLAG_AUTH_SWITH);
        String cancelSwitch = config.getParam(context, BusinessConfig.Key.FLAG_CANCEL_SWITH);
        String completeSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_SWITH);
        String completeVoidSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_SWITH);
        String refundSwitch = config.getParam(context, BusinessConfig.Key.FLAG_REFUND_SWITH);

        String shengFlag = config.getParam(context, BusinessConfig.Key.FLAG_SHENG_SWITH);
        String weiFlag = config.getParam(context, BusinessConfig.Key.FLAG_WEI_SWITH);
        String aliFlag = config.getParam(context, BusinessConfig.Key.FLAG_ALI_SWITH);
        String bankFlag = config.getParam(context, BusinessConfig.Key.FLAG_BANK_SWITH);
        String voidPsw = config.getParam(context, BusinessConfig.Key.FLAG_VOID_PSW);
        String cancelPsw = config.getParam(context, BusinessConfig.Key.FLAG_CANCEL_PSW);
        String completeVoidPsw = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_PSW);
        String completePsw = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_PSW);
        String voidCard = config.getParam(context, BusinessConfig.Key.FLAG_VOID_CARD);
        String completeVoidCard = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_CARD);
        String autoSignOut = config.getParam(context, BusinessConfig.Key.FLAG_AUTO_SIGN_OUT);
        String tipPrintDetail = config.getParam(context, BusinessConfig.Key.FLAG_TIP_PRINT_DETAIL);
        String printVoidDetail = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL);
        String tipPrintFailDetail = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_FAIL_DETAIL);
        String refundNeedPsw = config.getParam(context, BusinessConfig.Key.FLAG_REFUND_VOID_NEED_PSW);
        String authHandCard = config.getParam(context, BusinessConfig.Key.FLAG_AUTH_HAND_CARD);
        String isFeiJie = config.getParam(context, BusinessConfig.Key.FLAG_FEIJIE);
        String isShowLogo = config.getParam(context, BusinessConfig.Key.FLAG_SWITCH_LOGO);
        String isTracePsw = config.getParam(context, BusinessConfig.Key.FLAG_TRACE_PSW);
        String isFeijieFirst = config.getParam(context, BusinessConfig.Key.FLAG_PREFER_CLSS);
        String isUseIntegral = config.getParam(context, BusinessConfig.Key.FLAG_USE_INTEGRAL);
        String isUseDiscount=config.getParam(context, BusinessConfig.Key.FLAG_USE_DISCOUNT);
        String maxRefundAmt = config.getParam(context, BusinessConfig.Key.PARAM_MOST_REFUND);

        String scanQueryCount = config.getParam(context, BusinessConfig.Key.FLAG_SCAN_QUERY_COUNT);
        String scanWaitTime = config.getParam(context, BusinessConfig.Key.FLAG_SCAN_WAIT_TIME);
        String defineHeadStr = config.getParam(context, BusinessConfig.Key.PARAM_DEFINE_HEAD);

        String isOpenSign = config.getParam(context, BusinessConfig.Key.FLAG_IS_OPEN_SIGN);
        String tipComfirmSign = config.getParam(context, BusinessConfig.Key.FLAG_TIP_COMFIRM_SIGN);
        String signOutTime = config.getParam(context, BusinessConfig.Key.PARAM_SIGN_OUT_TIME);
        String signUploadTime = config.getParam(context, BusinessConfig.Key.PARAM_SIGN_UPLOAD_TIME);
        String maxSignCnt = config.getParam(context, BusinessConfig.Key.PARAM_MAX_SIGN_CNT);
        String maxResignCnt = config.getParam(context, BusinessConfig.Key.PARAM_MAX_RESIGN_CNT);
        String uploadTime = config.getParam(context, BusinessConfig.Key.PARAM_WHEN_UPLOAD);

        sale_swith.setChecked("1".equals(saleSwitch)?true:false);
        void_swith.setChecked("1".equals(voidSwitch)?true:false);
        refund_swith.setChecked("1".equals(refundSwitch)?true:false);
        balance_swith.setChecked("1".equals(balanceSwitch)?true:false);
        auth_swith.setChecked("1".equals(authSwitch)?true:false);
        cancel_swith.setChecked("1".equals(cancelSwitch)?true:false);
        complete_swith.setChecked("1".equals(completeSwitch)?true:false);
        completeVoid_swith.setChecked("1".equals(completeVoidSwitch)?true:false);
        sft_swith.setChecked("1".equals(shengFlag)?true:false);
        wei_swith.setChecked("1".equals(weiFlag)?true:false);
        ali_swith.setChecked("1".equals(aliFlag)?true:false);
        card_swith.setChecked("1".equals(bankFlag)?true:false);
        void_psw.setChecked("1".equals(voidPsw)?true:false);
        complete_void_psw.setChecked("1".equals(completeVoidPsw)?true:false);
        cancel_psw.setChecked("1".equals(cancelPsw)?true:false);
        complete_psw.setChecked("1".equals(completePsw)?true:false);
        viod_card.setChecked("1".equals(voidCard)?true:false);
        complete_void_card.setChecked("1".equals(completeVoidCard)?true:false);
        auto_sign_out.setChecked("1".equals(autoSignOut)?true:false);
        tip_detail.setChecked("1".equals(tipPrintDetail)?true:false);
        print_void_detail.setChecked("1".equals(printVoidDetail)?true:false);
        print_fail_detail.setChecked("1".equals(tipPrintFailDetail)?true:false);
        refund_admin_psw.setChecked("1".equals(refundNeedPsw)?true:false);
        auth_hand_card.setChecked("1".equals(authHandCard)?true:false);
        is_feijie.setChecked("1".equals(isFeiJie)?true:false);
        is_show_logo.setChecked("1".equals(isShowLogo)?true:false);
        is_trace_psw.setChecked("1".equals(isTracePsw)?true:false);
        is_first_feijie.setChecked("1".equals(isFeijieFirst)?true:false);
        is_query_integral.setChecked("1".equals(isUseIntegral)?true:false);
        is_query_discount.setChecked("1".equals(isUseDiscount)?true:false);
        if (maxRefundAmt.length() >= 3) {
            String newStr = maxRefundAmt.substring(0, maxRefundAmt.length() - 2) + "." + maxRefundAmt.substring(maxRefundAmt.length() - 2, maxRefundAmt.length());
            max_refund_amt.setText(newStr);
        } else {
            double newStr = DataHelper.formatDouble(Double.parseDouble(maxRefundAmt)/100.00);
            max_refund_amt.setText(newStr+"");
        }

        if (null != scanQueryCount) {
            scan_query_count.setText(scanQueryCount);
        }
        if (null != scanWaitTime) {
            scan_wait_time.setText(scanWaitTime);
        }
        if (null != defineHeadStr) {
            define_head.setText(defineHeadStr);
        }
        //电子签名页
        is_sign.setChecked("1".equals(isOpenSign)?true:false);
        tip_comfirm_sign.setChecked("1".equals(tipComfirmSign)?true:false);
        sign_out_time.setText(signOutTime);
        reload_sign_count.setText(signUploadTime);
        max_sign_count.setText(maxSignCnt);
        max_resign.setText(maxResignCnt);
        if ("1".equals(uploadTime)) {
            radioGroup.check(R.id.forwad);
        } else {
            radioGroup.check(R.id.back);
        }
        formatter.setMaximumFractionDigits(2);
        formatter.setGroupingSize(0);
        formatter.setRoundingMode(RoundingMode.FLOOR);
    }

    public void modifyParam(View view) {
        modify();
    }

    public void modifyParam2(View view) {
        modify();
    }

    private void modify() {
        String maxAmt = max_refund_amt.getText().toString().trim();
        String signOutTime = sign_out_time.getText().toString().trim();
        String reloadSignCount = reload_sign_count.getText().toString().trim();
        String maxSignCnt = max_sign_count.getText().toString().trim();
        String maxResign = max_resign.getText().toString().trim();
        String scanQueryCountStr = scan_query_count.getText().toString().trim();
        String scanWaitTimeStr = scan_wait_time.getText().toString().trim();
        String defineHeadStr = define_head.getText().toString().trim();
        if (StringUtils.isStrNull(signOutTime)) {
            ViewUtils.showToast(context, getString(R.string.tip_sign_out_time_not_null));
            return;
        }
        if (StringUtils.isStrNull(reloadSignCount)) {
            ViewUtils.showToast(context, getString(R.string.tip_reload_sign_count_not_null));
            return;
        }
        if (StringUtils.isStrNull(maxSignCnt)) {
            ViewUtils.showToast(context, getString(R.string.tip_max_sign_count_not_null));
            return;
        }
        if (StringUtils.isStrNull(maxResign)) {
            ViewUtils.showToast(context, getString(R.string.tip_max_resign_not_null));
            return;
        }
        int finalAmt = 0;
        if (StringUtils.isStrNull(maxAmt)) {
            ViewUtils.showToast(context,"请输入正确的退货金额！");
            return;
        }
        try {
             finalAmt = (int)DataHelper.formatDouble(Double.parseDouble(maxAmt) * 100);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            ViewUtils.showToast(context,"请输入正确的退货金额！");
            return;
        }
        int outTime = Integer.parseInt(signOutTime);
        int maxFlowCnt = Integer.parseInt(maxSignCnt);
        if (outTime < 15) {
            ViewUtils.showToast(context,"超时时长范围为15-999");
            return;
        }
        if (maxFlowCnt < 50 || maxFlowCnt > 300) {
            ViewUtils.showToast(context,"电子签最大交易笔数范围为50-300");
            return;
        }
        if (StringUtils.isStrNull(defineHeadStr)) {
            ViewUtils.showToast(context,"打印头部定义不能为空");
            return;
        }
        try {
            CommonManager commonManager = new CommonManager(TradeInfo.class, context);
            long counts = commonManager.getBatchCount();
            CommonManager commonManager2 = new CommonManager(ElecSignInfo.class, context);
            long signCount = commonManager2.getSignCount();
            long config = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.PARAM_MOST_TRANS);
            logger.info("已存储电子签名流水数量==>" + signCount);
            if (signCount >= maxFlowCnt) {
                logger.warn("电子签名流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, true);
            } else {
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, false);
            }
            logger.info("已存储成功流水数量==>" + counts);
            if (counts >= config) {
                logger.warn("交易流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ViewUtils.showToast(context, getString(R.string.tip_max_tran_query));
            return;
        }
        config.setParam(context, BusinessConfig.Key.FLAG_BALANCE_SWITH,balance_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_SALE_SWITH,sale_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_VOID_SWITH,void_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_AUTH_SWITH,auth_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_CANCEL_SWITH,cancel_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_SWITH,complete_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_SWITH,completeVoid_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_REFUND_SWITH,refund_swith.isChecked()==true?"1":"0");

        config.setParam(context, BusinessConfig.Key.FLAG_SHENG_SWITH,sft_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_WEI_SWITH,wei_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_ALI_SWITH,ali_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_BANK_SWITH,card_swith.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_VOID_PSW,void_psw.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_CANCEL_PSW,cancel_psw.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_PSW,complete_void_psw.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_PSW,complete_psw.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_VOID_CARD,viod_card.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_CARD,complete_void_card.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_AUTO_SIGN_OUT,auto_sign_out.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_TIP_PRINT_DETAIL,tip_detail.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL,print_void_detail.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_PRINT_FAIL_DETAIL,print_fail_detail.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_REFUND_VOID_NEED_PSW,refund_admin_psw.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_AUTH_HAND_CARD, auth_hand_card.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_FEIJIE,is_feijie.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_SWITCH_LOGO,is_show_logo.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_TRACE_PSW,is_trace_psw.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_PREFER_CLSS,is_first_feijie.isChecked()==true?"1":"0");
        config.setParam(context,BusinessConfig.Key.FLAG_USE_INTEGRAL,is_query_integral.isChecked()==true?"1":"0");
        config.setParam(context,BusinessConfig.Key.FLAG_USE_DISCOUNT,is_query_discount.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.PARAM_MOST_REFUND,finalAmt+"");

        config.setParam(context,BusinessConfig.Key.FLAG_SCAN_QUERY_COUNT,scanQueryCountStr+"");
        config.setParam(context,BusinessConfig.Key.FLAG_SCAN_WAIT_TIME,scanWaitTimeStr+"");

        config.setParam(context, BusinessConfig.Key.FLAG_IS_OPEN_SIGN,is_sign.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_TIP_COMFIRM_SIGN,tip_comfirm_sign.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.PARAM_SIGN_OUT_TIME,signOutTime);
        config.setParam(context, BusinessConfig.Key.PARAM_SIGN_UPLOAD_TIME,reloadSignCount);
        config.setParam(context, BusinessConfig.Key.PARAM_MAX_SIGN_CNT,maxSignCnt);
        config.setParam(context, BusinessConfig.Key.PARAM_MAX_RESIGN_CNT,maxResign);
        config.setParam(context, BusinessConfig.Key.PARAM_DEFINE_HEAD,defineHeadStr);
        ViewUtils.showToast(context, getString(R.string.tip_save_success));
        Intent intent = new Intent();
        intent.setAction("refresh");
        sendBroadcast(intent);
        activityStack.pop();
    }


    private void inputMoney(KeyEvent keyEvent){
        char keyValue = (char) -1;
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            return;
        }
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_0) {
            keyValue = '0';
        } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_1) {
            keyValue = '1';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_2) {
            keyValue = '2';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_3) {
            keyValue = '3';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_4) {
            keyValue = '4';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_5) {
            keyValue = '5';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_6) {
            keyValue = '6';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_7) {
            keyValue = '7';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_8) {
            keyValue = '8';
        }else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_9) {
            keyValue = '9';
        }
        if (max_refund_amt != null) {
            String text = max_refund_amt.getText().toString();
            double value = Double.valueOf(text);
            String changedText = text;
            switch (keyValue) {
                //删除
                case (char) -1:
                    if (value == 0.0) {
                        return;
                    }
                    changedText = formatter.format(value * 0.1) + "";
                    break;
                //小数点
                case '.':
                    break;
                //其他数字字符
                default:
                    if (text.length() > 8) {
                        return;
                    }
                    if (value == 0.0) {
                        changedText = Integer.valueOf(String.valueOf(keyValue)) * 0.01 + "";
                    } else {
                        changedText = (value * 10 + Integer.valueOf(String.valueOf(keyValue)) * 0.01) + "";
                    }
                    break;


            }
            changedText = "" + formatter.format(Double.valueOf(changedText));
            max_refund_amt.setText(changedText);
        }

    }

}

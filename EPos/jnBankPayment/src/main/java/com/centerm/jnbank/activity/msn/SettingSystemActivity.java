package com.centerm.jnbank.activity.msn;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.ElecSignInfo;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.utils.ViewUtils;

import java.sql.SQLException;

import config.BusinessConfig;


/**
 * 系统参数配置
 * Created by ysd on 2016/11/30.
 */

public class SettingSystemActivity extends BaseActivity {

    private EditText batchNo;
    private EditText batchFlowNo,print_count,reverce_count,max_trans_count;
    private Button modify;
    private CheckBox is_receive,is_send, is_print_paper, is_print_code,is_print_english, isMinus,is_auth_no;
    @Override
    public int onLayoutId() {
        return R.layout.activity_system_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_system);
        batchNo = (EditText) findViewById(R.id.extxt_batch_no);
        batchFlowNo = (EditText) findViewById(R.id.extxt_batch_flow_no);
        print_count = (EditText) findViewById(R.id.print_count);
        reverce_count = (EditText) findViewById(R.id.reverce_count);
        max_trans_count = (EditText) findViewById(R.id.max_trans_count);
        is_receive = (CheckBox) findViewById(R.id.is_receive);
        is_send = (CheckBox) findViewById(R.id.is_send);
        is_print_paper = (CheckBox) findViewById(R.id.print_paper);
        is_print_code = (CheckBox) findViewById(R.id.print_code);
        is_print_english = (CheckBox) findViewById(R.id.is_print_english);
        isMinus = (CheckBox) findViewById(R.id.is_mult);
        is_auth_no = (CheckBox) findViewById(R.id.is_auth_no);
        modify = (Button) findViewById(R.id.modify_system);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        BusinessConfig config = BusinessConfig.getInstance();
        String batchNoStr = config.getBatchNo(context);
        String terFlowStr = config.getPosSerial(context, false, false);
        String printCount = config.getParam(context, BusinessConfig.Key.PARAM_PRINT_COUNT);
        int reverseCount = config.getNumber(context, BusinessConfig.Key.PARAM_REVERSE_COUNT);
        int maxTransCount = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.PARAM_MOST_TRANS);
        String isSend = config.getParam(context, BusinessConfig.Key.FLAG_ISS_CHINESE);
        String isRec = config.getParam(context, BusinessConfig.Key.FLAG_REC_CHINESE);
        String isFeildCard = config.getParam(context, BusinessConfig.Key.FLAG_SHIELD_CARD);
        String isPrintPaper = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_PAPER);
        String isPrintCode = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_QRCODE);
        String isPrintEnglish = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_ENGLISH);
        String isPrintMinus = config.getParam(context, BusinessConfig.Key.FLAG_PRINT_MINUS);
        if (null != batchNoStr) {
            batchNo.setText(batchNoStr);
        }
        if (null != terFlowStr) {
            batchFlowNo.setText(terFlowStr);
        }
        if (null != printCount) {
            print_count.setText(printCount);
        }
        if (reverseCount>0) {
            reverce_count.setText(reverseCount+"");
        }
        if (maxTransCount>0) {
            max_trans_count.setText(maxTransCount+"");
        }
        is_receive.setChecked("1".equals(isRec)?true:false);
        is_send.setChecked("1".equals(isSend)?true:false);
        is_auth_no.setChecked("1".equals(isFeildCard)?true:false);
        is_print_english.setChecked("1".equals(isPrintEnglish)?true:false);
        is_print_paper.setChecked("1".equals(isPrintPaper)?true:false);
        is_print_code.setChecked("1".equals(isPrintCode)?true:false);
        isMinus.setChecked("1".equals(isPrintMinus)?true:false);
    }

    public void systemModify(View view) {
        String batchNoStr = batchNo.getText().toString().trim();
        String batchFlowNoStr = batchFlowNo.getText().toString().trim();
        String printCountStr = print_count.getText().toString().trim();
        String reverceCountStr = reverce_count.getText().toString().trim();
        String maxTransCountStr = max_trans_count.getText().toString().trim();
        if (null == batchNoStr || "".equals(batchNoStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_batch_number_not_null));
            return;
        }
        if (null == batchFlowNoStr || "".equals(batchFlowNoStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_batch_flow_not_null));
            return;
        }
      if (batchNoStr.length() < 6) {
            ViewUtils.showToast(context, getString(R.string.tip_batch_number));
            return;
        }
        if (batchFlowNoStr.length() < 6) {
            ViewUtils.showToast(context, getString(R.string.tip_batch_flow));
            return;
        }
        if ("000000".equals(batchNoStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_batch_number_not_zero));
            return;
        }
        if ("000000".equals(batchFlowNoStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_batch_flow_not_zero));
            return;
        }
        if ("999999".equals(batchNoStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_batch_number_not_more));
            return;
        }
        /*  if (StringUtils.isStrNull(printCountStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_print_count_not_null));
            return;
        }
        if (StringUtils.isStrNull(maxTransCountStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_max_trans_count));
            return;
        }
        if (StringUtils.isStrNull(reverceCountStr)) {
            ViewUtils.showToast(context, getString(R.string.tip_reverce_count));
            return;
        }

        int printC = Integer.parseInt(printCountStr);
        if (printC > 3||printC==0) {
            ViewUtils.showToast(context, getString(R.string.tip_print_count));
            return;
        }
        int maxCount = Integer.parseInt(maxTransCountStr);
        if (maxCount <=0) {
            ViewUtils.showToast(context,"最大交易笔数不能为0");
            return;
        }
        int reverseC = Integer.parseInt(reverceCountStr);
        if (reverseC > 3||reverseC==0) {
            ViewUtils.showToast(context, getString(R.string.tip_reverse_count));
            return;
        }*/
        try {
            CommonManager commonManager = new CommonManager(TradeInfo.class, context);
            long counts = commonManager.getBatchCount();
            CommonManager commonManager2 = new CommonManager(ElecSignInfo.class, context);
            long signCount = commonManager2.getSignCount();
            String tempCount = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.PARAM_MAX_SIGN_CNT);
            int configSign = Integer.parseInt(tempCount);
            logger.info("已存储交易流水数量==>" + counts);
            if (counts >= 500) {
                logger.warn("交易流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, true);
            } else {
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, false);
            }
            logger.info("已存储电子签名流水数量==>" + signCount);
            if (signCount >= configSign) {
                logger.warn("电子签名流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ViewUtils.showToast(context, getString(R.string.tip_max_tran_query));
            return;
        }
        BusinessConfig config = BusinessConfig.getInstance();
        config.setBatchNo(context, batchNo.getText().toString().trim());
        config.setPosSerial(context, batchFlowNo.getText().toString().trim());
        config.setParam(context,BusinessConfig.Key.PARAM_PRINT_COUNT,printCountStr+"");
        config.setNumber(context,BusinessConfig.Key.PARAM_REVERSE_COUNT,Integer.parseInt(reverceCountStr));
        config.setNumber(context,BusinessConfig.Key.PARAM_MOST_TRANS,500);
        config.setParam(context, BusinessConfig.Key.FLAG_ISS_CHINESE,is_send.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_REC_CHINESE,is_receive.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_SHIELD_CARD,is_auth_no.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_PRINT_PAPER,is_print_paper.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_PRINT_QRCODE,is_print_code.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_PRINT_ENGLISH,is_print_english.isChecked()==true?"1":"0");
        config.setParam(context, BusinessConfig.Key.FLAG_PRINT_MINUS,isMinus.isChecked()==true?"1":"0");
        ViewUtils.showToast(context, getString(R.string.tip_save_success));
        activityStack.pop();
    }
}

package com.centerm.jnbank.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.utils.ViewUtils;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import static com.centerm.jnbank.common.TransDataKey.iso_f11_origin;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f37;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f60_origin;
import static com.centerm.jnbank.common.TransDataKey.key_entryReferenceNo;
import static com.centerm.jnbank.common.TransDataKey.key_entryTraceNo;
import static com.centerm.jnbank.common.TransDataKey.key_oriAuthCode;
import static com.centerm.jnbank.common.TransDataKey.key_oriReference;
import static com.centerm.jnbank.common.TransDataKey.key_oriTransTime;

/**
 * 输入信息界面，例如消费撤销、退货等业务，需要输入原始交易信息，都在该界面完成。
 * 其中消费撤销，需要输入凭证号；退货需要输入原交易参考号、交易日期、退货金额
 * author:wanliang527</br>
 * date:2016/11/2</br>
 */

public class InputOriginInfoActivity extends BaseTradeActivity {

    private EditText posSerialEdit, tradeRefNoEdit, dateEdit, authCodeEdit, authDateEdit;
    private CommonDao<TradeInfo> dao;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        dao = new CommonDao<>(TradeInfo.class, dbHelper);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_input_origin_info;
    }

    @Override
    public void onInitView() {
//        setTitle(R.string.title_input_pos_serial);
        switch (transCode) {
            case TransCode.VOID:
            case TransCode.SCAN_CANCEL:
                findViewById(R.id.void_input_block).setVisibility(View.VISIBLE);
                findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                break;
            case TransCode.REFUND:
                findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                findViewById(R.id.void_input_block).setVisibility(View.GONE);
                findViewById(R.id.refund_input_block).setVisibility(View.VISIBLE);
                break;
            case TransCode.CANCEL:
                findViewById(R.id.auth_input_block).setVisibility(View.VISIBLE);
                findViewById(R.id.void_input_block).setVisibility(View.GONE);
                findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                break;
            case TransCode.AUTH_COMPLETE:
                findViewById(R.id.auth_input_block).setVisibility(View.VISIBLE);
                findViewById(R.id.void_input_block).setVisibility(View.GONE);
                findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                break;
            case TransCode.AUTH_SETTLEMENT:
                findViewById(R.id.auth_input_block).setVisibility(View.VISIBLE);
                findViewById(R.id.void_input_block).setVisibility(View.GONE);
                findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                break;
            case TransCode.COMPLETE_VOID:
                findViewById(R.id.void_input_block).setVisibility(View.VISIBLE);
                findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                break;
            case TransCode.SCAN_REFUND_W:
            case TransCode.SCAN_REFUND_Z:
            case TransCode.SCAN_REFUND_S:
                findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                findViewById(R.id.void_input_block).setVisibility(View.GONE);
                findViewById(R.id.refund_input_block).setVisibility(View.VISIBLE);
                break;
        }


        posSerialEdit = (EditText) findViewById(R.id.ori_pos_serial_edit);
        tradeRefNoEdit = (EditText) findViewById(R.id.trade_ref_no_edit);
        dateEdit = (EditText) findViewById(R.id.origin_date_edit);
        authCodeEdit = (EditText) findViewById(R.id.orig_auth_code_edit);
        authDateEdit = (EditText) findViewById(R.id.orig_auth_date_edit);

        if (entryFlag) {
            if (null != dataMap.get(key_entryTraceNo)) {
                posSerialEdit.setText(dataMap.get(key_entryTraceNo));
                posSerialEdit.setEnabled(false);
            }
            if (null != dataMap.get(key_entryReferenceNo)) {
                tradeRefNoEdit.setText(dataMap.get(key_entryReferenceNo));
                tradeRefNoEdit.setEnabled(false);
                dateEdit.requestFocus();
            }
        }
    }

    @Override
    public void onBackPressed() {
        activityStack.backTo(MainActivity.class);
        if (pbocService != null) {
            pbocService.abortProcess();
        }
    }

    public void onConfirmClick(View view) {
        String posSerial = posSerialEdit.getText().toString();
        String platSerial = tradeRefNoEdit.getText().toString();
        String date = dateEdit.getText().toString();
        String authCode = authCodeEdit.getText().toString();
        String authDate = authDateEdit.getText().toString();





        switch (transCode) {
            case TransCode.VOID:
                if (TextUtils.isEmpty(posSerial)) {
                    ViewUtils.showToast(context, "请输入凭证号");
                    return;
                }
                if (posSerial.length() != 6) {
                    ViewUtils.showToast(context,"凭证号长度为6");
                    return;
                }
                TradeInfo oriInfo = null;
                try {
                    oriInfo = queryAndInitTradeData(posSerial);
                    if (oriInfo != null) {
                       /* if (oriInfo.getFlag() == 2) {
                            ViewUtils.showToast(context, "该交易已撤销");
                        } else if (oriInfo.getFlag() == 3) {
                            ViewUtils.showToast(context, "该交易已冲正");
                        } else if (oriInfo.getFlag() == 4) {
                            ViewUtils.showToast(context, "该交易已退货");
                        } else {*/
                            jumpToNext(KEY_ORIGIN_INFO, oriInfo);
                        //}
                    } /*else {
                        ViewUtils.showToast(context, "未找到对应交易流水");
                    }*/
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case TransCode.REFUND:
                if (TextUtils.isEmpty(platSerial)) {
                    ViewUtils.showToast(context, "请输入检索参考号");
                    return;
                }
                if (platSerial.length() != 12) {
                    ViewUtils.showToast(context,"检索参考号长度为12");
                    return;
                }
                if (TextUtils.isEmpty(date)) {
                    ViewUtils.showToast(context, "请输入交易日期");
                    return;
                }
                if (date.length() != 4) {
                    ViewUtils.showToast(context,"日期长度为4");
                    return;
                }
                dataMap.put(key_oriReference, platSerial);
                dataMap.put(key_oriTransTime, date);
                jumpToNext();
                break;
            case TransCode.SCAN_REFUND_W:
            case TransCode.SCAN_REFUND_Z:
            case TransCode.SCAN_REFUND_S:
                if (TextUtils.isEmpty(platSerial)) {
                    ViewUtils.showToast(context, "请输入检索参考号");
                    return;
                }
                if (platSerial.length() != 12) {
                    ViewUtils.showToast(context,"检索参考号长度为12");
                    return;
                }
                if (TextUtils.isEmpty(date)) {
                    ViewUtils.showToast(context, "请输入交易日期");
                    return;
                }
                if (date.length() != 4) {
                    ViewUtils.showToast(context,"日期长度为4");
                    return;
                }
                dataMap.put(key_oriReference, platSerial);
                dataMap.put(key_oriTransTime, date);
               /* oriInfo = queryAndInitTradeDataByBefer(platSerial);
                if (oriInfo != null) {
                    String iso47 = oriInfo.getIso_f47();
                    logger.info("获取到原交易信息47域："+iso47);
                    if(!TextUtils.isEmpty(iso47)){
                        dataMap.put(iso_f47,iso47.split("\\|")[0]);
                    }
                }*/
                jumpToNext();
                break;
            case TransCode.CANCEL:
                if (TextUtils.isEmpty(authCode)) {
                    ViewUtils.showToast(context, "请输入原授权码");
                    return;
                }
                if (authCode.length() != 6) {
                    ViewUtils.showToast(context,"原授权码长度为6");
                    return;
                }
                if (TextUtils.isEmpty(authDate)) {
                    ViewUtils.showToast(context, "请输入交易日期");
                    return;
                }
                if (authDate.length() != 4) {
                    ViewUtils.showToast(context,"日期长度为4");
                    return;
                }
                dataMap.put(key_oriAuthCode, authCode);
                dataMap.put(key_oriTransTime, authDate);
                jumpToNext();
                break;
            case TransCode.AUTH_COMPLETE:
                if (TextUtils.isEmpty(authCode)) {
                    ViewUtils.showToast(context, "请输入原授权码");
                    return;
                }
                if (authCode.length() != 6) {
                    ViewUtils.showToast(context,"原授权码长度为6");
                    return;
                }
                if (TextUtils.isEmpty(authDate)) {
                    ViewUtils.showToast(context, "请输入交易日期");
                    return;
                }
                if (authDate.length() != 4) {
                    ViewUtils.showToast(context,"日期长度为4");
                    return;
                }
                dataMap.put(key_oriAuthCode, authCode);
                dataMap.put(key_oriTransTime, authDate);
                jumpToNext();
                break;
            case TransCode.AUTH_SETTLEMENT:
                if (TextUtils.isEmpty(authCode)) {
                    ViewUtils.showToast(context, "请输入原授权码");
                    return;
                }
                if (authCode.length() != 6) {
                    ViewUtils.showToast(context,"原授权码长度为6");
                    return;
                }
                if (TextUtils.isEmpty(authDate)) {
                    ViewUtils.showToast(context, "请输入交易日期");
                    return;
                }
                if (authDate.length() != 4) {
                    ViewUtils.showToast(context,"日期长度为4");
                    return;
                }
                dataMap.put(key_oriAuthCode, authCode);
                dataMap.put(key_oriTransTime, authDate);
                jumpToNext();
                break;
            case TransCode.COMPLETE_VOID:
                if (TextUtils.isEmpty(posSerial)) {
                    ViewUtils.showToast(context, "请输入凭证号");
                    return;
                }
                if (posSerial.length() != 6) {
                    ViewUtils.showToast(context,"凭证号长度为6");
                    return;
                }
                try {
                    oriInfo = queryAndInitTradeData(posSerial);
                    if (oriInfo != null) {
                        dataMap.put(TransDataKey.key_voucherNo, posSerial);
                        jumpToNext(KEY_ORIGIN_INFO, oriInfo);
                    } /*else {
                        ViewUtils.showToast(context, "无法找到对应交易流水");
                    }*/
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case TransCode.SCAN_CANCEL:
                if (TextUtils.isEmpty(posSerial)) {
                    ViewUtils.showToast(context, "请输入凭证号");
                    return;
                }
                try {
                    oriInfo = queryAndInitTradeData(posSerial);
                    if (oriInfo != null) {
                        dataMap.put(TransDataKey.key_voucherNo, posSerial);
                        jumpToNext(KEY_ORIGIN_INFO, oriInfo);
                    } /*else {
                        ViewUtils.showToast(context, "无法找到对应交易流水");
                    }*/
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    /**
     * 根据凭证号查找交易信息，如果存在则初始化交易数据
     *
     * @param iso11 凭证号
     * @return 查找到相关交易返回true，否则返回false
     */
    private TradeInfo queryAndInitTradeData(String iso11) throws SQLException {
        TradeInfo tradeInfo = dao.queryForId(iso11);
        if (tradeInfo == null) {
            logger.warn(iso11 + "==>无法找到该流水信息");
            //如果找不到对应的交易流水，或者流水对应的交易类型不符
            ViewUtils.showToast(context, "未找到对应交易流水");
            return tradeInfo;
        }
        if(TransCode.COMPLETE_VOID.equals(transCode) && !TransCode.AUTH_COMPLETE.equals(tradeInfo.getTransCode())){
            logger.warn(iso11 + "==>该笔流水对应的交易类型不符合");
            ViewUtils.showToast(context, "交易类型不符合");
            return null;
        }
        if (tradeInfo.getFlag() == 3) {
            logger.warn(iso11 + "==>流水已冲正");
            ViewUtils.showToast(context, "流水已冲正");
            return null;
        }

        if (tradeInfo.getFlag() == 2) {
            logger.warn(iso11 + "==>流水已撤销");
            ViewUtils.showToast(context, "流水已撤销");
            return null;
        }

        if (tradeInfo.getFlag() == 0){
            logger.warn(iso11 + "==>流水状态不合法");
            ViewUtils.showToast(context, "未找到对应交易流水");
            return null;
        }
        if (transCode.equals(TransCode.VOID) && !tradeInfo.getTransCode().equals(TransCode.SALE)) {
            logger.warn(iso11 + "==>该笔流水对应的交易类型不符合");
            ViewUtils.showToast(context, "交易类型不符合");
            return null;
        }
        if (transCode.equals(TransCode.SCAN_CANCEL) && !tradeInfo.getTransCode().equals(TransCode.SCAN_PAY_WEI)&& !tradeInfo.getTransCode().equals(TransCode.SCAN_PAY_ALI)&& !tradeInfo.getTransCode().equals(TransCode.SCAN_PAY_SFT)) {
            logger.warn(iso11 + "==>该笔流水对应的交易类型不符合");
            ViewUtils.showToast(context, "交易类型不符合");
            return null;
        }

        logger.debug("原交易信息：" + tradeInfo.toString());
        dataMap.put(iso_f4, tradeInfo.getIso_f4());//金额
        dataMap.put(iso_f22, "000");//服务点输入码，默认为未指明
        dataMap.put(iso_f37, tradeInfo.getIso_f37());//检索参考号
        dataMap.put(key_oriAuthCode, tradeInfo.getIso_f38());//授权标识应答码
        dataMap.put(iso_f41, tradeInfo.getIso_f41());//受卡机终端标识码
        dataMap.put(iso_f42, tradeInfo.getIso_f42());//受卡方标识码
        dataMap.put(iso_f11_origin, tradeInfo.getIso_f11());//原流水号
        dataMap.put(iso_f60_origin, tradeInfo.getIso_f60());//原交易60域中包含原批次号
        dataMap.put(key_oriReference, tradeInfo.getIso_f37());
        dataMap.put(key_oriTransTime, tradeInfo.getIso_f13());
        return tradeInfo;
    }
    private TradeInfo queryAndInitTradeDataByBefer(String iso37) {
        TradeInfo tradeInfo = null;
        QueryBuilder builder = dao.queryBuilder();
        List<TradeInfo> tradeInfos = null;
        try {
            tradeInfos = builder.where().eq("iso_f37", iso37).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (null != tradeInfos && tradeInfos.size() > 0) {
            tradeInfo = tradeInfos.get(0);
        }
        if (tradeInfo == null) {
            logger.warn(iso37 + "==>无法找到该参考号信息");
            //如果找不到对应的交易流水，或者流水对应的交易类型不符
            return tradeInfo;
        }
        if ("1".equals(tradeInfo.getFlag())) {
            logger.warn(iso37 + "==>流水已撤销");
            return null;
        }
        if (transCode.equals(TransCode.VOID) && !tradeInfo.getTransCode().equals(TransCode.SALE)) {
            logger.warn(iso37 + "==>该笔流水对应的交易类型不符合");
            return null;
        }

        logger.debug("原交易信息：" + tradeInfo.toString());
        dataMap.put(iso_f11_origin, tradeInfo.getIso_f11());//原流水号
        dataMap.put(iso_f60_origin, tradeInfo.getIso_f60());//原交易60域中包含原批次号
        return tradeInfo;
    }
}

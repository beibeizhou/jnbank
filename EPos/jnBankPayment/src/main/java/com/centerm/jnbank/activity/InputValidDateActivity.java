package com.centerm.jnbank.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.utils.ViewUtils;

import static com.centerm.jnbank.common.TransDataKey.iso_f14;

/**
 * 手输卡号时，输入卡有效期界面
 */

public class InputValidDateActivity extends BaseTradeActivity {

    private EditText validDate;
    private CommonDao<TradeInfo> dao;
    private String reg = "^[0-9]{4}$";
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
        return R.layout.activity_input_valid_date;
    }

    @Override
    public void onInitView() {
        validDate = (EditText) findViewById(R.id.valid_date);

    }

    @Override
    public void onBackPressed() {
        activityStack.backTo(MainActivity.class);
        if (pbocService != null) {
            pbocService.abortProcess();
        }
    }

    public void onConfirmClick(View view) {
        String date = validDate.getText().toString();
        if (!date.matches(reg)) {
            ViewUtils.showToast(context,"请输入正确的卡有效期");
            return;
        }
        dataMap.put(iso_f14,date);
        jumpToNext();
    }
}

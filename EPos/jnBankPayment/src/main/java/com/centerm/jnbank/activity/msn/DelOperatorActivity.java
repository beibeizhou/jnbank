package com.centerm.jnbank.activity.msn;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.utils.ViewUtils;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;


/**
 * Created by linwenhui on 2016/10/31.
 */

public class DelOperatorActivity extends BaseActivity {

    private Dao<Employee, String> employeeCommonDao;
    EditText optNo;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_del_operator;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        try {
            employeeCommonDao = dbHelper.getDao(Employee.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.removeDao(Employee.class);
        employeeCommonDao = null;
        super.onDestroy();
    }

    @Override
    public void onInitView() {
        TextView txtvwTitle = (TextView) findViewById(R.id.txtvw_title);
        txtvwTitle.setText(R.string.label_opt_del);
        optNo = (EditText) findViewById(R.id.opt_no);
    }

    public void onDelClick(View v) {
        String tagOptNo = optNo.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(tagOptNo) || tagOptNo.length() < 2) {
            ViewUtils.showToast(this, R.string.tip_please_input_opt_no);
            return;
        }

        if (tagOptNo.compareTo("01") < 0 || tagOptNo.compareTo("89") > 0) {
            ViewUtils.showToast(this, R.string.tip_opt_no_limit);
            return;
        }
        boolean ret = false;
        try {
            ret = employeeCommonDao.queryForId(tagOptNo) != null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (ret) {
            try {
                ret = employeeCommonDao.deleteById(tagOptNo) == 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (ret) {
                ViewUtils.showToast(this, R.string.tip_opt_delete_suc);
                activityStack.pop();
            } else {
                ViewUtils.showToast(this, R.string.tip_opt_delete_failure);
            }
        } else {
            ViewUtils.showToast(this, R.string.tip_opt_not_exsit);
        }

    }
}

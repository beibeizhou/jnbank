package com.centerm.jnbank.activity.msn;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Config;




/**
 * @author linwenhui
 * 修改密码界面，包括主管和操作员
 * @date 2016/10/28.
 */

public class ChangePwdMSNActivity extends BaseActivity {

    EditText editOldPwd;
    EditText edtxtNewPwd;
    EditText edtxtRenewPwd;

    CommonDao<Employee> employeeCommonDao;
    private String optId;
    private String currentOpt;
    private int maxLenth = 0;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_change_pwd_msn;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        employeeCommonDao = new CommonDao<>(Employee.class, dbHelper);
    }

    @Override
    public void onInitView() {
        optId = getIntent().getStringExtra("operaterId");
        TextView title = (TextView) findViewById(R.id.txtvw_title);
        editOldPwd = (EditText) findViewById(R.id.edtxt_old_pwd);
        edtxtNewPwd = (EditText) findViewById(R.id.edtxt_new_pwd);
        edtxtRenewPwd = (EditText) findViewById(R.id.edtxt_renew_pwd);
        if (null == optId) {
            currentOpt = Config.DEFAULT_MSN_ACCOUNT;
            if ("00".equals(currentOpt)) {
                title.setText(R.string.label_change_pwd_msn);
                editOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                edtxtNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                edtxtRenewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                maxLenth = 6;
            } else if ("99".equals(currentOpt)){
                title.setText(R.string.label_change_pwd_sys);
                editOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                edtxtNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                edtxtRenewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                maxLenth = 8;
            }
        } else {
            title.setText(R.string.label_change_opt_pwd);
            editOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            edtxtNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            edtxtRenewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            maxLenth= 4;
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.removeDao(Employee.class);
        employeeCommonDao = null;
        super.onDestroy();
    }

    public final String[] onValibPwd() {
        final String oldPwd = editOldPwd.getText().toString().trim().replace(" ", "");
        final String pwd = edtxtNewPwd.getText().toString().trim().replace(" ", "");
        final String rePwd = edtxtRenewPwd.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(oldPwd)) {
            ViewUtils.showToast(this, R.string.tip_old_pwd_empty);
            return null;
        }
        if (TextUtils.isEmpty(pwd)) {
            ViewUtils.showToast(this, R.string.tip_new_pwd_empty);
            return null;
        }
        if (TextUtils.isEmpty(rePwd)) {
            ViewUtils.showToast(this, R.string.tip_renew_pwd_empty);
            return null;
        }
        if (!pwd.equals(rePwd)) {
            ViewUtils.showToast(this, R.string.tip_pwd_not_same);
            return null;
        }
        if (pwd.length()!=maxLenth) {
            ViewUtils.showToast(this, "新密码需为"+maxLenth+"位");
            return null;
        }
        if (rePwd.length()!=maxLenth) {
            ViewUtils.showToast(this, "新密码需为"+maxLenth+"位");
            return null;
        }
        return new String[]{oldPwd, pwd};

    }


    public void onChangePwd(View v) {
        String[] pwds = onValibPwd();
        if (pwds != null) {

            Map<String, String> conditions = new HashMap<>();
            if (null != optId) {
                conditions.put("code", optId);
            } else if (null==optId&&"00".equals(currentOpt)){
                conditions.put("code", Config.DEFAULT_MSN_ACCOUNT);
            } else if (null == optId && "99".equals(currentOpt)) {
                conditions.put("code", Config.DEFAULT_ADMIN_ACCOUNT);
            }
            conditions.put("password", pwds[0]);
            List<Employee> employees = employeeCommonDao.queryByMap(conditions);
            if (employees != null && !employees.isEmpty()) {
                Employee employee = employees.get(0);
                employee.setPassword(pwds[1]);
                final boolean res = employeeCommonDao.update(employee);
                if (res) {
                    ViewUtils.showToast(this, R.string.tip_pwd_suc);
                    onBackPressed();
                }
            }else {
                ViewUtils.showToast(this,R.string.tip_pwd_old_pwd_error);
            }
        }
    }


}

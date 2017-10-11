package com.centerm.jnbank.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.base.MenuActivity;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;


/**
 * @author linwenhui
 * @date 2016/11/2.
 */

public class LoginActivity extends BaseActivity {
    private EditText edtxtAccount, edtxtPwd;
    private TextView title;
    private CommonDao<Employee> employeeCommonDao;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        employeeCommonDao = new CommonDao<>(Employee.class, dbHelper);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void onInitView() {
        title = (TextView) findViewById(R.id.txtvw_title);
        edtxtAccount = (EditText) findViewById(R.id.account_edit);
        edtxtPwd = (EditText) findViewById(R.id.pwd_edit);
        edtxtAccount.addTextChangedListener(new CutPassword());
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        title.setText("柜员签到");
    }

    public void onLoginClick(View v) {
        String tagAccount = edtxtAccount.getText().toString().trim().replace(" ", "");
        String tagPwd = edtxtPwd.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(tagAccount) || tagAccount.length() < 2) {
            ViewUtils.showToast(this, R.string.tip_please_input_account);
            return;
        }
        //系统管理员8位密码，主管操作员6位密码，一般操作员4位密码
        if (TextUtils.isEmpty(tagPwd)) {
            ViewUtils.showToast(this, R.string.label_login_input_pwd_empth);
            return;
        } else if (tagPwd.length() < 4) {
            ViewUtils.showToast(this, R.string.tip_pwd_length_illegal);
            return;
        }
        Map<String, String> conditions = new HashMap<>();
        conditions.put("code", tagAccount);
        List<Employee> employees = employeeCommonDao.queryByMap(conditions);
        if (employees != null && !employees.isEmpty()) {
            conditions.put("password", tagPwd);
            employees = employeeCommonDao.queryByMap(conditions);
            if (employees != null && !employees.isEmpty()) {
                if (Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount)) {
                    //系统管理员账号
                    //ViewUtils.showToast(context, R.string.tip_login_admin_suc);
                    jumpToManagerView(true);
                /*    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                            .KEY_OPER_ID, tagAccount);*/
                } else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
                    //主管操作员
                    //ViewUtils.showToast(context, R.string.tip_login_msn_suc);
                    jumpToManagerView(false);
                /*    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                            .KEY_OPER_ID, tagAccount);*/
                } else {
                    //普通操作员
                    if (Settings.hasTmk(context)) {
                       // ViewUtils.showToast(context, R.string.tip_login_opt_suc);
                        //未签退情况下，操作员直接退出，如果与上次登录的操作员号不同则要求重新签到
                        String operator = BusinessConfig.getInstance().getValue(context,
                                BusinessConfig.Key.KEY_OPER_ID);
                        if (!tagAccount.equals(operator)) {
                            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key
                                    .FLAG_SIGN_IN, false);
                        }
                        BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                                .KEY_OPER_ID, tagAccount);
                        jumpToMain();
                    } else {
                        DialogFactory.showMessageDialog(context, null, getString(R.string
                                .tip_login_to_down));
                    }
                }
            } else {
                ViewUtils.showToast(this, R.string.tip_login_pwd_error);
                edtxtPwd.setText("");
            }
        } else {
            ViewUtils.showToast(this, R.string.tip_account_not_exist);
        }
    }

    public class CutPassword implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() < 2)
                return;
            String tagAccount = edtxtAccount.getText().toString().trim().replace(" ", "");
            String tagPwd = edtxtPwd.getText().toString().trim().replace(" ", "");
            if (Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount)) {
                edtxtPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            } else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
                edtxtPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                if (tagPwd.length() > 6) {
                    edtxtPwd.setText(tagPwd.substring(0, 6));
                }
            } else {
                edtxtPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                if (tagPwd.length() > 4) {
                    edtxtPwd.setText(tagPwd.substring(0, 4));
                }
            }
        }
    }

    private void jumpToMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activityStack.backTo(MainActivity.class);
            }
        }, 300);
    }

    private void jumpToManagerView(boolean isAdmin) {
        Intent intent = new Intent(context, MenuActivity.class);
        if (isAdmin) {
            intent.putExtra(KEY_USER_FLAG, 1);
        } else {
            intent.putExtra(KEY_USER_FLAG, 2);
        }
        activityStack.pop();
        startActivity(intent);
    }
}

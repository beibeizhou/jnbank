package com.centerm.jnbank.activity;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;




/**
 * Created by ysd on 2016/11/30.
 */

public class LockAcitivty extends BaseActivity {

    private EditText optId,optPsw;
    private CommonDao<Employee> employeeCommonDao;

    @Override
    public int onLayoutId() {
        return R.layout.activity_lock;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText("已锁机");
        hideBackBtn();
        optId = (EditText) findViewById(R.id.et_opt_id);
        optPsw = (EditText) findViewById(R.id.et_opt_psw);
        optId.addTextChangedListener(new CutPassword());
        Button unLock = (Button) findViewById(R.id.unlock);
        unLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String optIdStr = optId.getText().toString().trim();
                String optPswStr = optPsw.getText().toString().trim();
                if (null == optIdStr || "".equals(optIdStr)) {
                    ViewUtils.showToast(context,"请输入账号！");
                    return;
                }
                if (null == optPswStr || "".equals(optPswStr)) {
                    ViewUtils.showToast(context,"请输入密码！");
                    return;
                }

                Map<String, String> conditions = new HashMap<>();
                conditions.put("code", optIdStr);
                conditions.put("password", optPswStr);
                if (Config.DEFAULT_MSN_ACCOUNT.equals(optIdStr)||Config.DEFAULT_ADMIN_ACCOUNT.equals(optIdStr)) {
                    logger.debug("主管或系统管理员账号");
                    List<Employee> employees = employeeCommonDao.queryByMap(conditions);
                    if (employees != null && !employees.isEmpty()) {
                        activityStack.pop();
                       /* DialogFactory.showSelectDialog(context, getString(R.string.tip_notification), getString(R.string.tips_need_auto_sign_out), new AlertDialog.ButtonClickListener() {
                            @Override
                            public void onClick(ButtonType button, View v) {
                                switch (button) {
                                    case POSITIVE:
                                        finish();
                                        break;
                                    case NEGATIVE:
                                        optPsw.setText("");
                                        break;
                                }
                            }
                        });*/
                    } else {
                        ViewUtils.showToast(context, R.string.tip_login_pwd_error);
                        optPsw.setText("");
                    }
                } else if (optIdStr.equals(BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID))) {
                    logger.debug("原操作员登录");
                    List<Employee> employees = employeeCommonDao.queryByMap(conditions);
                    if (employees != null && !employees.isEmpty()) {
                        activityStack.pop();
                    } else {
                        ViewUtils.showToast(context, R.string.tip_login_pwd_error);
                        optPsw.setText("");
                    }
                } else {
                    ViewUtils.showToast(context,"请输入原操作员或主管账号！");
                    optPsw.setText("");
                }
            }
        });
    }
    @Override
    public void afterInitView() {
        super.afterInitView();
        DbHelper dbHelper = new DbHelper(context);
        employeeCommonDao = new CommonDao<Employee>(Employee.class, dbHelper);
    }
    @Override
    public void onBackPressed() {
        DialogFactory.showMessageDialog(context, getString(R.string.tip_notification), getString(R.string.tips_need_unlock), new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
            }
        });
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
            String tagAccount = optId.getText().toString().trim().replace(" ", "");
            String tagPwd = optPsw.getText().toString().trim().replace(" ", "");
            if (Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount)) {
                optPsw.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            } else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
                optPsw.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                if (tagPwd.length() > 6) {
                    optPsw.setText(tagPwd.substring(0, 6));
                }
            } else {
                optPsw.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                if (tagPwd.length() > 4) {
                    optPsw.setText(tagPwd.substring(0, 4));
                }
            }
        }
    }
}
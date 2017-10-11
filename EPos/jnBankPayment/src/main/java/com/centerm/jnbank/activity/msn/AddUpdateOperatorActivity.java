package com.centerm.jnbank.activity.msn;

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
import java.util.HashMap;
import java.util.Map;

import config.Config;



/**
 * @author linwenhui
 * @date 2016/10/28.
 */

public class AddUpdateOperatorActivity extends BaseActivity {

    private int type = Config.OPT_TYPE_CREATE;
    Dao<Employee, String> employeeCommonDao;
    TextView txtvwTitle;
    EditText optNo, pwd, pwdConfirm;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_add_setting_operator;
    }

    @Override
    public void onInitView() {
        type = getIntent().getIntExtra(Config.OPT_TYPE_TIP, type);
        txtvwTitle = (TextView) findViewById(R.id.txtvw_title);
        optNo = (EditText) findViewById(R.id.opt_no);
        pwd = (EditText) findViewById(R.id.pwd);
        pwdConfirm = (EditText) findViewById(R.id.pwd_confirm);
        try {
            employeeCommonDao = dbHelper.getDao(Employee.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (type == Config.OPT_TYPE_CREATE) {
            txtvwTitle.setText(R.string.label_create_operator);
        } else if (type == Config.OPT_TYPE_UPDATE) {
            txtvwTitle.setText(R.string.label_update_operator);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.removeDao(Employee.class);
        employeeCommonDao = null;
        super.onDestroy();
    }

    public final Map<String, String> onValib() {
        String tagOptNo = optNo.getText().toString().trim().replace(" ", "");
        String tagPwd = pwd.getText().toString().trim().replace(" ", "");
        String tagPwdConfirm = pwdConfirm.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(tagOptNo)) {
            ViewUtils.showToast(this, R.string.tip_please_input_opt_no);
            return null;
        }
        if (tagOptNo.compareTo("01") < 0 || tagOptNo.compareTo("98") > 0) {
            ViewUtils.showToast(this, R.string.tip_opt_no_limit);
            return null;
        }
        if (TextUtils.isEmpty(tagPwd) || tagPwd.length() < 4) {
            ViewUtils.showToast(this, R.string.tip_please_input_opt_pwd);
            return null;
        }
        if (TextUtils.isEmpty(tagPwdConfirm) || tagPwdConfirm.length() < 4) {
            ViewUtils.showToast(this, R.string.tip_please_input_opt_pwd);
            return null;
        }
        if (!tagPwd.equals(tagPwdConfirm)) {
            ViewUtils.showToast(this, R.string.tip_opt_pwd_not_same);
            return null;
        }
        Map<String, String> conditions = new HashMap<>();
        conditions.put(Config.OPT_NO_TIP, tagOptNo);
        conditions.put(Config.OPT_PWD_TIP, tagPwd);
        return conditions;
    }

    public void onSubmit(View v) {
        Map<String, String> conditions = onValib();
        if (conditions == null)
            return;
        Employee employee = new Employee(conditions.get(Config.OPT_NO_TIP), conditions.get(Config.OPT_PWD_TIP));

        if (type == Config.OPT_TYPE_CREATE) {
            Employee emp = null;
            try {
                emp = employeeCommonDao.queryForId(employee.getCode());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (null!=emp) {
                ViewUtils.showToast(this, R.string.tip_opt_exsit);
            } else {
                boolean ret = false;
                try {
                    ret = employeeCommonDao.create(employee) == 1;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (ret) {
                    ViewUtils.showToast(this, R.string.tip_opt_create_suc);
                    setResult(1);
                    activityStack.pop();
                } else {
                    ViewUtils.showToast(this, R.string.tip_opt_create_failure);
                }
            }

        } else if (type == Config.OPT_TYPE_UPDATE) {
            boolean ret = false;
            try {
                ret = employeeCommonDao.update(employee) == 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (ret) {
                ViewUtils.showToast(this, R.string.tip_opt_update_suc);
                finish();
            } else {
                ViewUtils.showToast(this, R.string.tip_opt_update_failure);
            }
        }
    }
}

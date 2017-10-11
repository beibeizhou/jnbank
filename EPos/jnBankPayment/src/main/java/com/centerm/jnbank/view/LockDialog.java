package com.centerm.jnbank.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.jnbank.ActivityStack;
import com.centerm.jnbank.R;
import com.centerm.jnbank.activity.LoginActivityForFirst;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.utils.ViewUtils;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

/**
 * Created by ysd on 2016/12/2.
 */

public class LockDialog extends Dialog implements View.OnClickListener {
    protected ActivityStack activityStack = ActivityStack.getInstance();
    protected Logger logger = Logger.getLogger(this.getClass());
    private EditText optId,optPsw;
    private CommonDao<Employee> employeeCommonDao;
    private Context context;
    public LockDialog(Context context) {
        super(context, R.style.CustomDialog);
        init(context);
    }

    public LockDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected LockDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        DbHelper dbHelper = new DbHelper(context);
        employeeCommonDao = new CommonDao<Employee>(Employee.class, dbHelper);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_lock, null);
        optId = (EditText) v.findViewById(R.id.et_opt_id);
        optPsw = (EditText) v.findViewById(R.id.et_opt_psw);
        optId.addTextChangedListener(new CutPassword());
        Button unLock = (Button) v.findViewById(R.id.unlock);
        unLock.setOnClickListener(this);
        setCanceledOnTouchOutside(false);
        setContentView(v);
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_HOME:
                    case KeyEvent.KEYCODE_BACK:

                        return true;
                }
                return false;
            }
        });
    }


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
        if (Config.DEFAULT_MSN_ACCOUNT.equals(optIdStr)) {
            logger.debug("主管账号");
            List<Employee> employees = employeeCommonDao.queryByMap(conditions);
            if (employees != null && !employees.isEmpty()) {
                dismiss();
                BusinessConfig.getInstance().setFlag(context,BusinessConfig.Key.KEY_IS_LOCK,false);
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
                BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_OPER_ID,null);
                Intent intent = new Intent(context, LoginActivityForFirst.class);
                context.startActivity(intent);
                activityStack.removeExcept(LoginActivityForFirst.class);
            } else {
                ViewUtils.showToast(context, R.string.tip_login_pwd_error);
                optPsw.setText("");
            }
        } else if (BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID).equals(optIdStr)) {
            logger.debug("原操作员登录");
            List<Employee> employees = employeeCommonDao.queryByMap(conditions);
            if (employees != null && !employees.isEmpty()) {
                BusinessConfig.getInstance().setFlag(context,BusinessConfig.Key.KEY_IS_LOCK,false);
                //activityStack.remove(TradingActivity.class);
                dismiss();
            } else {
                ViewUtils.showToast(context, R.string.tip_login_pwd_error);
                optPsw.setText("");
            }
        } else {
            ViewUtils.showToast(context,"请输入原操作员或主管账号！");
            optPsw.setText("");
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

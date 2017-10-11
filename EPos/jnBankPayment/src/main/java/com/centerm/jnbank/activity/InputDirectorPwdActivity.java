package com.centerm.jnbank.activity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.NumberPad;

import config.Config;


/**
 * 主管操作员密码输入界面，适用于各种需要主管操作员密码的场景。
 * 作用仅仅是验证主管操作员的本地密码
 * author:wanliang527</br>
 * date:2016/11/2</br>
 */

public class InputDirectorPwdActivity extends BaseTradeActivity {
    private NumberPad numberPad;
    private TextView tipShow;
    private CheckBox[] indicatorArr;
    private CommonDao<Employee> operDao;
    private boolean isAdmin;//是否是系统管理员
    private StringBuilder stringBuilder;


    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        operDao = new CommonDao<>(Employee.class, dbHelper);
        isAdmin = "true".equals(tempMap.get(TransDataKey.keyIsAdmin));
        stringBuilder = new StringBuilder();
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_input_director_pwd;
    }

    @Override
    public void onInitView() {
        tipShow = (TextView) findViewById(R.id.tip_show);
        if (isAdmin) {
            setTitle(R.string.title_input_admin_pwd);
            tipShow.setText(R.string.tip_input_admin_pwd);
        } else {
            setTitle(R.string.title_input_director_pwd);
            tipShow.setText(R.string.tip_input_director_pwd);
        }

        indicatorArr = new CheckBox[]{
                (CheckBox) findViewById(R.id.indicator1),
                (CheckBox) findViewById(R.id.indicator2),
                (CheckBox) findViewById(R.id.indicator3),
                (CheckBox) findViewById(R.id.indicator4),
                (CheckBox) findViewById(R.id.indicator5),
                (CheckBox) findViewById(R.id.indicator6)};
        numberPad = (NumberPad) findViewById(R.id.number_pad_show);
        numberPad.setCallback(new NumberPad.KeyCallback() {
            @Override
            public void onPressKey(char i) {
                if (i == '.') {
                    return;
                }
                if (i == (char) -1) {
                    int len = stringBuilder.length();
                    if (len > 0) {
                        stringBuilder.deleteCharAt(len - 1);
                        changeIndicator(stringBuilder.length());
                    }
                    return;
                }
                stringBuilder.append(i);
                int len = stringBuilder.length();
                changeIndicator(len);
                String content = stringBuilder.toString();
                if (len == 6) {
                    String savedPwd;
                    if (isAdmin) {
                        Employee e = operDao.queryForId(Config.DEFAULT_ADMIN_ACCOUNT);
                        if (e == null) {
                            logger.warn("数据库中无法找到系统管理员账户信息");
                            savedPwd = Config.DEFAULT_ADMIN_PWD;
                        } else {
                            savedPwd = e.getPassword();
                        }
                    } else {
                        Employee e = operDao.queryForId(Config.DEFAULT_MSN_ACCOUNT);
                        if (e == null) {
                            logger.warn("数据库中无法找到主管操作员账户信息");
                            savedPwd = Config.DEFAULT_MSN_PWD;
                        } else {
                            savedPwd = e.getPassword();
                        }
                    }
                    if (content.equals(savedPwd)) {
                        setResult(RESULT_OK);
                        jumpToNext();
                    } else {
                        ViewUtils.showToast(context, R.string.tip_pwd_illegal);
                        stringBuilder.setLength(0);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeIndicator(0);
                            }
                        }, 50);
                    }
                }
            }
        });

/*        numberPad.setContentCallBack(new NumberPad.ContentCallBack() {
            @Override
            public void onReturn(String content) {
                int len = content == null ? 0 : content.length();
                changeIndicator(len);
                if (len == 6) {
                    String savedPwd = null;
                    if (isAdmin) {
                        Employee e = operDao.queryForId(Config.DEFAULT_ADMIN_ACCOUNT);
                        if (e == null) {
                            logger.warn("数据库中无法找到系统管理员账户信息");
                            savedPwd = Config.DEFAULT_ADMIN_PWD;
                        } else {
                            savedPwd = e.getPassword();
                        }
                    } else {
                        Employee e = operDao.queryForId(Config.DEFAULT_MSN_ACCOUNT);
                        if (e == null) {
                            logger.warn("数据库中无法找到主管操作员账户信息");
                            savedPwd = Config.DEFAULT_MSN_PWD;
                        } else {
                            savedPwd = e.getPassword();
                        }
                    }
                    if (content.equals(savedPwd)) {
                        jumpToNext();
                    } else {
                        ViewUtils.showToast(context, R.string.tip_pwd_illegal);
                        changeIndicator(0);
                    }
                }
            }
        });*/
    }

    private void changeIndicator(int pinLen) {
        switch (pinLen) {
            case 0:
                for (int i = 0; i < 6; i++) {
                    indicatorArr[i].setChecked(false);
                }
                break;
            case 1:
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 6:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                break;
        }
    }

}

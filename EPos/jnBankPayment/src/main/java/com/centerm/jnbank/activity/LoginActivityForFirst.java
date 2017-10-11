package com.centerm.jnbank.activity;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.base.MenuActivity;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.net.NetUtils;
import com.centerm.jnbank.task.AsyncAppInitTask;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.UseMobileNetwork;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.aidl.sys.AidlSystemSettingService;
import com.centerm.smartpos.aidl.sys.ApnNode;
import com.centerm.smartpos.constant.Constant;

import org.xutils.common.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;


/**
 * @author linwenhui
 * @date 2016/11/2.
 */

public class LoginActivityForFirst extends BaseActivity {
    private EditText edtxtAccount, edtxtPwd;
    private TextView title;
    private CommonDao<Employee> employeeCommonDao;
//    //绑定AIDL服务
//    private AidlDeviceManager serviceManager;
//    private AidlSystemSettingService systemSettingService;
//    private ServiceConnection conn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            serviceManager = AidlDeviceManager.Stub.asInterface(iBinder);
//            iniSystemService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//
//        }
//    };

//    /**
//     * 初始化系统服务
//     */
//    private void iniSystemService() {
//        try {
//            systemSettingService = AidlSystemSettingService.Stub.asInterface(serviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_SYS));
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//    private boolean bindService() {
//        boolean flag;
//        Intent intent = new Intent();
//        intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
//        final Intent eintent = new Intent(createExplicitFromImplicitIntent(getApplicationContext(), intent));
//        flag = getApplicationContext().bindService(eintent, conn, Context.BIND_AUTO_CREATE);
//        return flag;
//    }

//    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
//        // Retrieve all services that can match the given intent
//        PackageManager pm = context.getPackageManager();
//        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
//
//        // Make sure only one match was found
//        if (resolveInfo == null || resolveInfo.size() != 1) {
//            return null;
//        }
//
//        // Get component info and create ComponentName
//        ResolveInfo serviceInfo = resolveInfo.get(0);
//        String packageName = serviceInfo.serviceInfo.packageName;
//        String className = serviceInfo.serviceInfo.name;
//        ComponentName component = new ComponentName(packageName, className);
//
//        // Create a new intent. Use the old one for extras and such reuse
//        Intent explicitIntent = new Intent(implicitIntent);
//
//        // Set the component to be explicit
//        explicitIntent.setComponent(component);
//
//        return explicitIntent;
//    }
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if(hasFocus){
//            //如果apn设置不是专网，则进入应用时设置成专网
//            try {
//                ApnNode apnNode = systemSettingService.getDefaultApn();
//                if (apnNode == null) {
//                    return;
//                } else {
//                    if (!"czjnyh.js".equals(apnNode.getApn())) {
//                        int[] ids = systemSettingService.getAPNID("czjnyh.js");
//                        if (null != ids && ids.length != 0) {
//                            systemSettingService.setDefaultAPN(ids[0]);
//                            logger.info("id=" + apnNode.getId() + ",name=" + apnNode.getName() + ",apn=" + apnNode.getApn());
//                        }
//                    }
//                }
//
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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
        String posSerial = BusinessConfig.getInstance().getPosSerial(context);
        NetUtils.higherNetUseExample(context,NetworkCapabilities.TRANSPORT_CELLULAR);
        loading();
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        title.setText("柜员签到");
    }

    @Override
    protected void onResume() {
//        bindService();
        super.onResume();
        if (edtxtAccount != null) {
            edtxtAccount.setText("");
            edtxtAccount.requestFocus();
        }
        if (edtxtPwd != null) {
            edtxtPwd.setText("");
        }

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
                    jumpToManagerView(true);
                } else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
                    //主管操作员
//                    if (Settings.hasInit(context)) {
//                        if (Settings.hasTmk(context)) {
//                            jumpToManagerView(false);
//                        } else {
//                            DialogFactory.showMessageDialog(context, null, getString(R.string
//                                    .tip_login_to_down));
//                        }
//                    } else {
//                        DialogFactory.showMessageDialog(context, null, getString(R.string
//                                .tip_login_to_init));
//                    }
                    jumpToManagerView(false);
                } else {
                    //普通操作员
//                    if (Settings.hasInit(context)) {
//                        if (Settings.hasTmk(context)) {
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
//                        } else {
//                            DialogFactory.showMessageDialog(context, null, getString(R.string
//                                    .tip_login_to_down));
//                        }
//                    } else {
//                        DialogFactory.showMessageDialog(context, null, getString(R.string
//                                .tip_login_to_init));
//                    }
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
        Intent intent = new Intent(context, MainActivity.class);
        activityStack.pop();
        startActivity(intent);
    }

    private void jumpToManagerView(boolean isAdmin) {
        Intent intent = new Intent(context, MenuActivity.class);
        if (isAdmin) {
            intent.putExtra(KEY_USER_FLAG, 1);
        } else {
            intent.putExtra(KEY_USER_FLAG, 2);
        }
        startActivity(intent);
    }

    private void loading() {
        boolean firstLaunch = Settings.getValue(context, Settings.KEY.FIRST_TIME_LOADING, true);
        if (firstLaunch) {
            new AsyncAppInitTask(context) {
                @Override
                public void onStart() {
                    DialogFactory.showLoadingDialog(LoginActivityForFirst.this, "程序初始化中，请稍候...");
                    boolean b = CommonUtils.readIssExcelToDB(context);
                    if(b){
                        logger.info("iss卡信息表已存入数据库中");
                    }
                }

                @Override
                public void onFinish(Object o) {
                    DialogFactory.hideAll();
//                    ViewUtils.showToast(LoginActivityForFirst.this, "初始化完成！");
                }

                @Override
                public void onProgress(Integer counts, Integer index) {
                }
            }.execute("");
        }
    }

}

package com.centerm.jnbank.base;


import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.centerm.jnbank.R;
import com.centerm.jnbank.common.Settings;

/**
 * author:wanliang527</br>
 * date:2016/12/15</br>
 */

public class ImportKEKActivity extends BaseActivity {

    private EditText tekEdit;

    @Override
    public int onLayoutId() {
        return R.layout.activity_import_tek;
    }

    @Override
    public void onInitView() {
        tekEdit = (EditText) findViewById(R.id.tek_edit);
    }
    public void onConfirm(View view) {
        String value = tekEdit.getText().toString();
        if (TextUtils.isEmpty(value) || value.trim().length() != 32) {
            Toast.makeText(this, "TMK不合法", Toast.LENGTH_SHORT).show();
            return;
        }
        Settings.setParam(context, Settings.KEY.KEK,"324C0419C85EEA2694765164AE0E40BC");
        activityStack.pop();
    }
}

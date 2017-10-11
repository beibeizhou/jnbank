package com.centerm.jnbank.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.jnbank.ActivityStack;
import com.centerm.jnbank.R;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.utils.ViewUtils;

import org.apache.log4j.Logger;

/**
 * Created by ysd on 2016/12/2.
 */

public class InputCardNumberDialog extends Dialog implements View.OnClickListener {
    protected ActivityStack activityStack = ActivityStack.getInstance();
    protected Logger logger = Logger.getLogger(this.getClass());
    private EditText cardNum;
    private TextView title;
    private Button posBtn,nagBtn;
    private CommonDao<Employee> employeeCommonDao;
    private Context context;
    private ButtonClickListener clickListener;
    public InputCardNumberDialog(Context context) {
        super(context, R.style.CustomDialog);
        init(context);
    }

    public InputCardNumberDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected InputCardNumberDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        DbHelper dbHelper = new DbHelper(context);
        employeeCommonDao = new CommonDao<Employee>(Employee.class, dbHelper);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.input_card_number_dialog, null);
        cardNum = (EditText) v.findViewById(R.id.card_numer);
        title = (TextView) v.findViewById(R.id.dialog_title);
        posBtn = (Button) v.findViewById(R.id.negative_btn);
        nagBtn = (Button) v.findViewById(R.id.positive_btn);
        title.setText("支持手输卡号或其他读卡方式");
        posBtn.setOnClickListener(this);
        nagBtn.setOnClickListener(this);
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
        switch (v.getId()) {
            case R.id.positive_btn:
                String cardNum = getInputText();
                if (StringUtils.isStrNull(cardNum) || cardNum.length() < 13) {
                    ViewUtils.showToast(context,"卡号不能小于13位");
                    return;
                }
                dismiss();
                if (clickListener != null) {
                    clickListener.onClick(ButtonType.POSITIVE, v);
                }
                break;
            case R.id.negative_btn:
                getWindow().isFloating();
                dismiss();
                if (clickListener != null) {
                    clickListener.onClick(ButtonType.NEGATIVE, v);
                }
                break;
        }
    }

    public String getInputText(){
        return cardNum.getText().toString();
    }

    public ButtonClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ButtonClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ButtonClickListener {
        void onClick(ButtonType button, View v);
    }

    public enum ButtonType {
        POSITIVE,
        NEGATIVE
    }
}

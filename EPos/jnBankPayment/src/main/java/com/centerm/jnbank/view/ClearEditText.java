package com.centerm.jnbank.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.centerm.jnbank.R;

/**
 * 带有清除框内输入的EditText，自带一个默认的清除图标，如果需要设置清除图标，直接设置该对象的drawableRight即可。
 *
 * @author wanliang527
 * @date 2014-07-22
 */
public class ClearEditText extends EditText implements OnFocusChangeListener,
        TextWatcher {
    /**
     * 清空按钮
     */
    private Drawable mClearDrawable;

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearEditText(Context context) {
        super(context);
        init();
    }

    private void init() {
        // 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getResources().getDrawable(R.drawable.wl_ic_clear);
        }
        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
                mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    /**
     * 因为不能直接给EditText设置点击事件，所以用记住按下的位置来模拟点击事件。首先要获取触摸点的横坐标x，
     * 再分别获取图标的左右边距所在的横坐标drawableLeftPos、drawableRightPos 如果drawableLeftPos > x
     * > drawableRightPos，那么可以判断用户点击了清除的图标。 如果手指离开屏幕时的触摸点不在该控件上，那就认为是取消了点击动作。
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // mLog.logTest(String.valueOf(event.getY()));
                // 以自身左上角为参考点，当前触摸点的X坐标
                float x = event.getX();
                float y = event.getY();
                // 清除图标的左边界所在的X坐标
                int drawableLeftPos = getWidth() - getPaddingRight()
                        - mClearDrawable.getIntrinsicWidth();
                // 清除图标的右边界所在的X坐标
                int drawableRightPos = getWidth() - getPaddingRight();
                boolean xTouchable = x > drawableLeftPos
                        && x < drawableRightPos;
                boolean yTouchable = y > 0 && y < getHeight();
                if (xTouchable && yTouchable)
                    this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        setClearIconVisible(s.length() > 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}

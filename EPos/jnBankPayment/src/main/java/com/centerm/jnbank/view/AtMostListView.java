package com.centerm.jnbank.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 消除滑动冲突的ListView
 * 
 * @author wanliang527
 * @date 2014-5-8
 */
public class AtMostListView extends ListView {

    public AtMostListView(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);
    }

    public AtMostListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtMostListView(Context context) {
        super(context);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}

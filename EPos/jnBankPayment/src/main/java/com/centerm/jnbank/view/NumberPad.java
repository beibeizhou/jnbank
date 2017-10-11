package com.centerm.jnbank.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.jnbank.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * author: wanliang527</br>
 * date:2016/7/28</br>
 */
public class NumberPad extends LinearLayout implements View.OnClickListener {

    private final static int DEFAULT_COLUMNS = 3;
    private static List<Integer> RES_ARRAY = new ArrayList<>();
    private final static List<Integer> attrList = new ArrayList<>();
    private TextView showView;
    private DecimalFormat formatter = new DecimalFormat("#0.00");
    private Context context;
    private int columns = DEFAULT_COLUMNS;
    private KeyCallback callback;
    private ContentCallBack contentCallBack;
    private boolean isEnable = true;

    static {
        RES_ARRAY.add(R.drawable.num_1);
        RES_ARRAY.add(R.drawable.num_2);
        RES_ARRAY.add(R.drawable.num_3);
        RES_ARRAY.add(R.drawable.num_4);
        RES_ARRAY.add(R.drawable.num_5);
        RES_ARRAY.add(R.drawable.num_6);
        RES_ARRAY.add(R.drawable.num_7);
        RES_ARRAY.add(R.drawable.num_8);
        RES_ARRAY.add(R.drawable.num_9);
        RES_ARRAY.add(R.drawable.num_dot);
        RES_ARRAY.add(R.drawable.num_0);
        RES_ARRAY.add(R.drawable.num_del);
/*        attrList.add(R.attr.num_01);
        attrList.add(R.attr.num_02);
        attrList.add(R.attr.num_03);
        attrList.add(R.attr.num_04);
        attrList.add(R.attr.num_05);
        attrList.add(R.attr.num_06);
        attrList.add(R.attr.num_07);
        attrList.add(R.attr.num_08);
        attrList.add(R.attr.num_09);
        attrList.add(R.attr.num_dot);
        attrList.add(R.attr.num_0);
        attrList.add(R.attr.num_cancel);*/
    }

    public NumberPad(Context context) {
        super(context);
        initView(context);
    }

    public NumberPad(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NumberPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(value = Build.VERSION_CODES.LOLLIPOP)
    public NumberPad(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public void setPadEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    private void initView(Context context) {
        this.context = context;
        for (int i = 0; i < attrList.size(); i++) {
            RES_ARRAY.add(getThemeResourceId(attrList.get(i)));
            /*setBackgroundColor(getResources().getColor(R.color.font_white));
            if (i == 8) {
                setBackgroundColor(getResources().getColor(R.color.main_e3e7ee));
            }*/
        }
        formatter.setRoundingMode(RoundingMode.FLOOR);
        setOrientation(VERTICAL);

        int start = 0;
        int total = RES_ARRAY.size();
        int row = 0;
        while (start < total) {
            int end = start + columns;
            if (end > total) {
                end = total;
            }
            addView(createRows(context, row, RES_ARRAY.subList(start, end)), createParams());
            start = end;
            row++;
            if (start < total) {
                float size = context.getResources().getDimension(R.dimen.common_divider_size);
                if (size < 1) {
                    size = 1;
                }
                addView(createDivider(), new ViewGroup.LayoutParams(-1, (int) size));
            }
        }
    }

    private LinearLayout createRows(Context context, int rowIndex, List<Integer> resIds) {
        if (context == null || resIds == null || resIds.size() == 0) {
            return null;
        }
        LinearLayout row = new LinearLayout(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row.setOrientation(HORIZONTAL);
        for (int i = 0; i < resIds.size(); i++) {
            View view = createItem(inflater, resIds.get(i),i,rowIndex);
            int tag = rowIndex * columns + i;
            view.setTag(tag);
            row.addView(view, createParams());
            if (i != resIds.size() - 1) {
                float size = context.getResources().getDimension(R.dimen.common_divider_size);
                if (size < 1) {
                    size = 1;
                }
                row.addView(createDivider(), new ViewGroup.LayoutParams((int) size, -1));
            }

        }
        return row;
    }

    private LayoutParams createParams() {
        LayoutParams params = new LayoutParams(-1, -1);
        params.weight = 1;
        return params;
    }

    private View createDivider() {
        View view = new View(context);
        view.setBackgroundColor(context.getResources().getColor(R.color.common_divider));
        return view;
    }

    private View createItem(LayoutInflater inflater, int resId,int i,int rowIndex) {
        View view = inflater.inflate(R.layout.v_num_pad_item, null);
        ImageButton item = (ImageButton) view.findViewById(R.id.num_item);
        if(rowIndex==3&&i==2)//X-退格背景置灰
            item.setBackgroundColor(getResources().getColor(R.color.main_e3e7ee));
        item.setImageResource(resId);
        view.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View view) {
        if (!isEnable) {
            return;
        }
        int index = (int) view.getTag();
        char keyValue = (char) -1;
        switch (index) {
            case 0:
                keyValue = '1';
                break;
            case 1:
                keyValue = '2';
                break;
            case 2:
                keyValue = '3';
                break;
            case 3:
                keyValue = '4';
                break;
            case 4:
                keyValue = '5';
                break;
            case 5:
                keyValue = '6';
                break;
            case 6:
                keyValue = '7';
                break;
            case 7:
                keyValue = '8';
                break;
            case 8:
                keyValue = '9';
                break;
            case 9:
                keyValue = '.';
                break;
            case 10:
                keyValue = '0';
                break;
        }
        if (callback != null) {
            callback.onPressKey(keyValue);
        }
        if (showView != null) {
            String text = showView.getText().toString();
            double value = Double.valueOf(text);
            String changedText = text;
            switch (keyValue) {
                //删除
                case (char) -1:
                    if (value == 0.0) {
                        return;
                    }
                    changedText = formatter.format(value * 0.1) + "";
                    break;
                //小数点
                case '.':
                    break;
                //其他数字字符
                default:
                    if (text.length() > 10) {
                        return;
                    }
                    if (value == 0.0) {
                        changedText = Integer.valueOf(String.valueOf(keyValue)) * 0.01 + "";
                    } else {
                        //if (keyValue == '0') {
                        //    changedText = value * 10 + "";
                        //} else {
                        //if (text.endsWith("0")) {
                        //    changedText = value + (Integer.valueOf(String.valueOf(keyValue)) * 0.01) + "";
                        //} else {
                        changedText = (value * 10 + Integer.valueOf(String.valueOf(keyValue)) * 0.01) + "";
                        //}
                        // }
                    }
                    break;


            }
            changedText = "" + formatter.format(Double.valueOf(changedText));
            showView.setText(changedText);
        }


    }


    public void bindShowView(TextView view) {
        this.showView = view;
        if (showView != null) {
            showView.setText("0.00");
        }
    }


    public KeyCallback getCallback() {
        return callback;
    }

    public void setCallback(KeyCallback callback) {
        this.callback = callback;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public ContentCallBack getContentCallBack() {
        return contentCallBack;
    }

    public void setContentCallBack(ContentCallBack contentCallBack) {
        this.contentCallBack = contentCallBack;
    }

    public interface KeyCallback {
        void onPressKey(char i);
    }

    public interface ContentCallBack {
        void onReturn(String content);
    }

    private int getResourceId(Context context) {
        return -1;
    }



 /*   private class Adapter extends BaseAdapter {

//        private int resource = R.sub_item.v_num_pad_item;
//        private String[] from = new String[]{"imageId"};
//        private int[] to = new int[]{R.id.number_pad_show};

        private Context context;
        private int[] data;

        public Adapter(Context context, int[] data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int i) {
            return data[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.sub_item.v_num_pad_item, null);
            }

            ImageButton image = (ImageButton) convertView.findViewById(R.id.number_pad_show);
            return convertView;
        }
    }*/


    private int getThemeResourceId(int attrName) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attrName, value, true);
        return value.resourceId;
    }
}

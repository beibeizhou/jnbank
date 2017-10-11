package com.centerm.jnbank.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.centerm.jnbank.R;


/**
 * author: wanliang527</br>
 * date:2016/8/27</br>
 */
public class DividerGridView extends LinearLayout implements View.OnClickListener {

    private final static String TAG = "DividerGridView";

    private final static String TAG_SPACE_ITEM = "TagSpaceItem";
    private final static int DEFAULT_NUM_COLUMNS = 3;
    private final static int DEFAULT_DIVIDER_SIZE = 2;
    private final static int MAX_ROWS = 10;


    private Context context;

    private int numColumns = DEFAULT_NUM_COLUMNS;
    private int numRows = -1;//行数，如果指定行数大于0，所有行数将会填充满整个父控件
    private int dividerSize = DEFAULT_DIVIDER_SIZE;
    private int hDividerSize, vDividerSize;
    private int vDividerColor;
    private int hDividerColor;
    private Drawable vDivider;
    private Drawable hDivider;
    private boolean endDividerFlag;

    private BaseAdapter adapter;
    private AdapterView.OnItemClickListener itemClickListener;

    public DividerGridView(Context context) {
        super(context);
        init(context, null);
    }

    public DividerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DividerGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(value = Build.VERSION_CODES.LOLLIPOP)
    public DividerGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        setOrientation(VERTICAL);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DividerGridView);
            //列数
            numColumns = a.getInteger(R.styleable.DividerGridView_numColumns, 1);
            if (numColumns <= 0) {
                numColumns = DEFAULT_NUM_COLUMNS;
            }
            //行数
            numRows = a.getInteger(R.styleable.DividerGridView_numRows, -1);
            if (numRows > MAX_ROWS) {
                numRows = MAX_ROWS;
            }
            //分隔线的大小
            dividerSize = a.getDimensionPixelSize(R.styleable.DividerGridView_dividerSize, DEFAULT_DIVIDER_SIZE);
            hDividerSize = a.getDimensionPixelSize(R.styleable.DividerGridView_horizontalDividerSize, -1);
            vDividerSize = a.getDimensionPixelSize(R.styleable.DividerGridView_verticalDividerSize, -1);
            if (hDividerSize <= 0) {
                hDividerSize = dividerSize;
            }
            if (vDividerSize <= 0) {
                vDividerSize = dividerSize;
            }
            //横向分隔线颜色或者Drawable对象
            vDivider = a.getDrawable(R.styleable.DividerGridView_verticalDivider);
            if (vDivider == null) {
                vDividerColor = a.getColor(R.styleable.DividerGridView_verticalDivider, -1);
            }
            //竖向分隔线颜色或者Drawable对象
            hDivider = a.getDrawable(R.styleable.DividerGridView_horizontalDivider);
            if (hDivider == null) {
                hDividerColor = a.getColor(R.styleable.DividerGridView_horizontalDivider, -1);
            }
            //是否在末行添加分隔线
            endDividerFlag = a.getBoolean(R.styleable.DividerGridView_drawEndDivider, false);
            a.recycle();
        }
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void notifyDataSetChanged() {
        removeAllViews();
        if (adapter == null || adapter.getCount() <= 0) {
            return;
        }
        int counts = adapter.getCount();
        if (numRows <= 0) {
            numRows = counts / numColumns;
            if (counts % numColumns > 0) {
                numRows++;
            }
        }
        boolean validItemEndFlag = false;
        for (int i = 0; i < numRows; i++) {
            View[] temp = new View[numColumns];
            for (int j = 0; j < numColumns; j++) {
                int index = i * numColumns + j;
                if (index < counts) {
                    View view = adapter.getView(index, null, null);
                    view.setOnClickListener(this);
                    temp[j] = view;
                    view.setTag(R.id.tagPosition, index);
                } else {
                    temp[j] = genSpaceItemView();
                }
            }
            LinearLayout row = new LinearLayout(context);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            addView(row, params);
            bindItemsToRow(row, temp);
            if (validItemEndFlag) {
                //有效数据已经结束，后面的每一行都不需要再添加分割线
                continue;
            }
            if (isSpaceRow(temp)) {
                validItemEndFlag = true;
            } else {
                if (i == numRows - 1) {
                    if (endDividerFlag) {
                        addView(genVerticalDivider());
                    }
                } else {
                    if ((vDivider != null || vDividerColor > 0)) {
                        addView(genVerticalDivider());
                    }
                }
            }
        }
    }

    private boolean isSpaceRow(View... childs) {
        if (childs == null) {
            return true;
        }
        for (int i = 0; i < childs.length; i++) {
            if (!isSpaceItem(childs[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpaceItem(View view) {
        if (view == null || TAG_SPACE_ITEM.equals(view.getTag())) {
            return true;
        }
        return false;
    }

    private LinearLayout bindItemsToRow(LinearLayout row, View... items) {
        for (int i = 0; i < items.length; i++) {
            LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            row.addView(items[i], params);
            if (!isSpaceItem(items[i]) && i != items.length - 1) {
                row.addView(genHorizontallDivider());
            }
        }
        return row;
    }

    private View genVerticalDivider() {
        View divider = new View(context);
        divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, vDividerSize));
        if (vDivider != null) {
            divider.setBackgroundDrawable(vDivider);
        } else if (vDividerColor > 0) {
            divider.setBackgroundColor(vDividerColor);
        }
        return divider;
    }

    private View genHorizontallDivider() {
        View divider = new View(context);
        divider.setLayoutParams(new ViewGroup.LayoutParams(hDividerSize, ViewGroup.LayoutParams.MATCH_PARENT));
        if (hDivider != null) {
            divider.setBackgroundDrawable(hDivider);
        } else if (hDividerColor > 0) {
            divider.setBackgroundColor(hDividerColor);
        }
        return divider;
    }

    private View genSpaceItemView() {
        View spaceView = new View(context);
        spaceView.setTag(TAG_SPACE_ITEM);
        spaceView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        return spaceView;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag(R.id.tagPosition);
        if (itemClickListener != null) {
            itemClickListener.onItemClick(null, v, position, adapter.getItemId(position));
        }
    }

    public Object getItemAtPosition(int position) {
        return (adapter == null || position < 0) ? null : adapter.getItem(position);
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public int getDividerSize() {
        return dividerSize;
    }

    public void setDividerSize(int dividerSize) {
        this.dividerSize = dividerSize;
    }

    public int getvDividerColor() {
        return vDividerColor;
    }

    public void setvDividerColor(int vDividerColor) {
        this.vDividerColor = vDividerColor;
    }

    public int gethDividerColor() {
        return hDividerColor;
    }

    public void sethDividerColor(int hDividerColor) {
        this.hDividerColor = hDividerColor;
    }

    public Drawable getvDivider() {
        return vDivider;
    }

    public void setvDivider(Drawable vDivider) {
        this.vDivider = vDivider;
    }

    public Drawable gethDivider() {
        return hDivider;
    }

    public void sethDivider(Drawable hDivider) {
        this.hDivider = hDivider;
    }

    public boolean isEndDividerFlag() {
        return endDividerFlag;
    }

    public void setEndDividerFlag(boolean endDividerFlag) {
        this.endDividerFlag = endDividerFlag;
    }

    public int gethDividerSize() {
        return hDividerSize;
    }

    public void sethDividerSize(int hDividerSize) {
        this.hDividerSize = hDividerSize;
    }

    public int getvDividerSize() {
        return vDividerSize;
    }

    public void setvDividerSize(int vDividerSize) {
        this.vDividerSize = vDividerSize;
    }
}

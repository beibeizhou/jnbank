package com.centerm.jnbank.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.xml.menu.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author: wanliang527</br>
 * date:2016/8/27</br>
 */
public class GridViewPager extends LinearLayout {
    private final static String TAG = "GridViewPager";

    private final static int DEFAULT_NUM_COLUMNS = 3;
    private final static int DEFAULT_NUM_ROWS = 3;
    private final static int MAX_ROWS = 10;
    private final static int DEFAULT_DIVIDER_SIZE = 2;

    private Context context;
    private AttributeSet attrs;

    private int paddingTop, paddingLeft, paddingRight;
    private int numColumns = DEFAULT_NUM_COLUMNS;
    private int numRows = DEFAULT_NUM_ROWS;
    private int dividerSize = DEFAULT_DIVIDER_SIZE;
    private int indicatorHeight = -2;
    private int vDividerColor;
    private int hDividerColor;
    private int hDividerSize;
    private int vDividerSize;
    private int indicatorPadding;
    private Drawable vDivider;
    private Drawable hDivider;
    private boolean endDividerFlag;
    private int pageSize;
    private int totalItems;
    private Drawable indicatorOn;
    private Drawable indicatorOff;

    private ViewPager innerPager;
    private GridPagerAdapter adapter;
    private LinearLayout indicatorContainer;

    private ItemClickListener itemClickListener;
    private Map<String, ?> firstMap = null;
    private List<Map<String, ?>> secondList = new ArrayList<>();
    private List<Map<String, ?>> otherList = new ArrayList<>();
    private SimpleAdapter page1Adapter, page2Adapter;
    private ImageView imageView;
    private TextView textView;

    public GridViewPager(Context context) {
        super(context);
        init(context, null);
    }

    public GridViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GridViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(value = Build.VERSION_CODES.LOLLIPOP)
    public GridViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        this.attrs = attrs;
        setOrientation(VERTICAL);
        innerPager = new MyViewPager(context, attrs);
        innerPager.setId(R.id.innerPager);
        innerPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DividerGridView);
            //列数
            numColumns = a.getInteger(R.styleable.DividerGridView_numColumns, DEFAULT_NUM_COLUMNS);
            if (numColumns <= 0) {
                numColumns = DEFAULT_NUM_COLUMNS;
            }
            //行数
            numRows = a.getInteger(R.styleable.DividerGridView_numRows, DEFAULT_NUM_ROWS);
            if (numRows > MAX_ROWS) {
                numRows = MAX_ROWS;
            }
            //分隔线的大小
            dividerSize = a.getDimensionPixelSize(R.styleable.DividerGridView_dividerSize, DEFAULT_DIVIDER_SIZE);
            hDividerSize = a.getDimensionPixelSize(R.styleable.DividerGridView_horizontalDividerSize, -1);
            vDividerSize = a.getDimensionPixelSize(R.styleable.DividerGridView_verticalDividerSize, -1);
            if (hDividerSize < 0) {
                hDividerSize = dividerSize;
            }
            if (vDividerSize < 0) {
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

            //Indicator图标
            indicatorOn = a.getDrawable(R.styleable.DividerGridView_indicatorOn);
            indicatorOff = a.getDrawable(R.styleable.DividerGridView_indicatorOff);
            indicatorHeight = a.getDimensionPixelOffset(R.styleable.DividerGridView_indicatorHeight, -2);
            if (indicatorHeight < 0) {
                indicatorHeight = -2;
            }
            indicatorPadding = a.getDimensionPixelOffset(R.styleable.DividerGridView_indicatorPadding, 0);

            paddingTop = a.getDimensionPixelSize(R.styleable.DividerGridView_paddingTop, 0);
            paddingLeft = a.getDimensionPixelSize(R.styleable.DividerGridView_paddingLeft, 0);
            paddingRight = a.getDimensionPixelSize(R.styleable.DividerGridView_paddingRight, 0);
            a.recycle();
        }
    }

    public void setAdapter(GridPagerAdapter adapter) {
        this.adapter = adapter;
        if (adapter.data != null) {
            totalItems = adapter.data.size();
        }
        pageSize = 1;
       /* if ((totalItems-9) % (numRows * numColumns) != 0) {
            pageSize++;
        }*/
        initAdapterData();
        if (pageSize > 0) {
            final List<View> pages = new ArrayList<>();
            for (int i = 0; i < pageSize; i++) {
                pages.add(genPageView(i));
            }
            final PagerAdapter innerAdapter = new PagerAdapter() {
                @Override
                public int getCount() {
                    return pages.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return view.equals(object);
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    container.addView(pages.get(position));
                    return pages.get(position);
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView(pages.get(position));
                }
            };
            innerPager.setAdapter(innerAdapter);
            LayoutParams pagerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pagerParams.weight = 1;
            addView(innerPager, pagerParams);
            if (pageSize > 1) {
                indicatorContainer = new LinearLayout(context);
                indicatorContainer.setGravity(Gravity.CENTER_VERTICAL);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, indicatorHeight);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                updateIndicator(0);
                addView(indicatorContainer, params);
            }
        }
    }

    /**
     * 初始化各个Adapter的数据
     */
    private void initAdapterData() {
        secondList.clear();
        otherList.clear();
        for (int i = 0; i < adapter.data.size(); i++) {
            if (i == 0) {
                firstMap = adapter.data.get(i);
            } else {
                secondList.add(adapter.data.get(i));
            }

          /*  else if (i < 5) {
                c
            } else {
                otherList.add(adapter.data.get(i));
            }*/
        }
    }

    private View genPageView(final int pageIndex) {
        View view = null;
        if (adapter == null
                || adapter.data == null
                || adapter.data.size() <= 0
                || pageIndex >= pageSize) {
            return null;
        }
        if (pageIndex == 0) {
            final String TEXT = "text";
            final String ICON = "icon";
            final String ITEMS = "items";
            view = LayoutInflater.from(context).inflate(R.layout.grid_page1, null);
            RelativeLayout firstMenu = (RelativeLayout) view.findViewById(R.id.firt_menu);
            RelativeLayout firt_menu_qrcode = (RelativeLayout) view.findViewById(R.id.firt_menu_qrcode);
            imageView = (ImageView) view.findViewById(R.id.menu_icon_show);
            textView = (TextView) view.findViewById(R.id.menu_text_show);
//            initFirstMenu();
            firstMenu.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(view, 0);
                    }
                }
            });

            firt_menu_qrcode.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(view, 1);
                    }
                }
            });
            GridView gridView1 = (GridView) view.findViewById(R.id.page1_gridview);
            if (null == page1Adapter) {
                page1Adapter = new SimpleAdapter(context, secondList, adapter.layoutId, adapter.from, adapter.to) {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        v.setBackgroundResource(R.color.common_main_bg);
                        v.setTag(adapter.layoutId, getItem(position));
                        return v;
                    }
                };
                gridView1.setAdapter(page1Adapter);
            }
            gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(view, position +2);
                    }
                }
            });
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.grid_page2, null);
            GridView gridView2 = (GridView) view.findViewById(R.id.page2_gridview);
            if (null == page2Adapter) {
                page2Adapter = new SimpleAdapter(context, otherList, adapter.layoutId, adapter.from, adapter.to) {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        v.setBackgroundResource(R.color.common_main_bg);
                        v.setTag(adapter.layoutId, getItem(position));
                        return v;
                    }
                };
                gridView2.setAdapter(page2Adapter);
            }
            gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(view, 5 + position);
                    }
                }
            });
        }
        return view;
    }

    /**
     * 加载第一个菜单
     */
    private void initFirstMenu() {
        Integer s1 = (Integer) firstMap.get("icon");
        String s2 = (String) firstMap.get("text");
        MenuItem s3 = (MenuItem) firstMap.get("items");
        imageView.setImageResource(s1);
        textView.setText(s2);
    }

    private void updateIndicator(int which) {
        if (indicatorOn == null || indicatorOff == null) {
            return;
        }
        if (indicatorContainer != null) {
            int counts = indicatorContainer.getChildCount();
            if (counts < pageSize) {
                for (int i = 0; i < pageSize - counts; i++) {
                    ImageView dot = new ImageView(context);
                    indicatorContainer.addView(dot);
                }
            } else if (counts > pageSize) {
                for (int i = 0; i < counts - pageSize; i++) {
                    indicatorContainer.removeView(indicatorContainer.getChildAt(i));
                }
            }
            for (int i = 0; i < pageSize; i++) {
                ImageView indicator = (ImageView) indicatorContainer.getChildAt(i);
                if (i == which) {
                    indicator.setImageDrawable(indicatorOn);
                } else {
                    indicator.setImageDrawable(indicatorOff);
                }
                LayoutParams p = (LayoutParams) indicator.getLayoutParams();
                if (i == pageSize - 1) {
                    p.rightMargin = 0;
                } else {
                    p.rightMargin = indicatorPadding;
                }
                indicator.setLayoutParams(p);
            }
        }
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public static class GridPagerAdapter {
        private Context context;
        private int layoutId;
        private String[] from;
        private int[] to;
        List<Map<String, ?>> data;

        public GridPagerAdapter(Context context, int layoutId, String[] from, int[] to, List<Map<String, ?>> data) {
            this.context = context;
            this.layoutId = layoutId;
            this.from = from;
            this.to = to;
            this.data = data;
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


    private class MyViewPager extends ViewPager {

        public MyViewPager(Context context) {
            super(context);
        }

        public MyViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

    }

    public void reFrashData(List<Map<String, ?>> data) {
        this.adapter.data = data;
        initAdapterData();
        initFirstMenu();
        if (null != page1Adapter) {
            page1Adapter.notifyDataSetChanged();
        }
        if (null != page2Adapter) {
            page2Adapter.notifyDataSetChanged();
        }
    }
}


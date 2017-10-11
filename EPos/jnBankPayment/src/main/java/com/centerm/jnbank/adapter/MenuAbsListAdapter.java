package com.centerm.jnbank.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.xml.XmlTag;
import com.centerm.jnbank.xml.menu.Menu;
import com.centerm.jnbank.xml.menu.MenuItem;

import java.util.HashSet;
import java.util.Set;

import config.BusinessConfig;
import config.Config;

/**
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */

public class MenuAbsListAdapter extends BaseAdapter {

    private Context context;
    private Menu menu;
    private int layoutId;
    private static Set<String> showTogglefItems = new HashSet<>();

    static {
        showTogglefItems.add(XmlTag.MenuTag.PREFER_QUICK_PAY);
    }

    public MenuAbsListAdapter(Context context, Menu menu) {
        this.context = context;
        this.menu = menu;
        //判断菜单的视图结构
        layoutId = menu.getStructure().equals(Menu.ViewStructure.GRID)
                ? R.layout.common_menu_grid_item : R.layout.common_menu_list_item;
    }

    @Override
    public int getCount() {
        return menu.getCounts();
    }

    @Override
    public MenuItem getItem(int position) {
        return menu.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutId, null);
        }
        ImageView iconShow = (ImageView) convertView.findViewById(R.id.menu_icon_show);
        TextView textShow = (TextView) convertView.findViewById(R.id.menu_text_show);
        MenuItem item = menu.getItem(position);
            int iconId = getResId(item.getIconResName(), "drawable");
        iconShow.setImageResource(iconId);
        int textId = getResId(item.getTextResName(), "string");
        if (textId == R.string.menu) {
            textShow.setText(item.getChnTag());
        } else {
            textShow.setText(getResId(item.getTextResName(), "string"));
        }
        CheckBox toggle = (CheckBox) convertView.findViewById(R.id.toggle);
        if (showTogglefItems.contains(item.getEntag())) {
            toggle.setVisibility(View.VISIBLE);
            switch (item.getEntag()) {
                case XmlTag.MenuTag.PREFER_QUICK_PAY:
                    toggle.setChecked(BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PREFER_CLSS).equals("1")?true:false);
                    break;
            }
        } else {
            toggle.setVisibility(View.GONE);
        }
        return convertView;
    }

    private int getResId(String resName, String resType) {
        int id = 0;
        if (resName != null) {
            id = context.getResources().getIdentifier(resName, resType, context.getPackageName());
        }
        if (id == 0) {
            if ("drawable".equals(resType)) {
                id = Config.DEFAULT_MENU_ITEM_ICON;
            } else if ("string".equals(resType)) {
                id = R.string.menu;
            }
        }
        return id;
    }

/*    private boolean isShowToggle(int position) {
        if (toggleItems == null || toggleItems.length == 0) {
            return false;
        }
        for (int i = 0; i < toggleItems.length; i++) {
            if (toggleItems[i] == position) {
                return true;
            }
        }
        return false;
    }

    public void setToggleItems(int... positions) {
        toggleItems = positions;
        notifyDataSetChanged();
    }*/
}

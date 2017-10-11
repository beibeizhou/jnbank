package com.centerm.jnbank.xml.menu;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 子菜单实体类
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class Menu extends MenuItem {

    private ViewStructure structure = ViewStructure.LIST;
    private List<MenuItem> itemList = new ArrayList<>();

    public Menu() {
    }


    public Menu(String iconName, String textName) {
        super(iconName, textName);
    }

    public void add(MenuItem item) {
        itemList.add(item);
    }

    public ViewStructure getStructure() {
        return structure;
    }

    public void setStructure(ViewStructure structure) {
        this.structure = structure;
    }

    public int getCounts() {
        return itemList.size();
    }

    public MenuItem getItem(int positon) {
        if (positon >= getCounts()) {
            return null;
        }
        return itemList.get(positon);
    }

    public List<MenuItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<MenuItem> itemList) {
        this.itemList = itemList;
    }

    public void removeItem(String tag) {
        for (int i = 0; i < itemList.size(); i++) {
            MenuItem item = itemList.get(i);
            if (item.getEntag().equals(tag)) {
                itemList.remove(i);
                break;
            }
        }
    }
    public void removeAuthItem(String tag) {
        Menu menu = null;
        for (int i = 0; i < itemList.size(); i++) {
            MenuItem item = itemList.get(i);
            if (item.getEntag().equals("AUTH")) {
                menu = (Menu)itemList.get(i);
                break;
            }
        }
        if (null != menu) {
            for (int j = 0;j<menu.getItemList().size();j++) {
                MenuItem item = menu.getItemList().get(j);
                if (item.getEntag().equals(tag)) {
                    menu.getItemList().remove(j);
                    break;
                }
            }
        }
    }
    public MenuItem findItem(String tag) {
        for (int i = 0; i < itemList.size(); i++) {
            MenuItem item = itemList.get(i);
            if (item.getEntag().equals(tag)) {
                return itemList.get(i);
            }
        }
        return null;
    }

    public static final Parcelable.Creator<Menu> CREATOR = new Parcelable.Creator<Menu>() {
        @Override
        public Menu createFromParcel(Parcel source) {
            Menu menu = new Menu();
            menu.entag = source.readString();
            menu.chnTag = source.readString();
            menu.iconResName = source.readString();
            menu.textResName = source.readString();
            menu.processFile = source.readString();
            menu.transCode = source.readString();
            menu.structure = ViewStructure.valueOf(source.readString());
            source.readList(menu.itemList, getClass().getClassLoader());
            return menu;
        }

        @Override
        public Menu[] newArray(int size) {
            return new Menu[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(structure.name());
        dest.writeList(itemList);

    }

    /**
     * 视图结构
     */
    public enum ViewStructure {
        //九宫格
        GRID,
        //列表
        LIST
    }

    @Override
    public String toString() {
        return "Menu{" +
                "structure='" + structure + '\'' +
                ", entag='" + entag + '\'' +
                ", chnTag='" + chnTag + '\'' +
                ", iconResName='" + iconResName + '\'' +
                ", textResName='" + textResName + '\'' +
                ", processFile='" + processFile + '\'' +
                ", transCode='" + transCode + '\'' +
                '}';
    }
}

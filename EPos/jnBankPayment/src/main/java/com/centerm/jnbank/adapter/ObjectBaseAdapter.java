package com.centerm.jnbank.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linwenhui on 2016/11/4.
 */

public abstract class ObjectBaseAdapter<T> extends BaseAdapter {

    private List<T> mData;

    public ObjectBaseAdapter(Context mCtx) {
        mData = new ArrayList<>();
    }

    public void addObject(T model) {
        if (model != null && !this.mData.contains(model)) {
            this.mData.add(model);
            notifyDataSetChanged();
        }
    }

    public void updateView(List<T> list){
        this.mData = list;
        notifyDataSetChanged();
    }

    public void addAll(List<T> list) {
        this.mData.clear();
        this.mData.addAll(list);
        notifyDataSetChanged();
        /*if (list != null && !list.isEmpty()) {
            boolean isUpdate = false;
            for (T info : list) {
                if (!this.mData.contains(info)) {
                    this.mData.add(info);
                    isUpdate = true;
                }
            }
            if (isUpdate) {
                notifyDataSetChanged();
            }
        }*/
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}

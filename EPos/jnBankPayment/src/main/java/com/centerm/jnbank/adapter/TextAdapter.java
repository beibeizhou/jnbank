package com.centerm.jnbank.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by linwenhui on 2016/11/4.
 */

public  class TextAdapter<T> extends ObjectBaseAdapter<T> {
    private LayoutInflater mInflater;
    private int layoutResId, txtvwId;

    public TextAdapter(Context mCtx, @LayoutRes int layoutResId) {
        this(mCtx, layoutResId, 0);
    }

    public TextAdapter(Context mCtx, @LayoutRes int layoutResId, @IdRes int txtvwId) {
        super(mCtx);
        this.layoutResId = layoutResId;
        this.txtvwId = txtvwId;
        mInflater = LayoutInflater.from(mCtx);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView txtvw;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            if (txtvwId > 0) {
                txtvw = (TextView) convertView.findViewById(txtvwId);
                convertView.setTag(txtvw);
            } else
                txtvw = (TextView) convertView;
        } else {
            if (txtvwId > 0) {
                txtvw = (TextView) convertView.getTag();
            } else {
                txtvw = (TextView) convertView;
            }
        }
        convert(getItem(position), txtvw);
        return convertView;
    }

    public void convert(T model, TextView txtvw) {
        if (model instanceof String) {
            txtvw.setText((String) model);
        }
    }

}

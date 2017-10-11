package com.centerm.jnbank.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by linwenhui on 2016/11/4.
 */

public  class OperaterAdapter<T> extends ObjectBaseAdapter<T> {
    private LayoutInflater mInflater;
    private int layoutResId, txtvwId,editId,delId;


    public OperaterAdapter(Context mCtx, @LayoutRes int layoutResId, @IdRes int txtvwId,@IdRes int editId,@IdRes int delId) {
        super(mCtx);
        this.layoutResId = layoutResId;
        this.txtvwId = txtvwId;
        this.editId = editId;
        this.delId = delId;
        mInflater = LayoutInflater.from(mCtx);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder hoder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            hoder = new ViewHoder();
            if (txtvwId > 0) {
                hoder.txtvw = (TextView) convertView.findViewById(txtvwId);
                hoder.iv_edit = (ImageView) convertView.findViewById(editId);
                hoder.iv_del = (ImageView) convertView.findViewById(delId);
                convertView.setTag(hoder);
            }

        } else {
            if (txtvwId > 0) {
                hoder = (ViewHoder) convertView.getTag();
            }
        }
        convert(getItem(position), hoder);
        return convertView;
    }

    public void convert(T model, ViewHoder hoder) {
        if (model instanceof String) {
            hoder.txtvw.setText((String) model);
        }
    }
    public static class ViewHoder{
        public TextView txtvw;
        public ImageView iv_edit;
        public ImageView iv_del;
    }
}

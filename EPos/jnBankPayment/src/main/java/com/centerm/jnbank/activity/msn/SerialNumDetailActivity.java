package com.centerm.jnbank.activity.msn;

import android.widget.ListView;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.adapter.TextAdapter;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by linwenhui on 2016/11/4.
 */

public class SerialNumDetailActivity extends BaseActivity {

    CommonDao<TradeInfo> tradeInfoCommonDao;
    ListView lstvwNumDetail;
    TextAdapter<String> adapterNumDetaill;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_serial_num_detail;
    }

    @Override
    public void onInitView() {
        TextView txtvwTitle = (TextView) findViewById(R.id.txtvw_title);
        txtvwTitle.setText(R.string.label_serial_num_detail);
        lstvwNumDetail = (ListView) findViewById(R.id.lstvw_num_detail);
        adapterNumDetaill = new TextAdapter<>(this, R.layout.adapter_query_operator);
        lstvwNumDetail.setAdapter(adapterNumDetaill);
        tradeInfoCommonDao = new CommonDao<>(TradeInfo.class, dbHelper);
        try {
            List<TradeInfo> tradeInfos = tradeInfoCommonDao.queryBuilder().orderByRaw("iso_f13,iso_f12").query();
            if (tradeInfos != null && !tradeInfos.isEmpty()) {
                initDetail(tradeInfos.get(0));
            } else {
                initDetail(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initDetail(TradeInfo tradeInfo) {
        List<String> datas = new ArrayList<>();
        if (tradeInfo != null) {
            datas.add(getString(R.string.label_serial_num_detail_type) + tradeInfo.getTransCode());
            datas.add(getString(R.string.label_serial_num_detail_money) + tradeInfo.getIso_f4());
            datas.add(getString(R.string.label_serial_num_detail_no)+tradeInfo.getIso_f11());
            datas.add(getString(R.string.label_serial_num_detail_orderid));
            datas.add(getString(R.string.label_serial_num_detail_time) + tradeInfo.getIso_f13() + " " + tradeInfo.getIso_f12());
        } else {
            datas.add(getString(R.string.label_serial_num_detail_type));
            datas.add(getString(R.string.label_serial_num_detail_money));
            datas.add(getString(R.string.label_serial_num_detail_no));
            datas.add(getString(R.string.label_serial_num_detail_orderid));
            datas.add(getString(R.string.label_serial_num_detail_time));
        }
        adapterNumDetaill.addAll(datas);
    }
}

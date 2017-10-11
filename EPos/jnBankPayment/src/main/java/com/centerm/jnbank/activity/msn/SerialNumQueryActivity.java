package com.centerm.jnbank.activity.msn;

import com.centerm.jnbank.base.BaseActivity;

//import in.srain.cube.views.ptr.PtrClassicFrameLayout;
//import in.srain.cube.views.ptr.PtrDefaultHandler2;
//import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by linwenhui on 2016/11/4.
 */

public class SerialNumQueryActivity extends BaseActivity {
    @Override
    public int onLayoutId() {
        return 0;
    }

    @Override
    public void onInitView() {

    }


/*    private EditText edtxtSerialNumContent;
    private PtrClassicFrameLayout layoutSerialNum;
    private ListView lstvwSerialNum;
    private CommonDao<TradeInfo> tradeInfoCommonDao;
    private TradeInfoAdapter tradeInfoAdapter;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_serial_num_query;
    }

    @Override
    public void onInitView() {
        TextView title = (TextView) findViewById(R.id.txtvw_title);
        title.setText(R.string.label_serial_num);
        tradeInfoCommonDao = new CommonDao<>(TradeInfo.class, dbHelper);
        edtxtSerialNumContent = (EditText) findViewById(R.id.pos_serial_edit);
        layoutSerialNum = (PtrClassicFrameLayout) findViewById(R.id.layout_serial_num);
        layoutSerialNum.setPtrHandler(new PtrDefaultHandler2() {

            @Override
            public boolean checkCanDoLoadMore(PtrFrameLayout frame, View content, View footer) {
                return PtrDefaultHandler2.checkContentCanBePulledUp(frame, content, footer);
            }

            @Override
            public void onLoadMoreBegin(final PtrFrameLayout frame) {
                String tagQueryContent = edtxtSerialNumContent.getText().toString().trim().replace(" ", "");
                try {
                    List<TradeInfo> infos = tradeInfoCommonDao.queryBuilder().offset((long) tradeInfoAdapter.getCount()).limit(10l).where().like("iso_f11", tagQueryContent).query();
                    if (infos == null && infos.isEmpty()) {
                        ViewUtils.showToast(SerialNumQueryActivity.this, R.string.tip_serial_num_not_exist);
                        return;
                    }
                    tradeInfoAdapter.addAll(infos);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                frame.refreshComplete();
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {

            }
        });
        CommonDao commonDao = new CommonDao(TradeInfo.class,new DbHelper(context));
        List<TradeInfo> tradeInfos =commonDao.query();
        lstvwSerialNum = (ListView) findViewById(R.id.lstvw_serial_num);
        tradeInfoAdapter = new TradeInfoAdapter(this);
        tradeInfoAdapter.addAll(tradeInfos);
        lstvwSerialNum.setAdapter(tradeInfoAdapter);


    }

    public void onQueryClick(View v) {
        String tagQueryContent = edtxtSerialNumContent.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(tagQueryContent)) {
            ViewUtils.showToast(this, R.string.tip_serial_num_empty);
            return;
        }

        TradeInfo info = tradeInfoCommonDao.queryForId(tagQueryContent);
        if (info == null) {
            ViewUtils.showToast(this, R.string.tip_serial_num_not_exist);
            return;
        }
        tradeInfoAdapter.addObject(info);
    }

    private class TradeInfoAdapter extends ObjectBaseAdapter<TradeInfo> {
        private LayoutInflater inflater;

        public TradeInfoAdapter(Context mContext) {
            super(mContext);
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView code;
            TextView type;
            TextView money;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.adapter_serial_num_query, parent, false);
                code = (TextView) convertView.findViewById(R.id.code);
                type = (TextView) convertView.findViewById(R.id.type);
                money = (TextView) convertView.findViewById(R.id.money);
                convertView.setTag(R.id.code, code);
                convertView.setTag(R.id.type, type);
                convertView.setTag(R.id.money, money);
            } else {
                code = (TextView) convertView.getTag(R.id.code);
                type = (TextView) convertView.getTag(R.id.type);
                money = (TextView) convertView.getTag(R.id.money);
            }
            TradeInfo info = getItem(position);
            code.setText(info.getIso_f11());
            type.setText(info.getTransCode());
            money.setText(info.getIso_f4());
            return convertView;
        }

    }*/
}

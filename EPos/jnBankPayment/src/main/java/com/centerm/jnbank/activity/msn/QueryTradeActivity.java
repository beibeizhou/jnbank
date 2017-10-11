package com.centerm.jnbank.activity.msn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.adapter.ObjectBaseAdapter;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.List;

import config.Config;

/**
 * 交易查询界面
 * author:wanliang527</br>
 * date:2016/11/11</br>
 */

public class QueryTradeActivity extends BaseActivity {

    private final static int MSG_QUERY_SUCCESS = 0x01;
    private final static int MSG_CLEAR_LIST_DATA = 0x02;
    private final static int MSG_QUERY_NO_MORE_DATA = 0x03;
    private final static int MSG_QUERY_NO_DATA = 0x04;

    private CommonDao<TradeInfo> tradeInfoDao;
    private ListView listView;
    private Adapter adapter;
    private Adapter tempAdapter;
    private MaterialRefreshLayout refreshLayout;
    private boolean isLoadMoreFlag;
    private EditText posSerialEdit;
    private int counts;
    private int index;

    private boolean tempFlag;
    private CommonManager commonManager;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            refreshLayout.finishRefresh();
            refreshLayout.finishRefreshLoadMore();
            if (isPause()) {
                //为了防止窗体泄露
                return;
            }
            super.handleMessage(msg);
            int what = msg.what;
//            refreshLayout.finishRefresh();
//            refreshLayout.finishRefreshLoadMore();
            switch (what) {
                case MSG_QUERY_SUCCESS:
                    updateListView((List<TradeInfo>) msg.obj);
                    break;
                case MSG_CLEAR_LIST_DATA:
                    if (adapter != null) {
                        adapter.clear();
                    }
                    break;
                case MSG_QUERY_NO_MORE_DATA:
                    DialogFactory.showMessageDialog(context, null, getString(R.string.tip_no_more_trade_info));
                    isLoadMoreFlag = false;
                    refreshLayout.setLoadMore(isLoadMoreFlag);
                    break;
                case MSG_QUERY_NO_DATA:
                    DialogFactory.showMessageDialog(context, null, getString(R.string.tip_no_trade_info));
                    isLoadMoreFlag = false;
                    refreshLayout.setLoadMore(isLoadMoreFlag);
                    break;
            }
        }
    };


    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        tradeInfoDao = new CommonDao<>(TradeInfo.class, dbHelper);
        commonManager = new CommonManager(TradeInfo.class, context);
    }

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_query_trade;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_query_trade);
        showRightButton(getString(R.string.label_trade_summary));
        listView = (ListView) findViewById(R.id.list_v);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Adapter adp = (Adapter) parent.getAdapter();
                TradeInfo tradeInfo = adp.getItem(position);
                Intent intent = new Intent(context, TradeDetailActivity.class);
                intent.putExtra(KEY_TRADE_INFO, tradeInfo);
                startActivity(intent);
            }
        });
        refreshLayout = (MaterialRefreshLayout) findViewById(R.id.refresh_layout);
        isLoadMoreFlag = true;
        refreshLayout.setLoadMore(isLoadMoreFlag);
        refreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                tempFlag = true;
                posSerialEdit.setText("");
                tempFlag = false;
                asyncQueryTradeInfo(true);
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                asyncQueryTradeInfo(false);
            }
        });
        posSerialEdit = (EditText) findViewById(R.id.pos_serial_edit);
        posSerialEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!tempFlag && (s == null || s.toString().length() == 0) && adapter != null) {
                    //搜索框无内容时，恢复原来的列表内容
                    if (adapter != null) {
                        listView.setAdapter(adapter);
                    }
                    //恢复列表属性
                    refreshLayout.setLoadMore(isLoadMoreFlag);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        asyncQueryTradeInfo(true);
    }


    public void onSearchClick(View view) {
        String posSerial = posSerialEdit.getText().toString();
        if (TextUtils.isEmpty(posSerial) || posSerial.length() != 6) {
            ViewUtils.showToast(context, R.string.tip_input_pos_serial);
            return;
        }
        TradeInfo info = tradeInfoDao.queryForId(posSerial);
        if (info == null || info.getFlag() == 0|| info.getFlag() == 3 || info.getFlag() == 6) {
            logger.debug("搜索交易流水==>" + posSerial + (info != null) + "==>不显示");
            info = null;
        }
        if (info != null) {
            if("BALANCE".equals(info.getTransCode())){
                logger.warn("搜索交易流水为余额查询");
                info = null;
            }
        }
        if (info == null) {
            listView.setAdapter(null);
            DialogFactory.showMessageDialog(context, null, getString(R.string.tip_cannot_query_the_trade));
        } else {
            refreshLayout.setLoadMore(false);
            if (tempAdapter == null) {
                tempAdapter = new Adapter(context);
            }
            tempAdapter.clear();
            tempAdapter.addObject(info);
            listView.setAdapter(tempAdapter);
        }
    }

    @Override
    public void onRightButtonClick(View view) {
        Intent intent = new Intent(context, TradeSummaryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        //隐藏所有对话框为了防止窗体泄露
        DialogFactory.hideAll();
        super.onPause();
    }

    private void asyncQueryTradeInfo(final boolean refresh) {
        DialogFactory.showLoadingDialog(context, "查询中，请稍候...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    long start = System.currentTimeMillis();
                    counts = (int) commonManager.getValidCount();
                    if (counts == 0) {
                        handler.sendMessage(newMessage(MSG_QUERY_NO_DATA, null));
                        return;
                    }
                    final long offset = refresh ? 0 : index;//数据库查询的偏移量
                    logger.debug("查询流水偏移量==>" + offset);
                    if (offset >= counts) {
                        logger.warn("交易流水数量==>" + counts + "==>偏移量" + offset + "==>不再进行查询");
                        handler.sendMessage(newMessage(MSG_QUERY_NO_MORE_DATA, null));
                        return;
                    }
                    QueryBuilder<TradeInfo, String> qb = tradeInfoDao.queryBuilder();
                    qb.offset(offset);
                    qb.where().not().eq("flag", 0).and()//交易未成功
                            .not().eq("flag", 3).and()//冲正成功
                            .not().eq("flag", 6).and()//冲正失败
                            .not().eq("transCode", "BALANCE");
                    qb.limit(Config.TRADE_INFO_LIST_PAGE_SIZE);
                    qb.orderBy("iso_f11", false);
                    List<TradeInfo> dataList = qb.query();
                    if (refresh) {
                        index = 0;
                        //先清空
                        handler.sendMessage(newMessage(MSG_CLEAR_LIST_DATA, null));
                    }
                    if (dataList.size() > 0) {
                        handler.sendMessage(newMessage(MSG_QUERY_SUCCESS, dataList));
                    } else {
                        handler.sendMessage(newMessage(MSG_QUERY_NO_MORE_DATA, null));
                    }
                    long end = System.currentTimeMillis();
                    logger.info("查询交易流水完成==>耗时==>" + (end - start) / 1000.0);
                    DialogFactory.hideAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Message newMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        return msg;
    }

    private void updateListView(List<TradeInfo> dataList) {
        if (adapter == null) {
            adapter = new Adapter(context);
        }
        listView.setAdapter(adapter);
        if (dataList != null) {
            index += dataList.size();
        }
        if (counts == 0) {
            isLoadMoreFlag = false;
            refreshLayout.setLoadMore(false);
        } else {
            isLoadMoreFlag = true;
            refreshLayout.setLoadMore(true);
        }
        adapter.addAll(dataList);
    }


    private class Adapter extends ObjectBaseAdapter<TradeInfo> {

        public Adapter(Context mCtx) {
            super(mCtx);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TradeInfo data = getItem(position);
            int layoutId = R.layout.v_trade_record_item;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(layoutId, null);
            }
            String transCode = data.getTransCode();
            TextView posSerial = (TextView) convertView.findViewById(R.id.pos_serial_show);
            TextView transType = (TextView) convertView.findViewById(R.id.trans_type_show);
            TextView transAmt = (TextView) convertView.findViewById(R.id.trans_money_show);
            if (TransCode.CREDIT_SETS.contains(transCode)) {
                //贷记类交易标蓝
                transType.setTextColor(getResources().getColor(R.color.font_blue));
            } else {
                transType.setTextColor(getResources().getColor(R.color.font_black));
            }
            posSerial.setTextColor(getResources().getColor(R.color.font_black));
            transAmt.setTextColor(getResources().getColor(R.color.font_black));

            posSerial.setText(data.getIso_f11());
            transType.setText(getString(TransCode.codeMapName(transCode)));
            transAmt.setText(DataHelper.formatAmountForShow(data.getIso_f4()));
            return convertView;
        }
    }
}

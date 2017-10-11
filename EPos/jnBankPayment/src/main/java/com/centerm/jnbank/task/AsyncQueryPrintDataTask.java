package com.centerm.jnbank.task;

import android.content.Context;
import android.os.AsyncTask;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonManager;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncQueryPrintDataTask extends AsyncTask<Void, Integer, List<List<TradeInfo>>> implements CheckBillkListener {
    protected Logger logger = Logger.getLogger(this.getClass());
    private final CommonManager commonManager;
    protected final static long LONG_SLEEP = 1000;
    public AsyncQueryPrintDataTask(Context context) {
        commonManager = new CommonManager(TradeInfo.class, context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    @Override
    protected List<List<TradeInfo>> doInBackground(Void... params) {
        sleep(LONG_SLEEP);
        List<TradeInfo> jiejiList = new ArrayList<>();
        List<TradeInfo> daijiList = new ArrayList<>();
        List<TradeInfo> saleDetailList = new ArrayList<>();
        List<TradeInfo> rejestList = new ArrayList<>();
        List<TradeInfo> failList = new ArrayList<>();
        List<TradeInfo> batchDetail = new ArrayList<>();
        List<TradeInfo> reverseDetail = new ArrayList<>();
        List<List<TradeInfo>> lists = new ArrayList<>();
        try {
            jiejiList = commonManager.getDebitList();
            daijiList = commonManager.getCreditList();
            saleDetailList = commonManager.getTransDetail();
            rejestList = commonManager.getRefusedList();
            failList = commonManager.getFailList();
            batchDetail = commonManager.getBatchList();
            reverseDetail = commonManager.getReverseList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lists.add(jiejiList);
        lists.add(daijiList);
        lists.add(saleDetailList);
        lists.add(rejestList);
        lists.add(failList);
        lists.add(batchDetail);
        lists.add(reverseDetail);
        return lists;
    }
    protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPostExecute(List<List<TradeInfo>> lists) {
        super.onPostExecute(lists);
        onFinish(lists);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onFinish(List<List<TradeInfo>> lists) {

    }
}

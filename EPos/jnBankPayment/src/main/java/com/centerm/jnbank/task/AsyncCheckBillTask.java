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

public abstract class AsyncCheckBillTask extends AsyncTask<Void, Integer, List<List<TradeInfo>>> implements CheckBillkListener {
    protected Logger logger = Logger.getLogger(this.getClass());
    private final CommonManager commonManager;
    public AsyncCheckBillTask(Context context) {
        logger.debug("创建AsyncCheckBillTask");
        commonManager = new CommonManager(TradeInfo.class, context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    @Override
    protected List<List<TradeInfo>> doInBackground(Void... params) {
        logger.debug("执行AsyncCheckBillTask111111111");
        List<TradeInfo> magsCards = new ArrayList<>();
        List<TradeInfo> icCards = new ArrayList<>();
        List<TradeInfo> refundInfos = null;
        List<List<TradeInfo>> lists = new ArrayList<>();
        try {
            List<TradeInfo> transInfos = commonManager.getListForBatch();
            if (null != transInfos) {
                logger.debug("当前批次交易成功的记录有：" + transInfos.size());
                for (TradeInfo tradeInfo :
                        transInfos) {
                    String cardType = tradeInfo.getIso_f22().substring(0, 2);
                    if ("02".equals(cardType)) {//磁条卡
                        magsCards.add(tradeInfo);
                    } else {//其他的都归于ic卡
                        icCards.add(tradeInfo);
                    }
                }
                logger.debug("其中ic卡：" + icCards.size());
                logger.debug("其中磁条卡：" + magsCards.size());
            } else {
                logger.error("当前批次没有成功的交易");
            }
            //退货成功的流水
            refundInfos = commonManager.getRefundList();
            if (null != refundInfos) {
                logger.debug("当前批次退货记录：" + refundInfos.size());
            } else {
                logger.debug("当前批次没有退货记录");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lists.add(magsCards);
        lists.add(icCards);
        lists.add(refundInfos);
        return lists;
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

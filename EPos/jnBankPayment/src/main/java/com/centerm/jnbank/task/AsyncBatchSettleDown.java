package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonManager;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncBatchSettleDown extends BaseAsyncTask {
    protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private CommonManager commonManager;
    public AsyncBatchSettleDown(Context context) {
        super(context);
        commonManager = new CommonManager(TradeInfo.class, context);
        logger.debug("点击结算创建AsyncBatchSettleDown");
    }

    @Override
    protected Object doInBackground(Object[] params) {
        logger.debug("点击结算执行AsyncBatchSettleDown");
        sleep(LONG_SLEEP);
        List<TradeInfo> tradeInfos = null;
        try {
            tradeInfos = commonManager.getBatchList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (null != tradeInfos && tradeInfos.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}

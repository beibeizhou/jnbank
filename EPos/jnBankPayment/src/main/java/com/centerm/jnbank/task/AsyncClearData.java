package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncClearData extends BaseAsyncTask {
    private CommonManager commonManager;
    public AsyncClearData(Context context) {
        super(context);
        commonManager = new CommonManager(TradeInfo.class, context);
    }

    @Override
    protected Object doInBackground(Object[] params) {
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

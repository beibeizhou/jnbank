package com.centerm.jnbank.task.qrcode;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.task.BaseAsyncTask;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncQueryLastData extends BaseAsyncTask {
    private CommonManager commonManager;
    public AsyncQueryLastData(Context context) {
        super(context);
        commonManager = new CommonManager(TradeInfo.class, context);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        sleep(LONG_SLEEP);
        List<TradeInfo> tradeInfos = null;
        try {
            tradeInfos = commonManager.getLastCode();
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

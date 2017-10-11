package com.centerm.jnbank.task;

import com.centerm.jnbank.bean.TradeInfo;

import java.util.List;

/**
 * 任务进度监听器
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public interface CheckBillkListener{


    /**
     * 任务开始执行
     */
    void onStart();

    /**
     * 任务执行完成
     */
    void onFinish(List<List<TradeInfo>> lists);
}

package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.msg.MessageFactory;
import com.centerm.jnbank.net.SocketClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 该类用于实现队列式网络请求的异步任务
 * author:wanliang527</br>
 * date:2016/11/23</br>
 */

public abstract class AsyncMultiRequestTask extends BaseAsyncTask<String, String[]> {
    protected Map<String, String> dataMap;
    protected MessageFactory factory;
    protected SocketClient client;
    protected int taskRetryTimes;
    protected final String[] taskResult = new String[3];


    public AsyncMultiRequestTask(Context context, Map<String, String> dataMap) {
        super(context);
        this.dataMap = new HashMap<>();
        this.dataMap.putAll(dataMap);
        factory = new MessageFactory(context);
        client = SocketClient.getInstance(context);
    }

    @Override
    protected String[] doInBackground(String... params) {
        return taskResult;
    }
}

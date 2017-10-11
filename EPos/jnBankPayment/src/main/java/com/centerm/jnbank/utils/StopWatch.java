package com.centerm.jnbank.utils;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 计时器
 * author:wanliang527</br>
 * date:2016/12/26</br>
 */

public class StopWatch {

    private Logger logger = Logger.getLogger(StopWatch.class);
    private Context context;
    private long time;
    private Timer timer;
    private TimerTask task;
    private boolean startedFlag;
    private Handler handler;
    private TimeoutHandler timeoutHandler;


    public StopWatch(Context context, long time) {
        this.context = context;
        this.time = time;
        if (context != null) {
            this.handler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            if (timeoutHandler != null) {
                                timeoutHandler.onTimeout();
                            }
                            break;
                        case 2:
                            break;
                    }
                }
            };
        }
    }


    public void start() {
        if (startedFlag) {
            logger.warn("计时任务已开启==>将重置任务");
            stop();
        }
        init();
        logger.debug("开始执行计时任务");
        timer.schedule(task, time);
        startedFlag = true;
    }

    private void init() {
        if (timer == null) {
            timer = new Timer();
        }
        if (task == null) {
            task = createTask();
        }
    }

    public void reset() {
        if (startedFlag) {
            stop();
            start();
        } else {
            logger.debug("计时任务复位失败==>任务未开启");
        }
    }


    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        if (task != null) {
            task.cancel();
        }
        timer = null;
        task = null;
        startedFlag = false;
    }

    private TimerTask createTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                logger.debug("计时器==>计时结束");
                if (handler != null) {
                    handler.obtainMessage(1).sendToTarget();
                }
            }
        };
        return task;
    }


    public TimeoutHandler getTimeoutHandler() {
        return timeoutHandler;
    }

    public void setTimeoutHandler(TimeoutHandler timeoutHandler) {
        this.timeoutHandler = timeoutHandler;
    }

    public interface TimeoutHandler {
        void onTimeout();
    }

}

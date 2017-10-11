package com.centerm.jnbank.task;

/**
 * 任务进度监听器
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public interface ITaskListener<Result>{

    /**
     * 任务进度
     *
     * @param counts 总任务数
     * @param index  当前执行的任务
     */
    void onProgress(Integer counts, Integer index);

    /**
     * 任务开始执行
     */
    void onStart();

    /**
     * 任务执行完成
     */
    void onFinish(Result result);
}

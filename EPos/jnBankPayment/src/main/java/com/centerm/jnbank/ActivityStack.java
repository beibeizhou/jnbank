package com.centerm.jnbank;

import android.app.Activity;
import android.util.Log;

import java.util.Stack;

/**
 * Activity堆栈类，用于Actiivty管理
 * author: wanliang527</br>
 * date:2016/9/18</br>
 */
public class ActivityStack {

    private final static String TAG = ActivityStack.class.getSimpleName();
    private Stack<Activity> stack;
    private static ActivityStack instance;
    private boolean strictMode;//严格模式下，需要保证栈中每个Activity的实例对象只有一个


    /**
     * 获取实例对象
     *
     * @return Activity栈的实例对象
     */
    public static ActivityStack getInstance() {
        if (instance == null) {
            synchronized (ActivityStack.class) {
                if (instance == null) {
                    instance = new ActivityStack();
                }
            }
        }
        return instance;
    }

    private ActivityStack() {
        stack = new Stack<>();
    }

    public void logStackSize() {
        Log.d(TAG, "Stack size = " + stack.size());
    }

    /**
     * 入栈，如果当前栈中已经存在该Activity对象，则移除该对象后再压栈。
     *
     * @param activity 需要压栈的对象
     * @return 被压入栈中的Activity对象
     */
    public Activity push(Activity activity) {
        if (activity != null) {
            int p = stack.indexOf(activity);
            Log.v(TAG, "push " + activity.toString());
            if (p != -1) {
                stack.remove(p);
            } else {
                if (strictMode) {
                    remove(activity.getClass(), false);
                }
            }
            stack.push(activity);
        }
        logStackSize();
        return activity;
    }

    /**
     * 出栈并结束相应的Activity对象
     * 应用场景：回退到上一个界面
     *
     * @return 移除栈的Activity对象
     */
    public Activity pop() {
        return pop(true);
    }

    /**
     * 出栈
     *
     * @param finish 是否结束掉对应的Activity对象
     * @return 移除栈的Activity对象，如果该Activity需要被结束掉，那么返回null，防止内存泄漏。
     */
    public Activity pop(boolean finish) {
        Log.v(TAG, "pop activity");
        if (stack.isEmpty()) {
            return null;
        }
        Activity activity = stack.pop();
        if (finish && activity != null) {
            Log.i(TAG, "Finish activity " + activity.toString());
            activity.finish();
        }
        logStackSize();
        if (finish) {
            activity = null;
        }
        return activity;
    }

    /**
     * 回退到指定Activity中<b/>
     * 风险：如果当前栈中不包含指定类的对象，则栈中的所有的元素都会被移除，并且finish，相当于整个应用退出。
     *
     * @param clz Activity类对象
     * @return 成功回退返回true，失败返回false
     */
    public boolean backTo(Class<? extends Activity> clz) {
        boolean result = false;
        while (!stack.empty()) {
            Activity activity = pop(false);
//            Log.i("test", "Statck top activity = " + activity.getClass().getSimpleName());
//            Log.i("test", "Need back cls = " + clz.getSimpleName());
            if (!activity.getClass().equals(clz)) {
                //非指定Activity，则移除出栈并finish
                activity.finish();
            } else {
                //遇到指定Activity，再压入栈顶
                stack.push(activity);
                result = true;
                break;
            }
        }
        logStackSize();
        return result;
    }

    public void removeAll(){
        if (!stack.empty()) {
            for (Activity activity:
                    stack) {
                activity.finish();
            }
        }
    }


    /**
     * 回退到栈底
     */
    public void backToBottom() {
        while (!stack.empty()) {
            if (stack.size() > 1) {
                pop();
            }
        }
    }


    /**
     * 从堆栈中移除指定的Activity对象，如果finish标识为true，该方法可以代替Activity的finish方法
     *
     * @param activity Activity对象
     * @param finish   是否结束Activity
     * @return 移除成功返回true，移除失败返回false
     */
    public boolean remove(Activity activity, boolean finish) {
        Log.v(TAG, "remove  " + (activity == null ? "null" : activity.toString()));
        boolean r = stack.remove(activity);
        if (finish && activity != null && !activity.isFinishing()) {
            activity.finish();
        }
        logStackSize();
        return r;
    }

    /**
     * 可以代替Activity的finish方法
     *
     * @param activity Activity对象
     * @return 移除成功返回true，移除失败返回false
     */
    public boolean remove(Activity activity) {
        return remove(activity, true);
    }

    /**
     * 移除栈中指定Activity的实例对象，并且finish
     * 注意：该方法谨慎使用，该方法会循环遍历栈中对象，效率偏低
     * 推荐使用{@link #remove(Activity)}
     *
     * @param clz Activity类对象
     * @return 移除成功返回true，否则false
     */
    public boolean remove(Class<? extends Activity> clz) {
        boolean result = false;
        if (clz != null) {
            for (int i = 0; i < stack.size(); i++) {
                Activity activity = stack.get(i);
                if (activity.getClass().equals(clz)) {
                    remove(activity);
                }
            }
        }
        logStackSize();
        return result;
    }

    /**
     * 除指定页面外的其他页面，全部移除
     * @param clz
     */
    public void removeExcept(Class<? extends Activity> clz){
        if (stack == null) {
            Log.e(TAG, "activityStack为空，异常情况");
            return;
        }
        Stack<Activity> tempStack = new Stack<Activity>();
        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i).getClass().equals(clz)) {
                tempStack.add(stack.get(i));
                continue;
            }
            stack.get(i).finish();
        }
        stack.clear();
        stack.addAll(tempStack);
    }

    private void remove(Class<? extends Activity> clz, boolean finish) {
        for (int i = 0; i < stack.size(); i++) {
            Activity activity = stack.get(i);
            if (activity.getClass().equals(clz)) {
                remove(activity, finish);
            }
        }
    }

    public boolean contain(Class<? extends Activity> clz) {
        for (int i = 0; i < stack.size(); i++) {
            Activity activity = stack.get(i);
            if (activity.getClass().equals(clz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前是否是严格模式
     *
     * @return 严格模式返回true，否则返回false
     */
    public boolean isStrictMode() {
        return strictMode;
    }

    /**
     * 设置严格模式。
     * 严格模式下，需要保证栈中每个Activity的实例对象只有一个
     *
     * @param strictMode ture为严格模式
     */
    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }
}
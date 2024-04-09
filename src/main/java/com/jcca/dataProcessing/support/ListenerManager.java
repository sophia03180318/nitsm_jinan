package com.jcca.dataProcessing.support;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO
 * @className ListenerManager
 * @date 2023/10/20 11:54
 * @since 2.1.0.0
 */
public class ListenerManager<T> {
    /**
     * 当前存在的监听器
     */
    protected List<IListener> IListeners = new ArrayList<>();

    public void addDataSourceListener(IListener IListener) {
        IListeners.add(IListener);
    }

    /**
     * 删除监听器
     *
     * @param IListener
     */
    public void removeEventListener(IListener IListener) {
        IListeners.remove(IListener);
    }

    /**
     * 触发事件
     *
     * @param event
     */
    public void dispatureEvent(T event) {
        for (IListener IListener : IListeners) {
            IListener.onEvent(event);
        }
    }

    /**
     * 获取当前的监听器
     *
     * @return
     */
    public List<IListener> getListeners() {
        return this.IListeners;
    }
}

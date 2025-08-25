package com.jcca.dataProcessing.support;

/**
 * 采集适配
 *
 * @author Lvyp
 */
public interface IAdapter<T> {



    /**
     * 处理数据
     *
     * @param data 接收到的数据
     */
    void dispose(T data);

    /**
     * 获取对应的KEY
     *
     * @return KEY
     */
    String getCode();

    /**
     * 可用户获取一些过程中的处理信息
     *
     * @return 消息数量
     */
    String dataProcess();
}

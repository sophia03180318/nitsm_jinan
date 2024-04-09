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
     * @param data
     */
    void dispose(T data);

    /**
     * 获取对应的KEY
     *
     * @return
     */
    String getCode();

    /**
     * 可用户获取一些过程中的处理信息
     *
     * @return
     */
    String dataProcess();
}

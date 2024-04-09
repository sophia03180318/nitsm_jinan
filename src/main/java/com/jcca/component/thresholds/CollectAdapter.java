package com.jcca.component.thresholds;

import cn.hutool.json.JSONArray;

/**
 * 采集适配
 *
 * @author Lvyp
 */
public interface CollectAdapter {

    /**
     * 处理数据
     *
     * @param data
     */
    void dispose(JSONArray data);

    /**
     * 获取对应的KEY
     *
     * @return
     */
    String getCode();
}

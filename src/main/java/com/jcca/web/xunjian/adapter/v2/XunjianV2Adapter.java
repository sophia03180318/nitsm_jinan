package com.jcca.web.xunjian.adapter.v2;


import cn.hutool.json.JSONObject;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.entity.XunjianDetailV2;

/**
 * 指标巡检适配器
 */
public interface XunjianV2Adapter {


    /**
     * 获取对应的KEY
     *
     * @return 采集适配Code
     */
    String getCode();

    /**
     * 获取采集项名称
     * @return
     */
    String getName(Asset asset);

    /**
     * 含有阈值判定获取最大值
     * @return
     */
    Integer getMaxValue();

    /**
     * 采集命令
     * @return
     */
    String getCommand();

    /**
     * 发起巡检
     * @param asset 设备
     */
    XunjianDetailV2 xunJian(Asset asset, String xunjianRecordId,String orgMsg);

}

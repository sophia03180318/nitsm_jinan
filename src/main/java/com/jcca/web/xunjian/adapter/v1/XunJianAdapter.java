package com.jcca.web.xunjian.adapter.v1;

import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;

/**
 * 巡检
 *
 * @author Lvyp
 */
public interface XunJianAdapter {

    /**
     * 处理数据
     *
     * @param asset
     */
    XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail);

    /**
     * 获取对应的KEY
     *
     * @return
     */
    String getCode();

}

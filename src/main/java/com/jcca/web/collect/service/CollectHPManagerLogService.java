package com.jcca.web.collect.service;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.service.bean.HPManagerLogVo;

import java.util.List;

/**
 * 采集惠普管理口信息
 */
public interface CollectHPManagerLogService {

    /**
     * 采集惠普管理口的信息
     *
     * @param asset
     * @return
     */
    List<HPManagerLogVo> queryHPManagerLog(Asset asset) throws Exception;

}

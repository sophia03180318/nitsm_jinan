package com.jcca.web.collect.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectPort;

import java.util.List;

/**
 * 端口占用
 */
public interface CollectPortService extends IService<CollectPort> {

    /**
     * 更新端口
     * @param portList
     */
    void updatePortList(List<CollectPort> portList,String assetId);

}

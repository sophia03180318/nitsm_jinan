package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.TopoTag;

/**
 * @description: 拓扑图页签
 * @author sophia
 * @create: 2023/10/20 09:45
 **/
public interface TopoTagService extends IService<TopoTag> {

    void saveOrUpdateTag(TopoTag topoTag) throws Exception;

}

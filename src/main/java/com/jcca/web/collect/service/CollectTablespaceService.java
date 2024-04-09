package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectTablespace;

/**
 * @author hanwone
 * @date 2020-08-06 09:55:09
 **/
public interface CollectTablespaceService extends IService<CollectTablespace> {


    /**
     * 删除最新一条数据往前两小时的数据
     *
     * @param removeHour
     * @return
     */
    Boolean removeBeforeData(Integer removeHour);

    /**
     * 通过名字进行判定保存还是更新
     * 并且删除之前采集的旧的表单数据
     *
     * @param tablespace
     */
    void updateOrSaveByName(CollectTablespace tablespace);
}

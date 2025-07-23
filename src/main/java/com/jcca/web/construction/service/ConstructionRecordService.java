package com.jcca.web.construction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.construction.entity.ConstructionRecord;

import java.util.Date;

/**
 * 施工记录
 *
 * @author lyp
 */
public interface ConstructionRecordService extends IService<ConstructionRecord> {

    /**
     * 创建施工记录
     *
     * @param req
     */
    void create(ConstructionRecord req);

    /**
     * 删除施工记录
     *
     * @param id
     */
    void removeConstruction(String id);

    /**
     * 判定当前时间告警是否天窗
     *
     * @param assetId
     * @param occurTime
     * @return
     */
    Boolean isBlank(String assetId, Date occurTime);


    /**
     * 创建施工记录
     * 无事务方法，遵从外部事务管理
     *
     * @param req
     */
    void createV2(ConstructionRecord req);



    /**
     * 通过告警ID 查询符合条件的维护计划
     *
     * @param alarmId 告警ID
     */
    ConstructionRecord getConstructionRecord(String alarmId);
}

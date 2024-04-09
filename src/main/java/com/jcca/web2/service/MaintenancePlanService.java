package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.MaintenancePlan;
import com.jcca.web2.vo.MaintenancePlanVo;
import com.jcca.web2.vo.MaintenanceStatisticsVo;
import com.jcca.web2.vo.MaintenanceVo;

import java.util.List;

/**
 * @description: 维护计划
 * @author: sophia
 * @create: 2023/11/16 16:32
 **/
public interface MaintenancePlanService extends IService<MaintenancePlan> {

    List<MaintenancePlanVo> importProcess(List<MaintenancePlanVo> dataList);

    /**
     * 新增维护计划
     *
     * @param vo
     */
    void add(MaintenanceVo vo);

    /**
     * 维护计划统计
     *
     * @return
     */
    List<MaintenanceStatisticsVo> statistics();

    /**
     * 查询是否天窗告警
     *
     * @param orgId 资产所属组织ID
     * @return AlarmBlankConst 天窗告警标记，1正常时段告警，2天窗时段告警
     */
    Byte isAlarmBlank(String orgId);
}
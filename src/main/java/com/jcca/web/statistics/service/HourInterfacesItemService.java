package com.jcca.web.statistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.statistics.entity.HourInterfacesItem;

import java.util.Date;

/**
 * 端口明细一小时统计
 *
 * @author Lvyp
 */
public interface HourInterfacesItemService extends IService<HourInterfacesItem> {

    /**
     * 获取最后一次统计的时间
     *
     * @return
     */
    Date lastCreateTime();

}

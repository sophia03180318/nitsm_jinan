package com.jcca.web.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.statistics.dao.HourInterfacesItemMapper;
import com.jcca.web.statistics.entity.HourInterfacesItem;
import com.jcca.web.statistics.service.HourInterfacesItemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 告警一小时信息统计
 *
 * @author Lvyp
 */
@Service
public class HourInterfacesItemServiceImpl extends ServiceImpl<HourInterfacesItemMapper, HourInterfacesItem> implements HourInterfacesItemService {

    @Resource
    private HourInterfacesItemMapper houtInterMapper;

    @Override
    public Date lastCreateTime() {
        return houtInterMapper.maxCreateDate();
    }


}

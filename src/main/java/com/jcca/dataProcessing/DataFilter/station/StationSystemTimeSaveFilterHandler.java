package com.jcca.dataProcessing.DataFilter.station;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectStationSystemTimeEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.service.CollectSystemTimeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 车站运行时长信息过滤处理类
 * @className StationSystemTimeFilterHandler
 * @date 2023/10/27 10:01
 * @since 2.1.0.0
 */
@Component("stationSystemTimeSaveFilterHandler")
public class StationSystemTimeSaveFilterHandler extends IFilterHandler<CollectStationSystemTimeEntity> {

    @Resource
    private CollectSystemTimeService systemTimeService;

    @Override
    public boolean handler(CollectStationSystemTimeEntity info) {
        Date date = new Date();
        date.setTime(Long.valueOf(info.getCollectTime()));

        CollectSystemTime systime = new CollectSystemTime();
        systime.setId(MyIdUtil.getId());
        systime.setAssetId(info.getAssetId());
        systime.setCollectTime(date);
        systime.setCreateTime(date);
        systime.setTimeduration(info.getTimeduration());
        if (StrUtil.isNotEmpty(info.getRemoteTime())) {
            systime.setSystemDate(DateUtil.parse(info.getRemoteTime(), DatePattern.NORM_DATETIME_MS_PATTERN));
        }
        if (Objects.nonNull(info.getTimeSpan())) {
            systime.setTimeSpan(info.getTimeSpan());
        }
        systemTimeService.updateBatchByAssetId(Arrays.asList(systime));
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



}

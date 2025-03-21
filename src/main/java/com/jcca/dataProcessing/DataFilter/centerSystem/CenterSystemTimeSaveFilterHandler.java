package com.jcca.dataProcessing.DataFilter.centerSystem;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectSystemTimeEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.service.CollectSystemTimeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * @description: 保存中心设备的时间
 * @author: Lvyp
 * @create: 2023/11/30 16:41
 */
@Component("centerSystemTimeSaveFilterHandler")
public class CenterSystemTimeSaveFilterHandler extends IFilterHandler<CollectSystemTimeEntity> {

    @Resource
    private CollectSystemTimeService systemTimeService;

    @Override
    public boolean handler(CollectSystemTimeEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存中心设备的时间", info.getAssetIp());

        Date date = new Date();
        date.setTime(Long.valueOf(info.getCollectTime()));

        CollectSystemTime systime = new CollectSystemTime();
        systime.setId(MyIdUtil.getId());
        systime.setAssetId(info.getAssetId());
        systime.setCollectTime(date);
        systime.setCreateTime(new Date());
        systime.setTimeduration(info.getTimeduration());

        if (Objects.nonNull(info.getSystemDate())) {
            systime.setSystemDate(info.getSystemDate());
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

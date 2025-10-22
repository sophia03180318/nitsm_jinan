package com.jcca.dataProcessing.listener.alarmHandler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.service.AlarmWhitelistService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 告警变动过滤处理类
 * @className NetTimeAlarmHandler
 * @date 2023/10/20 9:51
 * @since 2.1.0.0
 */
@Component("alarmFilterHandler")
public class AlarmFilterHandler extends IFilterHandler<IEvent> {

    @Resource
    private AlarmWhitelistService alarmWhitelistService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean handler(IEvent info) {
        if(StrUtil.isEmpty(info.getAssetId())||StrUtil.isEmpty(info.getMapKey())){

            return false;
        }

        if(Objects.isNull(info.getEventAlarmLevelBaseEntity())||Objects.isNull(info.getEventAlarmLevelBaseEntity().getAlarmLevel())||
                AlarmLevelEnum.UN_CONFIG.getCode().equals(info.getEventAlarmLevelBaseEntity().getAlarmLevel())){
            //未设定告警级别的告警不上报，只存事件
            return true;
        }
        String alarmCoded = info.getEventRedisKey();
        String flag = info.getMapKey();

        int batchList = alarmWhitelistService.queryWhiteCount(flag, alarmCoded, info.getAssetId());
        return batchList == 0;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}

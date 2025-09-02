package com.jcca.dataProcessing.listener.alarmHandler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.manager.IDataChangeManagerService;
import com.jcca.dataProcessing.manager.bean.SaveAlarmResp;
import com.jcca.dataProcessing.manager.impl.EventInfoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.service.AlarmWhitelistService;
import org.springframework.data.redis.core.RedisTemplate;
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

        QueryWrapper<AlarmWhitelist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ALARM_CODE",alarmCoded);
        queryWrapper.eq("FLAG",flag);
        List<AlarmWhitelist> list = alarmWhitelistService.list(queryWrapper);
        if(Objects.isNull(list)||list.isEmpty()){
            return true;
        }

        return false;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}

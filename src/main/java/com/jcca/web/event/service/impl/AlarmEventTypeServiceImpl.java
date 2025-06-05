package com.jcca.web.event.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.init.constant.CacheConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.component.event.bean.AddEventItem;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.alarm.dao.AlarmRepositoryMapper;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.event.dao.AlarmEventTypeMapper;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventTypeStatusEnum;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.vo.ItemVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 告警事件类型
 *
 * @author lyp
 */
@Service
public class AlarmEventTypeServiceImpl extends ServiceImpl<AlarmEventTypeMapper, AlarmEventType>
        implements AlarmEventTypeService {

    @Resource
    private AlarmRepositoryMapper alarmRepoMapper;
    @Resource
    private RedisService redisServ;

    @Override
    public List<AddEventItem> getAllEventTypeByMsg(String uniqueCode, String originalMsg) {
        List<AlarmRepository> hitRepoList = alarmRepoMapper.selectOrgMsgContainStatusFlg(uniqueCode, originalMsg);

        List<String> eventTypeIdList = new ArrayList<String>();
        List<AddEventItem> eventTypeList = new ArrayList<AddEventItem>();

        for (AlarmRepository repo : hitRepoList) {
            String eventTypeId = repo.getEventTypeId();
            if (StrUtil.isEmpty(eventTypeId)) {
                continue;
            }
            if (eventTypeIdList.contains(eventTypeId)) {
                continue;
            }
            eventTypeIdList.add(eventTypeId);

            AlarmEventType type = getById(eventTypeId);
            if (EventTypeStatusEnum.DELETE.getCode().equals(type.getStatus())) {
                continue;
            }

            AddEventItem queue = new AddEventItem();
            queue.setRepo(repo);
            queue.setType(type);

            eventTypeList.add(queue);
        }

        return eventTypeList;
    }

    @Override
    public AlarmEventType queryUnkonwEvent() {
        Object object = redisServ.get(CacheConstant.BUSSESS_CACHE_UNKNOW_EVENT_TYPE_OBJ);

        if (Objects.nonNull(object)) {
            return (AlarmEventType) object;
        }

        QueryWrapper<AlarmEventType> queryWrapper = new QueryWrapper<AlarmEventType>();
        queryWrapper.eq("id", "1");
        AlarmEventType eventType = getOne(queryWrapper);
        if (Objects.isNull(eventType)) {
            eventType = new AlarmEventType();
            eventType.setCreateTime(new Date());
            eventType.setDescStr("系统预置的未知类型");
            eventType.setId("1");
            eventType.setName(EventUniqueCode.UNKONW_EVENT);
            eventType.setStatus(EventTypeStatusEnum.USED.getCode());
            save(eventType);
        }

        redisServ.set(CacheConstant.BUSSESS_CACHE_UNKNOW_EVENT_TYPE_OBJ, eventType);

        return eventType;
    }

    @Override
    public List<ItemVo> listTypeByAssetDesk(String assetDesk) {
        return alarmRepoMapper.listTypeByAssetDesk("%" + assetDesk + "%");
    }


}

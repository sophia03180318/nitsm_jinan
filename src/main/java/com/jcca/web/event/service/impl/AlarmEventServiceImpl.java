package com.jcca.web.event.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppListUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.event.bean.AddEventItem;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.alarm.controller.bean.AddAlarmRepoReq;
import com.jcca.web.alarm.dao.AlarmInfoMapper;
import com.jcca.web.alarm.dao.AlarmRepositoryMapper;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.event.dao.AlarmEventMapper;
import com.jcca.web.event.dao.AlarmEventRelMapper;
import com.jcca.web.event.dao.AlarmEventTypeMapper;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web2.dto.EventPageDto;
import com.jcca.web2.vo.EventPageVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 查询
 *
 * @author lyp
 */
@Service
public class AlarmEventServiceImpl extends ServiceImpl<AlarmEventMapper, AlarmEvent> implements AlarmEventService {

    private static final String CACHE_KEY = "ALARM_EVENT_SERVICE_CACHE:";

    @Resource
    private AlarmEventMapper alarmEventMapper;
    @Resource
    private RedisService redisServ;
    @Resource
    private AlarmEventTypeMapper typeMapper;
    @Resource
    private AlarmRepositoryMapper repositoryMapper;
    @Resource
    private AlarmEventRelMapper alarmEventRelMapper;
    @Resource
    private AlarmInfoMapper alarmInfoMapper;

    @Override
    public AlarmEvent getLastLog(AddEventItem req, String assetId, String flag, String UniqueCode) {
        AlarmEventType type = req.getType();
        AlarmRepository repo = req.getRepo();
        String cacheKey = "";
        if (Objects.isNull(repo)) {
            cacheKey = getCacheKey(assetId, type.getId(), flag, UniqueCode, null);
        } else {
            cacheKey = getCacheKey(assetId, type.getId(), flag, UniqueCode, repo.getId());
        }

        Object object = redisServ.get(cacheKey);

        if (Objects.nonNull(object)) {
            return JSONUtil.toBean(JSONUtil.parseObj(object), AlarmEvent.class);
        }

        AlarmEvent event = null;
        if (StrUtil.isEmpty(flag)) {
            event = alarmEventMapper.selectLastLogExceptFlag(assetId, type.getId(), UniqueCode);
        } else {
            event = alarmEventMapper.selectLastLog(assetId, type.getId(), flag, UniqueCode);
        }
        if (Objects.nonNull(event)) {
            redisServ.set(cacheKey, event, 86400L);
        }

        return event;
    }

    @Override
    public AlarmEvent getTypeLastLog(AddEventItem req, String assetId, String flag) {
        AlarmEvent event = null;
        if (StrUtil.isEmpty(flag)) {
            event = alarmEventMapper.selectTypeLastLogExceptFlag(assetId, req.getType().getId());
        } else {
            event = alarmEventMapper.selectTypeLastLog(assetId, req.getType().getId(), flag);
        }

        return event;
    }

    @Override
    public Boolean getEventStatus(String assetId, String typeId, String flag, AlarmEvent orgEvent) {
        AlarmEvent event = null;

        String cacheKey = getCacheKey(assetId, typeId, flag, orgEvent.getUniqueCode(), orgEvent.getRepositoryId());
        Object object = redisServ.get(cacheKey);

        if (Objects.nonNull(object)) {
            event = JSONUtil.toBean(JSONUtil.parseObj(object), AlarmEvent.class);
        } else {
            if (StrUtil.isEmpty(flag)) {
                event = alarmEventMapper.selectTypeLastLogExceptFlag(assetId, typeId);
            } else {
                event = alarmEventMapper.selectTypeLastLog(assetId, typeId, flag);
            }
        }

        if (Objects.isNull(event)) {
            return true;
        }
        if (EventLevelEnum.NORMAL.getCode().equals(event.getEventLevel())) {
            return true;
        }

        return false;
    }

    @Override
    public void removeByAssetId(String assetId) {
        QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<AlarmEvent>();
        queryWrapper.eq("ASSET_ID", assetId);
        remove(queryWrapper);

    }

    @Override
    public Boolean groupMatchFlagByCode(List<String> codeList) {
        List<String> list = alarmEventMapper.groupMatchFlagByCode(codeList);

        return list.size() == 1;
    }

    @Override
    public String getCacheKey(String assetid, String typeId, String flag, String uniqueCode, String repoId) {
        // 知识库因为配置了正常和异常匹配字段，同一类告警存在不同的知识库，所有缓存不可+知识库
        return CACHE_KEY + assetid + "_TYPE_" + typeId + "_FLAG_" + flag + "_UNCODE_" + uniqueCode;
    }

    @Override
    public void removeUnkonwEvent(AlarmRepository knowledge) throws Exception {
        QueryWrapper<AlarmEventType> typeWrapper = new QueryWrapper<AlarmEventType>();
        typeWrapper.eq("NAME", EventUniqueCode.UNKONW_EVENT);
        AlarmEventType type = typeMapper.selectOne(typeWrapper);

        if (Objects.isNull(type)) {
            throw new Exception("缺少名字为：【" + EventUniqueCode.UNKONW_EVENT + "】的事件类型");
        }

        String alarmCode = knowledge.getAlarmCode();
        String statusFlag = knowledge.getStatusFlag();

        QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<AlarmEvent>();
        queryWrapper.eq("EVENT_TYPE_ID", type.getId());
        queryWrapper.eq("UNIQUE_CODE", alarmCode);

        List<AlarmEvent> list = list(queryWrapper);

        List<AlarmEvent> collect = list.stream().filter(item -> item.getEventMsg().contains(statusFlag))
                .collect(Collectors.toList());
        if (Objects.nonNull(collect)) {
            List<String> idList = collect.stream().map(item -> item.getId()).collect(Collectors.toList());
            List<List<String>> inSplit = AppListUtils.inSplit(idList, 900);
            for (List<String> list2 : inSplit) {
                boolean removeByIds = removeByIds(list2);

                if (!removeByIds) {
                    throw new Exception("删除未知事件失败");
                }
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUnkonwEvent(AlarmRepository knowledge) throws Exception {
        QueryWrapper<AlarmEventType> typeWrapper = new QueryWrapper<AlarmEventType>();
        typeWrapper.eq("NAME", EventUniqueCode.UNKONW_EVENT);
        AlarmEventType type = typeMapper.selectOne(typeWrapper);

        if (Objects.isNull(type)) {
            throw new Exception("缺少名字为：【" + EventUniqueCode.UNKONW_EVENT + "】的事件类型");
        }

        String alarmCode = knowledge.getAlarmCode();
        String statusFlag = knowledge.getStatusFlag();

        QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<AlarmEvent>();
        queryWrapper.eq("EVENT_TYPE_ID", type.getId());
        queryWrapper.eq("UNIQUE_CODE", alarmCode);

        List<AlarmEvent> list = list(queryWrapper);

        List<AlarmEvent> collect = list.stream().filter(item -> item.getEventMsg().contains(statusFlag))
                .collect(Collectors.toList());
        if (Objects.nonNull(collect)) {
            for (AlarmEvent alarmEvent : collect) {
                alarmEvent.setEventTypeId(knowledge.getEventTypeId());
                alarmEvent.setRepositoryId(knowledge.getId());
                alarmEventMapper.updateById(alarmEvent);
            }
        }

    }

    @Override
    public List<AlarmEvent> selectAllEventByAlarmId(String id) {
        return alarmEventMapper.selectAllEventByAlarmId(id);
    }

    /**
     * 查看syslog日志
     *
     * @param assetId 资产ID
     * @param start
     * @param end
     * @return AlarmEvent
     */
    @Override
    public List<AlarmEvent> listSyslog(String assetId, Integer start, Integer end) {
        return alarmEventMapper.listSyslog(assetId, start, end);
    }

    @Override
    public AlarmEvent getLastPingLog(String assetId) {

        return alarmEventMapper.getLastPingLog(assetId);
    }

    @Override
    public IPage<EventPageVo> pageEventListV2(EventPageDto query) {
        Page page = new Page();
        page.setCurrent(query.getPage());
        page.setSize(query.getSize());
        IPage<EventPageVo> eventPageVoIPage = alarmEventMapper.pageEventListV2(page, query);
        List<EventPageVo> records = eventPageVoIPage.getRecords();
        for (EventPageVo record : records) {
            record.setWhiteRoleSize(alarmEventMapper.getWhiteSize(record.getEventId()));
        }
        eventPageVoIPage.setRecords(records);
        return eventPageVoIPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateKnowledge(AddAlarmRepoReq req) throws Exception {
        String alarmRepoId = req.getAlarmRepoId();
        String eventTypeId = req.getEventTypeId();
        if (StrUtil.isEmpty(alarmRepoId)) {

            throw new Exception("缺少知识库ID");
        }
        if (StrUtil.isEmpty(eventTypeId)) {
            throw new Exception("缺少事件类型ID");
        }

        AlarmRepository alarmRepo = repositoryMapper.selectById(alarmRepoId);
        if (Objects.isNull(alarmRepo)) {
            throw new Exception("知识库不存在");
        }

        updateOtherTab(alarmRepoId, eventTypeId, alarmRepo);

        // 更新知识库
        AlarmRepository alarmRepoUpdate = EntityBeanUtil.replaceParameter(req, alarmRepo, AlarmRepository.class);
        alarmRepoUpdate.setEventTypeId(eventTypeId);
        repositoryMapper.updateById(alarmRepoUpdate);
    }

    /**
     * 变更类型恢复告警
     *
     * @param alarmRepoId
     * @param eventTypeId
     * @param alarmRepo
     */
    private void updateOtherTab(String alarmRepoId, String eventTypeId, AlarmRepository alarmRepo) {
        String oldLinkEventTypeId = alarmRepo.getEventTypeId();
        if (!eventTypeId.equals(oldLinkEventTypeId)) {
            // 更新历史事件
            alarmEventMapper.batchUpdateTypeById(eventTypeId, alarmRepoId);

            // 查找此类型事件生成的关联告警
            QueryWrapper<AlarmEvent> deleteWrapper = new QueryWrapper<AlarmEvent>();
            deleteWrapper.eq("REPOSITORY_ID", alarmRepoId);

            List<AlarmEvent> eventList = alarmEventMapper.selectList(deleteWrapper);
            if (eventList.isEmpty()) {
                return;
            }
            List<String> eventIdList = eventList.stream().map(item -> item.getId()).collect(Collectors.toList());

            List<List<String>> inSplit = AppListUtils.inSplit(eventIdList, 999);
            boolean onces = true;
            Consumer<QueryWrapper<AlarmEventRel>> consumer = null;
            for (List<String> list : inSplit) {
                if (onces) {
                    consumer = wrapper -> wrapper.in("EVENT_ID", list);
                    onces = false;
                } else {
                    Consumer<? super QueryWrapper<AlarmEventRel>> after = wrapper -> wrapper.or().in("EVENT_ID", list);
                    consumer = consumer.andThen(after);
                }
            }

            // 查找关联关系
            QueryWrapper<AlarmEventRel> deleteRelWrapper = new QueryWrapper<AlarmEventRel>();
            deleteRelWrapper.and(consumer);
            List<AlarmEventRel> relList = alarmEventRelMapper.selectList(deleteRelWrapper);
            List<String> alarmIdList = relList.stream().map(item -> item.getAlarmId()).collect(Collectors.toList());
            List<List<String>> alarmInSplit = AppListUtils.inSplit(alarmIdList, 999);

            // 删除历史未确认或未恢复的告警
            for (List<String> alarmIdListItem : alarmInSplit) {
                alarmInfoMapper.batchRecoverAlarm(alarmIdListItem);
            }

        }
    }

}

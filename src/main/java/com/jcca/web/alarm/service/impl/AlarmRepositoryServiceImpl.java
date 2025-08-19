package com.jcca.web.alarm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.exception.common.VerifyException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.enums.EventEnum;

import com.jcca.dataProcessing.manager.threshold.Event;
import com.jcca.web.alarm.dao.AlarmRepositoryMapper;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.event.dao.AlarmEventMapper;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web2.dao.AlarmWhitelistMapper;
import com.jcca.web2.dto.EventRpoPageDto;
import com.jcca.web2.entity.AlarmWhitelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 告警知识库
 *
 * @author Lvyp
 */
@Service
public class AlarmRepositoryServiceImpl extends ServiceImpl<AlarmRepositoryMapper, AlarmRepository>
        implements AlarmRepositoryService {

    public static final String SUPER_REPO = "JCCA_SUPER";

    @Resource
    private AlarmRepositoryMapper alarmRepoMapper;
    @Resource
    private AlarmEventMapper eventMapper;
    @Resource
    private AlarmInfoService alarmInfoServ;
    @Resource
    private AlarmWhitelistMapper whiteMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<AlarmRepository> create(AlarmRepository copy) {
        EventLevelEnum flagType = EventLevelEnum.getEnumByCode(copy.getFlagType());
        if (Objects.isNull(flagType)) {
            return ResultVoUtil.paramError("请输入正确的FlagType（事件状态标识）", AlarmRepository.class);
        }

        if (EventLevelEnum.ABNORMAL == flagType && StrUtil.isEmpty(copy.getPlanStr())) {
            return ResultVoUtil.paramError("请填写此异常的处理方案", AlarmRepository.class);
        }

        String syncLock = "ALARM_REPO_KEY_2021_12_01";
        synchronized (syncLock.intern()) {
            QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
            queryWrapper.eq("ALARM_CODE", copy.getAlarmCode());
            queryWrapper.eq("STATUS_FLAG", copy.getStatusFlag());
            List<AlarmRepository> selectList = alarmRepoMapper.selectList(queryWrapper);
            if (!selectList.isEmpty()) {
                return  ResultVoUtil.success("保存成功", selectList.get(0));
            }
        }

        alarmRepoMapper.insert(copy);

        ResultVo<AlarmRepository> success = ResultVoUtil.success("保存成功", copy);

        return success;
    }

    @Override
    public List<AlarmRepository> getAllByEventId(String eventTypeId) {
        QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
        queryWrapper.eq("EVENT_TYPE_ID", eventTypeId);
        List<AlarmRepository> list = list(queryWrapper);
        return list;
    }

    @Override
    public List<AlarmRepository> getAllByAlarmCode(String alarmCode) {
        QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
        queryWrapper.eq("ALARM_CODE", alarmCode);
        List<AlarmRepository> list = list(queryWrapper);
        return list;
    }

    @Override
    public boolean canRecoverV2(String alarmCode) {
        QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
        queryWrapper.eq("ALARM_CODE", alarmCode);
        queryWrapper.eq("FLAG_TYPE", EventLevelEnum.NORMAL.getCode());
        AlarmRepository repo = getOne(queryWrapper);
        if (Objects.isNull(repo)) {
            return false;
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveV2(AlarmRepository copy) {
        //查询出所有此码的 命中关键字的更新
        QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<AlarmEvent>();
        queryWrapper.eq("UNIQUE_CODE",copy.getAlarmCode());
        queryWrapper.eq("EVENT_LEVEL",EventLevelEnum.UN_CONFIG.getCode());
        List<AlarmEvent> eventList = eventMapper.selectList(queryWrapper);
        for (AlarmEvent alarmEvent : eventList) {
            String eventMsg = alarmEvent.getEventMsg();
            if(eventMsg.contains(copy.getStatusFlag())){
                alarmEvent.setEventLevel(copy.getAlarmLevel());
                eventMapper.updateById(alarmEvent);
            }
        }

        String id = MyIdUtil.getId();
        copy.setId(id);
        save(copy);
    }

    @Override
    public IPage<AlarmRepository> pageListV2(EventRpoPageDto query) {
        Page page = new Page();
        page.setCurrent(query.getPage());
        page.setSize(query.getSize());
        return alarmRepoMapper.pageListV2(page, query);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByIdV2(String repoId) throws VerifyException {
        AlarmRepository repo = alarmRepoMapper.selectById(repoId);
        if (Objects.isNull(repo)) {
            return;
        }
        if (SUPER_REPO.equals(repo.getCreator())) {
            throw new VerifyException("系统初始化规则禁止操作");
        }
        QueryWrapper<AlarmWhitelist> query = new QueryWrapper<>();
        query.eq("ALARM_CODE", repo.getAlarmCode());

        alarmRepoMapper.deleteById(repoId);
        alarmInfoServ.recoverAlarmV2(repo.getAlarmCode(), null, null, "【告警规则移除，告警恢复】");
        whiteMapper.delete(query);

        Event event = new Event();
        event.setCode(EventEnum.deleteRedisKey.getCode());
        event.setRedisKey(repo.getAlarmCode());
    }

    @Override
    public void updateByIdV2(AlarmRepository copy) throws VerifyException {
        AlarmRepository repo = alarmRepoMapper.selectById(copy.getId());
        if (Objects.isNull(repo)) {
            return;
        }
        if (SUPER_REPO.equals(repo.getCreator())) {
            throw new VerifyException("系统初始化规则不可删除");
        }

        alarmRepoMapper.updateById(copy);

    }

}

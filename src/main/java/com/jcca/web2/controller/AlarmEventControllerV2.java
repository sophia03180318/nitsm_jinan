package com.jcca.web2.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.common.VerifyException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.manager.alarmRepo.AlarmRepoEvent;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.enums.EventTypeStatusEnum;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.dto.AddAlarmRepoDto;
import com.jcca.web2.dto.EventPageDto;
import com.jcca.web2.dto.EventRpoPageDto;
import com.jcca.web2.dto.UpdateAlarmRepoDto;
import com.jcca.web2.vo.EventPageVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 事件V2接口
 *
 * @description: 事件管理V2
 * @author: Lvyp
 * @create: 2023/11/23 13:32
 */
@RestController
@RequestMapping("/api/v2/event")
@Api(tags = "事件管理V2")
public class AlarmEventControllerV2 extends ListenerManager {


    @Resource
    private AlarmEventService eventServ;
    @Resource
    private AlarmEventTypeService eventTypeServ;
    @Resource
    private AlarmRepositoryService repositoryServ;

    @GetMapping("/queryNeedConfigRepo")
    @ApiOperation("查询需要配置级别的知识库")
    public ResultVo queryNeedConfigRepo(){
        QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("ALARM_LEVEL");
        List<AlarmRepository> list = repositoryServ.list(queryWrapper);
        return ResultVoUtil.success(list);
    }


    @GetMapping("/pageEventList")
    @ApiOperation("分页查询事件列表")
    public ResultVo pageEventList(EventPageDto query) {
        if(Objects.nonNull(query.getEventType()) && 4==query.getEventType()){
            query.setEventLevel(EventLevelEnum.UNKNOW.getCode());
            query.setEventType(null);
        }
        IPage<EventPageVo> pageResult = eventServ.pageEventListV2(query);
        return ResultVoUtil.success(pageResult);
    }

    @GetMapping("/typeQuery")
    @ApiOperation("事件分类查询")
    public ResultVo typeQuery(String name) {
        QueryWrapper<AlarmEventType> query = new QueryWrapper<AlarmEventType>();
        query.eq("STATUS", EventTypeStatusEnum.USED.getCode());
        if (StrUtil.isNotEmpty(name)) {
            query.like("NAME", name);
        }
        List<AlarmEventType> list = eventTypeServ.list(query);
        return ResultVoUtil.success(list);
    }

    @PostMapping("/addRepo")
    @ApiOperation("规则配置")
    public ResultVo addRepo(@Validated @RequestBody AddAlarmRepoDto repo) {
        AlarmRepository copy = EntityBeanUtil.copy(repo, AlarmRepository.class);
        repositoryServ.saveV2(copy);
        this.dispatureEvent(new AlarmRepoEvent());
        return ResultVoUtil.success();
    }

    @PostMapping("/updateRepo")
    @ApiOperation("规则编辑")
    public ResultVo updateRepo(@Validated @RequestBody UpdateAlarmRepoDto repo) {
        AlarmRepository copy = EntityBeanUtil.copy(repo, AlarmRepository.class);
        try {
            repositoryServ.updateByIdV2(copy);
            this.dispatureEvent(new AlarmRepoEvent());
        } catch (VerifyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ALARM_RULE_MANAGE, repo, e);
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), e.getMessage());
        }
        return ResultVoUtil.success();
    }

    @PostMapping("/removeRepo")
    @ApiOperation("规则删除")
    public ResultVo removeRepo(@RequestBody UpdateAlarmRepoDto repo) {
        String repoId = repo.getId();
        if (StrUtil.isEmpty(repoId)) {
            ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        try {
            repositoryServ.removeByIdV2(repoId);
            this.dispatureEvent(new AlarmRepoEvent());
        } catch (VerifyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ALARM_RULE_MANAGE, repoId, e);
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), e.getMessage());
        }
        return ResultVoUtil.success();
    }

    @GetMapping("/pageRepoList")
    @ApiOperation("分页查询规则列表")
    public ResultVo pageRepoList(EventRpoPageDto query) {
        IPage<AlarmRepository> pageResult = repositoryServ.pageListV2(query);
        return ResultVoUtil.success(pageResult);
    }

}

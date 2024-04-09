package com.jcca.web.event.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.AlarmTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.ValidatorUtils;
import com.jcca.dataProcessing.manager.threshold.Event;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.event.controller.bean.*;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventGroupLogicEnum;
import com.jcca.web.event.enums.EventRecoverFlagEnum;
import com.jcca.web.event.service.AlarmEventGroupService;
import com.jcca.web.event.service.AlarmEventTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 告警组
 *
 * @author lyp
 */
@Slf4j
@Api(tags = "事件告警规则组接口")
@RestController
@RequestMapping("/api/event/group")
public class AlarmEventGroupController extends ListenerManager {

    @Resource
    private AlarmEventGroupService groupServ;
    @Resource
    private AlarmEventTypeService typeServ;
    @Resource
    private AlarmInfoService alarmInfoServ;

    @PostMapping("/save")
    @ApiOperation(value = "保存告警组")
    @RequiresPermissions({"api:event:group:save"})
    @ActionLog(name = "新增告警规则组", title = "告警规则管理", key = LogTypeConstant.ADD)
    ResultVo<?> save(@RequestBody @Validated EventGroupAddReq req) {
        if (Objects.nonNull(req.getUseStage()) && req.getUseStage() > 0) {
            List<StageConfigBean> stageConfigList = req.getStageConfigList();
            for (StageConfigBean stageConfigBean : stageConfigList) {
                String validateReq = ValidatorUtils.validateReq(stageConfigBean);
                if (StrUtil.isNotEmpty(validateReq)) {
                    return ResultVoUtil.error(validateReq);
                }
            }
        }

        String msgTemp = req.getMsgTemp();
        if (msgTemp.getBytes(StandardCharsets.UTF_8).length > 255) {
            return ResultVoUtil.error("超过设定最大字符长度");
        }

        String replace = req.getEventTypeIds().replace(",", AlarmEventGroup.SPLIT_FLAG);
        req.setEventTypeIds(replace);

        AlarmEventGroup copy = EntityBeanUtil.copy(req, AlarmEventGroup.class);
        copy.setId(MyIdUtil.getId());
        copy.setCreateDate(new Date());
        if (Objects.nonNull(req.getStageConfigList())) {
            copy.setStageConfig(JSONUtil.parseArray(req.getStageConfigList()).toString());
        }

        groupServ.save(copy);

        this.dispatureEvent(new Event());

        return ResultVoUtil.success("保存成功");
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新事件类型")
    @RequiresPermissions({"api:event:group:update"})
    @ActionLog(name = "修改事件类型", title = "事件管理", key = LogTypeConstant.MODIFY)
    ResultVo<?> update(@RequestBody EventGroupUpdateReq req) {
        String msgTemp = req.getMsgTemp();
        if (msgTemp.getBytes(StandardCharsets.UTF_8).length > 255) {
            return ResultVoUtil.error("超过设定最大字符长度");
        }

        String id = req.getId();
        AlarmEventGroup group = groupServ.getById(id);

        if (Objects.isNull(group)) {
            return ResultVoUtil.error("待更新记录不存在");
        }

        if (Objects.nonNull(req.getUseStage()) && req.getUseStage() > 0) {
            List<StageConfigBean> stageConfigList = req.getStageConfigList();
            for (StageConfigBean stageConfigBean : stageConfigList) {
                String validateReq = ValidatorUtils.validateReq(stageConfigBean);
                if (StrUtil.isNotEmpty(validateReq)) {
                    return ResultVoUtil.error(validateReq);
                }
            }
        } else {
            //禁用了级别告警
            if (req.getMsgTemp().contains("内存")) {
                alarmInfoServ.recoverAlarm("【内存阈值】", null);
            } else if (req.getMsgTemp().contains("CPU")) {
                alarmInfoServ.recoverAlarm("【CPU阈值】", null);
            } else if (req.getMsgTemp().contains("磁盘")) {
                alarmInfoServ.recoverAlarm("【磁盘", null);
            } else if (req.getMsgTemp().contains("表空间")) {
                alarmInfoServ.recoverAlarm("【表空间", null);
            } else if (req.getMsgTemp().contains("温度")) {
                alarmInfoServ.recoverAlarm("【温度", null);
            }

        }

        String replace = req.getEventTypeIds().replace(",", AlarmEventGroup.SPLIT_FLAG);
        req.setEventTypeIds(replace);

        AlarmEventGroup replaceParameter = EntityBeanUtil.replaceParameter(req, group, AlarmEventGroup.class);

        if (Objects.nonNull(req.getStageConfigList())) {
            replaceParameter.setStageConfig(JSONUtil.parseArray(req.getStageConfigList()).toString());
        }

        groupServ.updateById(replaceParameter);

        this.dispatureEvent(new Event());
        return ResultVoUtil.success("更新成功");
    }

    @PostMapping("/remove/{id}")
    @ApiOperation(value = "删除告警规则组")
    @RequiresPermissions({"api:event:group:remove"})
    @ActionLog(name = "删除告警规则组", title = "告警规则管理", key = LogTypeConstant.REMOVEE)
    ResultVo<?> remove(@PathVariable("id") String id) {
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error("删除失败请输入ID");
        }
        try {
            groupServ.removeGroup(id);
            this.dispatureEvent(new Event());

            return ResultVoUtil.success("删除成功");
        } catch (Exception e) {
            log.error("删除告警规则组失败：{}", e.getMessage(), e);
            return ResultVoUtil.error("删除失败服务异常");
        }

    }

    @PostMapping("/page/query")
    @ApiOperation(value = "查询告警规则组")
    @RequiresPermissions({"api:event:group:page:query"})
    @ActionLog(name = "查询告警规则组", title = "告警规则管理", key = LogTypeConstant.QUERY)
    ResultVo<?> pageQuery(@RequestBody EventGroupQueryReq req) {
        IPage<AlarmEventGroup> page = PagePlugin.startPageT(Objects.isNull(req.getPage()) ? 1 : req.getPage(),
                Objects.isNull(req.getSize()) ? 10 : req.getSize(), AlarmEventGroup.class);

        QueryWrapper<AlarmEventGroup> queryWrapper = new QueryWrapper<AlarmEventGroup>();
        if (StrUtil.isNotEmpty(req.getName())) {
            queryWrapper.like("NAME", req.getName());
        }
        if (Objects.nonNull(req.getAlarmType())) {
            queryWrapper.eq("ALARM_TYPE", req.getAlarmType());
        }
        if (Objects.nonNull(req.getLevle())) {
            queryWrapper.eq("LEVLE", req.getLevle());
        }
        if (Objects.nonNull(req.getRecoverFlag())) {
            queryWrapper.eq("RECOVER_FLAG", req.getRecoverFlag());
        }

        queryWrapper.orderByDesc("CREATE_DATE");

        IPage<AlarmEventGroup> pageResult = groupServ.page(page, queryWrapper);
        List<AlarmEventGroup> records = pageResult.getRecords();
        List<EventGroupQueryResp> respList = EntityBeanUtil.copyList(records, EventGroupQueryResp.class);
        for (EventGroupQueryResp eventGroupQueryResp : respList) {
            String[] split = eventGroupQueryResp.getEventTypeIds().split(AlarmEventGroup.SPLIT_FLAG);
            List<String> asList = Arrays.asList(split);

            StringBuilder eventTypeNameStr = new StringBuilder();
            for (String typeId : asList) {
                AlarmEventType type = typeServ.getById(typeId);
                if (Objects.isNull(type)) {
                    continue;
                }
                eventTypeNameStr.append("【").append(type.getName()).append("】");
            }

            if (Objects.nonNull(eventGroupQueryResp.getUseStage()) && eventGroupQueryResp.getUseStage() > 0 && JSONUtil.isJsonArray(eventGroupQueryResp.getStageConfig())) {
                JSONArray alarmGroupList = JSONUtil.parseArray(eventGroupQueryResp.getStageConfig());
                List<StageConfigBean> stageConfigList = JSONUtil.toList(alarmGroupList, StageConfigBean.class);
                eventGroupQueryResp.setStageConfigList(stageConfigList);
            }

            eventGroupQueryResp.setEventTypeNameStr(eventTypeNameStr.toString());
            eventGroupQueryResp.setEventTypeIdList(asList);
            eventGroupQueryResp.setAlarmTypeStr(AlarmTypeEnum.getMsg(eventGroupQueryResp.getAlarmType()));
            eventGroupQueryResp
                    .setLogicalFlagStr(EventGroupLogicEnum.getMsgByCode(eventGroupQueryResp.getLogicalFlag()));
            eventGroupQueryResp
                    .setRecoverFlagStr(EventRecoverFlagEnum.getMsgByCode(eventGroupQueryResp.getRecoverFlag()));
        }

        PageBean<EventGroupQueryResp> resp = new PageBean<EventGroupQueryResp>();
        resp.setContent(respList);
        resp.setTotal(pageResult.getTotal());

        return ResultVoUtil.success(resp);
    }

}

package com.jcca.web.event.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.manager.alarmRepo.AlarmRepoEvent;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.alarm.controller.bean.AddAlarmRepoReq;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.event.controller.bean.*;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventTypeStatusEnum;
import com.jcca.web.event.service.AlarmEventGroupService;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.event.service.AlarmEventTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
@Api(tags = "告警事件类型接口")
@RestController
@RequestMapping("/api/event/type")
public class AlarmEventTypeController extends ListenerManager {

    @Resource
    private AlarmEventTypeService eventTypeServ;
    @Resource
    private AlarmEventGroupService groupServ;
    @Resource
    private AlarmRepositoryService alarmRepositoryServ;
    @Resource
    private AlarmEventService eventServ;

    @PostMapping("/save")
    @ApiOperation(value = "保存事件类型")
    @RequiresPermissions({"api:event:type:save"})
    @ActionLog(name = "新增事件类型", title = "事件管理", key = LogTypeConstant.ADD)
    ResultVo<?> save(@RequestBody @Validated EventTypeAddReq req) {
        AlarmEventType copy = EntityBeanUtil.copy(req, AlarmEventType.class);
        copy.setCreateTime(new Date());
        copy.setId(MyIdUtil.getId());
        copy.setStatus(EventTypeStatusEnum.USED.getCode());

        boolean save = eventTypeServ.save(copy);

        if (save) {
            return ResultVoUtil.success("保存成功");
        }

        return ResultVoUtil.error("保存失败");
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新事件类型")
    @RequiresPermissions({"api:event:type:update"})
    @ActionLog(name = "修改事件类型", title = "事件管理", key = LogTypeConstant.MODIFY)
    ResultVo<?> update(@RequestBody @Validated EventTypeUpdateReq req) {
        if (!StringUtils.isEmpty(req.getAssetDesks()) && StringUtils.isEmpty(req.getTypeAlias())) {
            return ResultVoUtil.warning("请输入事件类型别名");
        }
        if (StringUtils.isEmpty(req.getAssetDesks()) && !StringUtils.isEmpty(req.getTypeAlias())) {
            return ResultVoUtil.warning("请选择设备类型");
        }

        if (!StringUtils.isEmpty(req.getAssetDesks())) {
            req.setAssetDesks(req.getAssetDesks() + ",");
        }
        AlarmEventType type = eventTypeServ.getById(req.getId());
        if (Objects.isNull(type)) {
            return ResultVoUtil.warning("事件类型不存在");
        }
        AlarmEventType replaceParameter = EntityBeanUtil.replaceParameter(req, type, AlarmEventType.class);

        boolean updateFlag = eventTypeServ.updateById(replaceParameter);
        if (updateFlag) {
            return ResultVoUtil.success("更新成功");
        } else {
            return ResultVoUtil.error("更新失败");
        }
    }

    @PostMapping("/remove/{id}")
    @ApiOperation(value = "删除事件类型")
    @RequiresPermissions({"api:event:type:remove"})
    @ActionLog(name = "删除事件类型", title = "事件管理", key = LogTypeConstant.REMOVEE)
    ResultVo<?> remove(@RequestBody EventTypeRemoveReq req, @PathVariable("id") String id) {
        List<String> idList = req.getIdList();
        if (Objects.isNull(idList)) {
            idList = new ArrayList<String>();
        }

        if (StrUtil.isNotEmpty(id)) {
            idList.add(id);
        }
        List<AlarmEventType> typeList = new ArrayList<AlarmEventType>();
        for (String item : idList) {
            AlarmEventType type = eventTypeServ.getById(item);
            if (Objects.isNull(type)) {
                log.error("删除事件类型错误，记录不存在，ID:{}", item);
                continue;
            }

            List<AlarmEventGroup> groupList = groupServ.getAllByTypeId(item);
            if (groupList.size() != 0) {
                return ResultVoUtil.warning("事件类型：【" + type.getName() + "】已经在告警规则管理中配置，请先删除告警规则管理中相关配置");
            }

            List<AlarmRepository> alarmRepoList = alarmRepositoryServ.getAllByEventId(item);
            if (!alarmRepoList.isEmpty()) {
                return ResultVoUtil.warning("事件类型：【" + type.getName() + "】存在关联的知识库，请先移除事件类型下的知识库");
            }

            type.setStatus(EventTypeStatusEnum.DELETE.getCode());
            typeList.add(type);
        }

        boolean updateFlag = eventTypeServ.updateBatchById(typeList);

        if (updateFlag) {
            return ResultVoUtil.success("删除成功");
        } else {
            return ResultVoUtil.warning("删除失败");
        }
    }

    @PostMapping("/pageQuery")
    @ApiOperation(value = "查询事件类型")
    @RequiresPermissions({"api:event:type:pageQuery"})
    @ActionLog(name = "查询事件类型", title = "事件管理", key = LogTypeConstant.QUERY)
    ResultVo<?> pageQuery(@RequestBody EventTypePageQueryReq req) {
        IPage<AlarmEventType> page = PagePlugin.startPageT(Objects.isNull(req.getPage()) ? 1 : req.getPage(),
                Objects.isNull(req.getSize()) ? 10 : req.getSize(), AlarmEventType.class);

        QueryWrapper<AlarmEventType> queryWrapper = new QueryWrapper<AlarmEventType>();
        if (StrUtil.isNotEmpty(req.getName())) {
            queryWrapper.like("NAME", req.getName());
        }
        if (StrUtil.isNotEmpty(req.getUniqueCode())) {
            queryWrapper.like("UNIQUE_CODE", req.getUniqueCode());
        }

        queryWrapper.orderByDesc("CREATE_TIME");

        queryWrapper.eq("STATUS", EventTypeStatusEnum.USED.getCode());
        IPage<AlarmEventType> pageResult = eventTypeServ.page(page, queryWrapper);

        List<AlarmEventType> records = pageResult.getRecords();
        List<EventTypeQueryResp> copyList = EntityBeanUtil.copyList(records, EventTypeQueryResp.class);

        PageBean<EventTypeQueryResp> result = new PageBean<EventTypeQueryResp>();
        result.setContent(copyList);
        result.setTotal(pageResult.getTotal());

        return ResultVoUtil.success(result);
    }

    @PostMapping("/getAll")
    @ApiOperation(value = "查询全部事件类型")
    @RequiresPermissions({"api:event:type:pageQuery"})
    ResultVo<?> getAll() {
        QueryWrapper<AlarmEventType> queryWrapper = new QueryWrapper<AlarmEventType>();
        queryWrapper.eq("STATUS", 1);
        queryWrapper.orderByAsc("id");
        List<AlarmEventType> result = eventTypeServ.list(queryWrapper);

        return ResultVoUtil.success(result);
    }

    /**
     * 包含知识库列表
     *
     * @param req: 匹配码
     * @Author: syt
     * @Date: 2021/9/3/003 16:43
     */
    @PostMapping("/query/knowledge")
    @ApiOperation(value = "类型包含知识库")
    @RequiresPermissions("api:event:type:query:knowledge")
    public ResultVo<?> knowledgeBase(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String eventTypeId = reqJson.getStr("eventTypeId");

        QueryWrapper<AlarmRepository> repoQuery = new QueryWrapper<AlarmRepository>();
        if (StrUtil.isNotEmpty(eventTypeId)) {
            repoQuery.eq("EVENT_TYPE_ID", eventTypeId);
        }
        List<AlarmRepository> list = alarmRepositoryServ.list(repoQuery);

        return ResultVoUtil.success(list);
    }

    @PostMapping("/remove/knowledge")
    @ApiOperation(value = "将知识库从该类型中移除")
    @RequiresPermissions("api:event:type:remove:knowledge")
    @ActionLog(name = "将知识库从事件类型移除", title = "事件管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<?> removeKnowledge(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        JSONArray knowledgeIds = reqJson.getJSONArray("knowledgeIds");
        if (knowledgeIds.isEmpty()) {
            return ResultVoUtil.error("至少选择一个知识库");
        }

        List<String> list = JSONUtil.toList(knowledgeIds, String.class);

        List<AlarmRepository> repoList = new ArrayList<AlarmRepository>();
        for (String knowledgeId : list) {
            AlarmRepository knowledge = alarmRepositoryServ.getById(knowledgeId);
            if (Objects.isNull(knowledge)) {
                continue;
            }
            knowledge.setEventTypeId("");
            repoList.add(knowledge);
        }

        if (!repoList.isEmpty()) {
            alarmRepositoryServ.updateBatchById(repoList);
        }

        return ResultVoUtil.success();
    }

    @PostMapping("/add/knowledge")
    @ApiOperation(value = "新增告警知识库并关联类型")
    @RequiresPermissions("api:event:type:add:knowledge")
    public ResultVo<?> addKnowledge(@Validated @RequestBody AddAlarmRepoReq req) {
        AlarmRepository copy = EntityBeanUtil.copy(req, AlarmRepository.class);
        ResultVo<AlarmRepository> create = alarmRepositoryServ.create(copy);
        if (!ResultEnum.SUCCESS.getCode().equals(create.getCode())) {
            return ResultVoUtil.error(create.getMsg());
        }
        AlarmRepository data = create.getData();
        String eventTypeId = req.getEventTypeId();
        String id = data.getId();

        if (StrUtil.isEmpty(eventTypeId)) {
            return ResultVoUtil.error("请选择事件类型");
        }
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error("保存知识库出错：未返回ID");
        }

        data.setEventTypeId(eventTypeId);

        alarmRepositoryServ.updateById(data);
        // 删除此类型的未知事件
        try {
            eventServ.updateUnkonwEvent(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultVoUtil.error(e.getMessage());
        }
        //更新事件管理缓存
        this.dispatureEvent(new AlarmRepoEvent());
        return ResultVoUtil.success();

    }


    @PostMapping("/update/knowledge")
    @ApiOperation(value = "编辑告警知识库和关联新类型")
    @RequiresPermissions("api:event:type:add:knowledge")
    @ActionLog(name = "修改知识库关联的事件类型", title = "事件管理", key = LogTypeConstant.MODIFY)
    public ResultVo<?> updateKnowledge(@Validated @RequestBody AddAlarmRepoReq req) {
        try {
            eventServ.updateKnowledge(req);
            this.dispatureEvent(new AlarmRepoEvent());
            return ResultVoUtil.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultVoUtil.error(ResultEnum.ERROR);
        }
    }

    @GetMapping("/query/knowledge/{eventId}")
    @ApiOperation(value = "查询知识库")
    @RequiresPermissions("api:event:type:add:knowledge")
    public ResultVo<?> updateKnowledge(@PathVariable("eventId") String eventId) {
        AlarmEvent event = eventServ.getById(eventId);
        if (Objects.isNull(event)) {
            return ResultVoUtil.warning("该事件不存在");
        }
        if (Objects.isNull(event.getRepositoryId())) {
            return ResultVoUtil.success(new AlarmRepository());
        }

        AlarmRepository alarmRepo = alarmRepositoryServ.getById(event.getRepositoryId());

        if (Objects.isNull(alarmRepo)) {
            return ResultVoUtil.warning("该知识库已被删除");
        }

        return ResultVoUtil.success(alarmRepo);
    }


    @PostMapping("/link/knowledge")
    @ApiOperation(value = "类型关联告警知识库")
    @RequiresPermissions("api:event:type:link:knowledge")
    @ActionLog(name = "事件类型关联知识库", title = "事件管理", key = LogTypeConstant.MODIFY)
    public ResultVo<?> linkKnowledge(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String knowledgeId = reqJson.getStr("knowledgeId");
        String eventTypeId = reqJson.getStr("eventTypeId");

        if (StrUtil.isEmpty(eventTypeId) || StrUtil.isEmpty(knowledgeId)) {
            return ResultVoUtil.error("时间类型ID和知识库ID不能空");
        }

        AlarmRepository knowledge = alarmRepositoryServ.getById(knowledgeId);
        knowledge.setEventTypeId(eventTypeId);

        alarmRepositoryServ.updateById(knowledge);
        // 删除此类型的未知事件
        try {
            eventServ.removeUnkonwEvent(knowledge);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultVoUtil.error(e.getMessage());
        }

        return ResultVoUtil.success();
    }

}

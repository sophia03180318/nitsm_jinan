package com.jcca.web.event.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppListUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.controller.bean.EventInfoPageQueryReq;
import com.jcca.web.event.controller.bean.EventInfoPageQueryResp;
import com.jcca.web.event.controller.bean.EventStatisticsResp;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.enums.EventTypeStatusEnum;
import com.jcca.web.event.service.AlarmEventRelService;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.event.service.AlarmEventTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 告警事件
 *
 * @author lyp
 */
@Slf4j
@Api(tags = "事件接口")
@RestController
@RequestMapping("/api/event/info")
public class AlarmEventController {

    @Resource
    private AlarmEventService eventServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmEventTypeService eventTypeServ;
    @Resource
    private AlarmEventRelService eventRelServ;
    @Resource
    private AlarmRepositoryService alarmRepoServ;

    @PostMapping("/pageQuery")
    @ApiOperation(value = "查询事件")
    @RequiresPermissions({"api:event:info:pageQuery"})
    @ActionLog(name = "查询事件列表", title = "事件管理", key = LogTypeConstant.QUERY)
    ResultVo<?> pageQuery(@RequestBody EventInfoPageQueryReq req) {
        IPage<AlarmEvent> page = PagePlugin.startPageT(Objects.isNull(req.getPage()) ? 1 : req.getPage(),
                Objects.isNull(req.getSize()) ? 10 : req.getSize(), AlarmEvent.class);

        QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<AlarmEvent>();

        List<String> assetIds = ShiroUtil.getSubjectAssetIds();
        assetIds.add("x");

        if (StrUtil.isNotEmpty(req.getAssetName())) {
            QueryWrapper<Asset> assetQueryWrapper = new QueryWrapper<Asset>();
            assetQueryWrapper.like("name", req.getAssetName());
            List<Asset> list = assetServ.list(assetQueryWrapper);
            if (list.isEmpty()) {
                queryWrapper.eq("ASSET_ID", "x");
            } else {
                // 判定是否有交集，没有的话结果空
                List<Asset> collect = list.stream().filter(asset -> assetIds.contains(asset.getId()))
                        .collect(Collectors.toList());
                List<String> assetIdLists = collect.stream().map(item -> item.getId()).collect(Collectors.toList());
                assetIdLists.add("x");
                queryWrapper.in("ASSET_ID", assetIdLists);
            }
        } else {
            List<List<String>> inSplit = AppListUtils.inSplit(assetIds, 900);
            Consumer<QueryWrapper<AlarmEvent>> consumer = null;
            boolean onces = true;
            for (List<String> list : inSplit) {
                if (onces) {
                    consumer = wrapper -> wrapper.in("ASSET_ID", list);
                    onces = false;
                } else {
                    Consumer<? super QueryWrapper<AlarmEvent>> after = wrapper -> wrapper.or().in("ASSET_ID", list);
                    consumer = consumer.andThen(after);
                }
            }

            if (Objects.nonNull(consumer)) {
                queryWrapper.and(consumer);
            }
        }

        if (StrUtil.isNotEmpty(req.getEventTypeId())) {
            queryWrapper.eq("EVENT_TYPE_ID", req.getEventTypeId());
        }
        if (StrUtil.isNotEmpty(req.getEventLevel())) {
            queryWrapper.eq("EVENT_LEVEL", req.getEventLevel());
        }
        if (StrUtil.isNotEmpty(req.getUniqueCode())) {
            queryWrapper.eq("UNIQUE_CODE", req.getUniqueCode());
        }
        if (StrUtil.isNotEmpty(req.getAssetIp())) {
            Asset asset = assetServ.findOneByIp(req.getAssetIp());
            if (Objects.nonNull(asset)) {
                queryWrapper.eq("ASSET_ID", asset.getId());
            } else {
                queryWrapper.eq("ASSET_ID", "-1");
            }
        }

        if (Objects.nonNull(req.getBeginTime())) {
            DateTime beginOfDay = DateUtil.beginOfDay(req.getBeginTime());
            queryWrapper.gt("CREATE_TIME", beginOfDay.toJdkDate());
        }
        if (Objects.nonNull(req.getEndTime())) {
            DateTime endOfDay = DateUtil.endOfDay(req.getEndTime());
            queryWrapper.lt("CREATE_TIME", endOfDay.toJdkDate());
        }

        if (Objects.nonNull(req.getTimeScope())) {
            //
            Calendar today = Calendar.getInstance();
            if (req.getTimeScope().intValue() != 0) {
                today.add(Calendar.DATE, -req.getTimeScope());
            }
            Date time = today.getTime();
            DateTime beginOfDay = DateUtil.beginOfDay(time);
            DateTime endOfDay = DateUtil.endOfDay(new Date());
            queryWrapper.gt("CREATE_TIME", beginOfDay.toJdkDate());
            queryWrapper.lt("CREATE_TIME", endOfDay.toJdkDate());
        }

        if (StrUtil.isNotEmpty(req.getOrgMsg())) {
            queryWrapper.like("EVENT_MSG", req.getOrgMsg());
        }

        queryWrapper.orderByDesc("cast(ID as integer)");

        IPage<AlarmEvent> pageResult = eventServ.page(page, queryWrapper);
        List<AlarmEvent> records = pageResult.getRecords();

        List<EventInfoPageQueryResp> copyList = EntityBeanUtil.copyList(records, EventInfoPageQueryResp.class);
        for (EventInfoPageQueryResp resp : copyList) {
            fattenResp(resp);
        }

        PageBean<EventInfoPageQueryResp> pageResp = new PageBean<EventInfoPageQueryResp>();

        pageResp.setContent(copyList);
        pageResp.setTotal(pageResult.getTotal());

        return ResultVoUtil.success(pageResp);
    }

    @PostMapping("/queryByAlarmId/{id}")
    @ApiOperation(value = "查询告警相关的事件")
    @RequiresPermissions({"api:event:info:queryByAlarmId"})
    @ActionLog(name = "查询告警的相关事件", title = "告警管理", key = LogTypeConstant.QUERY)
    ResultVo<?> queryByAlarmId(@PathVariable("id") String id) {
        List<AlarmEventRel> listByAlarmId = eventRelServ.listByAlarmId(id);

        List<EventInfoPageQueryResp> respList = new LinkedList<EventInfoPageQueryResp>();

        for (AlarmEventRel alarmEventRel : listByAlarmId) {
            AlarmEvent event = eventServ.getById(alarmEventRel.getEventId());
            if (Objects.isNull(event)) {
                log.info("【事件管理-删除关联表中多余事件ID】：{}", alarmEventRel.getEventId());
                eventRelServ.removeById(alarmEventRel);
                continue;
            }
            EventInfoPageQueryResp copy = EntityBeanUtil.copy(event, EventInfoPageQueryResp.class);
            fattenResp(copy);
            respList.add(copy);
        }

        return ResultVoUtil.success(respList);
    }

    /**
     * 丰富响应查询
     *
     * @param resp
     */
    private void fattenResp(EventInfoPageQueryResp resp) {
        if (Objects.isNull(resp)) {
            return;
        }
        Asset asset = assetServ.getById(resp.getAssetId());

        if (Objects.isNull(asset)) {
            return;
        }

        AlarmEventType type = eventTypeServ.getById(resp.getEventTypeId());

        resp.setShowSyslogButton(false);
        resp.setAssetImage(asset.getAssetImage());
        resp.setAssetIp(asset.getIp());
        resp.setAssetName(asset.getName());
        resp.setAssetMode(asset.getAssetMode());
        if(Objects.isNull(type)){
            resp.setEventTypeName("原类型已删除");
        }else{
            resp.setEventTypeName(type.getName());
        }
        resp.setEventLevelStr(EventLevelEnum.getMsgByCode(resp.getEventLevel()));
        if (Objects.nonNull(type) && Objects.isNull(resp.getRepositoryId())) {
            resp.setShowSyslogButton(true);
        }
    }

    /**
     * 类型统计
     *
     * @param
     * @return
     */
    @PostMapping("/statistics")
    @ApiOperation(value = "事件类型统计")
    @RequiresPermissions("api:event:info:statistics")
    public ResultVo<?> statistics(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String eventTypeId = reqJson.getStr("eventTypeId");

        List<EventStatisticsResp> respList = new ArrayList<EventStatisticsResp>();

        List<AlarmEvent> list = eventServ.list();

        if (StrUtil.isNotEmpty(eventTypeId)) {
            AlarmEventType type = eventTypeServ.getById(eventTypeId);
            if (!EventTypeStatusEnum.USED.getCode().equals(type.getStatus())) {
                return ResultVoUtil.success(respList);
            }
            List<AlarmEvent> collect = list.stream().filter(item -> item.getEventTypeId().equals(eventTypeId))
                    .collect(Collectors.toList());

            EventStatisticsResp item = new EventStatisticsResp();
            item.setCount(list.size());
            item.setTypeCount(collect.size());
            item.setTypeName(type.getName());
            respList.add(item);
            return ResultVoUtil.success(respList);
        }

        List<AlarmEventType> typeList = eventTypeServ.list();

        BigDecimal count = new BigDecimal(0);
        for (AlarmEventType type : typeList) {
            List<AlarmEvent> collect = list.stream().filter(item -> item.getEventTypeId().equals(eventTypeId))
                    .collect(Collectors.toList());

            EventStatisticsResp resp = new EventStatisticsResp();
            resp.setTypeName(type.getName());
            resp.setCount(list.size());
            resp.setTypeCount(collect.size());

            count = count.add(new BigDecimal(collect.size()));

            respList.add(resp);
        }

        BigDecimal subtract = new BigDecimal(list.size()).subtract(count);
        if (subtract.intValue() != 0) {
            EventStatisticsResp resp = new EventStatisticsResp();
            resp.setTypeName("已删除的类型");
            resp.setCount(list.size());
            resp.setTypeCount(subtract.intValue());
        }

        return ResultVoUtil.success(respList);
    }

}

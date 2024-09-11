package com.jcca.web.common.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.thresholds.bean.EventLogBean;
import com.jcca.dataProcessing.Entity.CustomEvent;
import com.jcca.dataProcessing.Entity.DongHuanEntity;
import com.jcca.dataProcessing.dataAdpater.DongHuanAdapter;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.common.controller.req.CollectSyslogReq;
import com.jcca.web.common.controller.req.DsErrorLog;
import com.jcca.web.common.controller.req.EvenLog;
import com.jcca.web.common.controller.req.StationAlarmReqV1;
import com.jcca.web.common.entity.Alarm;
import com.jcca.web.common.entity.Device;
import com.jcca.web.common.entity.DhStation;
import com.jcca.web.common.service.DeviceService;
import com.jcca.web.common.service.DhAlarmService;
import com.jcca.web.common.service.DhStationService;
import com.jcca.web.common.service.PropertyService;
import com.jcca.web.event.enums.EventLevelEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 接收自定义日志信息
 *
 * @author lyp
 */
@Api(tags = "接收外事件")
@Slf4j
@RestController
@RequestMapping("/api/free/syslog")
public class ApiCollectSyslogController extends ListenerManager {

    @Value("${threeD.orgId}")
    private String orgId;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private DeviceService deviceService;
    @Resource
    private DhStationService stationService;
    @Resource
    private DataProcessManager dataProcessManager;
    @Resource
    private DhAlarmService alarmService;
    @Resource
    private PropertyService propertyService;



    @PostMapping("/stationEventMsg")
    public void stationRoutMsg(@RequestBody StationAlarmReqV1 alarmReq) {
        if (LogInputUtils.inputInfo(ServerTypeEnum.STATION_ALARM)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.STATION_ALARM, alarmReq.getStationIp(), JSONUtil.toJsonStr(alarmReq)));
        }

        String stationIp = alarmReq.getStationIp();
        Asset asset = null;
        if (Objects.nonNull(alarmReq.getAssetId())) {
            asset = assetServ.getById(alarmReq.getAssetId());
        } else {
            asset = assetServ.getOneByAllIp(stationIp);
        }
        if (Objects.isNull(asset)) {
            log.error("接收自定义日志信息-推送告警的车站未录入对应资产，车站IP：{} 或设备ID不存在：{}", alarmReq.getStationIp(), alarmReq.getAssetId());
            return;
        }


        CreateEventReq eventReq = EntityBeanUtil.copy(alarmReq, CreateEventReq.class);
        eventReq.setAsset(asset);
        eventReq.setAssetId(asset.getId());
        eventReq.setCreateTime(DateUtil.parse(alarmReq.getCreateTime(), "yyyy-MM-dd HH:mm:ss.SSS"));
        try {
            eventLogicServ.addEvent(eventReq);
        } catch (Exception e) {
            log.error("接收自定义日志信息-处理车站上报的事件失败：{}", e.getMessage(), e);
        }


    }

    @PostMapping("/aix")
    public void aixSyslog(@RequestBody @Validated CollectSyslogReq req) {

        if (LogInputUtils.inputInfo(ServerTypeEnum.AIX_SYSLOG)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.AIX_SYSLOG, req.getAssetId(), JSONUtil.toJsonStr(req)));
        }
        CustomEvent customEvent = new CustomEvent();
        customEvent.setAssetId(req.getAssetId());
        customEvent.setCreateTime(DateUtil.parse(req.getCreateTime(), "yyyyMMddHHmmss"));
        customEvent.setMsg(req.getOrgMsg());
        customEvent.setUniqueCode(req.getUniqueCode());
        customEvent.setType(StatusInfoChangeTypeEnum.event_aix_log.getCode());
        this.dispatureEvent(customEvent);


    }


    /**
     * 保存DS日志文件使用  仅用于文件下载
     * */
    @PostMapping("/saveLog")
    public void saveLog(@RequestBody @Validated DsErrorLog eventLog) {
        FileWriter writer = new FileWriter("/home/nitsm/file/" + eventLog.getAssetId() + ".txt");
        try {
            if (StringUtils.isNotEmpty(eventLog.getLogStr())) {
                log.info("接收自定义日志信息接收到DS日志数据");
                writer.write(eventLog.getLogStr());
            } else {
                List<EvenLog> logs = eventLog.getLogs();
                log.info("接收自定义日志信息接收到DS" + logs.size() + "条数据~");
                if (!logs.isEmpty()) {
                    log.debug("第一条日志:{}", logs.get(0).toString());
                    writer.write(logs.get(0).getData());
                    for (int i = 1; i < logs.size(); i++) {
                        writer.append("\r\n\r\n\r\n" + logs.get(i).getData());
                    }
                }
            }

        } catch (Exception e) {
            log.error("接收自定义日志信息-DS日志文件保存失败:{}", e.getMessage(), e);
        }
    }


    /**
     * 接收所有系列存储告警所用,将告警存放至告警队列
     * */
    @PostMapping("/raid")
    public void raidSyslog(@RequestBody @Validated EventLogBean req) {
        CustomEvent customEvent = new CustomEvent();
        customEvent.setAssetId(req.getAssetId());
        customEvent.setCreateTime(DateUtil.parse(req.getLastTime(), "yyMMddHHmmss"));
        customEvent.setMsg(req.getDescription());
        customEvent.setUniqueCode(req.getEventId());
        customEvent.setType(StatusInfoChangeTypeEnum.event_raid_log.getCode());
        this.dispatureEvent(customEvent);

    }

    /**
     * 同步动环设备
     */
    @PostMapping("/pullAllDevice")
    public void pullAllDevice() {
        Map<String, String> stationMap = stationService.list().stream().collect(Collectors.toMap(DhStation::getStationId, DhStation::getRoomId, (key1, kek2) -> key1));
        //读取设备表  -> 存入Asset
        List<Asset> assets = new ArrayList<>();
        List<Device> deviceList = deviceService.list();
        for (Device device : deviceList) {
            Asset asset = new Asset();
            asset.setId(device.getDeviceId());
            asset.setAssetCode(device.getDeviceId());
            asset.setName(device.getName());
            asset.setAssetMode(30);
            asset.setManufacturerId(30);
            asset.setWatch((byte)2);
            if (device.getDeviceType() < 10) {
                asset.setAssetImage("500" + device.getDeviceType());
            } else {
                asset.setAssetImage("50" + device.getDeviceType().toString());
            }
            asset.setOnlineTime(device.getBeginRunTime());
            if (!stationMap.containsKey(device.getParentID())) {
                continue;
            }
            //济南中心ID  定好组织ID后修改为配置文件中拿取
            asset.setOrgId(orgId);
            asset.setRoomId(stationMap.get(device.getParentID()));
            assets.add(asset);
        }
        if (!assets.isEmpty()) {
            //调用存储资产接口
            assetServ.saveDevice(assets);
        }
    }
    /***
     * 接收动环告警信息
     */
    @ApiOperation(value = "接收动环告警信息")
    @PostMapping("pullDeviceAlarm")
    public void pullDeviceAlarm() {
        List<Alarm> alarmLists = alarmService.getAlarm();
        for (Alarm alarm : alarmLists) {
            if (ObjectUtil.isNull(alarm.getDeviceId())) {
                alarm.setDeviceId(propertyService.getById(alarm.getPropertyId()).getParentID());
            }
            AssetMsgVo asset = assetServ.findMsgById(alarm.getDeviceId());
            if (Objects.isNull(asset)) {
                log.error("动环告警收到未录入数据，资产ID不存在：" + JSONUtil.toJsonStr(alarm));
            }
            try {
                DongHuanEntity dongHuanEntity = new DongHuanEntity();
                dongHuanEntity.setAssetId(alarm.getDeviceId());
                dongHuanEntity.setFlag(alarm.getPropertyId());
                dongHuanEntity.setCreateTime(alarm.getCreateTime());
                dongHuanEntity.setOriginalMsg("动环告警: "+alarm.getDescc());
                dongHuanEntity.setAssetName(asset.getAssetName());
                log.info("动环推送告警: " + JSONUtil.toJsonStr(dongHuanEntity));
                DongHuanAdapter dhAdapter = (DongHuanAdapter) dataProcessManager.getAdapater("dongHuanAdapter");
                dhAdapter.dispose(dongHuanEntity);
            } catch (Exception e2) {
                log.error("接收动环推送设备告警失败: " + e2.toString());
            }
        }


    }

}

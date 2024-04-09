package com.jcca.web.asset.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.vo.CabinetAlarmVo;
import com.jcca.web.asset.vo.CabinetGeneralVo;
import com.jcca.web.asset.vo.DetailCabinetOtherVo;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web.collect.entity.*;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.*;
import com.jcca.web.graph.vo.GraphStatusVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @ClassName ApiCabinetController
 * @Description 机柜前端接口
 * @Date 2020/4/27 10:15
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/cabinet")
@Api(tags = "机柜相关接口")
@Slf4j
public class ApiCabinetController {

    @Resource
    private CabinetService cabinetService;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private RoomService roomService;
    @Resource
    private AssetService assetService;
    @Resource
    private CollectCpuService collectCpuService;
    @Resource
    private CollectMemoryService collectMemoryService;
    @Resource
    private CollectDiskService collectDiskService;
    @Resource
    private CollectSystemTimeService collectSystemTimeService;
    @Resource
    private CollectSensorService collectSensorServ;
    @Resource
    private CollectConnectService collectConnectService;
    @Resource
    private CollectDsService dsService;
    @Resource
    private CollectRaidService raidService;

    @GetMapping("/systemCollectTime")
    public ResultVo systemCollectTime() {
        JSONObject respJson = new JSONObject();
        CollectCpu collectCpu = collectCpuService.selectMaxOne();
        if (Objects.nonNull(collectCpu)) {
            Date collectTime = collectCpu.getCollectTime();
            respJson.put("lastTime", DateUtil.format(collectTime, "yyyy-MM-dd HH:mm:ss"));
            return ResultVoUtil.success(respJson);
        }

        return ResultVoUtil.success(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取机房内机柜列表
     *
     * @param roomId
     * @return
     */
    @GetMapping("/list/{roomId}")
    @ApiOperation(value = "获取机房内机柜列表")
    public ResultVo list(@PathVariable("roomId") String roomId) {
        return ResultVoUtil.success(cabinetService.listByRoomId(roomId));
    }

    /**
     * 获取机柜简要详情及机柜内告警信息
     *
     * @param id 机柜ID
     * @return
     */
    @GetMapping("/general/{id}")
    @ApiOperation(value = "获取机柜简要详情及机柜内告警信息")
    public ResultVo general(@PathVariable("id") String id) {

        Cabinet cabinet = cabinetService.getById(id);
        if (Objects.isNull(cabinet)) {
            if (LogInputUtils.inputError(ServerTypeEnum.WEB_CABINET)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_CABINET, ErrorCodeEnum.WEB_CABINET_ERROR_01, "", "机柜管理-ID为[" + id + "]的机柜不存在"));
            }
            return ResultVoUtil.error("ID为[" + id + "]的机柜不存在");
        }

        CabinetGeneralVo generalVo = new CabinetGeneralVo();
        generalVo.setCabinetId(cabinet.getId());
        generalVo.setCabinetName(cabinet.getName());
        generalVo.setRowIndex(cabinet.getRowIndex());
        generalVo.setColumnIndex(cabinet.getColumnIndex());

        List<String> dontNeedRecoverList = Arrays.asList(ReceiveAlarmTypeEnum.SNMP.getCode(),
                ReceiveAlarmTypeEnum.SYSLOG.getCode(), ReceiveAlarmTypeEnum.CASCO_MASTER_CHANGE.getCode(),
                ReceiveAlarmTypeEnum.CASCO_VERSION_CHANGE.getCode());

        List<AlarmUnconfirmVo> alarmList = cabinetService.findAlarmByCabinetId(id);
        generalVo.setAlarmList(alarmList);

        return ResultVoUtil.success(generalVo);
    }

    /**
     * 机柜详情
     *
     * @return
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "机柜详情")
    @ActionLog(name = "查看机柜详情", title = "机柜管理", key = LogTypeConstant.QUERY)
    public ResultVo detail(@PathVariable("id") String id) {

        // 机柜所在机房
        Room room = roomService.findByCabinetId(id);
        if (Objects.isNull(room)) {
            return ResultVoUtil.paramError("机柜不在机房内", String.class);
        }

        Cabinet cab = cabinetService.getById(id);
        if (Objects.isNull(cab)) {
            return ResultVoUtil.paramError("机柜不存在", String.class);
        }

        Integer rowIndex = cab.getRowIndex();
        if (Objects.isNull(rowIndex)) {
            return ResultVoUtil.paramError("请配置机柜索引", String.class);
        }

        // 组织下当前排的所有机柜
        String orgId = room.getOrgId();
        List<Cabinet> cabinetList = cabinetService.findByOrgId(rowIndex, orgId);

        // 所有有告警的机柜
        List<CabinetAlarmVo> cabinetAlarmVoList = new ArrayList<>();
        List<GraphStatusVo> statusCabinetList = cabinetService.findStatusByOrgId(orgId);
        for (Cabinet cabinet : cabinetList) {
            String cabinetId = cabinet.getId();
            CabinetAlarmVo cabinetAlarmVo = new CabinetAlarmVo();
            cabinetAlarmVo.setId(cabinetId);
            cabinetAlarmVo.setName(cabinet.getName());
            for (GraphStatusVo graphStatusVo : statusCabinetList) {
                if (cabinetId.equals(graphStatusVo.getId())) {
                    cabinetAlarmVo.setAlarmLevel(graphStatusVo.getAlarmLevel());
                }
            }
            cabinetAlarmVoList.add(cabinetAlarmVo);
        }

        // 机柜详情
        List<DetailCabinetVo> detailVoList = cabinetService.findDetailById(id);

        Map<String, Object> resultMap = new HashMap<>(16);
        resultMap.put("cabinetList", cabinetAlarmVoList);
        resultMap.put("detailList", detailVoList);
        resultMap.put("columnIndex", cab.getColumnIndex());

        return ResultVoUtil.success(resultMap);
    }

    /**
     * 机柜内设备其他数据
     *
     * @return
     */
    @GetMapping("/other/{id}")
    @ApiOperation(value = "机柜内设备其他数据")
    public ResultVo other(@PathVariable("id") String id) {
        List<DetailCabinetOtherVo> otherVoList = new ArrayList<>();

        QueryWrapper<AssetAttach> attachWrapper = Wrappers.query();
        attachWrapper.eq("cabinet_id", id);
        List<AssetAttach> assetAttachList = assetAttachService.list(attachWrapper);
        for (AssetAttach attach : assetAttachList) {
            DetailCabinetOtherVo otherVo = new DetailCabinetOtherVo();
            String assetId = attach.getAssetId();
            Asset asset = assetService.getById(assetId);
            if (Objects.isNull(asset)) {
                return ResultVoUtil.error("资产[" + assetId + "]不存在");
            }
            if (asset.getWatch().intValue() == StatusEnum.NO.getCode()) {
                continue;
            }
            if (asset.getIsDel().intValue() != 1) {
                continue;
            }

            otherVo.setAssetId(assetId);
            otherVo.setIp(asset.getIp());
            otherVo.setAssetMode(asset.getAssetMode());
            otherVo.setAssetImage(asset.getAssetImage());

            //磁盘阵列填充 存储相关信息
            try {


                if (AssetModeConst.RAID.equals(asset.getAssetMode())) {
                    if (asset.getAssetImage().startsWith("DS")) {
                        List<CollectDS> capacity = dsService.findByType(assetId, 4);
                        if (Objects.nonNull(capacity) && !capacity.isEmpty()) {
                            CollectDS collectDS = capacity.get(0);
                            long usedCapacity = collectDS.getCapacity() - collectDS.getFreeCapacity();
                            otherVo.setTotalCapacity(ApiDsController.getNetFileSizeDescription(collectDS.getCapacity()));
                            otherVo.setUsedCapacity(ApiDsController.getNetFileSizeDescription(usedCapacity));
                            otherVo.setFreeCapacity(ApiDsController.getNetFileSizeDescription(collectDS.getFreeCapacity()));
                        }
                    } else if (asset.getAssetImage().startsWith("V") || asset.getAssetImage().startsWith("v")) {
                        List<CollectRaid> capacity = raidService.findByType(assetId, null, 3);
                        if (Objects.nonNull(capacity) && !capacity.isEmpty()) {
                            CollectRaid collectRaid = capacity.get(0);
                            long freeCapacity = collectRaid.getCapacity() - collectRaid.getUsedCapacity();
                            otherVo.setTotalCapacity(ApiDsController.getNetFileSizeDescription(collectRaid.getCapacity()));
                            otherVo.setUsedCapacity(ApiDsController.getNetFileSizeDescription(collectRaid.getUsedCapacity()));
                            otherVo.setFreeCapacity(ApiDsController.getNetFileSizeDescription(freeCapacity));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("磁盘阵列相关字段有空值" + e);
            }
            // CPU使用率
            List<CollectCpu> cpus = collectCpuService.getRealTimeData(assetId);
            if (CollectionUtil.isNotEmpty(cpus)) {
                Double cpuUsedRate = cpus.get(0).getCpuUsedRate();
                otherVo.setCpuUsedRate(cpuUsedRate);
            }

            // 内存使用率
            List<CollectMemory> mems = collectMemoryService.getRealTimeData(assetId);
            if (CollectionUtil.isNotEmpty(mems)) {
                Double memUsedRate = mems.get(0).getMemUsedRate();
                otherVo.setMemUsedRate(memUsedRate);
            }

            otherVo.setAssetName(asset.getName());

            // 服务器特有数据
            if (AssetModeConst.SERVER.equals(asset.getAssetMode())) {
                // 磁盘使用率
                List<CollectDisk> disks = collectDiskService.getRealTimeData(assetId);
                if (CollectionUtil.isNotEmpty(disks)) {
                    double rate = 0D;
                    for (CollectDisk disk : disks) {
                        Double usedRate = disk.getUsedRate();
                        if (usedRate - rate > 0) {
                            rate = usedRate;
                        }
                    }
                    otherVo.setUsedRate(rate);
                }

                // 从采集数据获取系统偏差时间 远程设备比当前时间慢了为负值，快了为正值 20210113hanwone
                otherVo.setTimeOffset(0L);
                otherVo.setSystemTime(DateUtil.formatDateTime(new Date()));
            }

            List<CollectSystemTime> realTimeData = collectSystemTimeService.getRealTimeData(asset.getId());
            if (CollUtil.isNotEmpty(realTimeData)) {
                CollectSystemTime collectSystemTime = realTimeData.get(0);
                if (Objects.nonNull(collectSystemTime.getTimeSpan())) {
                    otherVo.setTimeOffset(collectSystemTime.getTimeSpan() / 1000L);
                }
                if (Objects.nonNull(collectSystemTime.getSystemDate())) {
                    otherVo.setSystemTime(DateUtil.format(collectSystemTime.getSystemDate(), "yyyy-MM-dd HH:mm:ss"));
                }
                if (Objects.nonNull(collectSystemTime.getTimeduration())) {
                    otherVo.setTimeduration(collectSystemTime.getTimeduration());
                }
            }

            List<CollectSensor> sensorList = collectSensorServ.getRealTimeData(assetId);

            String temperature = "";
            if (!sensorList.isEmpty()) {
                for (CollectSensor item : sensorList) {
                    if (!SensorTypeEnum.GAUGE.name().equals(item.getSensorType())) {
                        continue;
                    }
                    if (StrUtil.isEmpty(temperature) || AppMathUtil.compare(item.getValue(), temperature)) {
                        temperature = item.getValue();
                    }
                }
            }

            otherVo.setTemperature(temperature);
            Boolean show = this.show(otherVo);
            if (show) {
                otherVoList.add(otherVo);
            }
        }

        return ResultVoUtil.success(otherVoList);
    }

    // 判断属性值是否为空
    private Boolean show(DetailCabinetOtherVo otherVo) {

        try {
            Class<?> aClass = Class.forName("com.jcca.web.asset.vo.DetailCabinetOtherVo");
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                String name = field.getName();
                if ("assetId".equals(name) || "assetName".equals(name))
                    continue;
                if (Objects.nonNull(field.get(otherVo)) && !Objects.equals("", field.get(otherVo)))
                    return true;
            }
        } catch (ClassNotFoundException | IllegalAccessException e) {
            log.error("机柜管理验证DetailCabinetOtherVo字段是否为空异常-ApiCabinetController", e);
            return false;
        }
        return false;
    }

    /**
     * 机柜内设备的系统连接数，系统运行时间
     *
     * @return
     */
    @GetMapping("/otherGetInfo/{id}")
    @ApiOperation(value = "机柜内设备其他数据")
    public ResultVo otherGetInfo(@PathVariable("id") String id) {
        List<DetailCabinetOtherVo> otherVoList = new ArrayList<>();
        QueryWrapper<AssetAttach> attachWrapper = Wrappers.query();
        attachWrapper.eq("cabinet_id", id);
        List<AssetAttach> assetAttachList = assetAttachService.list(attachWrapper);
        for (AssetAttach attach : assetAttachList) {
            String assetId = attach.getAssetId();
            Asset asset = assetService.getById(assetId);
            if (!AssetModeConst.TERMINAL.equals(asset.getDesk())) {
                DetailCabinetOtherVo otherVo = new DetailCabinetOtherVo();
                otherVo.setAssetId(assetId);

                List<CollectConnect> list = collectConnectService.getRealTimeData(assetId);
                if (CollUtil.isNotEmpty(list)) {
                    otherVo.setConnectionNum(list.get(0).getEstablishedNum());
                }
                otherVoList.add(otherVo);
            }
        }

        return ResultVoUtil.success(otherVoList);
    }

}

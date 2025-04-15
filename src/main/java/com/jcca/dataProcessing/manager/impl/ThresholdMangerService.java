package com.jcca.dataProcessing.manager.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.web.asset.entity.ThresholdAsset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.entity.ThresholdSection;
import com.jcca.web.asset.enums.ThresholdSectionEnum;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.asset.service.ThresholdSectionService;
import com.jcca.web.event.controller.bean.StageConfigBean;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.service.AlarmEventGroupService;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.enums.ThresholdCategoryEnum;
import com.jcca.web2.service.ThresholdManageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阈值事件监听管理程序
 *
 * @author Zhaozheng
 * @description TODO
 * @className thresholdMangerService
 * @date 2023/11/16 16:48
 * @since 2.1.0.0
 */
@Service
public class ThresholdMangerService implements ThresholdManager {

    private Map<String, ThresholdBaseEntity> thresholds = new ConcurrentHashMap<>(4096);


    @Resource
    private ThresholdAssetService thresholdAssetServ;
    @Resource
    private ThresholdSectionService thresholdSectionServ;
    @Resource
    private ThresholdProcessService processServ;
    @Resource
    private AlarmEventGroupService groupService;
    @Resource
    private AlarmEventTypeService eventTypeServ;
    @Resource
    private ThresholdManageService thresholdManageServ;


    /**
     * 初始化阈值
     * 设备阈值 code_assetId
     * 区间阈值 code_assetId_flag
     * 特定项阈值
     */
    public void init() {
        this.initThresholdV2();

    }

    @Override
    public void changeThreshold() {
        this.initThresholdV2();
    }


    /**
     * 适配V2版本的阈值初始化
     */
    private void initThresholdV2() {
        thresholds.clear();

        //进程初始化
        List<ThresholdProcess> processList = processServ.list();
        String key = "";
        for (ThresholdProcess thresholdProcess : processList) {
            if (thresholdProcess.getThresholdCpu() != null) {
                ThresholdBaseEntity baseEntity = new ThresholdBaseEntity();
                baseEntity.setBaseValue(Double.valueOf(thresholdProcess.getThresholdCpu()));
                key = StatusInfoChangeTypeEnum.event_process_cpu.getCode() + "_" + thresholdProcess.getAssetId() + "_" + thresholdProcess.getProcessName();
                thresholds.put(key, baseEntity);
            }

            if (thresholdProcess.getThresholdMemory() != null) {
                ThresholdBaseEntity baseEntity2 = new ThresholdBaseEntity();
                baseEntity2.setBaseValue(Double.valueOf(thresholdProcess.getThresholdMemory()));
                key = StatusInfoChangeTypeEnum.event_process_memory.getCode() + "_" + thresholdProcess.getAssetId() + "_" + thresholdProcess.getProcessName();
                thresholds.put(key, baseEntity2);
            }
        }

        List<ThresholdManage> list = thresholdManageServ.list();
        for (ThresholdManage item : list) {
            if (Objects.nonNull(item.getOnAlarm()) && item.getOnAlarm() == 0) {
                continue;
            }
            StatusInfoChangeTypeEnum type = null;
            if (ThresholdCategoryEnum.CPU.name().equals(item.getCategory())) {
                type = StatusInfoChangeTypeEnum.event_CPU_normal;
                key = StatusInfoChangeTypeEnum.event_CPU_normal.getCode() + "_" + item.getAssetId();
            } else if (ThresholdCategoryEnum.MEMORY.name().equals(item.getCategory())) {
                type = StatusInfoChangeTypeEnum.event_memory_normal;
                key = StatusInfoChangeTypeEnum.event_memory_normal.getCode() + "_" + item.getAssetId();
            } else if (ThresholdCategoryEnum.DISK.name().equals(item.getCategory())) {
                type = StatusInfoChangeTypeEnum.event_disk_normal;
                key = StatusInfoChangeTypeEnum.event_disk_normal.getCode() + "_" + item.getAssetId();
            } else if (ThresholdCategoryEnum.PACKET_LOSS_IN.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_inLose_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_inLose_normal;
            } else if (ThresholdCategoryEnum.PACKET_LOSS_OUT.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_outLose_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_outLose_normal;
            } else if (ThresholdCategoryEnum.CODE_ERROR_IN.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_inError_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_inError_normal;
            } else if (ThresholdCategoryEnum.CODE_ERROR_OUT.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_outError_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_outError_normal;
            } else if (ThresholdCategoryEnum.PORT_RATE_IN.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_in_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_in_normal;
            } else if (ThresholdCategoryEnum.PORT_RATE_OUT.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_out_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_out_normal;
            } else if (ThresholdCategoryEnum.TIME_DEVIATION.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_time_state.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_time_state;
            } else if (ThresholdCategoryEnum.TABLE_SPACE.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_db_tableSpace.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_db_tableSpace;
            } else if (ThresholdCategoryEnum.RUNNINGTIME_DEVIATION.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_run_time_state.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_run_time_state;
            } else if (ThresholdCategoryEnum.TEMPERATURE.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_temp_state_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_temp_state_normal;
            } else if (ThresholdCategoryEnum.SWITCH_OPTICAL_RX.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_optical_in_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_optical_in_normal;
            } else if (ThresholdCategoryEnum.SWITCH_OPTICAL_TX.name().equals(item.getCategory())) {
                key = StatusInfoChangeTypeEnum.event_port_optical_out_normal.getCode() + "_" + item.getAssetId();
                type = StatusInfoChangeTypeEnum.event_port_optical_out_normal;
            }

            if (Objects.isNull(type)) {
                AppLogUtils.buildLogInfo(LogFunctionEnum.ALARM_HANDLE, item.getCategory(), "阈值有新的类型不再初始化程序中……请联系程序员处理，位置：ThresholdMangerService.initThresholdV2");
                continue;
            }

            //普通阈值
            thresholdQuery(item.getAssetId(), type, item.getGeneral());
            //区间阈值
            thresholdSection(key, key, item.getRangeMin(), item.getRangeMax());
            //阶梯阈值
            thresholdLevel(key, item.getStepHigh(), item.getStepHigher(), item.getStepHighest());
        }
    }


    /**
     * 适配V1版本的阈值初始化
     */
    private void initThresholdV1() {
        //设备阈值
        thresholds.clear();
        List<ThresholdAsset> list = thresholdAssetServ.list();
        for (ThresholdAsset threshold : list) {
            //cpu
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_CPU_normal, threshold.getCpu());
            //内存
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_memory_normal, threshold.getMemory());
            //disk
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_disk_normal, threshold.getDisk());
            //接收丢包
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_port_inLose_normal, threshold.getPacketLossIn());
            //发送丢包率
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_port_outLose_normal, threshold.getPacketLossOut());
            //接收误码率
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_port_inError_normal, threshold.getCodeErrorIn());
            //发送误码率
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_port_outError_normal, threshold.getCodeErrorOut());
            //端口流入率
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_port_in_normal, threshold.getPortRateIn());
            //端口流出率
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_port_out_normal, threshold.getPortRateOut());
            //时间偏差
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_time_state, Objects.nonNull(threshold.getTimeDeviation()) ? threshold.getTimeDeviation().doubleValue() : null);
            //运行时长
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_run_time_state, Objects.nonNull(threshold.getRunningTimeDeviation()) ? threshold.getRunningTimeDeviation().doubleValue() : null);
            //表空间
            thresholdQuery(threshold.getAssetId(), StatusInfoChangeTypeEnum.event_db_tableSpace, Objects.nonNull(threshold.getTablespace()) ? threshold.getTablespace().doubleValue() : null);

            String assetId = threshold.getAssetId();

            List<ThresholdSection> thresholdSections = thresholdSectionServ.selectSectionConf(assetId);
            for (ThresholdSection thresholdSection : thresholdSections) {
                Double minPrice = thresholdSection.getMinPrice();
                Double maxPrice = thresholdSection.getMaxPrice();
                //设备所有启用的区间阈值
                String key = "";
                String key2 = "";
                if (ThresholdSectionEnum.CPU.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_CPU_normal.getCode() + "_" + threshold.getAssetId();
                } else if (ThresholdSectionEnum.MENORY.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_memory_normal.getCode() + "_" + threshold.getAssetId();
                } else if (ThresholdSectionEnum.PORT_IN.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_in_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_in_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.PORT_OUT.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_out_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_out_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.SEND_LOSE.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_outLose_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_outLose_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.RECEIVE_LOSE.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_inLose_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_inLose_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.SEND_ERROR_CODE.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_outError_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_outError_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.RECEIVE_ERROR_CODE.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_inError_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_inError_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.RECEIVE_POWER.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_optical_in_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_optical_in_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.SEND_POWER.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_port_optical_out_normal.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_port_optical_out_normal.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                } else if (ThresholdSectionEnum.DSIK.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_disk_normal.getCode() + "_" + threshold.getAssetId();
                } else if (ThresholdSectionEnum.DB_SPACE_NAME.name().equals(thresholdSection.getType())) {
                    key = StatusInfoChangeTypeEnum.event_db_tableSpace.getCode() + "_" + threshold.getAssetId();
                    key2 = StatusInfoChangeTypeEnum.event_db_tableSpace.getCode() + "_" + threshold.getAssetId() + "_" + thresholdSection.getFlag();
                }

                if (StrUtil.isNotEmpty(key)) {
                    thresholdSection(key, key2, minPrice, maxPrice);
                }

            }
        }

        List<ThresholdProcess> processList = processServ.list();
        for (ThresholdProcess thresholdProcess : processList) {
            if (thresholdProcess.getThresholdCpu() != null) {
                ThresholdBaseEntity baseEntity = new ThresholdBaseEntity();
                baseEntity.setBaseValue(Double.valueOf(thresholdProcess.getThresholdCpu()));
                String key = StatusInfoChangeTypeEnum.event_process_cpu.getCode() + "_" + thresholdProcess.getAssetId() + "_" + thresholdProcess.getProcessName();
                thresholds.put(key, baseEntity);
            }

            if (thresholdProcess.getThresholdMemory() != null) {
                ThresholdBaseEntity baseEntity2 = new ThresholdBaseEntity();
                baseEntity2.setBaseValue(Double.valueOf(thresholdProcess.getThresholdMemory()));
                String key2 = StatusInfoChangeTypeEnum.event_process_memory.getCode() + "_" + thresholdProcess.getAssetId() + "_" + thresholdProcess.getProcessName();
                thresholds.put(key2, baseEntity2);
            }
        }

        //阶段阈值
        QueryWrapper<AlarmEventType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EVENT_CATEGORY", StatusInfoChangeTypeEnum.event_CPU.getCode());
        AlarmEventType eventType = eventTypeServ.getOne(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EVENT_CATEGORY", StatusInfoChangeTypeEnum.event_temp.getCode());
        AlarmEventType eventTypeTemp = eventTypeServ.getOne(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EVENT_CATEGORY", StatusInfoChangeTypeEnum.event_db_tableSpace.getCode());
        AlarmEventType eventTypeTableSpace = eventTypeServ.getOne(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EVENT_CATEGORY", StatusInfoChangeTypeEnum.event_memory.getCode());
        AlarmEventType eventTypeMem = eventTypeServ.getOne(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EVENT_CATEGORY", StatusInfoChangeTypeEnum.event_port.getCode());
        AlarmEventType eventTypPort = eventTypeServ.getOne(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EVENT_CATEGORY", StatusInfoChangeTypeEnum.event_disk.getCode());
        AlarmEventType eventTypDIsk = eventTypeServ.getOne(queryWrapper);


        extractedGroup(eventType, StatusInfoChangeTypeEnum.event_CPU_normal);
        extractedGroup(eventTypeTemp, StatusInfoChangeTypeEnum.event_temp_state_normal);
        extractedGroup(eventTypeTableSpace, StatusInfoChangeTypeEnum.event_db_tableSpace);
        extractedGroup(eventTypeMem, StatusInfoChangeTypeEnum.event_memory_normal);

        extractedGroup(eventTypPort, StatusInfoChangeTypeEnum.event_port_in_normal);
        extractedGroup(eventTypPort, StatusInfoChangeTypeEnum.event_port_out_normal);
        extractedGroup(eventTypPort, StatusInfoChangeTypeEnum.event_port_inLose_normal);
        extractedGroup(eventTypPort, StatusInfoChangeTypeEnum.event_port_outLose_normal);
        extractedGroup(eventTypPort, StatusInfoChangeTypeEnum.event_port_inError_normal);
        extractedGroup(eventTypPort, StatusInfoChangeTypeEnum.event_port_outError_normal);
        extractedGroup(eventTypPort, StatusInfoChangeTypeEnum.event_port_optical_in_normal);

        extractedGroup(eventTypDIsk, StatusInfoChangeTypeEnum.event_disk_normal);

    }

    /**
     * 根据传入的参数提取一组告警事件的分组信息，并根据配置的值进行告警阈值设置。
     * 根据告警事件的严重程度设置不同的阈值
     *
     * @param eventType 告警事件类型
     * @param enums     状态信息变化类型枚举
     */
    private void extractedGroup(AlarmEventType eventType, StatusInfoChangeTypeEnum enums) {
        Integer usedFlag = 1;
        if (Objects.isNull(eventType)) {
            return;
        }
        List<AlarmEventGroup> groups = groupService.getAllByTypeId(eventType.getId());
        if (!groups.isEmpty() && usedFlag.equals(groups.get(0).getUseStage())) {
            AlarmEventGroup alarmEventGroup = groups.get(0);
            String stageConfig = alarmEventGroup.getStageConfig();
            JSONArray configArray = JSONUtil.parseArray(stageConfig);
            List<StageConfigBean> configList = JSONUtil.toList(configArray, StageConfigBean.class);
            //根据所配置值倒叙
            configList.sort(Comparator.comparing(StageConfigBean::getConfigNum).reversed());

            for (int i = 0; i < configList.size(); i++) {
                StageConfigBean item = configList.get(i);
                //告警
                if (i == 0) {
                    //最大的命中  其他不上报
                    thresholdLevel(enums.getCode(), null, null, item.getConfigNum().doubleValue());
                } else if (i == 1) {
                    //中间的命中  最大的恢复
                    thresholdLevel(enums.getCode(), null, item.getConfigNum().doubleValue(), null);
                } else {
                    //最小的命中  其他两个恢复
                    thresholdLevel(enums.getCode(), item.getConfigNum().doubleValue(), null, null);
                }
            }
        }
    }

    /**
     * 普通阈值赋值
     *
     * @param assetId
     * @param eventEnum
     */
    private void thresholdQuery(String assetId, StatusInfoChangeTypeEnum eventEnum, Double value) {
        ThresholdBaseEntity baseEntity = new ThresholdBaseEntity();

        if (Objects.nonNull(value)) {
            baseEntity.setBaseValue(value);
        }

        String key = eventEnum.getCode() + "_" + assetId;
        thresholds.put(key, baseEntity);
    }

    /**
     * 上下限阈值
     *
     * @param key
     * @param minPrice
     * @param maxPrice
     */
    private void thresholdSection(String key, String key2, Double minPrice, Double maxPrice) {
        ThresholdBaseEntity baseEntity = thresholds.get(key);
        if (Objects.isNull(baseEntity)) {
            baseEntity = new ThresholdBaseEntity();
        }
        baseEntity.setMaxValue(maxPrice);
        baseEntity.setMinValue(minPrice);
        if (StrUtil.isNotEmpty(key2)) {
            thresholds.put(key2, baseEntity);
        } else {
            thresholds.put(key, baseEntity);
        }
    }

    /**
     * 阶段阈值
     *
     * @param key
     * @param minPrice
     * @param maxPrice
     */
    private void thresholdLevel(String key, Double minPrice, Double hidePrice, Double maxPrice) {
        ThresholdBaseEntity baseEntity = thresholds.get(key);
        if (Objects.isNull(baseEntity)) {
            baseEntity = new ThresholdBaseEntity();
        }
        if (Objects.nonNull(maxPrice)) {
            baseEntity.setOneLevelValue(maxPrice);
        }
        if (Objects.nonNull(hidePrice)) {
            baseEntity.setTwoLevelValue(hidePrice);
        }
        if (Objects.nonNull(minPrice)) {
            baseEntity.setThreeLevelValue(minPrice);
        }
        thresholds.put(key, baseEntity);
    }


    @Override
    public ThresholdBaseEntity getThresholdValue(String code, String assetId, String flag) {
        return v2(code, assetId, flag);
    }

    /**
     * v2 获取
     *
     * @param code
     * @param assetId
     * @param flag
     * @return
     */
    public ThresholdBaseEntity v2(String code, String assetId, String flag) {
        String key = code + "_" + assetId;
        //进程
        String key2 = code + "_" + assetId + "_" + flag;
        ThresholdBaseEntity baseEntity = thresholds.get(key);
        if (Objects.isNull(baseEntity)) {
            baseEntity = thresholds.get(key2);
        }

        if (Objects.isNull(baseEntity)) {
            baseEntity = new ThresholdBaseEntity();
        }

        return baseEntity;
    }

    /**
     * v1获取阈值的逻辑
     *
     * @param code
     * @param assetId
     * @param flag
     * @return
     */
    public ThresholdBaseEntity v1(String code, String assetId, String flag) {
        //初始化阶段的
        String key = code + "_" + assetId + "_" + flag;
        if (StrUtil.isEmpty(flag)) {
            key = code + "_" + assetId;
        }
        ThresholdBaseEntity baseEntity = thresholds.get(key);
        //进程需要LIKE key
        if (StatusInfoChangeTypeEnum.event_process_memory.getCode().equals(code) || StatusInfoChangeTypeEnum.event_process_cpu.getCode().equals(code)) {
            if (Objects.isNull(baseEntity)) {
                Set<String> keySet = thresholds.keySet();
                for (String name : keySet) {
                    String replace = name.replace(code + "_" + assetId + "_", "");
                    if (flag.contains(replace)) {
                        baseEntity = thresholds.get(name);
                        //重新放一次  下次来了就直接查出来了
                        thresholds.put(code + "_" + assetId + "_" + flag, baseEntity);
                    }
                }
            }
        }
        //v1版本阶梯是所有设备公用一个的
        ThresholdBaseEntity group = thresholds.get(code);
        if (Objects.isNull(baseEntity)) {
            baseEntity = new ThresholdBaseEntity();
        }
        if (Objects.isNull(group)) {
            return baseEntity;
        }
        if (Objects.nonNull(group.getOneLevelValue())) {
            baseEntity.setOneLevelValue(group.getOneLevelValue());
        }
        if (Objects.nonNull(group.getTwoLevelValue())) {
            baseEntity.setTwoLevelValue(group.getTwoLevelValue());
        }
        if (Objects.nonNull(group.getThreeLevelValue())) {
            baseEntity.setThreeLevelValue(group.getThreeLevelValue());
        }
        return baseEntity;
    }

    @Override
    public String getThresholdRedisKey(String assetId, String assetIp) {
        return assetIp + ":" + assetId + ":" + StatusInfoChangeTypeEnum.statusEventValue.getCode();
    }

    @Override
    public String getThresholdMapKey(String eventCode, String valueTypeCode, String flag) {
        if (StrUtil.isNotEmpty(flag)) {
            return eventCode + "." + flag + "." + valueTypeCode;
        }
        return eventCode + "." + valueTypeCode;
    }


}

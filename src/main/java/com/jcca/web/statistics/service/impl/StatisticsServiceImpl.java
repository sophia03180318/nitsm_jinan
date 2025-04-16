package com.jcca.web.statistics.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.service.data.AbnormalAssetQuery;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.collect.service.CollectSensorService;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.statistics.dao.StatisticsMapper;
import com.jcca.web.statistics.entity.HourCpu;
import com.jcca.web.statistics.entity.HourMemory;
import com.jcca.web.statistics.entity.HourTemp;
import com.jcca.web.statistics.service.StatisticsService;
import com.jcca.web.statistics.service.bean.AssetHourMsg;
import com.jcca.web.statistics.service.bean.AssetMinuteMsg;
import com.jcca.web.statistics.service.bean.AssetRunTime;
import com.jcca.web.statistics.vo.AssetInfo;
import com.jcca.web.statistics.vo.AssetStatisticsReq;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.enums.BizTypeEnum;
import com.jcca.web2.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析相关信息
 *
 * @author Lvyp
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private StatisticsMapper statisticsMapper;
    @Resource
    private CollectCpuService cpuServ;
    @Resource
    private CollectMemoryService memoryServ;
    @Resource
    private CollectSensorService sensorServ;
    @Resource
    private SysOrgService orgService;
    @Resource
    private AssetService assetService;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private SysModuleConfigService sysModuleConfigService;

    /**
     * 告警最多的组织
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getOrgAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getOrgAlarm(paramMap);
    }

    /**
     * 告警最多的设备
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getAssetAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getAssetAlarm(paramMap);
    }

    /**
     * 告警最多的类别
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getCategoryAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getCategoryAlarm(paramMap);
    }

    /**
     * 按告警级别统计
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getLevelAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getLevelAlarm(paramMap);
    }

    /**
     * 本月度硬件更换种类统计
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getYearCategoryFix() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgIds", orgIds);
        paramMap.put("yearMonth", DateUtil.format(new Date(), "yyyy-MM"));
        return statisticsMapper.getYearCategoryFix(paramMap);
    }

    /**
     * 本年度告警月统计折线图
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getYearAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getYearAlarm(paramMap);
    }

    /**
     * 本月告警总次数
     *
     * @param
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getMonthAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getMonthAlarm(paramMap);
    }

    /**
     * 当天告警次数小时统计
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getToayAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getToayAlarm(paramMap);
    }

    /**
     * 获取所有未确认告警数量
     *
     * @param paramMap
     * @return
     */
    @Override
    public StatisticsAlarmVo getUnconfirmAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getUnconfirmAlarm(paramMap);
    }

    /**
     * 统计近一小时端口流量平均值
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getHourFlow(Map<String, Object> paramMap) {
        return statisticsMapper.getHourFlow(paramMap);
    }

    /**
     * 最近七天告警数据
     *
     * @param paramMap
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> get7Days(Map<String, Object> paramMap) {
        return statisticsMapper.get7Days(paramMap);
    }

    /**
     * 当天不同告警级别统计
     *
     * @param paramMap
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getDayLevelAlarm(Map<String, Object> paramMap) {
        return statisticsMapper.getDayLevelAlarm(paramMap);
    }

    /**
     * 按类型统计设备数量
     *
     * @param req
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getDeskAsset(AssetStatisticsReq req) {
        return statisticsMapper.getDeskAsset(req);
    }

    /**
     * 按厂商统计设备数量
     *
     * @param req
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getManufacturerAsset(AssetStatisticsReq req) {
        SysConfig sysConfig = sysModuleConfigService.getSysConfig();
        req.setShowJcca(sysConfig.getShowJcca());
        return statisticsMapper.getManufacturerAsset(req);
    }

    /**
     * 按型号统计设备数量
     *
     * @param req
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getModelAsset(AssetStatisticsReq req) {
        return statisticsMapper.getModelAsset(req);
    }

    /**
     * 按查询条件查询设备列表
     *
     * @param req
     * @return
     */
    @Override
    public List<AssetInfo> findAssetByPage(AssetStatisticsReq req) {
        return statisticsMapper.findAssetByPage(req);
    }

    /**
     * 获取总条数
     *
     * @param req
     * @return
     */
    @Override
    public Long countAssetInfo(AssetStatisticsReq req) {
        return statisticsMapper.countAssetInfo(req);
    }

    @Override
    public List<AssetRunTime> queryAssetRunTime() {

        return statisticsMapper.selectAssetRunTimeList();
    }

    @Override
    public List<AssetHourMsg> queryAssetHourMsg() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -6);

        List<DateTime> houtList = DateUtil.rangeToList(calendar.getTime(), new Date(), DateField.HOUR);
        Date startDate = houtList.get(0);
        Date endDate = houtList.get(houtList.size() - 1);

        String formatStart = DateUtil.format(startDate, "yyyy-MM-dd HH");
        String formatEnd = DateUtil.format(endDate, "yyyy-MM-dd HH");
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("endDate", formatEnd + ":59:59");
        paramsMap.put("startDate", formatStart + ":00:00");

        List<HourCpu> hourCpus = statisticsMapper.cpuHourList(paramsMap);
        List<HourMemory> hourMemories = statisticsMapper.memoryHourList(paramsMap);
        List<HourTemp> hourTemps = statisticsMapper.tempHourList(paramsMap);

        Map<String, List<HourCpu>> assetMap1 = hourCpus.stream().collect(Collectors.groupingBy(HourCpu::getAssetId));
        Map<String, List<HourMemory>> assetMap2 = hourMemories.stream().collect(Collectors.groupingBy(HourMemory::getAssetId));
        Map<String, List<HourTemp>> assetMap3 = hourTemps.stream().collect(Collectors.groupingBy(HourTemp::getAssetId));

        Set<String> assetIdList = new HashSet<String>();
        assetIdList.addAll(assetMap1.keySet());
        assetIdList.addAll(assetMap2.keySet());
        assetIdList.addAll(assetMap3.keySet());

        List<AssetHourMsg> respList = new ArrayList<AssetHourMsg>();
        for (String assetId : assetIdList) {
            AssetHourMsg msg = new AssetHourMsg();
            List<HourCpu> cpuList = assetMap1.get(assetId);
            List<HourMemory> memoryList = assetMap2.get(assetId);
            List<HourTemp> tempList = assetMap3.get(assetId);

            if (Objects.nonNull(cpuList) && !cpuList.isEmpty()) {
                msg.setAssetName(cpuList.get(0).getAssetName());
            } else if (Objects.nonNull(memoryList) && !memoryList.isEmpty()) {
                msg.setAssetName(memoryList.get(0).getAssetName());
            } else if (Objects.nonNull(tempList) && !tempList.isEmpty()) {
                msg.setAssetName(tempList.get(0).getAssetName());
            }

            msg.setAssetId(assetId);
            msg.setMemoryRateList(memoryList);
            msg.setCpuUsedRateList(cpuList);
            msg.setTemperatureList(tempList);

            respList.add(msg);
        }

        return respList;
    }

    @Override
    public AssetMinuteMsg queryMinuteMsg(String assetId) {
        IPage<CollectCpu> cpuPage = PagePlugin.startPageT(1, 20, CollectCpu.class);
        QueryWrapper<CollectCpu> cpuQuery = new QueryWrapper<CollectCpu>();
        cpuQuery.eq("ASSET_ID", assetId);
        cpuQuery.orderByDesc("ID");
        IPage<CollectCpu> cpuPageResult = cpuServ.page(cpuPage, cpuQuery);
        List<CollectCpu> cpuRecords = cpuPageResult.getRecords();

        IPage<CollectMemory> memoryPage = PagePlugin.startPageT(1, 20, CollectMemory.class);
        QueryWrapper<CollectMemory> memoryQuery = new QueryWrapper<CollectMemory>();
        memoryQuery.eq("ASSET_ID", assetId);
        memoryQuery.orderByDesc("ID");
        IPage<CollectMemory> memoryPageResult = memoryServ.page(memoryPage, memoryQuery);
        List<CollectMemory> memoryRecords = memoryPageResult.getRecords();

        IPage<CollectSensor> sensorPage = PagePlugin.startPageT(1, 20, CollectSensor.class);
        QueryWrapper<CollectSensor> sensorQuery = new QueryWrapper<CollectSensor>();
        sensorQuery.eq("ASSET_ID", assetId);
        sensorQuery.orderByDesc("ID");
        sensorQuery.eq("SENSOR_TYPE", "GAUGE");
        IPage<CollectSensor> page = sensorServ.page(sensorPage, sensorQuery);
        List<CollectSensor> sensorRecords = page.getRecords();

        AssetMinuteMsg msg = new AssetMinuteMsg();

        memoryRecords.sort(Comparator.comparing(CollectMemory::getId));
        cpuRecords.sort(Comparator.comparing(CollectCpu::getId));
        sensorRecords.sort(Comparator.comparing(CollectSensor::getId));

        msg.setMemoryRecords(memoryRecords);
        msg.setCpuRecords(cpuRecords);
        msg.setSensorRecords(sensorRecords);

        return msg;
    }

    /**
     * @description: 按组织类型统计厂商设备数量
     * @author: HanHW
     * @date: 2023/10/25 17:39
     * @param: [orgType]
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     */
    @Override
    public List<StatisticsAlarmVo> getManufacturerAssetV2(Byte orgType) {
        List<SysOrg> orgList = orgService.getListByOrgType(orgType);
        if (CollectionUtils.isEmpty(orgList)) {
            return new ArrayList<>();
        }
        List<String> orgIdList = orgList.stream().map(SysOrg::getId).collect(Collectors.toList());

        AssetStatisticsReq req = new AssetStatisticsReq();
        req.setOrgIdList(orgIdList);

        return this.getManufacturerAsset(req);
    }

    /**
     * @description: 分设备类型查询某天告警数
     * @author: HanHW
     * @date: 2023/10/26 16:00
     * @param: [day: yyyy-MM-dd]
     * @return: java.util.List<com.jcca.web2.vo.StatisticsVoV2>
     */
    @Override
    public List<StatisticsVo> findAssetAlarmByAssetModeV2(String day) {
        List<StatisticsVo> resultList = new ArrayList<>();
        // 分类型统计设备数量
        List<StatisticsAlarmVo> modeAssetList = statisticsMapper.getModeAssetV2();
        for (StatisticsAlarmVo vo : modeAssetList) {
            StatisticsVo resultVo = new StatisticsVo();
            resultVo.setId(vo.getId());
            resultVo.setTotal(vo.getTotal());
            resultVo.setName(vo.getName());
            resultVo.setAlarmTotal(0L);
            resultList.add(resultVo);
        }

        SysConfig sysConfig = sysModuleConfigService.getSysConfig();
        String showJcca = sysConfig.getShowJcca();
        // 分类型统计当天有告警设备数量
        day = "%" + day + "%";
        List<StatisticsVo> voList = statisticsMapper.findAssetAlarmByAssetModeV2(day, showJcca);
        // 数据整合
        Iterator<StatisticsVo> iterator = resultList.iterator();
        while (iterator.hasNext()) {
            StatisticsVo resultVo = iterator.next();
            String id1 = resultVo.getId();
            if (StringUtils.isEmpty(id1) || id1.startsWith("7")) {
                iterator.remove();
                continue;
            }
            for (StatisticsVo vo : voList) {
                String id2 = vo.getId();
                if (id1.equals(id2)) {
                    resultVo.setAlarmTotal(resultVo.getAlarmTotal() + vo.getTotal());
                }
            }
        }

        return resultList;
    }


    @Override
    public List<StatisticsVo> findAssetCenterAlarmByAssetModeV2() {
        List<StatisticsVo> resultList = new ArrayList<>();
        // 分类型统计设备数量
        List<StatisticsAlarmVo> modeAssetList = statisticsMapper.getModeAssetV2();
        for (StatisticsAlarmVo vo : modeAssetList) {
            StatisticsVo resultVo = new StatisticsVo();
            resultVo.setId(vo.getId());
            resultVo.setTotal(vo.getTotal());
            resultVo.setName(vo.getName());
            resultVo.setAlarmTotal(0L);
            resultList.add(resultVo);
        }
        List<StatisticsVo> voList = statisticsMapper.findAssetCenterAlarmByAssetModeV2();
        // 数据整合
        Iterator<StatisticsVo> iterator = resultList.iterator();
        while (iterator.hasNext()) {
            StatisticsVo resultVo = iterator.next();
            String id1 = resultVo.getId();
            if (StringUtils.isEmpty(id1) || id1.startsWith("7")) {
                iterator.remove();
                continue;
            }
            for (StatisticsVo vo : voList) {
                String id2 = vo.getId();
                if (id1.equals(id2)) {
                    resultVo.setAlarmTotal(resultVo.getAlarmTotal() + vo.getTotal());
                }
            }
        }

        return resultList;
    }

    /**
     * @description: 查看大屏 地图中车站数据
     * @author: HanHW
     * @date: 2023/10/27 11:33
     * @param: [orgTitle:组织名称]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    @Override
    public Map<String, Object> getStationMapDataV2(String orgTitle) {
        QueryWrapper<SysOrg> stationQueryWrapper = Wrappers.query();
        stationQueryWrapper.eq("TITLE", orgTitle);
        stationQueryWrapper.eq("TYPE", OrgTypeConst.STATION);
        stationQueryWrapper.eq("STATUS", StatusEnum.OK.getCode());
        SysOrg one = orgService.getOne(stationQueryWrapper);
        if (Objects.isNull(one)) {
            throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "该站未监控");
        }

        // 设备总数
        QueryWrapper<Asset> assetQueryWrapper = Wrappers.query();
        assetQueryWrapper.eq("ORG_ID", one.getId());
        assetQueryWrapper.eq("IS_DEL", StatusEnum.OK.getCode());
        List<Asset> assetList = assetService.list(assetQueryWrapper);
        int assetCount = assetList.size();

        // 异常设备数
        int abnormalCount = 0;
        List<String> assetIdList = assetList.stream().map(Asset::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(assetIdList)) {
            QueryWrapper<AlarmInfo> alarmQueryWrapper = Wrappers.query();
            alarmQueryWrapper.select("ASSET_ID");
            alarmQueryWrapper.in("ASSET_ID", assetIdList);
            alarmQueryWrapper.eq("STATUS", StatusConst.OK);
            alarmQueryWrapper.eq("BLANK", StatusConst.OK);
            alarmQueryWrapper.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode());
            alarmQueryWrapper.groupBy("ASSET_ID");
            abnormalCount = alarmInfoService.list(alarmQueryWrapper).size();
        }

        // 正常设备数
        int normalCount = assetCount - abnormalCount;

        // 返回数据
        Map<String, Object> respMap = new HashMap<>();
        respMap.put("orgId", one.getId());
        respMap.put("assetCount", assetCount);
        respMap.put("normalCount", normalCount);
        respMap.put("abnormalCount", abnormalCount);

        return respMap;
    }

    /**
     * @description: 获取业务数据相关告警统计 BizTypeEnum
     * <p>
     * CTC业务，通信质量，网络安全，电源，动环
     * @author: HanHW
     * @date: 2023/10/27 16:42
     * @param: []
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    @Override
    public List<BizAlarmVo> getBusinessAlarmV2() {

        String showJcca = sysModuleConfigService.getSysConfig().getShowJcca();
        List<BizAlarmVo> list = new ArrayList<>();
        BizTypeEnum[] values = BizTypeEnum.values();
        for (BizTypeEnum value : values) {
            if ("no".equals(showJcca) && value.name().equals(BizTypeEnum.JCCA.name())) {
                BizAlarmVo vo = new BizAlarmVo();
                vo.setBizType(value.code);
                vo.setBizName(value.description);
                vo.setAbnormalCount(0);
                list.add(vo);
                continue;
            }
            AbnormalAssetQuery query = new AbnormalAssetQuery();
            query.setEventCategory(value.code);
            query.setStatus((int) AlarmStateEnum.ALARM.getCode());
            query.setAlarmState((int) AlarmStateEnum.ALARM.getCode());
            query.setStatusLogical((int) StatusConst.OK);
            query.setBlank(AlarmBlankConst.NORMARL);

            Integer integer = alarmInfoService.queryAbnormalAssetV2(query);
            BizAlarmVo vo = new BizAlarmVo();
            vo.setBizType(value.code);
            vo.setBizName(value.description);
            vo.setAbnormalCount(integer);
            list.add(vo);
        }

        return list;
    }

    /**
     * @description: 近七天告警折线图
     * @author: HanHW
     * @date: 2023/10/30 13:54
     * @param: []
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    @Override
    public List<StatisticsAlarmVo> getSevenDaysAlarmLineV2() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -6);
        String last7Day = DateUtil.formatDate(c.getTime());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startDate", last7Day);
        paramMap.put("showJcca", sysModuleConfigService.getSysConfig().getShowJcca());
        List<StatisticsAlarmVo> latest7DaysList = statisticsMapper.getSevenDaysAlarmLineV2(paramMap);
        latest7DaysList = this.fill7DaysV2(latest7DaysList);
        return latest7DaysList;
    }


    @Override
    public List<StatisticsAlarmVo> getSevenDaysCenterAlarmLineV2() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -6);
        String last7Day = DateUtil.formatDate(c.getTime());
        List<StatisticsAlarmVo> latest7DaysList = statisticsMapper.getSevenDaysCenterAlarmLineV2(last7Day);
        latest7DaysList = this.fill7DaysV2(latest7DaysList);
        return latest7DaysList;
    }


    /**
     * @description: 采集指标实时监测
     * @author: HanHW
     * @date: 2023/11/1 10:12
     * @param: []
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    @Override
    @Deprecated
    public List<StatisticsAlarmVo> getCollectTargetAlarmV2() {

//        Calendar c = Calendar.getInstance();
//        c.add(Calendar.HOUR_OF_DAY, -2);
//        c.setTime(c.getTime());
//
//        String dayStr = DateUtil.format(c.getTime(), "yyyy-MM-dd HH:mm:ss");
//        List<StatisticsAlarmVo> list = statisticsMapper.getCollectTargetAlarmV2(dayStr);
//        if (!CollectionUtils.isEmpty(list)) {
//            return list;
//        }

        List<StatisticsAlarmVo> list = new ArrayList<>();

        // TODO 以下为测试数据
        StatisticsAlarmVo vo1 = new StatisticsAlarmVo();
        vo1.setName("CPU状态");
        vo1.setTotal(0L);

        StatisticsAlarmVo vo2 = new StatisticsAlarmVo();
        vo2.setName("内存状态");
        vo2.setTotal(0L);

        StatisticsAlarmVo vo3 = new StatisticsAlarmVo();
        vo3.setName("磁盘状态");
        vo3.setTotal(0L);

        StatisticsAlarmVo vo4 = new StatisticsAlarmVo();
        vo4.setName("进程状态");
        vo4.setTotal(0L);

        StatisticsAlarmVo vo5 = new StatisticsAlarmVo();
        vo5.setName("网卡状态");
        vo5.setTotal(0L);

        StatisticsAlarmVo vo6 = new StatisticsAlarmVo();
        vo6.setName("端口状态");
        vo6.setTotal(0L);

        StatisticsAlarmVo vo7 = new StatisticsAlarmVo();
        vo7.setName("存储状态");
        vo7.setTotal(0L);

        StatisticsAlarmVo vo8 = new StatisticsAlarmVo();
        vo8.setName("数据库状态");
        vo8.setTotal(0L);

        StatisticsAlarmVo vo9 = new StatisticsAlarmVo();
        vo9.setName("时钟同步状态");
        vo9.setTotal(0L);

        StatisticsAlarmVo vo10 = new StatisticsAlarmVo();
        vo10.setName("流量状态");
        vo10.setTotal(0L);

        list.add(vo1);
        list.add(vo2);
        list.add(vo3);
        list.add(vo4);
        list.add(vo5);
        list.add(vo6);
        list.add(vo7);
        list.add(vo8);
        list.add(vo9);
        list.add(vo10);

        return list;
    }

    /**
     * @description: 地图上的线路及车站
     * @author: HanHW
     * @date: 2023/11/3 15:07
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.StatistisMapOrgV2>
     **/
    @Override
    public List<StatistisMapOrg> getMapOrgListV2() {

        List<StatistisMapOrg> resList = new ArrayList<>();


        QueryWrapper<SysOrg> wrapperCenter = Wrappers.query();
        wrapperCenter.eq("TYPE", OrgTypeConst.CENTER);
        wrapperCenter.eq("STATUS", StatusEnum.OK.getCode());
        wrapperCenter.orderByAsc("SORT");
        wrapperCenter.isNotNull("STATIONX");
        wrapperCenter.isNotNull("STATIONY");
        List<SysOrg> centerList = orgService.list(wrapperCenter);

        for (SysOrg org : centerList) {
            MapStationVo center = new MapStationVo();
            center.setId(org.getId());
            center.setName(org.getTitle());
            center.setStationx(org.getStationx());
            center.setStationy(org.getStationy());
            StatistisMapOrg v2 = new StatistisMapOrg();
            v2.setLine(center);
            v2.setStations(new ArrayList<>());
            resList.add(v2);
        }

        QueryWrapper<SysOrg> wrapper = Wrappers.query();
        wrapper.eq("TYPE", OrgTypeConst.LINE);
        wrapper.eq("STATUS", StatusEnum.OK.getCode());
        wrapper.orderByAsc("SORT");
        List<SysOrg> lineList = orgService.list(wrapper);
        for (SysOrg sysOrg : lineList) {
            String id = sysOrg.getId();
            MapStationVo line = new MapStationVo();
            line.setId(id);
            line.setName(sysOrg.getTitle());

            StatistisMapOrg v2 = new StatistisMapOrg();
            v2.setLine(line);

            wrapper = Wrappers.query();
            wrapper.eq("PID", id);
            wrapper.eq("TYPE", OrgTypeConst.STATION);
            wrapper.eq("STATUS", StatusEnum.OK.getCode());
            wrapper.isNotNull("STATIONX");
            wrapper.isNotNull("STATIONY");
            wrapper.orderByAsc("SORT");
            List<SysOrg> stationList = orgService.list(wrapper);

            List<MapStationVo> stations = new ArrayList<>();
            for (SysOrg org : stationList) {
                MapStationVo station = new MapStationVo();
                station.setId(org.getId());
                station.setName(org.getTitle());
                station.setStationx(org.getStationx());
                station.setStationy(org.getStationy());
                stations.add(station);
            }
            v2.setStations(stations);
            resList.add(v2);
        }
        return resList;
    }

    /**
     * @description: 未确认告警数量 用于大屏推送
     * @author: HanHW
     * @date: 2023/11/7 11:35
     * @param: []
     * @return: void
     **/
    @Override
    public List<RollAlarmVo> rollAlarmV2() {
        SysConfig sysConfig = sysModuleConfigService.getSysConfig();
        String showJcca = sysConfig.getShowJcca();
        return statisticsMapper.getRollAlarmV2(showJcca);
    }

    @Override
    public List<RollAlarmVo> rollCenterAlarmV2() {
        return statisticsMapper.getRollCenterAlarmV2();
    }

    /**
     * @description: 获取有告警的车站
     * @author: HanHW
     * @date: 2023/11/7 13:08
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.RollAlarmVoV2>
     **/
    @Override
    public List<RollAlarmVo> stationAlarmV2() {

        return statisticsMapper.getStationAlarmV2(sysModuleConfigService.getSysConfig().getShowJcca());
    }

    /**
     * @description: 保存地图上车站坐标
     * @author: HanHW
     * @date: 2023/11/7 14:22
     * @param: [voList]
     * @return: void
     */
    @Override
    public void saveCoordsV2(List<RollAlarmVo> voList) {
        UpdateWrapper<SysOrg> update = Wrappers.update();
        update.set("STATIONX", null);
        update.set("STATIONY", null);
        orgService.update(update);
        for (RollAlarmVo vo : voList) {
            String orgName = vo.getOrgName();
            if (StringUtils.isEmpty(orgName)) continue;
            update = Wrappers.update();
            update.eq("TITLE", orgName);
            update.set("STATIONX", vo.getStationx());
            update.set("STATIONY", vo.getStationy());
            orgService.update(update);
        }
    }

    private List<StatisticsAlarmVo> fill7DaysV2(List<StatisticsAlarmVo> latest7DaysList) {
        List<StatisticsAlarmVo> latest7DaysAlarm = new ArrayList<>();
        for (int i = 6; i > -1; --i) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, -i);
            String dayStr = DateUtil.format(c.getTime(), "yyyy-MM-dd");

            Set<String> idSet = new HashSet<>();
            for (StatisticsAlarmVo vo : latest7DaysList) {
                if (dayStr.equals(vo.getName())) {
                    latest7DaysAlarm.add(vo);
                    idSet.add(vo.getId());
                }
            }
            for (int j = 1; j < 4; j++) {
                if (idSet.contains(j + "")) {
                    continue;
                }
                StatisticsAlarmVo result = new StatisticsAlarmVo();
                result.setTotal(0L);
                result.setName(dayStr);
                result.setId(j + "");
                latest7DaysAlarm.add(result);
            }
        }
        return latest7DaysAlarm;
    }

}

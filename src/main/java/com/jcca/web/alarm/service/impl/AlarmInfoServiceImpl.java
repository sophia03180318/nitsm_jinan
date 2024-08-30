package com.jcca.web.alarm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.constant.*;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.enums.BrokenOriginEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.component.client.StationCollectClient;
import com.jcca.component.event.bean.EventGroupAlarmTempData;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.quartz.alarm.bean.UnhealthyAsset;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.component.thresholds.impl.DisposeInterfaceAdapterImpl;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.alarm.controller.bean.AssetAlarmReq;
import com.jcca.web.alarm.dao.AlarmInfoMapper;
import com.jcca.web.alarm.dao.AlarmRepositoryMapper;
import com.jcca.web.alarm.dao.bean.QueryExportByTypeReq;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.alarm.service.data.AbnormalAssetQuery;
import com.jcca.web.alarm.service.data.AppAlarmFattenData;
import com.jcca.web.alarm.service.data.ExportAlarmReportBean;
import com.jcca.web.alarm.vo.AlarmDetailVo;
import com.jcca.web.alarm.vo.AlarmExportVo;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import com.jcca.web.asset.controller.bean.Repository;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.broken.dao.BrokenRecordMapper;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web.common.service.bean.ThreeDAlarmReq;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.construction.entity.ConstructionRecord;
import com.jcca.web.construction.service.ConstructionRecordService;
import com.jcca.web.event.dao.AlarmEventRelMapper;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.enums.EventRecoverFlagEnum;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web2.dto.AlarmPageDto;
import com.jcca.web2.dto.CabinetAlarmQueryDto;
import com.jcca.web2.dto.DialogsAlarmListDto;
import com.jcca.web2.dto.DisposeAlarmDto;
import com.jcca.web2.service.impl.IndexPageServiceImpl;
import com.jcca.web2.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 告警信息
 *
 * @author Lvyp
 */
@Service
@Slf4j
public class AlarmInfoServiceImpl extends ServiceImpl<AlarmInfoMapper, AlarmInfo> implements AlarmInfoService {

    /**
     * 正则
     */
    private static Pattern EVENT_PATTERN = Pattern.compile("event:event_[a-zA-Z0-9_]+[^:,]");

    @Resource
    private AlarmInfoMapper alarmInfoMapper;
    @Resource
    private AssetService assetService;
    @Resource
    private ConstructionRecordService constructionRecordService;
    @Value("${kbUrl}")
    private String kbUrl;
    @Resource
    private AlarmRepositoryMapper repoServ;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private AlarmEventService alarmEventServ;
    @Resource
    private AlarmEventRelMapper alarmEventRelMapper;
    @Resource
    private RedisService redisService;
    @Resource
    private AlarmRepositoryService alarmRepositoryServ;
    @Resource
    private StationCollectClient stationClient;
    @Resource
    private CollectInterfacesService collectInterServ;
    @Resource
    private BrokenRecordMapper brokenMapper;
    @Resource
    private BizManageService bizService;

    @Override
    public AlarmInfo getAssetAlarm(String alarmCode, String assetId, String alarmFlag) {
        return alarmInfoMapper.getAssetAlarmV2(alarmCode, assetId, alarmFlag);
    }

    /**
     * 获取导出告警数据
     *
     * @param ids
     * @return
     */
    @Override
    public List<AlarmExportVo> findExportAlarm(List<String> ids) {

        return alarmInfoMapper.findExportAlarm(ids);
    }

    /**
     * 告警详情
     *
     * @param id
     * @return
     */
    @Override
    public AlarmDetailVo findDetailById(String id) {
        return alarmInfoMapper.findDetailById(id);
    }


    /**
     * 查询是否有相同但未恢复告警
     *
     * @param paramMap
     * @return
     */
    @Override
    public AlarmInfo findOneAlarm(Map<String, Object> paramMap) {
        return alarmInfoMapper.findOneAlarm(paramMap);
    }

    /**
     * 查找当前用户管理的组织内的所有未确认告警
     *
     * @return
     */
    @Override
    public List<AlarmUnconfirmVo> findUnconfirmAlarm(Map<String, Object> paramMap) {
        return alarmInfoMapper.findUnconfirmAlarm(paramMap);
    }

    /**
     * 按条件导出全部告警
     *
     * @param query
     * @return
     */
    @Override
    public List<AlarmExportVo> findExportAllAlarm(AlarmInfoPageQuery query) {
        return alarmInfoMapper.findExportAllAlarm(query);
    }

    /**
     * 获取资产 未确认或者已确认未恢复 告警
     *
     * @param req
     * @return
     */
    @Override
    public List<AlarmUnconfirmVo> findAssetAlarm(AssetAlarmReq req) {

        return alarmInfoMapper.findAssetAlarm(req);
    }

    @Override
    public List<AlarmInfo> findValidAlarmByCorrElationId(String corrElationId, String assetId, String alarmCode) {
        List<AlarmInfo> list = alarmInfoMapper.selectValidAlarmByCorrElationIdAndAlarmCode(corrElationId, assetId,
                alarmCode);
        if (Objects.isNull(list)) {
            list = new ArrayList<AlarmInfo>();
        }

        return list;
    }

    @Override
    public List<AlarmInfo> findValidAlarmByCorrElationId(String corrElationId, String assetId) {
        List<AlarmInfo> list = alarmInfoMapper.selectValidAlarmByCorrElationId(corrElationId, assetId);
        if (Objects.isNull(list)) {
            list = new ArrayList<AlarmInfo>();
        }

        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String exeEventAlarm(AlarmEventGroup group, AlarmEvent alarmEvent) {
        String assetId = alarmEvent.getAssetId();
        Asset asset = assetService.getById(assetId);

        if (Objects.isNull(asset)) {
            log.error("生成告警信息错误：资产不存在-{}", asset.getId());
            return null;
        }

        String id = MyIdUtil.getId();

        Date occurTime = alarmEvent.getCreateTime();
        String alarmMsg = formatAlarmMsg(group, alarmEvent, asset);

        byte blank = AlarmBlankConst.NORMARL;
        QueryWrapper<ConstructionRecord> construtionWrapper = Wrappers.query();
        construtionWrapper.like("influence", assetId);
        construtionWrapper.le("start_time", occurTime);
        construtionWrapper.ge("end_time", occurTime);
        ConstructionRecord one = constructionRecordService.getOne(construtionWrapper);
        if (Objects.nonNull(one)) {
            blank = AlarmBlankConst.BLANK;
        }

        AlarmInfo alarmInfo = new AlarmInfo();

        alarmInfo.setAssetId(assetId);
        alarmInfo.setAssetIp(asset.getIp());
        alarmInfo.setAssetName(asset.getName());
        alarmInfo.setOrgId(asset.getOrgId());


        alarmInfo.setAlarmLevel(group.getLevle().byteValue());
        alarmInfo.setAlarmToRecord(AlarmToRecordConst.UNTRANSFORM);
        alarmInfo.setBlank(blank);
        alarmInfo.setContent(alarmMsg);
        alarmInfo.setDescription(alarmEvent.getEventMsg());
        alarmInfo.setCorrelationId(group.getId());
        alarmInfo.setIsShowRecover(AlarmShowRecoverConst.NO_SHOW);
        alarmInfo.setTitle(group.getName());
        alarmInfo.setType(group.getAlarmType());
        alarmInfo.setOccurTime(occurTime);
        alarmInfo.setLastTime(occurTime);
        alarmInfo.setStatus(AlarmStatusEnum.UNCONFIRM.getCode());
        alarmInfo.setAlarmState(AlarmStateEnum.ALARM.getCode());
        alarmInfo.setId(id);

        //
        alarmInfo.setAlarmCode(getEventAlarmCode(group.getId(), alarmEvent.getFlag()));

        if (EventGroupRecoverConst.CANNOT.equals(group.getRecoverFlag())) {
            //alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            //这里不恢复的上来后也显示告警，人工点完之后就认为恢复
            alarmInfo.setAlarmState(AlarmStateEnum.ALARM.getCode());
            alarmInfo.setIsShowRecover(AlarmInfo.SHOW_RECOVER_NO_FLAG);
        }

        List<AlarmRepository> repoList = repoServ.selectListByAlarmCode(alarmEvent.getUniqueCode());
        if (!repoList.isEmpty()) {
            alarmInfo.setOpinion(repoList.get(0).getPlanStr());
        }

        // 保存
        save(alarmInfo);

        //推送车站
        String eventTypeIds = group.getEventTypeIds();
        String[] eventTypeList = eventTypeIds.split("-");
        notifyPingToStation(Arrays.asList(eventTypeList), assetId, false);
        return id;
    }


    @Override
    public String exeAgainEventAlarm(AlarmEventGroup group, AlarmInfo alarmInfo, Integer alarmFlag, AlarmEvent alarmEvent) {
        if (Objects.isNull(alarmInfo)) {
            return "";
        }
        String assetId = alarmInfo.getAssetId();

        String lockKey = "ASSET_ALARM_TWO_" + assetId;
        synchronized (lockKey.intern()) {
            String eventTypeIds = group.getEventTypeIds();
            String[] eventTypeArray = eventTypeIds.split("-");
            List<String> eventTypeList = Arrays.asList(eventTypeArray);

            Asset asset = assetService.getById(assetId);

            if (alarmFlag > 0 && AlarmStateEnum.ALARM.getCode().equals(alarmInfo.getAlarmState())) {
                alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
                if (EventRecoverFlagEnum.CAN.getCode().equals(group.getRecoverFlag())) {
                    alarmInfo.setIsShowRecover(AlarmShowRecoverConst.SHOW);
                }
                alarmInfo.setLastTime(new Date());

                updateById(alarmInfo);
                // 恢复推送到前端
                //pushRedisAlarm(asset, alarmInfo.getBlank(), alarmInfo.getAlarmLevel());
                //通知车站
                notifyPingToStation(eventTypeList, assetId, true);
            } else if (alarmFlag < 0 && AlarmStateEnum.RECOVER.getCode().equals(alarmInfo.getAlarmState())) {
                // 二次上告警
                if (EventRecoverFlagEnum.CAN.getCode().equals(group.getRecoverFlag())) {
                    alarmInfo.setAlarmState(AlarmStateEnum.ALARM.getCode());
                    alarmInfo.setLastTime(alarmEvent.getCreateTime());
                }

                if (alarmInfo.getAlarmLevel() != group.getLevle().byteValue()) {
                    String alarmMsg = formatAlarmMsg(group, alarmEvent, asset);
                    alarmInfo.setAlarmLevel(group.getLevle().byteValue());
                    alarmInfo.setContent(alarmMsg);
                }

                alarmInfo.setIsShowRecover(AlarmShowRecoverConst.NO_SHOW);
                alarmInfo.setLastTime(new Date());

                updateById(alarmInfo);

                //通知车站
                notifyPingToStation(eventTypeList, assetId, false);
            }

        }

        return alarmInfo.getId();
    }


    /**
     * 通知车站 ping状态
     *
     * @param eventTypeList
     * @param assetId
     * @param status
     */
    public void notifyPingToStation(List<String> eventTypeList, String assetId, boolean status) {
        //如果是ping告警推送状态到车站
        try {
            for (String typeId : eventTypeList) {
                List<AlarmRepository> alarmRepos = alarmRepositoryServ.getAllByEventId(typeId);
                for (AlarmRepository alarmRepo : alarmRepos) {
                    stationClient.notifyStationPingStatus(assetId, status, alarmRepo.getAlarmCode());
                    stationClient.notifyStationAlarmStatus(assetId, status, alarmRepo.getAlarmCode());
                }
            }
        } catch (Exception e) {
            log.error("通知车站ping状态异常：" + e.getMessage(), e);
        }
    }


    /**
     * 获取告警信息
     *
     * @param group
     * @param alarmEvent
     * @param asset
     */
    @Override
    public String formatAlarmMsg(AlarmEventGroup group, AlarmEvent alarmEvent, Asset asset) {
        AssetBelong assetInfo = assetService.findAssetBelongById(asset.getId());

        String msgTemp = group.getMsgTemp();

        EventGroupAlarmTempData tempData = new EventGroupAlarmTempData();
        if (Objects.nonNull(assetInfo)) {
            StringBuilder str = new StringBuilder();
            if (StrUtil.isNotEmpty(assetInfo.getOrgName())) {
                str.append(assetInfo.getOrgName());
                str.append("（组织）");
            }
            if (StrUtil.isNotEmpty(assetInfo.getRoomName())) {
                str.append(assetInfo.getRoomName());
                str.append("（机房）");
            }
            if (StrUtil.isNotEmpty(assetInfo.getCabinetName())) {
                str.append(assetInfo.getCabinetName());
                str.append("（机柜）");
            }
            tempData.setOrgPosition(str.toString());
        } else {
            tempData.setOrgPosition("");
        }

        String keyValue = DictUtil.keyValue("ALARM_LEVEL", group.getLevle().toString());

        tempData.setLevel(keyValue);
        tempData.setAssetGroupCode(asset.getAssetCode());
        tempData.setAssetIp(asset.getIp());
        tempData.setAssetName(asset.getName());
        tempData.setBaseValue(alarmEvent.getBaseValue());
        tempData.setCollectValue(alarmEvent.getCollectValue());
        tempData.setCollectTimeStr(DateUtil.format(alarmEvent.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        tempData.setEventFlag(alarmEvent.getFlag());
        tempData.setContent(alarmEvent.getEventMsg());

        // 设置进程内存CPU占用率TOP5
        this.setProcessTop5(tempData, asset.getId());

        JSONObject parseObj = JSONUtil.parseObj(tempData);
        Set<String> keyList = parseObj.keySet();
        for (String key : keyList) {
            msgTemp = msgTemp.replace(key, parseObj.getStr(key));
        }

        return msgTemp;
    }

    private void setProcessTop5(EventGroupAlarmTempData tempData, String assetId) {
        Object cpu = redisService.get(RedisCacheConst.TOP5_PROCESS_CPU + assetId);
        Object mem = redisService.get(RedisCacheConst.TOP5_PROCESS_MEM + assetId);

        if (Objects.nonNull(cpu)) {
            JSONArray arr = JSONUtil.parseArray(cpu.toString());
            List<CollectProcessBean> processTop5List = JSONUtil.toList(arr, CollectProcessBean.class);
            int size = processTop5List.size();
            StringBuilder sb = new StringBuilder("进程CPU使用率TOP5：[");
            CollectProcessBean bean;
            for (int i = 0; i < size; i++) {
                bean = processTop5List.get(i);
                sb.append(bean.getName());
                if (i < (size - 1)) {
                    sb.append("，");
                }
            }
            sb.append("]");
            tempData.setProcessTop5CPU(sb.toString());
        } else {
            tempData.setProcessTop5CPU(" ");
        }

        if (Objects.nonNull(mem)) {
            JSONArray arr = JSONUtil.parseArray(mem.toString());
            List<CollectProcessBean> processTop5List = JSONUtil.toList(arr, CollectProcessBean.class);
            int size = processTop5List.size();
            StringBuilder sb = new StringBuilder("进程内存使用率TOP5：[");
            CollectProcessBean bean;
            for (int i = 0; i < size; i++) {
                bean = processTop5List.get(i);
                sb.append(bean.getName());
                if (i < (size - 1)) {
                    sb.append("，");
                }
            }
            sb.append("]");
            tempData.setProcessTop5Mem(sb.toString());
        } else {
            tempData.setProcessTop5Mem(" ");
        }
    }

    @Override
    public String getEventAlarmCode(String groupId, String eventFlag) {
        boolean empty = StrUtil.isEmpty(eventFlag);
        String alarmCode = groupId + "_" + (empty ? "" : eventFlag);
        return alarmCode;
    }

    @Override
    public String getContent(String content, byte alarmStatus, Integer showRecover, String time) {
        if (AlarmInfo.SHOW_RECOVER.equals(showRecover)) {
            if (AlarmStateEnum.RECOVER.getCode() == alarmStatus) {
                // 拼接恢复
                return "【告警恢复】" + content + "--告警于" + time + "恢复!";
            }
        }
        return content;
    }

    @Override
    public List<String> listTitle() {
        return alarmInfoMapper.listTitle();
    }

    @Override
    public Integer queryMaxLevel(String assetId) {
        Byte level = alarmInfoMapper.queryMaxAlarmLevel(assetId);
        if (Objects.isNull(level)) {
            return 0;
        }
        return level.intValue();
    }

    @Override
    public boolean keepAlarm() {
        QueryWrapper<SysModuleConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("NAME", "config:keepAlarm");
        List<SysModuleConfig> list = configService.list(queryWrapper);
        try {
            return list.get(0).getValue().equals("open");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<AlarmInfo> getOccurTime() {
        return alarmInfoMapper.getOccurTime();
    }

    @Override
    public List<String> queryDescriptionList(String id) {

        return alarmInfoMapper.queryDescriptionList(id);
    }

    @Override
    public List<Repository> queryDescriptionListV2(String id) {
        AlarmInfo alarmInfo = getById(id);
        Asset asset = assetService.getById(alarmInfo.getAssetId());

        List<Repository> repoList = new ArrayList<Repository>();
        List<String> optionList = alarmInfoMapper.queryDescriptionList(id);

        Repository repository = new Repository();
        repository.setType(DictUtil.keyValue("ASSET_MODE", asset.getDesk() + ""));
        repository.setManufacturer(DictUtil.keyValue("ASSET_FACTORY", asset.getManufacturerId() + ""));
        repository.setModel(asset.getAssetImage());
        repository.setMsg(alarmInfo.getContent());


        for (String option : optionList) {
            Repository repo = new Repository();
            repo.setPlan(option);
            repoList.add(repo);
        }
        Boolean ping = null;
        try {
            ping = TestIpUtil.ping(kbUrl, 1);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ALARM_LIST, kbUrl, e);
            return repoList;
        }
        if (!ping) {
            log.error(kbUrl + " Ping不通  请检查~");
            return repoList;
        }
        try {
            String body = HttpRequest.post(kbUrl + ":9996/api/free/pushPlanList").setReadTimeout(10000).setConnectionTimeout(10000).body(JSONUtil.toJsonStr(repository)).execute().body();
            log.debug("访问专家知识库{}：响应数据：{}", kbUrl, body);
            String code = JSONUtil.parseObj(body).getStr("code", "999");
            if (!"200".equals(code)) {
                return repoList;
            }
            String json = JSONUtil.parseObj(body).getStr("data", "");
            JSONArray respBody = JSONUtil.parseArray(json);
            List<Repository> repositoryList = JSONUtil.toList(respBody, Repository.class);

            if (Objects.isNull(repositoryList) || repositoryList.isEmpty()) {
                return repoList;
            }

            return repositoryList;
        } catch (Exception e) {
            log.error(e.toString());
            return repoList;
        }


    }

    /**
     * 按照单个组织ID查询最高告警级别
     *
     * @param orgId 组织ID
     * @return 告警级别
     */
    @Override
    public Integer getMaxLevelByOrgId(String orgId) {
        Integer level = alarmInfoMapper.queryMaxAlarmLevelByOrgId(orgId);
        if (Objects.isNull(level)) {
            return 0;
        }
        return level;
    }

    /**
     * 按照批量组织ID查询最高告警级别
     *
     * @param orgIds 组织ID列表
     * @return 告警级别
     */
    @Override
    public Integer getMaxLevelByOrgIds(List<String> orgIds) {
        Integer level = alarmInfoMapper.queryMaxAlarmLevelByOrgIds(orgIds);
        if (Objects.isNull(level)) {
            return 0;
        }
        return level;
    }

    @Override
    public List<ExportAlarmReportBean> getReportListByOrgType(QueryExportByTypeReq req) {
        return alarmInfoMapper.selectExportListByType(req);
    }

    @Override
    public List<ExportAlarmReportBean> getReportListByOrgIds(QueryExportByTypeReq req) {
        return alarmInfoMapper.selectExportListByOrgIds(req);
    }


    @Override
    public List<JSONObject> selectAppAlarmInfo(Integer pageSize, Integer pageIndex) {
        QueryWrapper<AlarmInfo> alarmInfoQuery = new QueryWrapper<AlarmInfo>();
        alarmInfoQuery.eq("STATUS", AlarmStatusEnum.UNCONFIRM.getCode());

        List<AlarmInfo> selectList = null;
        if (Objects.isNull(pageSize) || Objects.isNull(pageIndex)) {
            selectList = alarmInfoMapper.selectList(alarmInfoQuery);
        } else {
            IPage<AlarmInfo> page = PagePlugin.startPageT(pageIndex, pageSize, AlarmInfo.class);
            IPage<AlarmInfo> pageResult = page(page, alarmInfoQuery);
            selectList = pageResult.getRecords();
        }
        List<AppAlarmFattenData> copyList = EntityBeanUtil.copyList(selectList, AppAlarmFattenData.class);

        List<JSONObject> respList = new ArrayList<JSONObject>();
        for (AppAlarmFattenData appAlarmFattenData : copyList) {
            List<AlarmEvent> eventList = alarmEventServ.selectAllEventByAlarmId(appAlarmFattenData.getId());
            if (Objects.isNull(eventList)) {
                eventList = new ArrayList<AlarmEvent>();
            }
            appAlarmFattenData.setEventList(eventList);

            JSONObject parseObj = JSONUtil.parseObj(appAlarmFattenData);
            respList.add(parseObj);
        }
        return respList;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delAlarm(String id) {
        AlarmInfo alarmInfo = alarmInfoMapper.selectById(id);
        if (Objects.isNull(alarmInfo)) {
            return;
        }
        QueryWrapper<AlarmEventRel> delMapper = new QueryWrapper<AlarmEventRel>();
        delMapper.eq("alarm_id", id);

        List<String> eventIdList = alarmEventRelMapper.selectEventIdByAlarmId(id);

        //删除
        alarmInfoMapper.deleteById(id);
        alarmEventRelMapper.delete(delMapper);
        for (String eventId : eventIdList) {
            AlarmEvent event = alarmEventServ.getById(eventId);
            if (Objects.isNull(event)) {
                continue;
            }

            //判断如果是端口告警则改一下map
            if (EventUniqueCode.INTERFACES_UP_DOWN_UNIQUE_CODE.equals(event.getUniqueCode())) {
                String assetId = event.getAssetId();
                String portName = event.getFlag();

                List<CollectInterfaces> collectInterfaces = collectInterServ.selectByAssetAndPortName(assetId, portName);
                if (Objects.nonNull(collectInterfaces) && !collectInterfaces.isEmpty()) {
                    Map<Integer, Boolean> integerBooleanMap = DisposeInterfaceAdapterImpl.interfacesStatusMap.get(assetId);
                    if (Objects.nonNull(integerBooleanMap)) {
                        integerBooleanMap.put(collectInterfaces.get(0).getPortIndexRank(), true);
                        DisposeInterfaceAdapterImpl.interfacesStatusMap.put(assetId, integerBooleanMap);
                    }
                }
            }

            alarmEventServ.removeById(event);

            //清楚redis的eventKey V2
            redisService.deleteHashMap(event.getFlag(), event.getUniqueCode());

            //车站，重新推送一次
            stationClient.notifyResetStatus(event);
        }
        Asset asset = assetService.getById(alarmInfo.getAssetId());
        //采集器 重新推送一遍
        redisService.delProcess(asset.getId(), asset.getAssetCode());
        redisService.delPingStatus(asset.getId());

        //删除v2缓存标识
        String mapKey = alarmInfo.getAlarmFlag().replaceFirst("_", ":");
        mapKey = mapKey.replaceFirst("_", ":");
        redisService.hmDel(alarmInfo.getAlarmCode(), mapKey);
    }

    @Override
    public List<AlarmInfo> findUnconfirmAlarm(int level, int days) {
        String date = LocalDateTime.now().plusDays(-days).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return alarmInfoMapper.findKeepAlarm(level, date + " 00:00:00");
    }

    @Override
    public List<UnhealthyAsset> findUnHealthyAsset(int num, int days) {
        String date = LocalDateTime.now().plusDays(-days).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return alarmInfoMapper.findUnHealthyAsset(num, date + " 00:00:00");

    }

    @Override
    public List<AlarmInfo> selectUnAscertainAlarm() {
        return alarmInfoMapper.selectUnAscertainAlarm();
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delDbAlarm(String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            return;
        }
        List<AlarmRepository> linkRepo = alarmRepositoryServ.getAllByAlarmCode(StatusInfoChangeTypeEnum.event_db_connect.getCode());
        List<AlarmRepository> thRepo = alarmRepositoryServ.getAllByAlarmCode(StatusInfoChangeTypeEnum.event_db_tableSpace.getCode());
        List<AlarmRepository> thRepo1 = alarmRepositoryServ.getAllByAlarmCode(StatusInfoChangeTypeEnum.event_tableSpace_sectionOne.getCode());
        List<AlarmRepository> thRepo2 = alarmRepositoryServ.getAllByAlarmCode(StatusInfoChangeTypeEnum.event_tableSpace_sectionTwo.getCode());
        List<AlarmRepository> thRepo3 = alarmRepositoryServ.getAllByAlarmCode(StatusInfoChangeTypeEnum.event_tableSpace_sectionThree.getCode());
        linkRepo.addAll(thRepo);
        linkRepo.addAll(thRepo1);
        linkRepo.addAll(thRepo2);
        linkRepo.addAll(thRepo3);

        List<String> eventTypeIds = linkRepo.stream().map(AlarmRepository::getEventTypeId).collect(Collectors.toList());
        eventTypeIds.add("x");

        QueryWrapper<AlarmEvent> eventQuery = new QueryWrapper<>();
        eventQuery.in("EVENT_TYPE_ID", eventTypeIds);
        eventQuery.eq("ASSET_ID", assetId);
        List<AlarmEvent> list = alarmEventServ.list(eventQuery);
        List<String> eventIds = list.stream().map(AlarmEvent::getId).collect(Collectors.toList());
        eventIds.add("x");
        //查询所有的告警ID
        List<List<String>> eventIdListGroup = AppListUtils.inSplit(eventIds, 900);
        //删除事件
        for (List<String> eventIdList : eventIdListGroup) {
            QueryWrapper<AlarmEventRel> relQuery = new QueryWrapper<>();
            relQuery.in("EVENT_ID", eventIdList);
            List<AlarmEventRel> alarmEventRels = alarmEventRelMapper.selectList(relQuery);
            List<String> alarmIds = alarmEventRels.stream().map(AlarmEventRel::getAlarmId).collect(Collectors.toList());
            alarmIds.add("x");
            //删除关联
            alarmEventRelMapper.delete(relQuery);
            //删除告警
            QueryWrapper<AlarmInfo> alarmQuery = new QueryWrapper<>();
            alarmQuery.in("id", alarmIds);
            alarmInfoMapper.delete(alarmQuery);
            alarmEventServ.removeByIds(eventIdList);
        }
    }

    @Override
    public void recoverProcess(String processName, String assetId) {
        alarmInfoMapper.recoverProcess(processName, assetId);
    }

    /**
     * 查询设备业务告警信息
     *
     * @param req
     * @return
     */
    @Override
    public Object findBizAlarm(AssetAlarmReq req) {
        return alarmInfoMapper.findBizAlarm(req);
    }

    @Override
    public void recoverAlarm(String msg, String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            alarmInfoMapper.recoverAlarmByMsg(msg);
        } else {
            alarmInfoMapper.recoverAlarm(msg, assetId);
        }
    }

    @Override
    public List<AlarmInfo> broadcastAlarmList(SysConfig sysConfig) {

        if ("no".equals(sysConfig.getAffirmStatus()) && "no".equals(sysConfig.getRecoveredStatus())) {
            return null;
        }
        List<String> jccaAssets = null;
        if (!"yes".equals(sysConfig.getShowJcca())) {
            jccaAssets = assetService.listJccaId();
        }
        ArrayList<Integer> list = new ArrayList<>();
        if ("yes".equals(sysConfig.getFirstLevel())) {
            list.add(1);
        }
        if ("yes".equals(sysConfig.getSecondLevel())) {

            list.add(2);
        }
        if ("yes".equals(sysConfig.getThirdLevel())) {

            list.add(3);
        }
        sysConfig.setLevel(list);
        List<AlarmInfo> alarmInfos = alarmInfoMapper.broadcastAlarmList(sysConfig);
        if (ObjectUtil.isNotNull(jccaAssets)) {
            ArrayList<AlarmInfo> alarmList = new ArrayList<>();
            for (AlarmInfo alarmInfo : alarmInfos) {
                if (!jccaAssets.contains(alarmInfo.getAssetId())) {
                    alarmList.add(alarmInfo);
                }
            }
            return alarmList;
        }

        return alarmInfos;
    }

    @Override
    public void blankAlarm(ConstructionRecord constructionRecordco) {
        String influence = constructionRecordco.getInfluence();
        String[] split = influence.split(",");
        ArrayList<String> ids = ListUtil.toList(split);
        UpdateWrapper<AlarmInfo> uw = new UpdateWrapper<>();
        uw.in("ASSET_ID", ids);
        uw.between("OCCUR_TIME", constructionRecordco.getStartTime(), constructionRecordco.getEndTime());
        //uw.ne("BLANK",2);·
        uw.set("BLANK", 2);
        //  List<AlarmInfo> list = this.list(uw);
        this.update(uw);
    }

    @Override
    public List<DialogsAlarmListVo> queryDialogsVoListV2(DialogsAlarmListDto query) {

        String username = query.getUserName();
        try {
            if (StrUtil.isEmpty(username)) {
                username = ShiroUtil.getSubject().getUsername();
            }
        } catch (Exception e) {
            username = IndexPageServiceImpl.ROOT;
        }
        if (!IndexPageServiceImpl.ROOT.equals(username)) {
            List<String> assetIds = assetService.listIdByUserNameV2(username, 1);
            if (assetIds.isEmpty()) {
                return new ArrayList<>();
            }
            query.setAssetIdList(assetIds);
        }
        return alarmInfoMapper.queryDialogsVoListV2(query);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disposeAlarmV2(DisposeAlarmDto dto) {
        List<String> alarmIdList = dto.getAlarmIdList();

        Map<String, List<String>> recordMap = new HashMap<>();
        Date date = new Date();
        for (String alarmId : alarmIdList) {
            AlarmInfo alarmInfo = alarmInfoMapper.selectById(alarmId);
            if (Objects.isNull(alarmInfo)) {
                continue;
            }
            Asset asset = assetService.getById(alarmInfo.getAssetId());
            if (Objects.isNull(asset)) {
                continue;
            }
            String orgId = asset.getOrgId();
            List<String> mapList = recordMap.get(orgId);
            if (Objects.isNull(mapList)) {
                mapList = new ArrayList<>();
                mapList.add(asset.getId());
                recordMap.put(orgId, mapList);
            } else if (!mapList.contains(asset.getId())) {
                mapList.add(asset.getId());
                recordMap.put(orgId, mapList);
            }

            //是否可恢复 如不可恢复直接设置为恢复
            if (!alarmRepositoryServ.canRecoverV2(alarmInfo.getAlarmCode())) {
                alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            }

            alarmInfo.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
            alarmInfo.setRemark(dto.getRemark());
            alarmInfo.setConfirmor(dto.getConfirmor());
            alarmInfoMapper.updateById(alarmInfo);

            if (dto.needDisposeBroken()) {
                //告警转为故障记录
                saveBrokenV2(dto, alarmId, date, alarmInfo, asset);
            }
        }

        if (dto.needDisposeRecord()) {
            Set<String> orgIdList = recordMap.keySet();
            for (String orgId : orgIdList) {
                //添加施工计划
                List<String> assetIdList = recordMap.get(orgId);
                StringBuilder builder = new StringBuilder("");
                for (int i = 0; i < assetIdList.size(); i++) {
                    String assetId = assetIdList.get(i);
                    builder.append(assetId);
                    if (i != assetIdList.size()) {
                        builder.append(",");
                    }
                }
                ConstructionRecord req = BeanUtil.copyProperties(dto, ConstructionRecord.class);
                req.setInfluence(builder.toString());
                req.setOrgId(orgId);
                req.setCreateTime(date);
                req.setCreator(ShiroUtil.getSubject().getUsername());
                req.setModifyTime(date);
                req.setModifier(ShiroUtil.getSubject().getUsername());
                constructionRecordService.createV2(req);
            }
        }


    }

    @Override
    public List<MonitoringItemVo> getMonitoringItemV2(List<String> refuseList) {
        if (Objects.isNull(refuseList)) {
            refuseList = new ArrayList<>();
        }
        String patternKey = "event:*";
        List<String> keys = redisService.getKeyByPattern(patternKey);
        String eventKeys = keys.toString().replace("[", "").replace("]", "");
        Matcher matcher = EVENT_PATTERN.matcher(eventKeys);
        List<MonitoringItemVo> itemList = new ArrayList<MonitoringItemVo>();

        List<String> filterList = new ArrayList<>();
        while (matcher.find()) {
            String key = matcher.group(0);
            if (filterList.contains(key) || refuseList.contains(key)) {
                continue;
            }

            Integer count = 0;
            List<String> errorList = redisService.getKeyByPattern(key + ":*");
            for (String eventKey : errorList) {
                Map<String, Object> hashMap = redisService.getHashMap(eventKey);
                int size = hashMap.size();
                count = count + size;
            }
            MonitoringItemVo vo = new MonitoringItemVo();
            vo.setName(StatusInfoChangeTypeEnum.getName(key));
            vo.setCode(key);
            vo.setTotal(count);
            itemList.add(vo);
            filterList.add(key);
        }
        return itemList;
    }

    @Override
    public Integer queryAbnormalAssetV2(AbnormalAssetQuery query) {
        Integer integer = alarmInfoMapper.queryAbnormalAssetV2(query);

        return Objects.isNull(integer) ? 0 : integer;
    }

    /**
     * 保存故障记录
     *
     * @param dto
     * @param alarmId
     * @param date
     * @param alarmInfo
     * @param asset
     */
    private void saveBrokenV2(DisposeAlarmDto dto, String alarmId, Date date, AlarmInfo alarmInfo, Asset asset) {
        BrokenRecord copy = BeanUtil.copyProperties(dto, BrokenRecord.class);
        copy.setOrigin(BrokenOriginEnum.MANUAL_WORK.getCode());
        copy.setStatus(BrokenRecordConst.UNPROCESSED);
        copy.setAssetId(asset.getId());
        copy.setAlarmId(alarmId);
        copy.setAlarmTitle(alarmInfo.getTitle());
        copy.setAlarmLevel(alarmInfo.getAlarmLevel());
        copy.setInfluence(asset.getIp());
        copy.setCreateTime(date);
        copy.setCreator(ShiroUtil.getSubject().getUsername());
        copy.setModifyTime(date);
        copy.setModifier(ShiroUtil.getSubject().getUsername());

        String brokenId = MyIdUtil.getId();
        copy.setId(brokenId);
        brokenMapper.insert(copy);
        bizService.saveByBizAndOrg(brokenId, BizManageConstant.BROKEN);
    }

    @Override
    public List<CabinetAlarmInfoVo> selectCabinetAlarmV2(CabinetAlarmQueryDto query) {
        return alarmInfoMapper.selectCabinetAlarmV2(query);
    }

    @Override
    public IPage<AlarmPageVo> pageV2(AlarmPageDto query) {
        Page page = new Page();
        page.setCurrent(query.getPage());
        page.setSize(query.getSize());

        SysConfig sysConfig = configService.getSysConfig();
        if(sysConfig.showJcca()){
            query.setShowJcca(1);
        }else{
            query.setShowJcca(2);
        }

        return alarmInfoMapper.pageV2(page, query);
    }

    @Override
    public List<AlarmPageStatisticsVo> statisticsV2(AlarmPageDto query) {
        List<AlarmPageStatisticsVo> alarmPageStatisticsVos = alarmInfoMapper.statisticsV2(query);
        for (AlarmPageStatisticsVo alarmPageStatisticsVo : alarmPageStatisticsVos) {
            String key = alarmPageStatisticsVo.getKey();
            if ("ALARM_CODE".equals(query.getGroupField())) {
                if (StrUtil.isEmpty(key)) {
                    alarmPageStatisticsVo.setKey("未定义的分类");
                    alarmPageStatisticsVo.setCode("");
                } else {
                    alarmPageStatisticsVo.setCode(key);
                    alarmPageStatisticsVo.setKey(StatusInfoChangeTypeEnum.getName(key));
                }
            } else if ("ALARM_LEVEL".equals(query.getGroupField())) {
                if (StrUtil.isEmpty(key)) {
                    alarmPageStatisticsVo.setKey("未定义的级别");
                    alarmPageStatisticsVo.setCode("");
                } else {
                    alarmPageStatisticsVo.setCode(key);
                    alarmPageStatisticsVo.setKey(AlarmLevelEnum.getMsg(Byte.valueOf(key)));
                }
            }
        }

        return alarmPageStatisticsVos;
    }

    @Override
    public void recoverAlarmV2(String alarmCode, String assetId, String flag, String msg) {
        alarmInfoMapper.recoverAlarmV2(alarmCode, assetId, flag, msg);
    }



    @Override
    public List<ThreeDAlarmReq> getThreeDAlarm(String roomId1, String roomId2) {
        return alarmInfoMapper.getThreeDAlarm(roomId1,roomId2);

    }


    @Override
    public AlarmInfo selectUnOverAlarm(String alarmCode) {

        return alarmInfoMapper.selectUnOverAlarm(alarmCode);
    }

}

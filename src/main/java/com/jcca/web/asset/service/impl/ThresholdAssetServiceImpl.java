package com.jcca.web.asset.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.bean.constant.ThresholdAutoFlagConst;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.dao.ThresholdMapper;
import com.jcca.web.asset.dao.ThresholdSectionMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdAsset;
import com.jcca.web.asset.entity.ThresholdSection;
import com.jcca.web.asset.enums.ThresholdSectionEnum;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.AssetThresholdQueryV2;
import com.jcca.web.asset.service.bean.VerifyThresholdReq;
import com.jcca.web.asset.service.bean.VerifyThresholdResp;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.event.controller.bean.StageConfigBean;
import com.jcca.web.event.dao.AlarmEventGroupMapper;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hanwone
 * @date 2020-05-20 10:28
 **/
@Service
public class ThresholdAssetServiceImpl extends ServiceImpl<ThresholdMapper, ThresholdAsset>
        implements ThresholdAssetService {

    @Resource
    private RedisService redisService;
    @Resource
    private ThresholdSectionMapper thresholdSectionMapper;
    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmEventGroupMapper eventGroupMapper;
    @Resource
    private CollectInterfacesService interfacesServ;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private ThresholdMapper thresholdMapper;


    @Override
    public ThresholdBaseEntity queryAssetThresholdV2(AssetThresholdQueryV2 query) {
        QueryWrapper<ThresholdAsset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ASSET_ID", query.getAssetId());
        ThresholdAsset one = getOne(queryWrapper);

        ThresholdBaseEntity baseEntity = new ThresholdBaseEntity();

        //普通阈值
        if (Objects.nonNull(one)) {
            JSONObject jsonObject = JSONUtil.parseObj(one);
            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                if (key.equals(query.getProperty())) {
                    baseEntity.setBaseValue(jsonObject.getDouble(key));
                }
            }
        }

        //区间阈值
        ThresholdSection thresholdSection = thresholdSectionMapper.selectSectionConf(query.getAssetId(), query.getThreshold().name(), query.getFlag());
        if (Objects.nonNull(thresholdSection)) {
            Double minPrice = thresholdSection.getMinPrice();
            Double maxPrice = thresholdSection.getMaxPrice();

            baseEntity.setMaxValue(maxPrice);
            baseEntity.setMinValue(minPrice);
        }

        //阶段阈值--等雅慧设计好之后填入

        return baseEntity;
    }

    @Override
    public List<CreateEventReq> disposeVerifyThresholdSectionResp(VerifyThresholdSectionResp thresholdSectionResp, String eventGroupConstant, Date collectDate, String assetId, String collectValue, String flag) {
        List<CreateEventReq> respList = new ArrayList<CreateEventReq>();

        if (Objects.isNull(thresholdSectionResp)) {
            return respList;
        }
        String msgFlag = "";
        if (!StrUtil.isEmpty(flag)) {
            msgFlag = "【" + flag + "】";
        }

        if (thresholdSectionResp.isThreeLevelEvent()) {
            CreateEventReq eventReqItem = new CreateEventReq();
            eventReqItem.setEventLevel(thresholdSectionResp.isThreeLevelEventStatus() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode());
            eventReqItem.setOriginalMsg(msgFlag + thresholdSectionResp.getThreeEventMsg());
            eventReqItem.setAssetId(assetId);
            eventReqItem.setBaseValue(thresholdSectionResp.getThreeEventValue());
            eventReqItem.setCollectValue(collectValue);
            eventReqItem.setUniqueCode(thresholdSectionResp.getThreeEventCode());
            eventReqItem.setFlag(flag);
            eventReqItem.setGroupFlag(eventGroupConstant);
            eventReqItem.setCreateTime(collectDate);

            respList.add(eventReqItem);
        }
        if (thresholdSectionResp.isTwoLevelEvent()) {
            CreateEventReq eventReqItem = new CreateEventReq();
            eventReqItem.setEventLevel(thresholdSectionResp.isTwoLevelEventStatus() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode());
            eventReqItem.setOriginalMsg(msgFlag + thresholdSectionResp.getTwoEventMsg());
            eventReqItem.setAssetId(assetId);
            eventReqItem.setBaseValue(thresholdSectionResp.getTwoEventValue());
            eventReqItem.setCollectValue(collectValue);
            eventReqItem.setUniqueCode(thresholdSectionResp.getTwoEventCode());
            eventReqItem.setFlag(flag);
            eventReqItem.setGroupFlag(eventGroupConstant);
            eventReqItem.setCreateTime(collectDate);
            respList.add(eventReqItem);
        }
        if (thresholdSectionResp.isOneLevelEvent()) {
            CreateEventReq eventReqItem = new CreateEventReq();
            eventReqItem.setEventLevel(thresholdSectionResp.isOneLevelEventStatus() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode());
            eventReqItem.setOriginalMsg(msgFlag + thresholdSectionResp.getOneEventMsg());
            eventReqItem.setAssetId(assetId);
            eventReqItem.setBaseValue(thresholdSectionResp.getOneEventValue());
            eventReqItem.setCollectValue(collectValue);
            eventReqItem.setUniqueCode(thresholdSectionResp.getOneEventCode());
            eventReqItem.setFlag(flag);
            eventReqItem.setGroupFlag(eventGroupConstant);
            eventReqItem.setCreateTime(collectDate);
            respList.add(eventReqItem);
        }

        return respList;
    }


    @Override
    public VerifyThresholdSectionResp verifySectionThreshold(Double collectValue, String eventUniqueCode) {
        VerifyThresholdSectionResp resp = new VerifyThresholdSectionResp();
        resp.closeLevelEvent(false);

        List<String> alarmEventGroups = eventGroupMapper.selectStageConfigByAlarmCode(eventUniqueCode);
        if (Objects.isNull(alarmEventGroups) || alarmEventGroups.isEmpty() || Objects.isNull(collectValue)) {
            return resp;
        }
        String config = alarmEventGroups.get(0);
        if (!JSONUtil.isJsonArray(config)) {
            return resp;
        }

        JSONArray configArray = JSONUtil.parseArray(config);
        List<StageConfigBean> configList = JSONUtil.toList(configArray, StageConfigBean.class);
        //根据所配置值倒叙
        configList.sort(Comparator.comparing(StageConfigBean::getConfigNum).reversed());

        String msg2 = String.format("分级告警采集到值%s,阶段恢复！", collectValue);
        boolean havaAlarm = false;
        for (int i = 0; i < configList.size(); i++) {
            StageConfigBean item = configList.get(i);
            int configNum = item.getConfigNum();
            if (collectValue > configNum) {
                havaAlarm = true;
                String msg = String.format("分级告警设定值%s,采集到值%s,超阈值！", configNum, collectValue);

                //告警
                if (i == 0) {
                    //最大的命中  其他不上报
                    resp.openThreeEventAlarm(eventUniqueCode + "_0", configNum, msg);
                } else if (i == 1) {
                    //中间的命中  最大的恢复
                    resp.openTwoEventAlarm(eventUniqueCode + "_1", configNum, msg);
                    resp.closeThreeEventAlarm(eventUniqueCode + "_0", msg2, configList.get(0).getConfigNum());
                } else {
                    //最小的命中  其他两个恢复
                    resp.openOneEventAlarm(eventUniqueCode + "_2", configNum, msg);
                    resp.closeTwoEventAlarm(eventUniqueCode + "_1", msg2, configList.get(1).getConfigNum());
                    resp.closeThreeEventAlarm(eventUniqueCode + "_0", msg2, configList.get(0).getConfigNum());
                }
                break;
            }
        }

        if (!havaAlarm) {
            //一个没命中  全部恢复
            if (configList.size() == 1) {
                resp.closeThreeEventAlarm(eventUniqueCode + "_0", msg2, configList.get(0).getConfigNum());
            } else if (configList.size() == 2) {
                resp.closeThreeEventAlarm(eventUniqueCode + "_0", msg2, configList.get(0).getConfigNum());
                resp.closeTwoEventAlarm(eventUniqueCode + "_1", msg2, configList.get(1).getConfigNum());
            } else if (configList.size() == 3) {
                resp.closeThreeEventAlarm(eventUniqueCode + "_0", msg2, configList.get(0).getConfigNum());
                resp.closeTwoEventAlarm(eventUniqueCode + "_1", msg2, configList.get(1).getConfigNum());
                resp.closeOneEventAlarm(eventUniqueCode + "_2", msg2, configList.get(2).getConfigNum());
            }
        }

        return resp;
    }


    /**
     * 处理阈值
     */
    @Override
    public VerifyThresholdResp verifyThreshold(VerifyThresholdReq req) {

        String collectValue = req.getCollectValue();

        if (req.getHaveSection()) {
            // 需要校验区间阈值
            VerifyThresholdResp resp = disposeSectionCase(req);
            if (Objects.nonNull(resp)) {
                return resp;
            }
        }

        if (req.getType() == ThresholdSectionEnum.SEND_POWER || req.getType() == ThresholdSectionEnum.RECEIVE_POWER) {
            return null;
        }

        ThresholdAssetVo threshold = findAssetThreshold(req.getAssetId());
        if (Objects.isNull(threshold)) {
            return null;
        }

        VerifyThresholdResp resp = new VerifyThresholdResp();

        String baseValue = ThresholdSectionEnum.getBaseValue(threshold, req.getType());

        ThresholdSectionEnum type = req.getType();
        String flag = req.getFlag();
        // 输出结果：采集值是否大于设定值
        String format = String.format("%s %s：%s %%,设定值：%s %%,阈值指标正常！", StrUtil.isNotEmpty(flag) ? flag : "",
                type.getMsg(), collectValue, baseValue);

        resp.setAlarmStatus(false);
        resp.setMsg(format);
        resp.setBaseValue(baseValue);

        if (AppMathUtil.compare(collectValue, baseValue)) {
            format = String.format("%s %s：%s %%,设定值：%s %%,超阈值！", StrUtil.isNotEmpty(flag) ? flag : "",
                    type.getMsg(), collectValue, baseValue);
            resp.setMsg(format);
            resp.setAlarmStatus(true);
        }

        return resp;
    }

    /**
     * 处理阶段阈值
     *
     * @param req
     * @return
     */
    private VerifyThresholdResp disposeSectionCase(VerifyThresholdReq req) {
        String flagName = StrUtil.isNotEmpty(req.getFlag()) ? req.getFlag() : "";

        ThresholdSectionEnum type = req.getType();
        String collectValue = req.getCollectValue();
        if (type == ThresholdSectionEnum.PORT_IN || type == ThresholdSectionEnum.PORT_OUT) {
            collectValue = req.getPortIntOrOutSpeed().toString();
        }

        ThresholdSection sectionConf = thresholdSectionMapper.selectSectionConf(req.getAssetId(), type.name(),
                req.getFlag());

        if (Objects.nonNull(sectionConf)) {
            // 判定是否超过最大或者最小阈值
            VerifyThresholdResp resp = new VerifyThresholdResp();
            Double maxPrice = sectionConf.getMaxPrice();
            Double minPrice = sectionConf.getMinPrice();

            resp.setAlarmStatus(false);
            resp.setBaseValue(maxPrice.toString());

            String format = String.format("%s %s :%s %%,处于设定的范围：%s %% - %s %% 之间,阈值指标正常！", flagName, type.getMsg(),
                    collectValue, minPrice, maxPrice);

            if (type == ThresholdSectionEnum.PORT_IN || type == ThresholdSectionEnum.PORT_OUT) {
                format = String.format("%s %s :%s kbps,处于设定的范围：%s kbps -  %s kbps之间,阈值指标正常！", flagName, type.getMsg(),
                        collectValue, minPrice, maxPrice);
            } else if (type == ThresholdSectionEnum.SEND_POWER || type == ThresholdSectionEnum.RECEIVE_POWER) {
                format = String.format("%s %s :%s dBm,处于设定的范围：%s dBm -  %s dBm之间,阈值指标正常！", flagName, type.getMsg(),
                        collectValue, minPrice, maxPrice);
            }

            if (AppMathUtil.compare(collectValue, maxPrice.toString())) {
                resp.setAlarmStatus(true);

                resp.setBaseValue(maxPrice.toString());
                format = String.format("%s%s :%s %%,超过设定的上限阈值%s %%,超阈值！", flagName, type.getMsg(), collectValue, maxPrice);

                if (type == ThresholdSectionEnum.PORT_IN || type == ThresholdSectionEnum.PORT_OUT) {
                    format = String.format("%s%s :%s kbps,超过设定的上限阈值%s kbps,超阈值！", flagName, type.getMsg(), collectValue, maxPrice);
                } else if (type == ThresholdSectionEnum.SEND_POWER || type == ThresholdSectionEnum.RECEIVE_POWER) {
                    format = String.format("%s%s :%s dBm,超过设定的上限阈值%s dBm,超阈值！", flagName, type.getMsg(), collectValue, maxPrice);
                }
            } else if (AppMathUtil.compare(minPrice.toString(), collectValue)) {
                resp.setAlarmStatus(true);

                resp.setBaseValue(minPrice.toString());

                format = String.format("%s%s :%s %%,低于设定的下限阈值%s %%,低于设定最小阈值,超阈值！", flagName, type.getMsg(), collectValue,
                        minPrice);

                if (type == ThresholdSectionEnum.PORT_IN || type == ThresholdSectionEnum.PORT_OUT) {
                    format = String.format("%s%s :%s kbps,低于设定的下限阈值%s kbps,低于设定最小阈值,超阈值！", flagName, type.getMsg(), collectValue,
                            minPrice);
                } else if (type == ThresholdSectionEnum.SEND_POWER || type == ThresholdSectionEnum.RECEIVE_POWER) {
                    format = String.format("%s%s :%s dBm,低于设定的下限阈值%s dBm,低于设定最小阈值,超阈值！", flagName, type.getMsg(), collectValue,
                            minPrice);
                }
            }

            resp.setMsg(format);

            return resp;
        }
        return null;
    }


    /**
     * 根据资产ID获取阈值
     *
     * @param assetId
     * @return
     */
    @Override
    public ThresholdAssetVo findAssetThreshold(String assetId) {
        Object o = redisService.get(RedisCacheConst.THRESHOLD_ALARM_PRE + assetId);
        if (Objects.nonNull(o)) {
            return JSONUtil.toBean(o.toString(), ThresholdAssetVo.class);
        }

        // 查看设备阈值
        ThresholdAsset threshold = this.getById(assetId);
        if (Objects.isNull(threshold)) {
            return null;
        }

        ThresholdAssetVo vo = new ThresholdAssetVo();
        BeanUtil.copyProperties(threshold, vo);

        redisService.set(RedisCacheConst.THRESHOLD_ALARM_PRE + assetId, JSONUtil.toJsonStr(vo));
        return vo;
    }

    @Override
    public ThresholdAsset findByAssetId(String id) {
        QueryWrapper<ThresholdAsset> queryWrapper = new QueryWrapper<ThresholdAsset>();
        queryWrapper.eq("ASSET_ID", id);
        List<ThresholdAsset> list = list(queryWrapper);
        if (Objects.isNull(list) || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    @Override
    public ThresholdAsset querySpecialConf(String id) {
        QueryWrapper<ThresholdAsset> queryWrapper = new QueryWrapper<ThresholdAsset>();
        queryWrapper.eq("ASSET_ID", id);
        queryWrapper.eq("AUTO_FLAG", 2);

        List<ThresholdAsset> list = list(queryWrapper);
        if (Objects.isNull(list) || list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }


    @Override
    public ThresholdAssetVo queryDefaultConf(List<String> assetIds, Integer assetMode) {
        QueryWrapper<ThresholdAsset> queryWrapper = new QueryWrapper<ThresholdAsset>();
        queryWrapper.eq("ASSET_MODE", assetMode);
        queryWrapper.eq("AUTO_FLAG", 1);
        List<ThresholdAsset> list = list(queryWrapper);

        if (list.isEmpty() || assetIds.isEmpty()) {
            return new ThresholdAssetVo();
        }

        List<ThresholdAsset> userThresholdConfList = list.stream().filter(p -> assetIds.contains(p.getAssetId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userThresholdConfList)) {
            return new ThresholdAssetVo();
        }

        ThresholdAsset thresholdAsset = userThresholdConfList.get(0);
        ThresholdAssetVo vo = new ThresholdAssetVo();
        BeanUtil.copyProperties(thresholdAsset, vo);

        //获取有设备权限的默认阈值
        if (assetMode != AssetModeEnum.SERVER.getCode()) {
            return vo;
        }

        //服务器需要还原运行时长
        //linux
        List<Asset> linuxAssets = assetServ.listByCollectionType(Arrays.asList(0, 2));
        //windows
        List<Asset> windowsAssets = assetServ.listByCollectionType(Arrays.asList(1));

        for (ThresholdAsset assetThreshold : userThresholdConfList) {
            if (Objects.isNull(assetThreshold.getRunningTimeDeviation())) {
                continue;
            }
            String assetId = assetThreshold.getAssetId();
            List<Asset> isLinux = linuxAssets.stream().filter(p -> p.getId().equals(assetId)).collect(Collectors.toList());
            if (Objects.isNull(vo.getRunningTimeDeviationLinux()) && !isLinux.isEmpty()) {
                vo.setRunningTimeDeviationLinux(assetThreshold.getRunningTimeDeviation());
            }
            List<Asset> isWindows = windowsAssets.stream().filter(p -> p.getId().equals(assetId)).collect(Collectors.toList());
            if (Objects.isNull(vo.getRunningTimeDeviationWindows()) && !isWindows.isEmpty()) {
                vo.setRunningTimeDeviationWindows(assetThreshold.getRunningTimeDeviation());
            }

            if (Objects.isNull(vo.getRunningTimeDeviationWindows()) && Objects.isNull(vo.getRunningTimeDeviationLinux())) {
                return vo;
            }
        }

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeSpecial(String assetId, List<String> assetIds) {
        Asset asset = assetServ.getById(assetId);
        ThresholdAssetVo defaultConfig = queryDefaultConf(assetIds, asset.getAssetMode());

        QueryWrapper<ThresholdAsset> deleteWrapper = new QueryWrapper<ThresholdAsset>();
        deleteWrapper.eq("ASSET_ID", assetId);

        remove(deleteWrapper);

        ThresholdAsset entity = EntityBeanUtil.copy(defaultConfig, ThresholdAsset.class);
        entity.setAssetId(assetId);
        entity.setCreateTime(new Date());
        entity.setCreator("系统");
        entity.setAssetMode(asset.getAssetMode());
        entity.setAutoFlag(ThresholdAutoFlagConst.ORG_THRESHOLD);


        if (asset.getAssetMode() != AssetModeEnum.SERVER.getCode()) {
            save(entity);
            return;
        }

        if (Arrays.asList(0, 2).contains(asset.getCollectionType())) {
            if (Objects.nonNull(defaultConfig.getRunningTimeDeviationLinux())) {
                entity.setRunningTimeDeviation(defaultConfig.getRunningTimeDeviationLinux());
            }
        } else {
            if (Objects.nonNull(defaultConfig.getRunningTimeDeviationWindows())) {
                entity.setRunningTimeDeviation(defaultConfig.getRunningTimeDeviationWindows());
            }
        }

        save(entity);
    }

    /**
     * 如果阈值已经删除则恢复告警
     * <p>
     * 分阶是单独的告警
     */
    @Override
    public void recoverAlarm(String assetId) throws Exception {
        ThresholdAsset assetThreshold = findByAssetId(assetId);
        Asset asset = assetServ.getById(assetId);

        if (Objects.isNull(asset) || Objects.isNull(assetThreshold)) {
            return;
        }

        if (AssetModeEnum.SERVER.getCode() == asset.getAssetMode()) {
            //服务器  CPU、磁盘、内存、时间偏差、运行时长 需要判定
            if (Objects.isNull(assetThreshold.getCpu())) {
                //未设定Cpu告警
                this.recoverAlarm(assetId, ThresholdSectionEnum.CPU, null, "CPU使用率");
            }
            if (Objects.isNull(assetThreshold.getDisk())) {
                //未设定磁盘
                this.recoverAlarm(assetId, ThresholdSectionEnum.DSIK, null, "磁盘使用率");
            }
            if (Objects.isNull(assetThreshold.getMemory())) {
                //内存
                this.recoverAlarm(assetId, ThresholdSectionEnum.MENORY, null, "内存使用率");
            }
            if (Objects.isNull(assetThreshold.getTimeDeviation())) {
                //时间偏差
                this.recoverAlarm(assetId, null, "NO", "时间偏差");
            }
            if (Objects.isNull(assetThreshold.getRunningTimeDeviation())) {
                //运行时长
                this.recoverAlarm(assetId, null, "NO", "运行时长");
            }

        } else if (AssetModeEnum.ROUTER.getCode() == asset.getAssetMode() || AssetModeEnum.SWITCH.getCode() == asset.getAssetMode()) {
            //交换机  cpu、内存、端口流入、端口流出、接收丢包、接收误码、发送丢包、发送误码
            if (Objects.isNull(assetThreshold.getCpu())) {
                //未设定Cpu告警
                this.recoverAlarm(assetId, ThresholdSectionEnum.CPU, null, "CPU使用率");
            }
            if (Objects.isNull(assetThreshold.getMemory())) {
                //内存
                this.recoverAlarm(assetId, ThresholdSectionEnum.MENORY, null, "内存使用率");
            }
            List<CollectInterfaces> realTimeData = interfacesServ.getRealTimeData(assetId);
            for (CollectInterfaces item : realTimeData) {
                if (Objects.isNull(assetThreshold.getPortRateIn())) {
                    //端口流入
                    this.recoverAlarm(assetId, ThresholdSectionEnum.PORT_IN, item.getPortName(), "端口流入率");
                }
                if (Objects.isNull(assetThreshold.getPortRateOut())) {
                    //端口流出
                    this.recoverAlarm(assetId, ThresholdSectionEnum.PORT_OUT, item.getPortName(), "端口流出率");
                }
                if (Objects.isNull(assetThreshold.getPacketLossIn())) {
                    //接收丢包
                    this.recoverAlarm(assetId, ThresholdSectionEnum.RECEIVE_LOSE, item.getPortName(), "接收丢包");
                }
                if (Objects.isNull(assetThreshold.getPacketLossOut())) {
                    //发送丢包
                    this.recoverAlarm(assetId, ThresholdSectionEnum.SEND_LOSE, item.getPortName(), "发送丢包");
                }
                if (Objects.isNull(assetThreshold.getCodeErrorIn())) {
                    //接收误码
                    this.recoverAlarm(assetId, ThresholdSectionEnum.RECEIVE_ERROR_CODE, item.getPortName(), "接收误码");
                }
                if (Objects.isNull(assetThreshold.getCodeErrorIn())) {
                    //发送误码
                    this.recoverAlarm(assetId, ThresholdSectionEnum.SEND_ERROR_CODE, item.getPortName(), "发送误码");
                }
            }
        }
    }

    /**
     * 添加恢复事件
     *
     * @param assetId
     * @param sectionType
     * @param sectionFlag
     * @param orgMsgFlag
     * @throws Exception
     */
    public void recoverAlarm(String assetId, ThresholdSectionEnum sectionType, String sectionFlag, String orgMsgFlag) throws Exception {

        ThresholdSection thresholdSection = null;
        if (Objects.nonNull(sectionType) && !"NO".equals(sectionFlag)) {
            thresholdSection = thresholdSectionMapper.selectSectionConf(assetId, sectionType.name(), sectionFlag);
        }
        if (Objects.nonNull(thresholdSection)) {
            //设定的有上下限
            return;
        }
        alarmInfoService.recoverAlarm(orgMsgFlag, assetId);
    }

    /**
     * 按资产类型查询阈值
     *
     * @param assetMode 资产类型
     * @return 阈值
     */
    @Override
    public ThresholdAsset findByAssetMode(Integer assetMode) {
        List<String> subjectAssetIds = ShiroUtil.getSubjectAssetIds();
        List<String> ids = new ArrayList<>();
        List<ThresholdAsset> list = new ArrayList<>();
        if (subjectAssetIds.size() > 1000) {
            for (int i = 0; i < subjectAssetIds.size(); i++) {
                ids.add(subjectAssetIds.get(i));
                if (i % 900 == 0) {
                    QueryWrapper<ThresholdAsset> query = Wrappers.query();
                    query.eq("ASSET_MODE", assetMode);
                    query.eq("AUTO_FLAG", ThresholdAutoFlagConst.ORG_THRESHOLD);
                    query.in("ASSET_ID", ids);
                    query.eq("ROWNUM", 1);
                    list = this.list(query);
                    ids = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(list)) {
                        break;
                    }
                }
            }
        } else {
            QueryWrapper<ThresholdAsset> query = Wrappers.query();
            query.eq("ASSET_MODE", assetMode);
            query.eq("AUTO_FLAG", ThresholdAutoFlagConst.ORG_THRESHOLD);
            query.in("ASSET_ID", subjectAssetIds);
            query.eq("ROWNUM", 1);
            list = this.list(query);
        }
        return list.get(0);
    }

    /**
     * 按资产类型查询运行时长
     *
     * @param assetMode 资产类型
     * @return 运行时长
     */
    @Override
    public List<Integer> findRuntimeByAssetMode(Integer assetMode) {
        return thresholdMapper.findRuntimeByAssetMode(assetMode);
    }

    /**
     * 设置CPU使用率
     *
     * @param asset   资产
     * @param cpuLoad CPU负载
     */
    @Override
    public void setCpuLoad(Asset asset, Double cpuLoad) {
        ThresholdAsset threshold = this.getById(asset.getId());
        if (Objects.isNull(threshold)) {
            threshold = new ThresholdAsset();
            threshold.setAssetId(asset.getId());
            threshold.setAssetMode(asset.getAssetMode());
            threshold.setCpuLoad(cpuLoad);
            this.save(threshold);
        } else {
            threshold.setCpuLoad(cpuLoad);
            this.updateById(threshold);
        }
    }

}
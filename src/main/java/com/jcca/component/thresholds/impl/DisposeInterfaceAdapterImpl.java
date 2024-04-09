package com.jcca.component.thresholds.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.constants.AppLogHead;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectInterfaceBean;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.enums.ThresholdSectionEnum;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdReq;
import com.jcca.web.asset.service.bean.VerifyThresholdResp;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 硬件端口数据处理
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeInterfaceAdapterImpl implements CollectAdapter {

    public static final String CACHE_KEY_STATUS = "CACHE_STATUS:SWITCH:";

    @Resource
    private CollectInterfacesService interfaceServ;
    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private RedisService redisService;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private TopoAssetPortService topoAssetPortServ;
    @Resource
    private AssetLinkAssetService assetLinkAssetServ;
    @Resource
    private AssetHidConfService hidConfServ;

    /**
     * 设备的状态记录器
     * key:资产ID
     * value:(key:端口rank value:状态 通true 不通 false)
     */
    public static Map<String, Map<Integer, Boolean>> interfacesStatusMap = new HashMap<String, Map<Integer, Boolean>>(10);


    /**
     * 处理硬件端口信息
     *
     * @param
     */
    @Override
    public void dispose(JSONArray data) {
        List<CollectInterfaceBean> interfaces = JSONUtil.toList(data, CollectInterfaceBean.class);
        if (interfaces.isEmpty()) {
            return;
        }
        String assetId = interfaces.get(0).getAssetId();
        String code = MyIdUtil.getId();
        List<CollectInterfaces> interfaceList = new ArrayList<CollectInterfaces>();

        //查询配置了禁用的端口
        List<String> portNameList = hidConfServ.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.PORT.name()).stream().map(AssetHidConf::getFlag).collect(Collectors.toList());

        for (CollectInterfaceBean item : interfaces) {

            //属性数值有问题的跳过
            if (!CollectInterfaceBean.verifyData(item)) {
                continue;
            }
            //虚拟口丢包误码强制为零
            if (item.getPortIndex().contains(".")) {
                item.setDiscardPacketsOut(0L);
                item.setErrorCodeIn(0L);
                item.setLosePacketsOutRate("0");
                item.setErroCodeInRate("0");
            }
            Date date = new Date();
            date.setTime(Long.parseLong(item.getCollectTime()));
            CollectInterfaces copy = EntityBeanUtil.copy(item, CollectInterfaces.class);
            copy.setId(MyIdUtil.getId());
            copy.setCollectCode(code);
            copy.setCollectTime(date);
            copy.setPortSpeed(item.getPortSpeed());
            copy.setPortInSpeed(item.getPortInSpeed());
            copy.setPortOutSpeed(item.getPortOutSpeed());
            copy.setLosePacketsOutRate(Double.valueOf(item.getLosePacketsOutRate()));
            copy.setLosePacketsInRate(Double.valueOf(item.getLosePacketsInRate()));
            copy.setErroCodeOutRate(Double.valueOf(item.getErroCodeOutRate()));
            copy.setErroCodeInRate(Double.valueOf(item.getErroCodeInRate()));
            copy.setPortIndexRank(item.getPortIndexRank());
            // 采集器portIn,errorCodeOut...存总值 itsm portIn,errorCodeOut... 存差值
            copy.setPortInCount(item.getPortIn());
            copy.setPortOutCount(item.getPortOut());
            copy.setErrorCodeInCount(item.getErrorCodeIn());
            copy.setErrorCodeOutCount(item.getErrorCodeOut());
            copy.setDiscardPacketsInCount(item.getDiscardPacketsIn());
            copy.setDiscardPacketsOutCount(item.getDiscardPacketsOut());
            //计算
            calculate(copy, item);

            AssetLinkAsset linkAssetByAsset = assetLinkAssetServ.findLinkAssetByAsset(assetId, item.getPortIndex());

            String atAssetMsg = "未识别设备";
            if (Objects.nonNull(linkAssetByAsset)) {
                atAssetMsg = assetLinkAssetServ.formatMsg(linkAssetByAsset.getLinkAssetId(), linkAssetByAsset.getLinkAssetIp());
            }

            String lockKey = item.getAssetId() + item.getPortIndex();
            synchronized (lockKey.intern()) {
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setAssetId(assetId);
                eventReq.setUniqueCode(EventUniqueCode.INTERFACES_UP_DOWN_UNIQUE_CODE);
                eventReq.setFlag(item.getPortName());
                eventReq.setGroupFlag(EventGroupConstant.INTERFACES_UP_DOWN);
                eventReq.setCreateTime(date);
                eventReq.setBaseValue(atAssetMsg);

                //获取状态记录器
                Map<Integer, Boolean> statusMap = interfacesStatusMap.get(assetId);
                if (Objects.isNull(statusMap)) {
                    statusMap = interfaceServ.getDbInterfacesStatus(assetId);
                    interfacesStatusMap.put(assetId, statusMap);
                }

                Boolean oldStatus = statusMap.get(item.getPortIndexRank());
                Boolean nowIsUp = InterfaceStatus.isUp(item.getStatus());
                if (nowIsUp) {
                    //更新topo_asset_port表状态数据
                    topoAssetPortServ.updatePortStatus(item.getAssetId(), item.getPortIndex(),
                            InterfaceStatus.OK.getCode().intValue());
                } else {
                    topoAssetPortServ.updatePortStatus(item.getAssetId(), item.getPortIndex(),
                            InterfaceStatus.NO.getCode().intValue());
                }
                //首次处理不产生任何告警处理   //被禁用的端口直接不对比状态不执行事件推送
                if (Objects.isNull(oldStatus) || portNameList.contains(item.getPortName())) {
                    //更新端口状态记录
                    statusMap.put(item.getPortIndexRank(), InterfaceStatus.isUp(item.getStatus()));
                    interfacesStatusMap.put(assetId, statusMap);
                    interfaceList.add(copy);
                    continue;
                } else if (oldStatus && !nowIsUp) {
                    statusMap.put(item.getPortIndexRank(), InterfaceStatus.isUp(item.getStatus()));
                    interfacesStatusMap.put(assetId, statusMap);
                    //上断事件
                    eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                    String format = String.format("端口：%s,已断开,原始状态：%s", item.getPortName(), item.getStatus());
                    eventReq.setOriginalMsg(format);
                    try {
                        log.info(format);
                        eventLogicServ.addEvent(eventReq);
                    } catch (Exception e) {
                        log.error("处理端口通断事件异常：{}", e.getMessage(), e);
                    }
                } else if (!oldStatus && nowIsUp) {
                    statusMap.put(item.getPortIndexRank(), InterfaceStatus.isUp(item.getStatus()));
                    interfacesStatusMap.put(assetId, statusMap);
                    //上通事件
                    eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                    String format = String.format("端口：%s,已连接,原始状态：%s", item.getPortName(), item.getStatus());
                    eventReq.setOriginalMsg(format);
                    try {
                        log.info(format);
                        eventLogicServ.addEvent(eventReq);
                        //更新端口状态记录
                    } catch (Exception e) {
                        //异常不更新状态表  下次再发一次
                        log.error("处理端口通断事件异常：{}", e.getMessage(), e);
                    }
                }
            }


            // 未被禁用的端口处理阈值
            if (!portNameList.contains(item.getPortName())) {
                disposeThresholdCompare(copy);
            }
            interfaceList.add(copy);
        }

        if (interfaceList.isEmpty()) {
            return;
        }

        // 保存采集数据
        interfaceServ.saveBatch(interfaceList);
        // 更新实时数据
        interfaceServ.updateRealTimeData(interfaceList);
    }


    /**
     * 这个计算可能是多余的需要和
     * 张雅惠对一下 暂时先保留
     */
    private void calculate(CollectInterfaces copy, CollectInterfaceBean item) {
        List<CollectInterfaces> oldInterfacesList = interfaceServ.getRealTimeData(item.getAssetId());
        List<CollectInterfaces> oldInterfaces = oldInterfacesList.stream()
                .filter(obj -> obj.getPortIndex().equals(item.getPortIndex())).collect(Collectors.toList());

        if (oldInterfaces.size() != 0) {
            CollectInterfaces oldInterface = oldInterfaces.get(0);
            // 计算
            BigDecimal errorCodeIn = new BigDecimal(item.getErrorCodeIn())
                    .subtract(new BigDecimal(oldInterface.getErrorCodeInCount()));
            BigDecimal errorCodeOut = new BigDecimal(item.getErrorCodeOut())
                    .subtract(new BigDecimal(oldInterface.getErrorCodeOutCount()));
            BigDecimal discardPacketsIn = new BigDecimal(item.getDiscardPacketsIn())
                    .subtract(new BigDecimal(oldInterface.getDiscardPacketsInCount()));
            BigDecimal discardPacketsOut = new BigDecimal(item.getDiscardPacketsOut())
                    .subtract(new BigDecimal(oldInterface.getDiscardPacketsOutCount()));
            copy.setErrorCodeIn(errorCodeIn.longValue());
            copy.setErrorCodeOut(errorCodeOut.longValue());
            copy.setDiscardPacketsIn(discardPacketsIn.longValue());
            copy.setDiscardPacketsOut(discardPacketsOut.longValue());

            Long oldportIn = oldInterface.getPortInCount();
            Long oldportOut = oldInterface.getPortOutCount();

            if (Objects.isNull(oldportIn)) {
                oldportIn = 0L;
            }
            if (Objects.isNull(oldportOut)) {
                oldportOut = 0L;
            }

            long portIn = new BigDecimal(copy.getPortIn()).subtract(new BigDecimal(oldportIn)).longValue();
            long portOut = new BigDecimal(copy.getPortOut()).subtract(new BigDecimal(oldportOut)).longValue();

            copy.setPortIn(portIn < 0 ? 0 : portIn);
            copy.setPortOut(portOut < 0 ? 0 : portOut);
        }
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.INTERFACE;
    }


    /**
     * 触发阈值事件
     *
     * @param item
     */
    private void addEvent(CollectInterfaces item, ThresholdSectionEnum type, String collectValue,
                          String eventUniqueCode, Long portInOrOurSpeed) {

        if (Objects.isNull(collectValue)) {
            return;
        }

        String assetId = item.getAssetId();
        String lockKey = eventUniqueCode + "_" + item.getAssetId() + "_" + item.getPortIndex();
        synchronized (lockKey.intern()) {
            VerifyThresholdReq req = new VerifyThresholdReq();
            req.setAssetId(assetId);
            req.setHaveSection(true);
            req.setType(type);
            //CollectValue = 占带宽的百分比
            req.setCollectValue(collectValue);
            req.setFlag(item.getPortName());
            if (Objects.nonNull(portInOrOurSpeed)) {
                //换算单位   流入流出率换算成 kbps单位
                BigDecimal portInOrOurSpeedBig = new BigDecimal(portInOrOurSpeed).divide(new BigDecimal(8000), 2, BigDecimal.ROUND_HALF_UP);
                req.setPortIntOrOutSpeed(portInOrOurSpeedBig.doubleValue());
            }
            VerifyThresholdResp verifyResp = thresholdServ.verifyThreshold(req);

            if (Objects.isNull(verifyResp)) {
                return;
            }

            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setOriginalMsg(verifyResp.getMsg());

            String cacheKey = CACHE_KEY_STATUS + assetId + ":" + eventUniqueCode + ":" + item.getPortIndex();
            Object cacheFlag = redisService.get(cacheKey);
            if (verifyResp.getAlarmStatus()) {
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                if (Objects.nonNull(cacheFlag)) {
                    return;
                }
                redisService.set(cacheKey, cacheKey);
            } else {
                if (Objects.isNull(cacheFlag)) {
                    return;
                }
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                redisService.remove(cacheKey);
            }

            eventReq.setAssetId(assetId);
            eventReq.setBaseValue(verifyResp.getBaseValue());
            eventReq.setCollectValue(collectValue);
            eventReq.setUniqueCode(eventUniqueCode);
            eventReq.setFlag(item.getPortName());
            eventReq.setGroupFlag(EventGroupConstant.INTERFACES_TH);
            eventReq.setCreateTime(item.getCollectTime());

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理端口阈值事件异常：{}", e.getMessage(), e);
            }
        }

    }

    /**
     * 处理采集参数是否触发告警
     *
     * @param copy
     */
    private void disposeThresholdCompare(CollectInterfaces copy) {

        String rxPower = copy.getRxPower();
        String txPower = copy.getTxPower();

        // 发送光功率
        if (Objects.nonNull(txPower)) {
            addEvent(copy, ThresholdSectionEnum.SEND_POWER, txPower, EventUniqueCode.THRE_INTERFACES_SEND_POWER, null);
        }
        if (Objects.nonNull(rxPower)) {
            // 接收光功率
            addEvent(copy, ThresholdSectionEnum.RECEIVE_POWER, rxPower, EventUniqueCode.THRE_INTERFACES_RECEIVE_POWER, null);
        }

        ThresholdAssetVo threshold = thresholdServ.findAssetThreshold(copy.getAssetId());
        if (Objects.isNull(threshold)) {
            log.debug(AppLogUtils.logStr(AppLogHead.DISPOSE_INTERFACE_SERVICE, "阈值配置空，未进行阈值比较",
                    "assetid:" + copy.getAssetId()));
            return;
        }

        String collectLosePacketsRateOut = new BigDecimal(copy.getLosePacketsOutRate())
                .divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP).toString();
        String collectLosePacketsRateIn = new BigDecimal(copy.getLosePacketsInRate())
                .divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP).toString();
        String collectCodErroOut = new BigDecimal(copy.getErroCodeOutRate())
                .divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP).toString();
        String collectCodeErroIn = new BigDecimal(copy.getErroCodeInRate())
                .divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP).toString();

        // 发送丢包率-阈值
        addEvent(copy, ThresholdSectionEnum.SEND_LOSE, collectLosePacketsRateOut,
                EventUniqueCode.THRE_INTERFACES_SEND_LOSE_UNIQUE_CODE, null);
        // 接收丢包率-阈值
        addEvent(copy, ThresholdSectionEnum.RECEIVE_LOSE, collectLosePacketsRateIn,
                EventUniqueCode.THRE_INTERFACES_RECEIVE_LOSE_UNIQUE_CODE, null);
        // 接收误码率-阈值
        addEvent(copy, ThresholdSectionEnum.RECEIVE_ERROR_CODE, collectCodeErroIn,
                EventUniqueCode.THRE_INTERFACES_RECEIVE_ERROR_UNIQUE_CODE, null);
        // 发送误码率-阈值
        addEvent(copy, ThresholdSectionEnum.SEND_ERROR_CODE, collectCodErroOut,
                EventUniqueCode.THRE_INTERFACES_SEND_ERROR_UNIQUE_CODE, null);
        // 带宽
        Long portSpeed = copy.getPortSpeed();
        //端口流入每秒速率 bit/s
        Long portInSpeed = copy.getPortInSpeed();
        //端口流入每秒速率 bit/s
        Long portOutSpeed = copy.getPortOutSpeed();

        //带宽 单位为B   乘8之后变成bit  与portIn 单位相同
        BigDecimal speedBig = new BigDecimal(portSpeed).multiply(new BigDecimal(8));

        BigDecimal portInSpeedBig = new BigDecimal(portInSpeed);
        if (!BigDecimal.ZERO.equals(portInSpeedBig) && !BigDecimal.ZERO.equals(speedBig)) {

            BigDecimal portInRate = portInSpeedBig.divide(speedBig, 2, BigDecimal.ROUND_HALF_DOWN);
            // 端口流入率
            addEvent(copy, ThresholdSectionEnum.PORT_IN, portInRate.toString(),
                    EventUniqueCode.THRE_INTERFACES_PORT_IN_UNIQUE_CODE, portInSpeed);
        }
        BigDecimal portOutSpeedBig = new BigDecimal(portOutSpeed);
        if (!BigDecimal.ZERO.equals(portOutSpeedBig) && !BigDecimal.ZERO.equals(speedBig)) {
            BigDecimal portOutRate = portOutSpeedBig.divide(speedBig, 2, BigDecimal.ROUND_HALF_DOWN);
            // 端口流出率
            addEvent(copy, ThresholdSectionEnum.PORT_OUT, portOutRate.toString(),
                    EventUniqueCode.THRE_INTERFACES_PORT_OUT_UNIQUE_CODE, portOutSpeed);
        }
    }


}

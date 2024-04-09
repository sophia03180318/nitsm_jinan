package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectNetworkCardBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.collect.controller.route.bean.RouteMsg;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理网卡采集数据
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeNetAdapterImpl implements CollectAdapter {

    public static final String DEFAULT_VALUE_STR = "--";
    private static final String NET_CARD_CACHE = "NET_CARD_DOWN_CACHE:";

    private static Map<String, String> CACHE_MAP = new HashMap<String, String>();

    @Resource
    private RedisService redisService;
    @Resource
    private CollectNetworkCardService networkService;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private AssetLinkAssetService assetLinkAssetServ;
    @Resource
    private AssetHidConfService hidConfServ;

    /**
     * 网卡数据处理
     */
    @Override
    public void dispose(JSONArray data) {
        List<CollectNetworkCardBean> netList = JSONUtil.toList(data, CollectNetworkCardBean.class);
        String collectCode = MyIdUtil.getId();

        List<CollectNetworkCard> collectResultList = new ArrayList<CollectNetworkCard>();
        List<String> entityNameList = new ArrayList<String>();

        String assetId = netList.get(0).getAssetId();
        List<CollectNetworkCard> lastLog = networkService.getRealTimeData(assetId);
        if (Objects.isNull(lastLog)) {
            lastLog = new ArrayList<CollectNetworkCard>();
        }

        for (CollectNetworkCardBean item : netList) {
            if (StrUtil.isEmpty(item.getAssetId()) || StrUtil.isEmpty(item.getCollectTime())
                    || StrUtil.isEmpty(item.getName()) || Objects.isNull(item.getStatus())) {
                continue;
            }

            if(StrUtil.isEmpty(item.getIp())||DEFAULT_VALUE_STR.equals(item.getIp())){
                List<CollectNetworkCard> collect = lastLog.stream().filter(p -> p.getName().equals(item.getName()))
                        .collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    item.setIp(collect.get(0).getIp());
                }
            }

            if (StrUtil.isEmpty(item.getIp()) || DEFAULT_VALUE_STR.equals(item.getIp())) {
                if(!item.getName().contains("组")||item.getName().contains("WFP")||item.getName().contains("QoS")){
                    continue;
                }
            }

            Date date = new Date();
            date.setTime(Long.parseLong(item.getCollectTime()));

            CollectNetworkCard net = EntityBeanUtil.copy(item, CollectNetworkCard.class);
            net.setId(MyIdUtil.getId());
            net.setCollectTime(date);
            net.setCollectCode(collectCode);
            if(StrUtil.isNotEmpty(net.getMacAddress())){
                net.setMacAddress(net.getMacAddress().toLowerCase());
            }

            collectResultList.add(net);
            entityNameList.add(net.getName());

            if (CollectNetCardStatus.UP.getCode().equals(item.getStatus())
                    || CollectNetCardStatus.DOWN.getCode().equals(item.getStatus())) {
                //查询网卡是否配置了禁用
                QueryWrapper<AssetHidConf> queryWrapper = new QueryWrapper<AssetHidConf>();
                queryWrapper.eq("asset_id",assetId);
                queryWrapper.eq("flag",net.getName());
                queryWrapper.eq("type",AssetHidConf.TypeEnum.NET_CARD.name());
                List<AssetHidConf> list = hidConfServ.list(queryWrapper);

                if(list.isEmpty()){
                    addEvent(net);
                }
            }
        }

        if (collectResultList.isEmpty()) {
            return;
        }

        // 批量更新采集数据
        networkService.updateBatchByAssetId(collectResultList);
        // 更新缓存信息
        networkService.updateRealTimeData(collectResultList);

    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.NET_CARD;
    }

    /**
     * 触发事件
     */
    void addEvent(CollectNetworkCard item) {
        String assetId = item.getAssetId();
        String lockKey = item.getAssetId() + item.getName();
        synchronized (lockKey.intern()) {
            CreateEventReq eventReq = new CreateEventReq();

            boolean compare = CollectNetCardStatus.DOWN.getCode().equals(item.getStatus());

            String cacheKey = NET_CARD_CACHE + lockKey;
            // 二次验证
            Object object = redisService.get(cacheKey);

            if (compare) {
                // 断
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = String.format("网卡：%s，状态发生中断", item.getName());
                eventReq.setOriginalMsg(format);

                if (Objects.nonNull(object)) {
                    return;
                }
                redisService.set(cacheKey, cacheKey);
            } else {
                if (Objects.isNull(object)) {
                    return;
                }
                CACHE_MAP.put(cacheKey, cacheKey);

                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("网卡：%s，状态正常", item.getName());
                eventReq.setOriginalMsg(format);

                redisService.remove(cacheKey);
            }

            AssetLinkAsset linkAsset = assetLinkAssetServ.findAssetByLinkAsset(assetId, item.getIp());

            eventReq.setAssetId(assetId);
            eventReq.setUniqueCode(EventUniqueCode.NETWORK_UP_DOWN_UNIQUE_CODE);
            eventReq.setFlag(item.getName());
            eventReq.setGroupFlag(EventGroupConstant.NET_WORK);
            eventReq.setCreateTime(item.getCollectTime());
            eventReq.setBaseValue("未识别设备");
            if(Objects.nonNull(linkAsset)){
                eventReq.setBaseValue(assetLinkAssetServ.formatMsg(linkAsset.getAssetId(),linkAsset.getPortIp()));
            }

            try {
                // log.info("上送网卡数据" + assetId + "=" + JSONUtil.toJsonStr(eventReq));
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理网卡事件异常：{}", e.getMessage(), e);
            }
        }
    }

}

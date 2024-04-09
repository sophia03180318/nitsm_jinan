package com.jcca.web.collect.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.dao.CollectInterfacesMapper;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.graph.vo.StatisticsInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 硬件端口信息
 *
 * @author Lvyp
 */
@Service
public class CollectInterfacesServiceImpl extends ServiceImpl<CollectInterfacesMapper, CollectInterfaces>
        implements CollectInterfacesService {

    private static final String INTERFACE_KEY = "INTERFACE:TAB:KEY:";
    private static final String INTERFACE_ALARM = "INTERFACE:ALARM:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectInterfacesMapper interfaceMapper;
    @Resource
    private AssetService assetService;

    @Override
    public void updateRealTimeData(List<CollectInterfaces> datas) {
        CollectInterfaces interfaces = datas.get(0);
        String key = INTERFACE_KEY + interfaces.getAssetId();

        String toJson = new Gson().toJson(datas);

        redisService.set(key, toJson);
    }

    @Override
    public String getAlarmCode(CollectInterfaces interfaces, String type) {
        return INTERFACE_ALARM + interfaces.getPortIndex() + "ASSETID" + interfaces.getAssetId() + type;
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectInterfaces> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectInterfaces> list = this.list(query);
        for (CollectInterfaces collect : list) {
            Date collectTime = collect.getCollectTime();
            if (Objects.nonNull(collectTime)) {
                calendar.setTime(collectTime);
                calendar.add(Calendar.HOUR_OF_DAY, -hour);
                Date time = calendar.getTime();
                query = Wrappers.query();
                query.eq("asset_id", collect.getAssetId());
                query.lt("collect_time", time);
                this.remove(query);
            }
        }
        return true;
    }

    @Override
    public List<CollectInterfaces> getRealTimeData(String assetId) {
        Assert.isTrue(StrUtil.isNotEmpty(assetId), "assetid must be not null");
        List<CollectInterfaces> data = interfaceMapper.selectRealTimeData(assetId);
        return data;
    }

    @Override
    public Map<Integer, Boolean> getDbInterfacesStatus(String assetId) {
        List<CollectInterfaces> data = interfaceMapper.selectRealTimeData(assetId);
        Map<Integer, Boolean> map = new HashMap<>();
        for (CollectInterfaces port : data) {
            map.put(port.getPortIndexRank(), InterfaceStatus.isUp(port.getStatus()));
        }
        return map;
    }

    @Override
    public List<CollectInterfaces> statistics(Date startDate, Date endDate) {

        return interfaceMapper.statisticsGroupByAsset(startDate, endDate);
    }

    @Override
    public CollectInterfaces findInterfaceByLinkIp(String remoteIp) {
        // TODO Auto-generated method stub
        List<CollectInterfaces> ipList = interfaceMapper.findByLinkIp(remoteIp);
        if (ipList.isEmpty()) {
            return null;
        }
        return ipList.get(0);
    }

    @Override
    public List<StatisticsInfoVo> selectLinePortIn(String assetId, String portName) {

        return interfaceMapper.selectLinePortIn(assetId, portName, 30);
    }

    @Override
    public List<StatisticsInfoVo> selectLinePortOut(String assetId, String portIndex) {

        return interfaceMapper.selectLinePortOut(assetId, portIndex, 30);
    }

    @Override
    public CollectInterfaces findByAssetIdAndPortRank(String assetId, String portIndexRank) {
        CollectInterfaces item = interfaceMapper.selectByAssetIdAndPortRank(assetId, portIndexRank);

        return item;
    }

    @Override
    public Double getPortFlow(String assetId, String portIndex, Integer type) {
        List<StatisticsInfoVo> portLog = new ArrayList<StatisticsInfoVo>();
        if (type > 0) {
            portLog = interfaceMapper.selectLinePortIn(assetId, portIndex, 3);
        } else {
            portLog = interfaceMapper.selectLinePortOut(assetId, portIndex, 3);
        }
        if (portLog.size() != 2) {
            return 0.0;
        }

        StatisticsInfoVo newLog = portLog.get(0);
        StatisticsInfoVo oldLog = portLog.get(1);

        long between = DateUtil.between(newLog.getEndDate(), oldLog.getEndDate(), DateUnit.SECOND);
        BigDecimal divide = BigDecimal.valueOf(newLog.getValue()).divide(new BigDecimal(between), BigDecimal.ROUND_HALF_UP,
                2);
        return divide.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public List<CollectInterfaces> filterPort(String assetId) {
        List<CollectInterfaces> portList = new ArrayList<>();
        List<CollectInterfaces> realTimeData = this.getRealTimeData(assetId);
        List<Integer> portType = Arrays.asList(6, 18, 22);
        Asset asset = assetService.getById(assetId);
        for (CollectInterfaces item : realTimeData) {
            if (!portType.contains(item.getPortType())) {
                continue;
            }
            if (!assetService.isStationAsset(assetId) && !AssetModeConst.B24.equals(asset.getAssetImage())) {
                if ( ObjectUtil.isNull(item.getPortLinkType()) || 1 != item.getPortLinkType()){
                    continue;
                }
            }
            portList.add(item);
        }
        return portList;
    }

    @Override
    public List<CollectInterfaces> selectByAssetAndPortName(String assetId, String portName) {
        return interfaceMapper.selectByAssetAndPortName(assetId,portName);
    }

    @Override
    public String getPortNameByFullName(String assetId, String portFullName) {
        List<String> names = interfaceMapper.selectPortNameByFullName(assetId, portFullName);
        if (!names.isEmpty()) {
            return names.get(0);
        }
        return "";
    }

    @Override
    public List<StatisticsInfoVo> selectLinePortDbmIn(String assetId, String portName) {

        return interfaceMapper.selectLinePortDbmIn(assetId, portName, 30);
    }

    @Override
    public List<StatisticsInfoVo> selectLinePortDbmOut(String assetId, String portName) {

        return interfaceMapper.selectLinePortDbmOut(assetId, portName, 30);
    }

}

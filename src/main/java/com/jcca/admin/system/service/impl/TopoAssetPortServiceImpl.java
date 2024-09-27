package com.jcca.admin.system.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.TopoAssetPortMapper;
import com.jcca.admin.system.entity.TopoAssetPort;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.dao.CollectInterfacesMapper;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.graph.vo.TopoPortInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yu_chen
 * @date 2020-08-18 13:53
 **/
@Service
public class TopoAssetPortServiceImpl extends ServiceImpl<TopoAssetPortMapper, TopoAssetPort>
        implements TopoAssetPortService {
    @Resource
    private TopoAssetPortMapper topoAssetPortMapper;
    @Resource
    private CollectInterfacesMapper collectInterfacesMapper;
    @Resource
    private CollectInterfacesService interServ;
    @Resource
    private AssetService assetService;
    public static final Integer DOWN_STATUS = 2;
    public static final Integer UNKONW_STATUS = 0;
    /**
     * 拓扑图状态维护锁池
     */
    public static Map<String, Object> lockPool = new ConcurrentHashMap<>();

    @Override
    public List<AssetPortVo> selectPortListByAsset(String assetId) {
        Asset asset = assetService.getById(assetId);
        List<AssetPortVo> port = null;
        if (assetService.isStationAsset(assetId) || AssetModeConst.B24.equals(asset.getAssetImage())) {
            port = topoAssetPortMapper.selectPort2(assetId);
        } else {
            port = topoAssetPortMapper.selectPort(assetId);
            if (Objects.isNull(port) || port.isEmpty()) {
                port = topoAssetPortMapper.selectPort2(assetId);
            }
        }

        // 查询状态一小时之前是断则置为灰色
        for (AssetPortVo item : port) {
            Date updateDate = item.getUpdateDate();
            if (DOWN_STATUS.equals(item.getStatus()) && Objects.nonNull(updateDate)) {

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, -1);
                Date time = calendar.getTime();
                long between = DateUtil.between(updateDate, time, DateUnit.MS, false);
                if (between > 0) {
                    item.setStatus(UNKONW_STATUS);
                }
            }

        }
        return port;
    }

    @Override
    public List<AssetPortVo> selectAssetPort(String assetId, String pcbId) {
        List<AssetPortVo> assetPortVos = topoAssetPortMapper.selectAssetPort(assetId, pcbId, "1");
        if(assetPortVos.isEmpty()){
            //可能是光交
            assetPortVos = topoAssetPortMapper.selectAssetPort(assetId, pcbId, "0");
        }

        return assetPortVos;
    }

    @Override
    public List<AssetPortVo> selectAssetPort2(String assetId, String pcbId) {
        return topoAssetPortMapper.selectAssetPort2(assetId, pcbId);
    }

    @Override
    public List<AssetPortVo> selectAssetPortQuery(String assetId) {
        Asset asset = assetService.getById(assetId);
        if (assetService.isStationAsset(assetId) || AssetModeConst.B24.equals(asset.getAssetImage())) {
            return topoAssetPortMapper.selectAssetPortQuery(assetId);
        } else {
            List<AssetPortVo> assetPortVos = topoAssetPortMapper.selectAssetPortQuery2(assetId);
            if (Objects.isNull(assetPortVos) || assetPortVos.isEmpty()) {
                assetPortVos = topoAssetPortMapper.selectAssetPortQuery(assetId);
            }
            return assetPortVos;
        }

    }

    @Override
    public List<AssetPortVo> selectAssetAllPort(String assetId) {
        return topoAssetPortMapper.selectAssetAllPort(assetId);
    }

    @Override
    public List<AssetPortVo> selectAssetPortVlan(String assetId) {
        return topoAssetPortMapper.selectAssetPortVlan(assetId);
    }

    @Override
    public Boolean deleteAssetPort(String assetId, String pcbId) {
        QueryWrapper<TopoAssetPort> query = new QueryWrapper<>();
        query.eq("ASSET_ID", assetId);
        if (StrUtil.isNotEmpty(pcbId)) {
            query.eq("PCB_ID", pcbId);
        }
        int delete = topoAssetPortMapper.delete(query);
        return true;
    }

    @Override
    public TopoPortInfoVo selectPortIndex(String assetId, String portName) {
        List<CollectInterfaces> realTimeData = interServ.getRealTimeData(assetId);
        if (Objects.isNull(realTimeData) || realTimeData.isEmpty()) {
            return null;
        }
        return collectInterfacesMapper.selectPortIndex(assetId, portName, realTimeData.get(0).getCollectCode());
    }

    @Override
    public void updatePortStatus(String assetId, String portIndex, Integer status) {
        String lockkey = assetId;
        lockPool.putIfAbsent(lockkey, new Object());
        Object lock = lockPool.get(lockkey);
        synchronized (lock) {
            QueryWrapper<TopoAssetPort> query = Wrappers.query();
            query.eq("asset_id", assetId);
            query.eq("port_index", portIndex);
            List<TopoAssetPort> list = this.list(query);
            for (TopoAssetPort one : list) {
                if (Objects.nonNull(one) && !one.getStatus().equals(status)) {
                    one.setStatus(status);
                    one.setUpdateDate(new Date());
                    this.updateById(one);
                }
            }
        }
    }

    @Override
    public List<AssetPortVo> querySonList(String portIndex, String assetId) {
        return topoAssetPortMapper.selectSonList(portIndex, assetId);
    }

    @Override
    public List<AssetPortVo> querySonetSonList(String portIndex, String assetId) {
        return topoAssetPortMapper.querySonetSonList(portIndex, assetId);
    }

    /**
     * 查询资产有无配置拓扑图端口
     *
     * @param assetId   资产ID
     * @param portIndex 端口索引
     * @return
     */
    @Override
    public TopoAssetPort findAssetPort(String assetId, String portIndex) {
        List<TopoAssetPort> assetPort = topoAssetPortMapper.findAssetPort(assetId, portIndex);
        if(assetPort.isEmpty()){
            return null;
        }

        return assetPort.get(0);

    }

}
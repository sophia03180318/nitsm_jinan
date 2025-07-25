package com.jcca.web.collect.service.impl.topoDiscovery;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.constant.AssetManufacturerNameFlagConst;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.controller.route.bean.AssetLinkConst;
import com.jcca.web.collect.dao.CollectNetworkCardMapper;
import com.jcca.web.collect.dao.CollectRouteMapper;
import com.jcca.web.collect.entity.CollectRoute;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectTopoDiscoveryService;
import com.jcca.web.collect.service.bean.*;
import com.jcca.web2.service.AssetManufacturerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CollectTopoDiscoveryHandlerService extends ServiceImpl<CollectRouteMapper, CollectRoute> {
    @Resource
    private CollectTopoDiscoveryService collectTopoDiscoveryService;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectNetworkCardMapper collectNetMapper;
    @Resource
    private CollectRouteMapper collectRouteMapper;
    @Resource
    private AssetLinkAssetService linkAssetService;
    @Resource
    private AssetManufacturerService assetManufacturerService;

    public static boolean discoveryRunnig = false;

    public boolean getDiscoverStatus() {
        return discoveryRunnig;
    }

    //是否开启网络拓扑发现
    public Boolean isOpenDiscovery() {
        QueryWrapper<SysModuleConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("NAME", "config:netdiscovery");
        List<SysModuleConfig> list = configService.list(queryWrapper);
        try {
            return list.get(0).getValue().equals("open");
        } catch (Exception e) {
            return false;
        }

    }


    // 查询网络设备相关信息
    public String getAllNetAssetInfo() {
        if (discoveryRunnig) {
            return "拓扑发现正在执行";
        }
        try {
            discoveryRunnig = true;
            log.info("--------------------拓扑发现开始执行--------------------");
            List<Asset> list = assetServ.getCenterNetworklList();// 获取所有网络设备
            Map<String, Object> assetContainIp = new HashMap<>();
            Map<String, Object> assetALLMap = new HashMap<>();
            Map<String, String> assetAllArpInfo = new HashMap<>();
            for (Asset asset : list) {
                // 获取资产的端口信息（获取此设备端口上或者vlan上所有配置的信息）
                Map<String, AssetIpInfoVo> listAssetIp = null;
                Map<String, Object> assetPort = null;
                Map<String, Object> mapAssetVlan = null;
                Map<String, List<BridgeVo>> bridgeMap = null;
                Map<String, Object> interfaces = (Map<String, Object>) collectTopoDiscoveryService.interfaceInfo(asset);
                if (interfaces != null) {
                    listAssetIp = (Map<String, AssetIpInfoVo>) interfaces.get("assetIp") == null ? new HashMap<>()
                            : (Map<String, AssetIpInfoVo>) interfaces.get("assetIp");
                    assetContainIp.putAll(listAssetIp);
                    assetPort = (Map<String, Object>) interfaces.get("assetPort") == null ? new HashMap<>()
                            : (Map<String, Object>) interfaces.get("assetPort");
                    mapAssetVlan = (Map<String, Object>) interfaces.get("assetVlan") == null ? new HashMap<>()
                            : (Map<String, Object>) interfaces.get("assetVlan");
                    interfaces.remove("assetIp");
                } else {
                    continue;
                }
                // 获取资产的Arp信息
                log.info("--------------------拓扑发现：开始查找设备" + asset.getIp() + "arp关系--------------------");
                DicoveryRelatedInfo arp = (DicoveryRelatedInfo) collectTopoDiscoveryService.arpInfo(asset, assetAllArpInfo);
                for (int i = 0; i < 3; i++) {
                    if (arp == null) {
                        arp = (DicoveryRelatedInfo) collectTopoDiscoveryService.arpInfo(asset, assetAllArpInfo);
                    } else {
                        break;
                    }
                }
                if (arp != null) {
                    Map<String, List<TargetInfoVo>> arpMap = arp.getMap();
                    if (arpMap != null) {
                        Iterator<String> iterator1 = arpMap.keySet().iterator();
                        while (iterator1.hasNext()) {
                            String next = (String) iterator1.next();
                            List<TargetInfoVo> listArp = arpMap.get(next);
                            // 看看这个arp查到的信息是否再vlan上
                            AssetTargetInfoVo assetTargetInfoVo = (AssetTargetInfoVo) mapAssetVlan.get(next);
                            if (assetTargetInfoVo == null) {
                                // 如果不再vlan上则是在端口上
                                assetTargetInfoVo = (AssetTargetInfoVo) assetPort.get(next);

                            }
                            if (assetTargetInfoVo != null) {
                                String localPortIp = assetTargetInfoVo.getLocalPortIp();
                                // 除下与本身相同的IP//// 直接排除type=3的静态IP就行
                                if (listArp != null && listArp.size() > 0) {
                                    List<TargetInfoVo> listVo = listArp.stream()
                                            .filter(item -> !(item.getTargetPortIp() != null
                                                    && item.getTargetPortIp().equals(localPortIp)))
                                            .collect(Collectors.toList());
                                    // 本地设备对应的对端设备端口信息
                                    assetTargetInfoVo.setTargetInfoVos(listVo);
                                }
                            }

                        }
                    }
                }

                ArrayList<String> manufacturerList = new ArrayList<>();
                manufacturerList.addAll(AssetManufacturerNameFlagConst.IBM);
                manufacturerList.addAll(AssetManufacturerNameFlagConst.H3C);
                manufacturerList.addAll(AssetManufacturerNameFlagConst.HUA_WEI);
                manufacturerList.addAll(AssetManufacturerNameFlagConst.YAN_HUA);

                DicoveryRelatedInfo route = null;
                if (assetManufacturerService.isManufacturer(manufacturerList, asset.getManufacturerId())) {
                    for (int i = 0; i < 3; i++) {
                        if (route == null) {
                            route = (DicoveryRelatedInfo) collectTopoDiscoveryService.routeInfo(asset);
                        } else {
                            break;
                        }
                    }
                }
                if (route != null) {
                    Map<String, List<TargetInfoVo>> routeMap = route.getMap();
                    mapHanler(routeMap, assetPort, mapAssetVlan, "ROUTE");
                }
                // cisco设备的CDP协议 查询较为详细 用来补充刚ARP查询中对端的详细信息
                DicoveryRelatedInfo cdp = null;
                if (assetManufacturerService.isManufacturer(AssetManufacturerNameFlagConst.CISCO, asset.getManufacturerId())) {
                    log.info("--------------------拓扑发现：开始查找设备" + asset.getIp() + "CDP关系");
                    cdp = (DicoveryRelatedInfo) collectTopoDiscoveryService.cdpInfo(asset);
                    if (cdp != null) {
                        Map<String, List<TargetInfoVo>> cdpMap = cdp.getMap();
                        mapHanler(cdpMap, assetPort, mapAssetVlan, "CDP");
                    }
                }
                assetALLMap.put(asset.getIp(), interfaces);

                for (int i = 0; i < 3; i++) {
                    if (mapAssetVlan != null && mapAssetVlan.keySet() != null) {
                        if (assetManufacturerService.isManufacturer(AssetManufacturerNameFlagConst.CISCO, asset.getManufacturerId())) {
                            Iterator<String> iterator1 = mapAssetVlan.keySet().iterator();
                            while (iterator1.hasNext()) {
                                String next = (String) iterator1.next();
                                // 设备的vlan
                                AssetTargetInfoVo assetVlan = (AssetTargetInfoVo) mapAssetVlan.get(next);
                                BridgeRelatedInfo bridgeRelatedInfo = null;
                                bridgeRelatedInfo = (BridgeRelatedInfo) collectTopoDiscoveryService.bridgeInfo(asset,
                                        assetVlan.getLocalPortIndexName());
                                log.info("--------------------拓扑发现：开始查找设备" + asset.getIp()
                                        + "bridge关系--------------------");
                                bridgeMap = this.bridgeHanler(bridgeRelatedInfo, assetVlan, assetPort, bridgeMap,
                                        assetContainIp, assetALLMap);

                            }
                        } else {
                            BridgeRelatedInfo bridgeRelatedInfo = assetManufacturerService.isManufacturer(AssetManufacturerNameFlagConst.H3C, asset.getManufacturerId()) ?
                                    (BridgeRelatedInfo) collectTopoDiscoveryService.h3CbridgeInfo(asset) : (BridgeRelatedInfo) collectTopoDiscoveryService.huaWeibridgeInfo(asset);
                            Iterator<String> iterator1 = mapAssetVlan.keySet().iterator();
                            while (iterator1.hasNext()) {
                                String next = (String) iterator1.next();
                                // 设备的vlan
                                AssetTargetInfoVo assetVlan = (AssetTargetInfoVo) mapAssetVlan.get(next);
                                log.info("--------------------拓扑发现：开始查找设备" + asset.getIp()
                                        + "bridge关系--------------------");
                                bridgeMap = this.bridgeHanler(bridgeRelatedInfo, assetVlan, assetPort, bridgeMap,
                                        assetContainIp, assetALLMap);
                            }
                        }
                        if (bridgeMap != null) {
                            break;
                        }
                    }
                }
            }
            this.setIPInfo(assetALLMap, assetAllArpInfo);
            this.setPortChannel(assetALLMap);
            this.setPcInfo(assetALLMap, assetContainIp);

        } catch (Exception e) {
            log.error("拓扑发现失败", e);
        } finally {
            log.info("--------------------拓扑发现执行结束--------------------");
            discoveryRunnig = false;
        }

        // 保存对端连接信息
        try {
            Executor executor = (Executor) SpringContextUtil.getBean("transferDataExecutor");
            executor.execute(() -> {
                try {
                    linkAssetService.saveLinkAsset(AssetLinkConst.AUTO_SNIFFER); // 自动发现
                } catch (Exception e) {
                    log.error("异步执行对端设备保存失败", e);
                }
            });
        } catch (Exception e) {
            log.error("通过自动发现保存设备连接信息异常", e);
        }

        return "拓扑发现执行完成";

    }


    public void setPortChannel(Map<String, Object> assetALLMap) {
        Iterator<String> iterator = assetALLMap.keySet().iterator();
        while (iterator.hasNext()) {
            String next = (String) iterator.next();
            //获取设备的端口的所有信息
            Map<String, Object> assetAllPortInfo = (Map<String, Object>) assetALLMap.get(next);
            Map<String, Object> assetPort = (Map<String, Object>) assetAllPortInfo.get("assetPort");
            ArrayList<AssetTargetInfoVo> assetTargetInfoVosPortChannel = (ArrayList<AssetTargetInfoVo>) assetAllPortInfo.get("portChannel");
            for (AssetTargetInfoVo portChannelAssetTarget : assetTargetInfoVosPortChannel) {
                //获取portchannel对应的真实
                AssetTargetInfoVo portAssetTarget = (AssetTargetInfoVo) assetPort.get(portChannelAssetTarget.getPortChannelTargetIndex());
                if (portAssetTarget.getTargetInfoVos() == null) {
                    log.info("port-channel对应本地端口为空：" + next + ",端口索引：" + portAssetTarget.getLocalPortIndex() + "端口名称：" + portAssetTarget.getLocalPortIndexName());
                } else {
                    this.portChannelHanler(portChannelAssetTarget, portAssetTarget);
                }
            }

        }

    }

    public void setIPInfo(Map<String, Object> assetALLMap, Map<String, String> assetAllArpInfo) {
        Iterator<String> iterator = assetALLMap.keySet().iterator();
        while (iterator.hasNext()) {
            String next = (String) iterator.next();
            //获取设备的端口的所有信息
            Map<String, Object> assetAllPortInfo = (Map<String, Object>) assetALLMap.get(next);
            //获取设备的实体端口信息（assetPort）里存的是每个端口所对应的对端信息（可能包含多个对端信息）
            Map<String, Object> assetPort = (Map<String, Object>) assetAllPortInfo.get("assetPort");
            Iterator<String> iteratorPort = assetPort.keySet().iterator();
            while (iteratorPort.hasNext()) {
                String portIndex = (String) iteratorPort.next();
                //获取端口的对端信息
                AssetTargetInfoVo assetTargetInfoVo = (AssetTargetInfoVo) assetPort.get(portIndex);
                List<TargetInfoVo> targetInfoVos = assetTargetInfoVo.getTargetInfoVos();
                if (targetInfoVos != null && targetInfoVos.size() > 0) {
                    List listVo = targetInfoVos.stream().filter(
                            item -> (item.getCollectType() != null && (item.getCollectType().equals("CDP") || item.getCollectType().equals("ROUTE"))))
                            .collect(Collectors.toList());
                    //如果过滤有CDP或者ROUTER的对端设备，直接取CDP就行了，说明对端设备是确定的
                    if (listVo != null && listVo.size() > 0) {
                        assetTargetInfoVo.setTargetInfoVos(listVo);
                        targetInfoVos = listVo;
                    }
                    for (TargetInfoVo targetInfoVo : targetInfoVos) {
                        if (targetInfoVo.getTargetMac() != null && targetInfoVo.getTargetPortIp() == null) {
                            if (targetInfoVo.getCollectType() == null) {
                                targetInfoVo.setCollectType("arp");
                            }
                            targetInfoVo.setTargetPortIp(assetAllArpInfo.get(targetInfoVo.getTargetMac()));
                        }

                    }
                }
            }

        }

    }


    /**
     * 拓扑发现寻找主机设备信息
     *
     * @param assetALLMap
     */
    public void setPcInfo(Map<String, Object> assetALLMap, Map<String, Object> assetContainIp) {
        log.info("--------------------拓扑发现：开始查找网络设备与硬件设备关系--------------------");
        // 将主机设备转化成map形式
        List<CollectRoute> collectRoutes = new ArrayList<>();
        List<CollectNetworkCardVo> list = collectNetMapper.getRealTimeDataAllCenterPc();
        Map<String, AssetIpPcInfoVo> mapPc = new HashMap<>();
        for (CollectNetworkCardVo collectNetworkCardVo : list) {
            AssetIpPcInfoVo assetIpPcInfoVo = new AssetIpPcInfoVo();
            assetIpPcInfoVo.setAssetIp(collectNetworkCardVo.getAssetIp());
            assetIpPcInfoVo.setAssetType(AssetModeConst.SERVER);
            assetIpPcInfoVo.setPortIp(collectNetworkCardVo.getIp());
            assetIpPcInfoVo.setAssetName(collectNetworkCardVo.getAssetName());
            assetIpPcInfoVo.setMac(collectNetworkCardVo.getMacAddress());
            assetIpPcInfoVo.setPortName(collectNetworkCardVo.getName());
            assetIpPcInfoVo.setAssetId(collectNetworkCardVo.getAssetId());
            mapPc.put(collectNetworkCardVo.getIp(), assetIpPcInfoVo);
        }


        Iterator<String> iterator = assetALLMap.keySet().iterator();
        while (iterator.hasNext()) {//迭代所有的网络设备
            String next = (String) iterator.next();
            //获取设备的端口的所有信息
            Map<String, Object> assetAllPortInfo = (Map<String, Object>) assetALLMap.get(next);
            //获取设备的实体端口信息（assetPort）里存的是每个端口所对应的对端信息（可能包含多个对端信息）
            Map<String, Object> assetPort = (Map<String, Object>) assetAllPortInfo.get("assetPort");
            Iterator<String> iteratorPort = assetPort.keySet().iterator();
            while (iteratorPort.hasNext()) {
                String portIndex = (String) iteratorPort.next();
                //获取端口的对端信息
                AssetTargetInfoVo assetTargetInfoVo = (AssetTargetInfoVo) assetPort.get(portIndex);
                List<TargetInfoVo> targetInfoVos = assetTargetInfoVo.getTargetInfoVos();
                if (targetInfoVos == null || targetInfoVos.size() == 0) {//无对端信息
                    CollectRoute collectRoute = this.setBaseInfo(assetTargetInfoVo, portIndex);
                    collectRoutes.add(collectRoute);
                } else {//如果有对端设备信息
                    List<CollectRoute> noNetWorkAssetList = new ArrayList<CollectRoute>();//对端设备中没有网络设备
                    List<CollectRoute> netWorkAssetList = new ArrayList<CollectRoute>();//对端设备中存在网络设备
                    for (TargetInfoVo targetInfoVo : targetInfoVos) {
                        // 本地端口中本身含有对端端口信息，直接添加
                        if (targetInfoVo.getTargetPort() != null && !"".equals(targetInfoVo.getTargetPort())) {
                            AssetIpInfoVo assetIpInfoVo = (AssetIpInfoVo) assetContainIp.get(targetInfoVo.getTargetPortIp());
                            if (assetIpInfoVo != null) {
                                CollectRoute collectRoute = this.setBaseInfo(assetTargetInfoVo, portIndex);
                                collectRoute = this.setTargetBaseInfo(collectRoute, targetInfoVo);
                                collectRoute.setAtName(assetIpInfoVo.getAssetName());
                                collectRoute.setAtType(assetIpInfoVo.getAssetType());
                                collectRoute.setAtAssetId(targetInfoVo.getTargetAssetId() == null ? assetIpInfoVo.getAssetId() : targetInfoVo.getTargetAssetId());
                                collectRoute.setAtPortIndexName(targetInfoVo.getTargetPort());
                                //加mac
                                collectRoute.setAtPhysAddress(assetIpInfoVo.getMac());

                                netWorkAssetList.add(collectRoute);
                            }
                        } else {//如果本地端口中，只有对端设备的IP，那么就需要查找对端设备
                            AssetIpPcInfoVo assetIpPcInfoVo = mapPc.get(targetInfoVo.getTargetPortIp());
                            if (assetIpPcInfoVo != null) {//查找到对端设备为主机设备
                                CollectRoute collectRoute = this.setBaseInfo(assetTargetInfoVo, portIndex);
                                collectRoute.setAtName(assetIpPcInfoVo.getAssetName());
                                collectRoute.setAtType(assetIpPcInfoVo.getAssetType());
                                collectRoute.setAtAssetId(assetIpPcInfoVo.getAssetId());
                                //加mac
                                collectRoute.setAtPhysAddress(assetIpPcInfoVo.getMac());
                                noNetWorkAssetList.add(collectRoute);
                            } else {
                                AssetIpInfoVo assetIpInfoVo = (AssetIpInfoVo) assetContainIp.get(targetInfoVo.getTargetPortIp());
                                if (assetIpInfoVo != null) {
                                    //查找到对端设备为网络设备
                                    String assetIpTarget = assetIpInfoVo.getAssetIp();
                                    Map<String, Object> mapTarget = (Map<String, Object>) assetALLMap.get(assetIpTarget);
                                    if (mapTarget != null) {
                                        //对端的端口信息
                                        Map<String, Object> mapTargetPort = (Map<String, Object>) mapTarget.get("assetPort");
                                        mapTargetPort.forEach((key, value) -> {
                                            AssetTargetInfoVo vo = (AssetTargetInfoVo) value;
                                            boolean contain = false;
                                            //如果对端设备的端口IP直接和本地端口的对端IP信息相同
                                            if (vo.getLocalPortIp() != null && vo.getLocalPortIp().equals(targetInfoVo.getTargetPortIp())) {
                                                contain = true;//找到对端端口
                                            } else if (vo.getTargetInfoVos() != null) {//（对端端口）的对端端口信息中查找是否可以匹配
                                                List<TargetInfoVo> infoVos = vo.getTargetInfoVos();
                                                for (TargetInfoVo vot : infoVos) {
                                                    //对端端口信息中含有本地端口的ip地址
                                                    if (vot.getTargetPortIp() != null && vot.getTargetPortIp().equals(assetTargetInfoVo.getLocalPortIp())) {
                                                        contain = true;
                                                        //对端端口信息中含有本地端口的mac地址
                                                    } else if (vot.getTargetMac() != null && vot.getTargetMac().equals(assetTargetInfoVo.getLocalPortMac())) {
                                                        contain = true;
                                                    } else if (vot.getTargetPortIp() != null && (vot.getTargetPortIp().equals(assetTargetInfoVo.getAssetIp()) || vot.getTargetPortIp().equals(assetTargetInfoVo.getLocalPortIp()))) {
                                                        contain = true;
                                                    }
                                                }
                                            }
                                            if (contain) {
                                                CollectRoute collectRoute = this.setBaseInfo(assetTargetInfoVo, portIndex);
                                                collectRoute.setAtPortIndexName(vo.getLocalPortIndexName());
                                                collectRoute.setAtAssetId(assetIpInfoVo.getAssetId());
                                                collectRoute.setAtName(assetIpInfoVo.getAssetName());
                                                //加mac
                                                collectRoute.setAtPhysAddress(assetIpInfoVo.getMac());
                                                netWorkAssetList.add(collectRoute);
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                    if (netWorkAssetList.size() > 0) {
                        collectRoutes.addAll(netWorkAssetList);
                    } else {
                        collectRoutes.addAll(noNetWorkAssetList);
                    }
                }
            }
        }
        collectRouteMapper.deleteAll();
        this.saveBatch(collectRoutes);
    }

    public CollectRoute setBaseInfo(AssetTargetInfoVo assetTargetInfoVo, String nextTarget) {
        CollectRoute collectRoute = new CollectRoute();
        collectRoute.setCreateDate(new Date());
        collectRoute.setAssetId(assetTargetInfoVo.getLocalAssetId());
        collectRoute.setPortIndexRank(nextTarget);
        collectRoute.setPortIndexName(assetTargetInfoVo.getLocalPortIndexName());
        collectRoute.setPortIp(assetTargetInfoVo.getLocalPortIp());
        collectRoute.setPortMacAddress(assetTargetInfoVo.getLocalPortMac());
        return collectRoute;

    }

    public CollectRoute setTargetBaseInfo(CollectRoute collectRoute, TargetInfoVo targetInfoVo) {
        collectRoute.setAtPhysAddress(targetInfoVo.getTargetMac());
        collectRoute.setAtNetAddress(targetInfoVo.getTargetPortIp());
        collectRoute.setPortOrgName(targetInfoVo.getBelongVlan());
        collectRoute.setAtPortIndexName(targetInfoVo.getTargetPort());
        collectRoute.setRemark(targetInfoVo.getCollectType());
        return collectRoute;

    }

    // 找寻对端端口实体信息
    public AssetTargetInfoVo searchTargetInfo(Map<String, Object> assetContainIp, Map<String, Object> assetALLMap,
                                              String targetPortIp) {
        AssetIpInfoVo assetIpINfoVo = (AssetIpInfoVo) assetContainIp.get(targetPortIp);
        if (assetIpINfoVo != null) {
            String assetIp = assetIpINfoVo.getAssetIp();
            Map<String, Object> map = (Map<String, Object>) assetALLMap.get(assetIp);
            if (map != null) {
                Map<String, Object> mapAssetport = (Map<String, Object>) map.get("assetPort");
                if (mapAssetport != null) {
                    AssetTargetInfoVo assetTargetInfoVo = (AssetTargetInfoVo) mapAssetport
                            .get(assetIpINfoVo.getPortIndex());
                    return assetTargetInfoVo;
                }
            }
        }
        return null;

    }

    public void mapHanler(Map<String, List<TargetInfoVo>> map, Map<String, Object> assetPort,
                          Map<String, Object> mapAssetVlan, String collectType) {
        if (map != null) {
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String next = (String) iterator.next();
                List<TargetInfoVo> listCDPorRoute = map.get(next);
                AssetTargetInfoVo assetTargetInfoVo = (AssetTargetInfoVo) mapAssetVlan.get(next);
                if (assetTargetInfoVo == null) {
                    assetTargetInfoVo = (AssetTargetInfoVo) assetPort.get(next);
                }


                if (assetTargetInfoVo != null) {
                    List<TargetInfoVo> targetInfoVos = assetTargetInfoVo.getTargetInfoVos();
                    String localPortIp = assetTargetInfoVo.getLocalPortIp();
                    // 除去与本身相同的IP
                    List<TargetInfoVo> listVo = listCDPorRoute.stream().filter(
                            item -> !(item.getTargetPortIp() != null && item.getTargetPortIp().equals(localPortIp)))
                            .collect(Collectors.toList());
                    if (targetInfoVos == null) {
                        assetTargetInfoVo.setTargetInfoVos(listVo);
                    } else {

                        List<TargetInfoVo> newTargetInfo = new ArrayList<>();
                        for (TargetInfoVo info : listVo) {//CDP或者ROUTE找到的对端设备
                            boolean contain = false;
                            for (TargetInfoVo targetInfoVo : targetInfoVos) {//本身端口已经有的对端设备
                                if (targetInfoVo.getTargetPortIp().equals(info.getTargetPortIp())) {//本身就有的IP信息
                                    if (info.getTargetPort() != null && !"".equals(info.getTargetPort())) {
                                        targetInfoVo.setTargetPort(info.getTargetPort());
                                    }
                                    targetInfoVo.setCollectType(collectType);//CDP协议或者route协议匹配上已经有的信息，填充对端端口名称信息
                                    contain = true;
                                }
                            }
                            if (!contain) {
                                newTargetInfo.add(info);
                            }
                        }
                        assetTargetInfoVo.getTargetInfoVos().addAll(newTargetInfo);
                    }
                }
            }
        }
    }

    /**
     * @param portChannel portchannel端口
     * @param realPort    portchannel对应的真实端口
     *                    将portchannel的对端信息付给真实的端口上
     */
    public void portChannelHanler(AssetTargetInfoVo portChannel, AssetTargetInfoVo realPort) {
        if (realPort.getTargetInfoVos() == null || realPort.getTargetInfoVos().size() == 0) {
            realPort.setTargetInfoVos(portChannel.getTargetInfoVos());
        } else {
            List<TargetInfoVo> list = new ArrayList<>();
            List<TargetInfoVo> portChannelTargetList = portChannel.getTargetInfoVos();
            List<TargetInfoVo> realPortTargetList = realPort.getTargetInfoVos();
            //--------------------------------修改--------------------------------
            if (Objects.isNull(portChannelTargetList)) {
                portChannelTargetList = new ArrayList<TargetInfoVo>();
            }
            if (Objects.isNull(realPortTargetList)) {
                realPortTargetList = new ArrayList<TargetInfoVo>();
            }
            for (TargetInfoVo portChannelTarget : portChannelTargetList) {
                for (TargetInfoVo realPortTarget : realPortTargetList) {
                    //关联设备端口的IP地址
                    String targetPortIp = portChannelTarget.getTargetPortIp();
                    //关联设备的Mac地址
                    String targetMac = portChannelTarget.getTargetMac();
                    String targetPort = portChannelTarget.getTargetPort();
                    String belongVlan = portChannelTarget.getBelongVlan();
                    if ((targetPortIp != null && targetPortIp.equals(realPortTarget.getTargetPortIp()))
                            || (targetMac != null && targetMac.equals(realPortTarget.getTargetMac()))
                            || (targetPort != null && targetPort.equals(realPortTarget.getTargetPort()))) {
                        if (realPortTarget.getTargetPortIp() == null) {
                            realPortTarget.setTargetPortIp(targetPortIp);
                        }
                        if (realPortTarget.getTargetMac() == null) {
                            realPortTarget.setTargetMac(targetMac);
                        }
                        if (realPortTarget.getTargetPort() == null) {
                            realPortTarget.setTargetPort(targetPort);
                        }
                        if (realPortTarget.getBelongVlan() == null) {
                            realPortTarget.setBelongVlan(belongVlan);
                        }
                    } else {
                        list.add(portChannelTarget);
                    }
                }
            }
            if (list.size() > 0) {
                realPortTargetList.addAll(list);
            }
        }
    }

    /**
     * @param assetContainIp
     * @param assetALLMap
     * @param listVo
     * @param assetTargetInfoVo 本地端口对端设备
     */
    public void setTargetInfo(Map<String, Object> assetContainIp, Map<String, Object> assetALLMap,
                              List<TargetInfoVo> listVo, AssetTargetInfoVo assetTargetInfoVo) {

        for (TargetInfoVo targetInfoVo : listVo) {
            // 获得真实端口信息
            AssetTargetInfoVo target = this.searchTargetInfo(assetContainIp, assetALLMap,
                    targetInfoVo.getTargetPortIp());
            if (target != null) {
                // 本地对端设备放入对端设备端口信息
                targetInfoVo.setTargetPort(target.getLocalPortIndexName());
                targetInfoVo.setTargetAssetIp(target.getAssetIp());
                targetInfoVo.setTargetAssetId(target.getLocalAssetId());
                // 对端设备对应本地端口的信息
                List<TargetInfoVo> targetInfoVoAsset = target.getTargetInfoVos();
                // 对端的本地信息
                for (TargetInfoVo targetLocal : targetInfoVoAsset) {
                    if (targetLocal.getTargetPortIp() != null
                            && (targetLocal.getTargetPortIp().equals(assetTargetInfoVo.getLocalPortIp())
                            || targetLocal.getTargetPortIp().equals(assetTargetInfoVo.getAssetIp()) || targetLocal.getTargetPortIp().equals(targetInfoVo.getTargetPortIp()))) {
                        if (assetTargetInfoVo.getLocalPortIndexName() != null && targetLocal.getTargetPort() == null) {
                            targetLocal.setTargetPort(assetTargetInfoVo.getLocalPortIndexName());
                            targetLocal.setCollectType(targetLocal.getCollectType() + "Change");
                        }

                        targetLocal.setTargetAssetIp(assetTargetInfoVo.getAssetIp());
                        targetLocal.setTargetAssetId(assetTargetInfoVo.getLocalAssetId());
                    }
                }
            }
        }
    }

    public Map<String, List<BridgeVo>> bridgeHanler(BridgeRelatedInfo bridgeRelatedInfo, AssetTargetInfoVo assetVlan,
                                                    Map<String, Object> assetPort, Map<String, List<BridgeVo>> bridgeMap, Map<String, Object> assetContainIp,
                                                    Map<String, Object> assetALLMap) {
        if (bridgeRelatedInfo != null && bridgeRelatedInfo.getMap() != null) {// bridgeRelatedInfo 会获得此vlan对应实体端口通信的对端信息
            Iterator<String> iteratorBridge = bridgeRelatedInfo.getMap().keySet().iterator();

            while (iteratorBridge.hasNext()) {
                String portIndex = (String) iteratorBridge.next();
                List<BridgeVo> values = bridgeRelatedInfo.getMap().get(portIndex);
                if (values == null) {
                    continue;
                }
                List<TargetInfoVo> targetInfoVoList = assetVlan.getTargetInfoVos();
                if (targetInfoVoList == null) {
                    targetInfoVoList = new ArrayList<TargetInfoVo>();
                    assetVlan.setTargetInfoVos(targetInfoVoList);
                }

                for (BridgeVo value : values) {
                    if (value == null) {
                        continue;
                    }
                    List<TargetInfoVo> listVo = targetInfoVoList.stream().filter(
                            item -> item.getTargetMac() != null && item.getTargetMac().equals(value.getTargetMac()))
                            .collect(Collectors.toList());
                    TargetInfoVo target = null;
                    // vlan中存的target
                    if (listVo != null && listVo.size() > 0) {
                        target = listVo.get(0);
                        if (!(target.getCollectType() != null && target.getCollectType().equals("CDP"))) {
                            target.setCollectType("bridge");
                        }
                        target.setBelongVlanIndex(assetVlan.getLocalPortIndex());
                        target.setBelongVlan(value.getBelongVlan());
                    } else {
                        target = new TargetInfoVo();
                        target.setTargetMac(value.getTargetMac());
                        targetInfoVoList.add(target);
                    }
                    // 填充端口信息,将vlan中的对端信息填充到实际的端口上
                    AssetTargetInfoVo assetportTarget = (AssetTargetInfoVo) assetPort.get(portIndex);
                    if (assetportTarget == null) {
                        break;
                    }
                    List<TargetInfoVo> list1 = assetportTarget.getTargetInfoVos();
                    if (list1 == null) {
                        list1 = new ArrayList<>();
                        list1.add(target);
                        assetportTarget.setTargetInfoVos(list1);
                    } else {
                        TargetInfoVo finalTarget = target;
                        List<TargetInfoVo> list2 = list1.stream()
                                .filter(item -> item.getTargetPortIp() != null && item.getTargetPortIp().equals(finalTarget.getTargetPortIp()))
                                .collect(Collectors.toList());
                        if (list2.isEmpty()) {
                            if (!(target.getCollectType() != null && target.getCollectType().equals("CDP"))) {
                                target.setCollectType("bridge");
                            }
                            target.setBelongVlanIndex(assetVlan.getLocalPortIndex());
                            target.setBelongVlan(value.getBelongVlan());
                            target.setTargetMac(value.getTargetMac());// mac地址
                            list1.add(target);
                        } else {
                            TargetInfoVo targetAssetport = list2.get(0);
                            if (!(target.getCollectType() != null && target.getCollectType().equals("CDP"))) {
                                target.setCollectType("bridge");
                            }
                            targetAssetport.setBelongVlanIndex(assetVlan.getLocalPortIndex());
                            targetAssetport.setBelongVlan(value.getBelongVlan());
                            targetAssetport.setTargetMac(value.getTargetMac());// mac地址
                        }
                    }

                    this.setTargetInfo(assetContainIp, assetALLMap, list1, assetportTarget);


                }
                if (bridgeMap == null) {
                    bridgeMap = bridgeRelatedInfo.getMap();
                } else {
                    bridgeMap.putAll(bridgeRelatedInfo.getMap());
                }
            }
        }
        return bridgeMap;
    }
}

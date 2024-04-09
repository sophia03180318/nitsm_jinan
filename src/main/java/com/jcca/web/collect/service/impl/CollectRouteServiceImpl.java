package com.jcca.web.collect.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.collect.controller.route.bean.RouteMsg;
import com.jcca.web.collect.dao.CollectRouteMapper;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.entity.CollectRoute;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.collect.service.bean.TopoRouteVo;
import com.jcca.web.collect.service.bean.VlanPortBean;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 网络路由信息
 *
 * @author lyp
 */
@Slf4j
@Service
public class CollectRouteServiceImpl extends ServiceImpl<CollectRouteMapper, CollectRoute>
        implements CollectRouteService {

    // 索引、物理地址、对端IP attable
    // private static final List<String> MIB_LIST =
    // Arrays.asList("1.3.6.1.2.1.3.1.1.1", "1.3.6.1.2.1.3.1.1.2",
    // "1.3.6.1.2.1.3.1.1.3");

    // 索引、物理地址、对端IP ipNetToMediaIfIndex 、ipNetToMediaType（4代表的是本机的Ip,3代表对端IP）
    private static final List<String> MIB_LIST = Arrays.asList("1.3.6.1.2.1.4.22.1.1", "1.3.6.1.2.1.4.22.1.2",
            "1.3.6.1.2.1.4.22.1.3", "1.3.6.1.2.1.4.22.1.4");

    // 获取vlan下的物理地址和端口
    private static final List<String> VLAN_MIN = Arrays.asList("1.3.6.1.2.1.17.4.3.1.1", "1.3.6.1.2.1.17.4.3.1.2");
    // 获取端口索引
    private static final String VLAN_MIN_GET_PORT_INDEX = "1.3.6.1.2.1.17.1.4.1.2";

    /**
     * vlan 标识
     */
    public static final String VLAN_FLAG = "VL";

    @Resource
    private OutService outServ;
    @Resource
    private CollectInterfacesService interfaceServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectNetworkCardService netWorkServ;
    @Resource
    private CollectRouteMapper collectRouteMapper;

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    @Override
    public void collectRoute(Asset asset) {
        log.info("===========发起对端设备采集========");

        if (Objects.isNull(asset)) {
            log.error("采集路由失败：资产空");
            return;
        }
        Integer assetMode = asset.getAssetMode();
        List<Integer> asList = Arrays.asList(42, 201);

        if (!asList.contains(assetMode)) {
            log.error("仅支持路由器和交换机类型");
            return;
        }

        Map<String, CollectRoute> routeMap = new HashMap<String, CollectRoute>();

        try {
            beginCollectorLink(asset, routeMap);
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            return;
        }

        // 采集结果
        Collection<CollectRoute> collectRouteList = routeMap.values();
        Map<String, List<VlanPortBean>> vlanCacheMap = new HashMap<String, List<VlanPortBean>>();

        // 结果整理
        List<CollectRoute> resultList = new ArrayList<CollectRoute>();

        exeVlanLink(asset, collectRouteList, vlanCacheMap, resultList);

        QueryWrapper<CollectRoute> queryWrapper = new QueryWrapper<CollectRoute>();
        queryWrapper.eq("ASSET_ID", asset.getId());
        List<CollectRoute> dbRouteList = list(queryWrapper);

        // 判断共有的端口
        List<String> collectRoutePortList = resultList.stream().map(CollectRoute::getPortIndexRank)
                .collect(Collectors.toList());
        List<String> dbRoutePortList = dbRouteList.stream().map(CollectRoute::getPortIndexRank)
                .collect(Collectors.toList());
        List<String> commonPort = dbRoutePortList.stream().filter(item -> collectRoutePortList.contains(item))
                .collect(Collectors.toList());

        if (!commonPort.isEmpty()) {
            // 删除共有的交际
            QueryWrapper<CollectRoute> removeWrapper = new QueryWrapper<CollectRoute>();
            removeWrapper.in("PORT_INDEX_RANK", commonPort);
            removeWrapper.eq("ASSET_ID", asset.getId());
            remove(queryWrapper);
        }

        // 处理对端设备信息
        for (CollectRoute route : resultList) {
            route.setId(MyIdUtil.getId());

            // 查看历史时候设置过备注和资产名称
            List<CollectRoute> oldRouteList = dbRouteList.stream()
                    .filter(item -> item.getAssetId().equals(route.getAssetId())
                            && item.getPortIndexRank().equals(route.getPortIndexRank())
                            && (Objects.nonNull(item.getAtPhysAddress()) && item.getAtPhysAddress().equals(route.getAtPhysAddress())))
                    .collect(Collectors.toList());

            if (!oldRouteList.isEmpty()) {
                CollectRoute collectRoute = oldRouteList.get(0);
                route.setRemark(collectRoute.getRemark());
                if (Objects.isNull(route.getAtAssetId()) && StrUtil.isNotEmpty(collectRoute.getAtName())) {
                    route.setAtName(collectRoute.getAtName());
                    route.setAtPortName(collectRoute.getAtPortName());
                }
            }

            Asset linkAsset = null;

            // 通过端口表的端口IP查一下
            CollectInterfaces item = interfaceServ.findInterfaceByLinkIp(route.getAtNetAddress());

            if (Objects.nonNull(item)) {
                linkAsset = assetServ.getById(item.getAssetId());

                if (Objects.nonNull(linkAsset) && !linkAsset.getId().equals(route.getAssetId())) {
                    route.setAtAssetId(linkAsset.getId());
                    route.setAtName(linkAsset.getName());
                    route.setAtType(linkAsset.getAssetMode());
                    route.setAtPortName(item.getPortName());
                    route.setAtPortIndexName(item.getPortIndex());

                    continue;
                }
            }

            // 通过网卡信息匹配
            String atPhysAddress = route.getAtPhysAddress();
            CollectNetworkCard card = netWorkServ.findByMacAddress(atPhysAddress);
            if (Objects.isNull(card)) {
                card = netWorkServ.findByMacAddress(atPhysAddress.toUpperCase());
                if (Objects.nonNull(card)) {
                    route.setAtPhysAddress(atPhysAddress.toUpperCase());
                }
            }
            if (Objects.isNull(card)) {
                card = netWorkServ.findByMacAddress(atPhysAddress.toLowerCase());
                if (Objects.nonNull(card)) {
                    route.setAtPhysAddress(atPhysAddress.toLowerCase());
                }
            }
            if (Objects.nonNull(card)) {
                linkAsset = assetServ.getById(card.getAssetId());
                if (Objects.nonNull(linkAsset) && !linkAsset.getId().equals(route.getAssetId())) {
                    route.setAtAssetId(linkAsset.getId());
                    route.setAtName(linkAsset.getName());
                    route.setAtType(linkAsset.getAssetMode());
                    route.setAtPortName(card.getName());
                    route.setAtPortIndexName(card.getName());
                    continue;
                }
            }

        }

        saveBatch(resultList);

        log.error("===========对端设备采集成功========");
    }

    /**
     * 处理vlan对端物理口信息
     *
     * @param asset
     * @param collectRouteList
     * @param vlanCacheMap
     * @param resultList
     */
    private void exeVlanLink(Asset asset, Collection<CollectRoute> collectRouteList,
                             Map<String, List<VlanPortBean>> vlanCacheMap, List<CollectRoute> resultList) {
        for (CollectRoute item : collectRouteList) {
            // vlan 处理
            CollectInterfaces interfacesEntity = interfaceServ.findByAssetIdAndPortRank(item.getAssetId(),
                    item.getPortIndexRank());

            if (Objects.isNull(interfacesEntity)) {
                continue;
            }

            String interfacePortName = interfacesEntity.getPortName();
            if (StrUtil.isEmpty(interfacePortName)) {
                continue;
            }

            item.setPortIndexName(interfacesEntity.getPortIndex());
            item.setPortName(interfacePortName);
            item.setPortOrgName(interfacePortName);
            item.setPortIp(interfacesEntity.getLinkIp());
            item.setPortMacAddress(interfacesEntity.getLinkPhyAddress());

            String portName = interfacePortName.toUpperCase();
            if (Objects.isNull(interfacesEntity) || !portName.contains(VLAN_FLAG)) {
                resultList.add(item);
                continue;
            }

            List<VlanPortBean> vlanList = vlanCacheMap.get(portName);

            // 处理vlan
            try {
                if (Objects.isNull(vlanList) || vlanList.isEmpty()) {
                    vlanList = exeVlan(asset, portName);
                    vlanCacheMap.put(portName, vlanList);
                }
            } catch (Exception e) {
                log.error("VLAN对应的物理地址查询失败：VLAN信息处理失败，对端设备发现程序终止！" + e.getMessage(), e);
                resultList.add(item);
                continue;
            }
            String atPhysAddress = item.getAtPhysAddress();

            List<VlanPortBean> collect = vlanList.stream().filter(vlan -> vlan.getMacAddress().equals(atPhysAddress))
                    .collect(Collectors.toList());

            if (Objects.isNull(collect) || collect.isEmpty()) {
                log.error("VLAN对应的物理地址查询失败：未发现VLAN对端的物理地址！" + atPhysAddress);
                resultList.add(item);

                continue;
            }
            VlanPortBean vlanPortBean = collect.get(0);

            String portIndex = vlanPortBean.getPortIndex();
            item.setPortIndexRank(portIndex);

            // vlan 对应的端口
            CollectInterfaces vlanInterfaces = interfaceServ.findByAssetIdAndPortRank(item.getAssetId(), portIndex);
            if (Objects.nonNull(vlanInterfaces)) {
                item.setPortName(vlanInterfaces.getPortName());
                item.setPortIndexName(vlanInterfaces.getPortIndex());
            }

            resultList.add(item);
        }
    }

    /**
     * 开始采集
     *
     * @param asset
     * @param routeMap
     * @throws Exception
     */
    private void beginCollectorLink(Asset asset, Map<String, CollectRoute> routeMap) throws Exception {
        for (int i = 0; i < MIB_LIST.size(); i++) {
            String mib = MIB_LIST.get(i);
            BusinessGetSnmpResultReq getReq = new BusinessGetSnmpResultReq();
            getReq.setCommunity(asset.getOsUser());
            getReq.setIp(asset.getIp());
            getReq.setMib("." + mib);
            getReq.setType("WALK");

            BusinessGetSnmpResultResp snmpResult = null;
            log.info("采集路由信息REQ：{}", JSONUtil.toJsonStr(getReq));
            snmpResult = outServ.getSnmpResult(getReq);
            log.info("采集路由信息RESP：{}", JSONUtil.toJsonStr(snmpResult));

            if (!BusinessGetSnmpResultResp.SUNNCESS.equals(snmpResult.getCode())) {
                throw new Exception(snmpResult.getMsg());
            }

            // 采集
            List<SnmpExecuteResult> resultList = snmpResult.getResultList();
            // 处理采集回来的结果
            setValue(asset, routeMap, i, mib, resultList);

        }
    }

    /**
     * 处理采集回来的信息
     *
     * @param asset
     * @param routeMap
     * @param i
     * @param mib
     * @param resultList
     */
    private void setValue(Asset asset, Map<String, CollectRoute> routeMap, int i, String mib,
                          List<SnmpExecuteResult> resultList) {
        for (SnmpExecuteResult item : resultList) {
            String oid = item.getOid();
            String value = item.getValue();

            String mapKey = oid.replace(mib, "");

            CollectRoute collectRoute = routeMap.get(mapKey);
            if (Objects.isNull(collectRoute)) {
                collectRoute = new CollectRoute();
                collectRoute.setAssetId(asset.getId());
                collectRoute.setCreateDate(new Date());
            }

            if (i == 0) {
                collectRoute.setPortIndexRank(value);
                routeMap.put(mapKey, collectRoute);
            } else if (i == 1) {
                collectRoute.setAtPhysAddress(value);
                routeMap.put(mapKey, collectRoute);
            } else if (i == 2) {
                collectRoute.setAtNetAddress(value);
                routeMap.put(mapKey, collectRoute);
            } else if (i == 3) {
                if (!"3".equals(value)) {
                    routeMap.remove(mapKey);
                }
            }

        }
    }

    /**
     * 获取vlan 下的物理端口索引和对端Mac地址
     *
     * @param asset
     * @param vlanName
     * @throws Exception
     */
    private List<VlanPortBean> exeVlan(Asset asset, String vlanName) throws Exception {

        String vlanId = vlanName.toUpperCase().replace(VLAN_FLAG, "");
        Map<String, VlanPortBean> cacheMap = new HashMap<String, VlanPortBean>();

        for (int i = 0; i < VLAN_MIN.size(); i++) {
            String mib = VLAN_MIN.get(i);

            BusinessGetSnmpResultReq getReq = new BusinessGetSnmpResultReq();
            getReq.setCommunity(asset.getOsUser() + "@" + vlanId);
            getReq.setIp(asset.getIp());
            getReq.setMib("." + mib);
            getReq.setType("WALK");

            BusinessGetSnmpResultResp snmpResult = null;

            log.info("vlan信息查询REQ：" + JSONUtil.toJsonStr(getReq));
            snmpResult = outServ.getSnmpResult(getReq);
            log.info("vlan信息查询RESP：" + JSONUtil.toJsonStr(snmpResult));

            if (!BusinessGetSnmpResultResp.SUNNCESS.equals(snmpResult.getCode())
                    || snmpResult.getResultList().isEmpty()) {
                // 重新再采集一下
                getReq.setCommunity(asset.getOsUser());
                log.info("vlan信息重新发起查询REQ：" + JSONUtil.toJsonStr(getReq));
                snmpResult = outServ.getSnmpResult(getReq);
                log.info("vlan信息重新发起RESP：" + JSONUtil.toJsonStr(snmpResult));
            }

            if (!BusinessGetSnmpResultResp.SUNNCESS.equals(snmpResult.getCode())) {
                throw new Exception(snmpResult.getMsg());
            }

            List<SnmpExecuteResult> resultList = snmpResult.getResultList();

            for (SnmpExecuteResult result : resultList) {
                String oid = result.getOid();
                String value = result.getValue();
                String[] split = oid.split("\\.");

                String key = split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                        + "." + split[split.length - 1];

                if (i == 0) {
                    // MAC地址列表
                    VlanPortBean vlanPort = new VlanPortBean();
                    vlanPort.setMacAddress(value.toLowerCase());
                    vlanPort.setVlanId(vlanId);

                    cacheMap.put(key, vlanPort);
                } else if (i == 1) {
                    // MAC对应的序号
                    VlanPortBean vlanPortBean = cacheMap.get(key);
                    if (Objects.nonNull(vlanPortBean)) {
                        vlanPortBean.setRank(value);
                        cacheMap.put(key, vlanPortBean);
                    }
                }
            }
        }

        Collection<VlanPortBean> valnPortList = cacheMap.values();

        // 采集最后一项指标
        BusinessGetSnmpResultReq getReq = new BusinessGetSnmpResultReq();
        getReq.setCommunity(asset.getOsUser() + "@" + vlanId);
        getReq.setIp(asset.getIp());
        getReq.setMib("." + VLAN_MIN_GET_PORT_INDEX);
        getReq.setType("WALK");

        BusinessGetSnmpResultResp snmpResult = null;

        log.info("vlan查询index信息REQ：" + JSONUtil.toJsonStr(getReq));
        snmpResult = outServ.getSnmpResult(getReq);
        log.info("vlan查询index信息RESP：" + JSONUtil.toJsonStr(snmpResult));

        if (!BusinessGetSnmpResultResp.SUNNCESS.equals(snmpResult.getCode()) || snmpResult.getResultList().isEmpty()) {
            getReq.setCommunity(asset.getOsUser());
            log.info("重新发起vlan查询index信息REQ：" + JSONUtil.toJsonStr(getReq));
            snmpResult = outServ.getSnmpResult(getReq);
            log.info("重新发起vlan查询index信息RESP：" + JSONUtil.toJsonStr(snmpResult));
        }
        if (!BusinessGetSnmpResultResp.SUNNCESS.equals(snmpResult.getCode())) {
            throw new Exception(snmpResult.getMsg());
        }

        List<VlanPortBean> respList = new ArrayList<VlanPortBean>();

        List<SnmpExecuteResult> resultList = snmpResult.getResultList();

        for (SnmpExecuteResult vlanPortBean : resultList) {
            String oid = vlanPortBean.getOid();
            String value = vlanPortBean.getValue();
            String[] split = oid.split("\\.");
            String rank = split[split.length - 1];

            List<VlanPortBean> resp = valnPortList.stream().filter(item -> item.getRank().equals(rank))
                    .collect(Collectors.toList());
            if (resp.isEmpty()) {
                // 用value 匹配一下，发现华为的直接出来就是端口索引，不用多走一部
                resp = valnPortList.stream().filter(item -> item.getRank().equals(value)).collect(Collectors.toList());
            }
            if (resp.isEmpty()) {
                continue;
            }

            for (VlanPortBean vlanPort : resp) {
                vlanPort.setPortIndex(value);
                respList.add(vlanPort);
            }

        }

        return respList;
    }

    @Override
    public List<CollectRoute> queryCollectRoute(String assetId) {
        QueryWrapper<CollectRoute> queryWrapper = new QueryWrapper<CollectRoute>();
        queryWrapper.eq("ASSET_ID", assetId);
        queryWrapper.isNotNull("AT_ASSET_ID");
        return list(queryWrapper);
    }

    @Override
    public RouteMsg queryRouteMsg(String assetId, String macAddr) {
        QueryWrapper<CollectRoute> queryWrapper = new QueryWrapper<CollectRoute>();
        queryWrapper.eq("AT_PHYS_ADDRESS", macAddr);
        List<CollectRoute> list = list(queryWrapper);
        if (Objects.isNull(list) || list.isEmpty()) {
            return null;
        }

        List<CollectNetworkCard> cardItem = netWorkServ.selectByMacAddressAndAssetId(macAddr, assetId);
        if (cardItem.isEmpty()) {
            return null;
        }
        CollectNetworkCard card = cardItem.get(0);

        CollectRoute route = list.get(0);

        Asset localAsset = assetServ.getById(assetId);
        Asset remoteAsset = assetServ.getById(route.getAssetId());

        CollectInterfaces interfaceItem = interfaceServ.findByAssetIdAndPortRank(route.getAssetId(),
                route.getPortIndexRank());

        RouteMsg msg = new RouteMsg();
        msg.setLocalAsset(localAsset);
        msg.setLocalPort(card.getName());
        msg.setPortStatus(card.getStatus());
        if (Objects.nonNull(interfaceItem)) {
            msg.setRemotePort(interfaceItem.getPortIndex());
        }
        msg.setRemoteAsset(remoteAsset);
        msg.setPortIndexRank(route.getPortIndexRank());
        return msg;
    }

    @Override
    public List<TopoRouteVo> queryTopoRoute(String assetId) {
        return collectRouteMapper.getRouteMsg(assetId);
    }

    /**
     * 查找对端信息
     *
     * @param assetId
     * @return
     */
    @Override
    public List<AssetLinkAssetVo> findAtAssetAndPort(String assetId) {
        return collectRouteMapper.findAtAssetAndPort(assetId);
    }

}

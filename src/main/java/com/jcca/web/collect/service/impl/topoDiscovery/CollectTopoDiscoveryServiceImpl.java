package com.jcca.web.collect.service.impl.topoDiscovery;

import com.jcca.common.enums.StatusEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectTopoDiscoveryService;
import com.jcca.web.collect.service.bean.*;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CollectTopoDiscoveryServiceImpl implements CollectTopoDiscoveryService {
    @Resource
    private CollectInterfacesService interfaceServ;
    @Resource
    private OutService outServ;
    //arp协议
    //.1.3.6.1.2.1.4.22.1.1    ipNetToMediaIfIndex     该记录对应的接口索引
    //.1.3.6.1.2.1.4.22.1.2    ipNetToMediaPhysAddress   介质依赖的物理地址
    //.1.3.6.1.2.1.4.22.1.3    ipNetToMediaNetAddress    与物理地址对应的IP地址
    //.1.3.6.1.2.1.4.22.1.4    ipNetToMediaType           地址映射类型
    // 在RFC1213-MIB文件中的ip.ipNetToMediaTable.ipNetToMediaEntry
    public static final Map<String, String> ARP_MIB_MAP = new HashMap() {
        {
            put("ipNetToMediaIfIndex", ".1.3.6.1.2.1.4.22.1.1");
            put("ipNetToMediaPhysAddress", ".1.3.6.1.2.1.4.22.1.2");
            put("ipNetToMediaNetAddress", ".1.3.6.1.2.1.4.22.1.3");
            put("ipNetToMediaType", ".1.3.6.1.2.1.4.22.1.4");
        }
    };
    //    CDP协议
    //.1.3.6.1.4.1.9.9.23.1.2.1.1.3    cdpCacheAddressType   类型     ip(1)
    //.1.3.6.1.4.1.9.9.23.1.2.1.1.4     cdpCacheAddress   相连的IP地址  38 01 01 05    56.1.1.5
    //.1.3.6.1.4.1.9.9.23.1.2.1.1.7     cdpCacheDevicePort 相连设备的端口信息  GigabitEthernet1/0/23
    //在CISCO-CDP-MIB 下的CDPcache下
    public static final Map<String, String> CDP_MIB_MAP = new HashMap() {
        {
            put("cdpCacheAddressType", ".1.3.6.1.4.1.9.9.23.1.2.1.1.3");
            put("cdpCacheAddress", ".1.3.6.1.4.1.9.9.23.1.2.1.1.4");
            put("cdpCacheDevicePort", ".1.3.6.1.4.1.9.9.23.1.2.1.1.7");
        }
    };
    //    bridge协议
    //.1.3.6.1.2.1.17.4.3.1.1    dot1dTpFdbAddress   该节点标识接口学习到的MAC地址信息。
    //.1.3.6.1.2.1.17.4.3.1.2     dot1dTpFdbPort     该节点标识学习MAC地址的端口号。
    //.1.3.6.1.2.1.17.4.3.1.3     dot1dTpFdbStatus   该节点标识接口的状态。
    //在BRIDGE-MIB 中的dot1dTpFdbTable
    public static final Map<String, String> BRIDGE_MIB_MAP = new HashMap() {
        {
            put("dot1dTpFdbAddress", ".1.3.6.1.2.1.17.4.3.1.1");
            put("dot1dTpFdbPort", ".1.3.6.1.2.1.17.4.3.1.2");
//            put("dot1dTpFdbStatus", ".1.3.6.1.2.1.17.4.3.1.3");
            put("dot1dBasePortIfIndex", "1.3.6.1.2.1.17.1.4.1.2");

        }
    };

    public static final Map<String, String> BRIDGE_MIB_MAP_H3C = new HashMap() {
        {
            put("dot1dTpFdbPort", ".1.3.6.1.2.1.17.7.1.2.2.1.2");
        }
    };
    //    路由协议
    //.1.3.6.1.2.1.4.21.1.1    ipRouteDest   路由的目标地址
    //.1.3.6.1.2.1.4.21.1.2     ipRouteIfIndex     路由的端口索引
    //.1.3.6.1.2.1.4.21.1.7     ipRouteNextHop   路由的下一跳地址。
    //在RFC1213-mib 中的ipRouteTable
    public static final Map<String, String> ROUTER_MIB_MAP = new HashMap() {
        {
            put("ipRouteDest", ".1.3.6.1.2.1.4.21.1.1");
            put("ipRouteIfIndex", ".1.3.6.1.2.1.4.21.1.2");
            put("ipRouteNextHop", ".1.3.6.1.2.1.4.21.1.7");

        }
    };
    // 在 HAWEI-L2VlanMiB中   查询vlan
    public static final Map<String, String> VLAN_MIB_MAP = new HashMap() {
        {
            put("vmVlan", ".1.3.6.1.4.1.9.9.68.1.2.2.1.2");
        }
    };

    //
    //在cisco-vlan-membership-mib 中的vmMembershipTable
    public static final Map<String, String> HUAWEI_VLAN_MIB_MAP = new HashMap() {
        {
            put("vmVlan", "1.3.6.1.4.1.2011.5.25.42.3.1.1.1.1.6");
        }
    };


    //默认为cisco设备的vlan信息
    @Override
    public Object vlanInfo(Asset asset) {
        VlanInfo vlanInfo = new VlanInfo();
        vlanInfo.setAssetIp(asset.getIp());
        List<SnmpExecuteResult> vmVlan = this.getCollectInfo(VLAN_MIB_MAP.get("vmVlan"), asset, null);
        for (SnmpExecuteResult result : vmVlan) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String portIndex = split[split.length - 1];
            vlanInfo.getMap().put(portIndex, value);
        }
        return vlanInfo;
    }

    @Override
    public Object HuaWeivlanInfo(Asset asset) {
        VlanInfo vlanInfo = new VlanInfo();
        vlanInfo.setAssetIp(asset.getIp());
        List<SnmpExecuteResult> vmVlan = this.getCollectInfo(HUAWEI_VLAN_MIB_MAP.get("vmVlan"), asset, null);
        if (vmVlan == null) {
            return null;
        }
        for (SnmpExecuteResult result : vmVlan) {
            String oid = result.getOid();
            String portIndex = result.getValue();
            String[] split = oid.split("\\.");
            String value = split[split.length - 1];
            if (!portIndex.equals("-1")) {
                vlanInfo.getMap().put(portIndex, value);
            }

        }
        return vlanInfo;
    }

    /**
     * 查询此设备端口信息
     *
     * @param asset
     * @return
     */
    @Override
    public Object interfaceInfo(Asset asset) {
        Map<String, Object> mapAll = null;
        Map<String, Object> map = null;
        Map<String, AssetIpInfoVo> listAssetIp = new HashMap<>();
        List<AssetTargetInfoVo> assetTargetInfoVosPortChannel = new ArrayList<>();
        Map<String, Object> mapAssetVlan = new HashMap<>();
        List<CollectInterfaces> list = interfaceServ.getRealTimeData(asset.getId());

        for (CollectInterfaces collectInterfaces : list) {
            //如果端口状态不正常，跳过,端口只要是亮的就会有mac地址
            if (!(InterfaceStatus.OK.getCode().equals(collectInterfaces.getStatus()) && collectInterfaces.getLinkPhyAddress() != null && !"--".equals(collectInterfaces.getLinkPhyAddress()))) {
                continue;
            }

            if (map == null) {
                map = new HashMap();
            }
            //将索引号，端口信息存入到map中
            AssetTargetInfoVo assetTargetInfoVo = new AssetTargetInfoVo();
            assetTargetInfoVo.setLocalAssetId(asset.getId());
            assetTargetInfoVo.setAssetIp(asset.getIp());
            assetTargetInfoVo.setLocalPortIndex(collectInterfaces.getPortIndexRank().toString());
            assetTargetInfoVo.setLocalPortIndexName(collectInterfaces.getPortIndex());
            if (collectInterfaces.getLinkPhyAddress() != null && !"--".equals(collectInterfaces.getLinkPhyAddress())) {
                assetTargetInfoVo.setLocalPortMac(collectInterfaces.getLinkPhyAddress());
            }
            if (collectInterfaces.getLinkIp() != null && !"--".equals(collectInterfaces.getLinkIp())) {
                assetTargetInfoVo.setLocalPortIp(collectInterfaces.getLinkIp());
            }
            //端口中配置有IP或者vlan中有IP,并且状态是正常的
            if (collectInterfaces.getLinkIp() != null && !"--".equals(collectInterfaces.getLinkIp()) && StatusEnum.OK.getCode().equals(collectInterfaces.getStatus())) {
                AssetIpInfoVo assetIpInfoVo = new AssetIpInfoVo();
                assetIpInfoVo.setPortIp(collectInterfaces.getLinkIp());
                assetIpInfoVo.setMac(collectInterfaces.getLinkPhyAddress());
                assetIpInfoVo.setAssetIp(asset.getIp());
                assetIpInfoVo.setAssetType(asset.getAssetMode());
                assetIpInfoVo.setAssetName(asset.getName());
                assetIpInfoVo.setAssetId(asset.getId());
                assetIpInfoVo.setPortIndex(collectInterfaces.getPortIndexRank().toString());
                assetIpInfoVo.setPortIndexName(collectInterfaces.getPortIndex());
                listAssetIp.put(collectInterfaces.getLinkIp(), assetIpInfoVo);
            }
            //如果是vlan，则放入vlan的map中
            if (collectInterfaces.getPortName().toLowerCase(Locale.ROOT).contains("vlan") || collectInterfaces.getPortName().toLowerCase(Locale.ROOT).contains("vl")) {
                mapAssetVlan.put(collectInterfaces.getPortIndexRank().toString(), assetTargetInfoVo);
                //如果是port-channel的端口
            } else if (collectInterfaces.getPortIndex().toLowerCase(Locale.ROOT).contains("port-channel") || collectInterfaces.getPortIndex().toLowerCase(Locale.ROOT).contains("portchannel")) {
                List list1 = list.stream().filter(item -> (
                        (!item.getPortIndexRank().toString().equals(collectInterfaces.getPortIndexRank().toString()))
                                && collectInterfaces.getLinkPhyAddress().equals(item.getLinkPhyAddress()))).collect(Collectors.toList());
                if (list1 != null && list1.size() > 0) {
                    //port-channel 端口的mac地址会和真实的端口的mac地址相同。将portchannel所对应的实体端口的索引号存储起来
                    CollectInterfaces collectInterfaces1 = (CollectInterfaces) list1.get(0);
                    assetTargetInfoVo.setPortChannelTargetIndex(collectInterfaces1.getPortIndexRank().toString());
                    assetTargetInfoVosPortChannel.add(assetTargetInfoVo);
                }
                map.put(collectInterfaces.getPortIndexRank().toString(), assetTargetInfoVo);
            } else {
                map.put(collectInterfaces.getPortIndexRank().toString(), assetTargetInfoVo);

            }

        }
        //将关联资产的所有ip信息存入到map中
        if (map != null) {
            mapAll = new HashMap<>();
            mapAll.put("assetPort", map);
            mapAll.put("assetIp", listAssetIp);
            mapAll.put("assetVlan", mapAssetVlan);
            mapAll.put("portChannel", assetTargetInfoVosPortChannel);
        }
        return mapAll;
    }


    @Override
    public Object arpInfo(Asset asset, Map<String, String> assetAllArpInfo) {
        DicoveryRelatedInfo dicoveryRelatedInfo = new DicoveryRelatedInfo();
        dicoveryRelatedInfo.setAssetIp(asset.getIp());
        List<SnmpExecuteResult> ipNetToMediaIfIndex = this.getCollectInfo(ARP_MIB_MAP.get("ipNetToMediaIfIndex"), asset, null);
        if (ipNetToMediaIfIndex == null) {
            return null;
        }
        for (SnmpExecuteResult result : ipNetToMediaIfIndex) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            TargetInfoVo targetInfoVo = new TargetInfoVo();
            targetInfoVo.setCollectType("arp");
            targetInfoVo.setTargetPortIp(key);
            if (dicoveryRelatedInfo.getMap().get(value) != null) {//如果存在此数据，就将数据放入map中
                dicoveryRelatedInfo.getMap().get(value).add(targetInfoVo);
            } else {//如果不存在此数据，就新建数据将数据放入map中
                List<TargetInfoVo> list = new ArrayList();
                list.add(targetInfoVo);
                dicoveryRelatedInfo.getMap().put(value, list);
            }
        }
        List<SnmpExecuteResult> ipNetToMediaPhysAddress = this.getCollectInfo(ARP_MIB_MAP.get("ipNetToMediaPhysAddress"), asset, null);
        if (ipNetToMediaPhysAddress == null) {
            return null;
        }
        for (SnmpExecuteResult result : ipNetToMediaPhysAddress) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String index = split[split.length - 5];
            String index1 = split[split.length - 6];
            String key = split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            List<TargetInfoVo> list = dicoveryRelatedInfo.getMap().get(index) == null ? dicoveryRelatedInfo.getMap().get(index1) : dicoveryRelatedInfo.getMap().get(index);
            if (list != null) {
                TargetInfoVo targetInfoVo = list.stream().filter(item -> item.getTargetPortIp().equals(key)).collect(Collectors.toList()).get(0);
                if (targetInfoVo != null) {
                    targetInfoVo.setTargetMac(value);
                    if (assetAllArpInfo.get(targetInfoVo.getTargetMac()) == null) {
                        assetAllArpInfo.put(targetInfoVo.getTargetMac(), targetInfoVo.getTargetPortIp());
                    }
                }
            }
        }
        // List<SnmpExecuteResult> ipNetToMediaNetAddress= this.getCollectInfo(ARP_MIB_MAP.get("ipNetToMediaNetAddress"),asset);
        List<SnmpExecuteResult> ipNetToMediaType = this.getCollectInfo(ARP_MIB_MAP.get("ipNetToMediaType"), asset, null);
        if (ipNetToMediaType == null) {
            return null;
        }
        for (SnmpExecuteResult result : ipNetToMediaType) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String index = split[split.length - 5];
            String index1 = split[split.length - 6];
            String key = split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            List<TargetInfoVo> list = dicoveryRelatedInfo.getMap().get(index) == null ? dicoveryRelatedInfo.getMap().get(index1) : dicoveryRelatedInfo.getMap().get(index);
            TargetInfoVo targetInfoVo = list.stream().filter(item -> item.getTargetPortIp().equals(key)).collect(Collectors.toList()).get(0);
            if (targetInfoVo != null) {
                targetInfoVo.setType(value);

            }
        }
        return dicoveryRelatedInfo;
    }

    @Override
    public Object cdpInfo(Asset asset) {
        DicoveryRelatedInfo dicoveryRelatedInfo = new DicoveryRelatedInfo();
        dicoveryRelatedInfo.setAssetIp(asset.getIp());
        List<SnmpExecuteResult> cdpCacheAddress = this.getCollectInfo(CDP_MIB_MAP.get("cdpCacheAddress"), asset, null);
        if (cdpCacheAddress == null) {
            return null;
        }
        for (SnmpExecuteResult result : cdpCacheAddress) {
            TargetInfoVo targetVo = new TargetInfoVo();
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 2];
            String[] splitTargetIP = null;
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (value.contains(":")) {
                splitTargetIP = value.split("\\:");
            } else {
                splitTargetIP = value.split(" ");
                log.info(asset.getId() + "的CDP1信息：" + value);
            }
            //-----------------------------------------------------------修改
            if (splitTargetIP.length < 4) {
                //targetVo.setTargetPortIp("");
                log.info(asset.getIp() + ":" + asset.getId() + "的CDP1信息：" + value + ",格式不正确");
            } else {
                String targetIp = Integer.parseInt(splitTargetIP[splitTargetIP.length - 4], 16) + "." + Integer.parseInt(splitTargetIP[splitTargetIP.length - 3], 16) + "." + Integer.parseInt(splitTargetIP[splitTargetIP.length - 2], 16) + "." + Integer.parseInt(splitTargetIP[splitTargetIP.length - 1], 16);
                targetVo.setTargetPortIp(targetIp);
            }


            if (dicoveryRelatedInfo.getMap().get(key) != null) {//如果存在此数据，就将数据放入map中
                dicoveryRelatedInfo.getMap().get(key).add(targetVo);
            } else {//如果不存在此数据，就新建数据将数据放入map中
                List<TargetInfoVo> list = new ArrayList();
                list.add(targetVo);
                dicoveryRelatedInfo.getMap().put(key, list);
            }
        }

        List<SnmpExecuteResult> cdpCacheDevicePort = this.getCollectInfo(CDP_MIB_MAP.get("cdpCacheDevicePort"), asset, null);
        if (cdpCacheDevicePort == null) {
            return null;
        }
        for (SnmpExecuteResult result : cdpCacheDevicePort) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 2];
            List<TargetInfoVo> list = dicoveryRelatedInfo.getMap().get(key);
            if (list != null) {
                TargetInfoVo targetInfoVo = list.get(0);
                if (targetInfoVo != null) {
                    targetInfoVo.setTargetPort(value);
                    targetInfoVo.setCollectType("CDP");
                }
            }

        }
        return dicoveryRelatedInfo;
    }

    @Override
    public Object bridgeInfo(Asset asset, String vlanName) {
        Map<String, String> dot1dTpFdbAddressMap = new HashMap<>();
        Map<String, String> dot1dBasePortIfIndexMap = new HashMap<>();

        //正则表达式，用于匹配非数字串，+号用于匹配出多个非数字串
        String regEx = "[^0-9]+";
        Pattern pattern = Pattern.compile(regEx);
        //用定义好的正则表达式拆分字符串，把字符串中的数字留出来
        String[] cs = vlanName == null ? null : pattern.split(vlanName);
        BridgeRelatedInfo bridgeRelatedInfo = new BridgeRelatedInfo();
        bridgeRelatedInfo.setAssetIp(asset.getIp());
        List<SnmpExecuteResult> dot1dTpFdbAddress = this.getCollectInfo(BRIDGE_MIB_MAP.get("dot1dTpFdbAddress"), asset, cs == null ? null : cs[cs.length - 1]);
        if (dot1dTpFdbAddress == null) {
            return null;
        }
        for (SnmpExecuteResult result : dot1dTpFdbAddress) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 6] + "." + split[split.length - 5] + "." + split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            dot1dTpFdbAddressMap.put(key, value);
        }
        List<SnmpExecuteResult> dot1dBasePortIfIndex = this.getCollectInfo(BRIDGE_MIB_MAP.get("dot1dBasePortIfIndex"), asset, cs == null ? null : cs[cs.length - 1]);
        if (dot1dBasePortIfIndex == null) {
            return null;
        }
        for (SnmpExecuteResult result : dot1dBasePortIfIndex) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 1];
            dot1dBasePortIfIndexMap.put(key, value);

        }


        List<SnmpExecuteResult> dot1dTpFdbPort = this.getCollectInfo(BRIDGE_MIB_MAP.get("dot1dTpFdbPort"), asset, cs == null ? null : cs[cs.length - 1]);
        if (dot1dTpFdbPort == null) {
            return null;
        }
        for (SnmpExecuteResult result : dot1dTpFdbPort) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 6] + "." + split[split.length - 5] + "." + split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            String dot1dBasePortIfIndexStr = dot1dBasePortIfIndexMap.get(value);
            if (dot1dBasePortIfIndexStr != null) {
                BridgeVo bridgeVo = new BridgeVo();
                bridgeVo.setBelongVlan(vlanName);
                bridgeVo.setTargetMac(dot1dTpFdbAddressMap.get(key));
                if (bridgeRelatedInfo.getMap().get(dot1dBasePortIfIndexStr) == null) {
                    List<BridgeVo> bridgeVos = new ArrayList<>();
                    bridgeVos.add(bridgeVo);
                    bridgeRelatedInfo.getMap().put(dot1dBasePortIfIndexStr, bridgeVos);
                } else {
                    bridgeRelatedInfo.getMap().get(dot1dBasePortIfIndexStr).add(bridgeVo);
                }
            }

        }
        return bridgeRelatedInfo;
    }

    @Override
    public Object h3CbridgeInfo(Asset asset) {
        BridgeRelatedInfo bridgeRelatedInfo = new BridgeRelatedInfo();
        Map<String, String> dot1dBasePortIfIndexMap = new HashMap<>();

        List<SnmpExecuteResult> dot1dBasePortIfIndex = this.getCollectInfo(BRIDGE_MIB_MAP.get("dot1dBasePortIfIndex"), asset, null);
        if (dot1dBasePortIfIndex == null) {
            return null;
        }
        for (SnmpExecuteResult result : dot1dBasePortIfIndex) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 1];
            dot1dBasePortIfIndexMap.put(key, value);
        }


        List<SnmpExecuteResult> dot1dTpFdbPort = this.getCollectInfo(BRIDGE_MIB_MAP_H3C.get("dot1dTpFdbPort"), asset, null);
        if (dot1dTpFdbPort == null) {
            return null;
        }
        for (SnmpExecuteResult result : dot1dTpFdbPort) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String one = Integer.toHexString(Integer.parseInt(split[split.length - 6]));
            String two = Integer.toHexString(Integer.parseInt(split[split.length - 5]));
            String three = Integer.toHexString(Integer.parseInt(split[split.length - 4]));
            String four = Integer.toHexString(Integer.parseInt(split[split.length - 3]));
            String five = Integer.toHexString(Integer.parseInt(split[split.length - 2]));
            String six = Integer.toHexString(Integer.parseInt(split[split.length - 1]));
            one = one.length() == 1 ? ("0" + one) : one;
            two = two.length() == 1 ? ("0" + two) : two;
            three = three.length() == 1 ? ("0" + three) : three;
            four = four.length() == 1 ? ("0" + four) : four;
            five = five.length() == 1 ? ("0" + five) : five;
            six = six.length() == 1 ? ("0" + six) : six;
            String key = one + ":" +
                    two + ":" +
                    three + ":" +
                    four + ":" +
                    five + ":" +
                    six;
            BridgeVo bridgeVo = new BridgeVo();
            bridgeVo.setBelongVlan(split[split.length - 7]);
            bridgeVo.setTargetMac(key);
            value=dot1dBasePortIfIndexMap.get(value);
            if (bridgeRelatedInfo.getMap().get(value) == null) {
                List<BridgeVo> bridgeVos = new ArrayList<>();
                bridgeVos.add(bridgeVo);
                bridgeRelatedInfo.getMap().put(value, bridgeVos);
            } else {
                bridgeRelatedInfo.getMap().get(value).add(bridgeVo);
            }


        }
        return bridgeRelatedInfo;
    }

    @Override
    public Object huaWeibridgeInfo(Asset asset) {
        Map<String, String> dot1dTpFdbAddressMap = new HashMap<>();
        BridgeRelatedInfo bridgeRelatedInfo = new BridgeRelatedInfo();
        bridgeRelatedInfo.setAssetIp(asset.getIp());
        List<SnmpExecuteResult> dot1dTpFdbAddress = this.getCollectInfo(BRIDGE_MIB_MAP.get("dot1dTpFdbAddress"), asset, null);
        if (dot1dTpFdbAddress == null) {
            return null;
        }
        for (SnmpExecuteResult result : dot1dTpFdbAddress) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 6] + "." + split[split.length - 5] + "." + split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            dot1dTpFdbAddressMap.put(key, value);
        }
        List<SnmpExecuteResult> dot1dTpFdbPort = this.getCollectInfo(BRIDGE_MIB_MAP.get("dot1dTpFdbPort"), asset, null);
        if (dot1dTpFdbPort == null) {
            return null;
        }
        for (SnmpExecuteResult result : dot1dTpFdbPort) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String key = split[split.length - 6] + "." + split[split.length - 5] + "." + split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            if (dot1dTpFdbAddressMap.get(key) != null) {
                BridgeVo bridgeVo = new BridgeVo();
                bridgeVo.setTargetMac(dot1dTpFdbAddressMap.get(key));
                if (bridgeRelatedInfo.getMap().get(value) == null) {
                    List<BridgeVo> bridgeVos = new ArrayList<>();
                    bridgeVos.add(bridgeVo);
                    bridgeRelatedInfo.getMap().put(value, bridgeVos);
                } else {
                    bridgeRelatedInfo.getMap().get(value).add(bridgeVo);
                }

            }


        }
        return bridgeRelatedInfo;
    }

    @Override
    public Object routeInfo(Asset asset) {
        DicoveryRelatedInfo dicoveryRelatedInfo = new DicoveryRelatedInfo();
        dicoveryRelatedInfo.setAssetIp(asset.getIp());
        List<SnmpExecuteResult> ipRouteIfIndex = this.getCollectInfo(ROUTER_MIB_MAP.get("ipRouteIfIndex"), asset, null);
        if (ipRouteIfIndex == null) {
            return null;
        }
        for (SnmpExecuteResult result : ipRouteIfIndex) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String ip = split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            TargetInfoVo targetInfoVo = new TargetInfoVo();
            targetInfoVo.setLocalIp(ip);
            if (dicoveryRelatedInfo.getMap().get(value) != null) {//如果存在此数据，就将数据放入map中
                dicoveryRelatedInfo.getMap().get(value).add(targetInfoVo);
            } else {//如果不存在此数据，就新建数据将数据放入map中
                List<TargetInfoVo> list = new ArrayList();
                list.add(targetInfoVo);
                dicoveryRelatedInfo.getMap().put(value, list);
            }
        }
        List<SnmpExecuteResult> ipRouteNextHop = this.getCollectInfo(ROUTER_MIB_MAP.get("ipRouteNextHop"), asset, null);
        if (ipRouteNextHop == null) {
            return null;
        }
        for (SnmpExecuteResult result : ipRouteNextHop) {
            String oid = result.getOid();
            String value = result.getValue();
            String[] split = oid.split("\\.");
            String ip = split[split.length - 4] + "." + split[split.length - 3] + "." + split[split.length - 2]
                    + "." + split[split.length - 1];
            Map<String, List<TargetInfoVo>> map = dicoveryRelatedInfo.getMap();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                List<TargetInfoVo> targetInfoVos = (List<TargetInfoVo>) entry.getValue();
                for (TargetInfoVo targetInfoVo : targetInfoVos) {
                    if (targetInfoVo.getLocalIp().equals(ip)) {
                        targetInfoVo.setTargetPortIp(value);
                        targetInfoVo.setCollectType("ROUTE");
                    }
                }
            }

        }
        return dicoveryRelatedInfo;
    }

    public List<SnmpExecuteResult> getCollectInfo(String command, Asset asset, String vlanName) {
        BusinessGetSnmpResultReq getReq = new BusinessGetSnmpResultReq();
        if (vlanName != null) {
            getReq.setCommunity(asset.getOsUser() + "@" + vlanName);
        } else {
            getReq.setCommunity(asset.getOsUser());
        }

        getReq.setIp(asset.getIp());
        getReq.setMib(command);
        getReq.setType("WALK");
        try {
            log.info(getReq.toString());
            BusinessGetSnmpResultResp snmpResult = outServ.getSnmpResult(getReq);
            if (!BusinessGetSnmpResultResp.SUNNCESS.equals(snmpResult.getCode())) {
                log.error("设备IP：" + asset.getIp() + ",community:" + getReq.getCommunity() + ",mib:" + command + "采集错误");
                return null;
            }
            // 采集
            List<SnmpExecuteResult> resultList = snmpResult.getResultList();
            return resultList;
        } catch (Exception e) {
            log.error("设备IP：" + asset.getIp() + "," + command + "采集错误", e);
        }
        return null;
    }
}

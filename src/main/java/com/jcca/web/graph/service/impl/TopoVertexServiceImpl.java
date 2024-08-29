package com.jcca.web.graph.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.vo.AssetTargetVo;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.service.CollectSystemTimeService;
import com.jcca.web.graph.dao.TopoVertexMapper;
import com.jcca.web.graph.entity.TopoVertex;
import com.jcca.web.graph.service.TopoVertexService;
import com.jcca.web.graph.vo.TopoPortStatus;
import com.jcca.web.graph.vo.TopoVertexAlarmLevelVo;
import com.jcca.web.graph.vo.TopoVertexAlarmStatusVo;
import com.jcca.web.graph.vo.TopoVertexVo;
import com.jcca.web2.vo.BizTopoCenterVo;
import com.jcca.web2.vo.CabinetTopoDetailVo;
import com.jcca.web2.vo.CabinetTopoVo;
import com.jcca.web2.vo.RoomTopoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:35
 */
@Service
public class TopoVertexServiceImpl extends ServiceImpl<TopoVertexMapper, TopoVertex> implements TopoVertexService {

    @Resource
    private TopoVertexMapper topoVertexMapper;
    @Resource
    private SysOrgService orgServ;
    @Resource
    private CollectSystemTimeService collectSystemTimeService;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private RoomService roomService;


    @Override
    public List<TopoVertexVo> selectNodeByAsset(String nodeType, String orgId) {
        List<TopoVertexVo> list = topoVertexMapper.selectNodeByAsset(nodeType, orgId, Arrays.asList(AssetModeConst.ROUTER,
                AssetModeConst.SWITCH, AssetModeConst.RAID), null);
        return list;
    }

    @Override
    public List<TopoVertexVo> selectNodeByAsset2(String nodeType, String orgId) {
        List<TopoVertexVo> list = topoVertexMapper.selectNodeByAsset2(nodeType, orgId, Arrays.asList(AssetModeConst.ROUTER,
                AssetModeConst.SWITCH, AssetModeConst.RAID, AssetModeConst.SERVER), null);
        return list;
    }

    @Override
    public List<TopoVertexVo> selectNodeByAssetName(String nodeType, String orgId, String name) {
        List<TopoVertexVo> list = topoVertexMapper.selectNodeByAsset(nodeType, orgId, Arrays.asList(AssetModeConst.ROUTER,
                AssetModeConst.SWITCH, AssetModeConst.RAID), name);
        return list;
    }

    @Override
    public List<TopoVertexVo> selectNetworkAssetTopoNodeByAsset(String nodeType, String assetId) {
        return topoVertexMapper.selectNetworkAssetTopoNodeByAsset(assetId);
    }

    @Override
    public List<TopoVertexVo> selectPcTopoNodeByAssetName(String nodeType, String orgId, String name) {
        return topoVertexMapper.selectPcTopoNodeByAsset(nodeType, orgId, AssetModeConst.SERVER, name);
    }

    @Override
    public List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByAsset(String nodeType, String orgId) {
        return topoVertexMapper.selectNodeAlarmLevelByAsset(nodeType, orgId, AssetModeConst.ROUTER,
                AssetModeConst.SWITCH);
    }

    @Override
    public List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByAsset2(String nodeType, String orgId) {
        return topoVertexMapper.selectNodeAlarmLevelByAsset2(nodeType, orgId, AssetModeConst.ROUTER,
                AssetModeConst.SWITCH);
    }

    @Override
    public List<TopoVertexAlarmStatusVo> topoNetWorkVertexStatus(String nodeType, String assetId) {
        return topoVertexMapper.topoNetWorkVertexStatus(nodeType, assetId);
    }

    @Override
    public List<TopoVertexAlarmLevelVo> selectNetWorkTopoNodeAlarmLevelByAsset(String nodeType, String assetId,
                                                                               String orgId) {
        return topoVertexMapper.selectNetWorkTopoNodeAlarmLevelByAsset(nodeType, assetId, orgId);
    }

    @Override
    public List<TopoVertexAlarmLevelVo> selectPcTopoNodeAlarmLevelByAsset(String nodeType, String orgId) {
        List<TopoVertexAlarmLevelVo> topoVertexAlarmLevelVos = topoVertexMapper.selectPcTopoNodeAlarmLevelByAsset(nodeType, orgId, AssetModeConst.SERVER);

        SysModuleConfig config = configService.getSysModuleConfig("config:systemTime");
        if (Objects.isNull(config)) {
            SysModuleConfig config1 = new SysModuleConfig();
            config1.setId(MyIdUtil.getId());
            config1.setName("config:systemTime");
            config1.setValue("close");
            config1.setDescription("open:打开调度台时间偏差显示   close:关闭调度台时间偏差显示");
            config1.setOrgId("0");
            config1.setServiceType(3);
            configService.save(config1);
            config = config1;
        }
        if ("open".equals(config.getValue())) {
            for (TopoVertexAlarmLevelVo topoVertexAlarmLevelVo : topoVertexAlarmLevelVos) {
                //时间偏差
                List<CollectSystemTime> collectSystemTimes = collectSystemTimeService.getRealTimeData(topoVertexAlarmLevelVo.getAssetId());
                if (collectSystemTimes != null && collectSystemTimes.size() > 0) {
                    CollectSystemTime c = collectSystemTimes.get(0);
                    Long systemTime = c.getTimeSpan();
                    if (ObjectUtil.isNull(systemTime)) {
                        topoVertexAlarmLevelVo.setSystemTime("0");
                    } else {
                        double v = systemTime.doubleValue() / 1000;
                        topoVertexAlarmLevelVo.setSystemTime(v + "");
                    }
                } else {
                    topoVertexAlarmLevelVo.setSystemTime("0");
                }
            }
        }
        return topoVertexAlarmLevelVos;
    }

    @Override
    public List<TopoVertexVo> selectNodeByCabnet(String nodeType, String orgId, String roomId) {
        return topoVertexMapper.selectNodeByCabnet(nodeType, orgId, roomId);
    }


    //原topo查机柜方法
    public List<RoomTopoVo> selectNodeByCenterCabinetV3(String orgId) {
        ArrayList<RoomTopoVo> roomTopoVos = new ArrayList<>();
        List<CabinetTopoVo> list = topoVertexMapper.selectNodeByCenterCabinetV2(orgId);
        List<CabinetTopoVo> alarms = topoVertexMapper.selectAlarmByCenterCabinetV2(orgId);
        for (CabinetTopoVo cabinetTopoVo : list) {
            for (CabinetTopoVo alarm : alarms) {
                if (ObjectUtil.isNotNull(cabinetTopoVo.getCabinetId()) && cabinetTopoVo.getCabinetId().equals(alarm.getCabinetId())) {

                    cabinetTopoVo.setAlarmLevel(alarm.getAlarmLevel());
                }
            }
        }
        Map<String, List<CabinetTopoVo>> roomNameAndC = list.stream().collect(Collectors.groupingBy(CabinetTopoVo::getRoomId));
        for (Map.Entry<String, List<CabinetTopoVo>> kv : roomNameAndC.entrySet()) {
            RoomTopoVo roomTopoVo = new RoomTopoVo();
            roomTopoVo.setRoomId(kv.getKey());
            Room room = roomService.getById(kv.getKey());
            roomTopoVo.setRoomName(room.getName());
            if (ObjectUtil.isNotNull(kv.getValue()) && !kv.getValue().isEmpty()) {
                roomTopoVo.setTopoNode(kv.getValue());
            }
            roomTopoVos.add(roomTopoVo);
        }
        return roomTopoVos;
    }

    @Override
    public List<RoomTopoVo> selectNodeByCenterCabinetV2(String orgId) {
        ArrayList<RoomTopoVo> roomTopoVos = new ArrayList<>();
        List<CabinetTopoVo> list = topoVertexMapper.selectCenterCabinetByOrgV2(orgId);
        List<CabinetTopoVo> alarms = topoVertexMapper.selectAlarmByCenterCabinetV2(orgId);
        for (CabinetTopoVo cabinetTopoVo : list) {
            for (CabinetTopoVo alarm : alarms) {
                if (ObjectUtil.isNotNull(cabinetTopoVo.getCabinetId()) && cabinetTopoVo.getCabinetId().equals(alarm.getCabinetId())) {

                    cabinetTopoVo.setAlarmLevel(alarm.getAlarmLevel());
                }
            }
        }
        Map<String, List<CabinetTopoVo>> roomNameAndC = list.stream().collect(Collectors.groupingBy(CabinetTopoVo::getRoomId));
        for (Map.Entry<String, List<CabinetTopoVo>> kv : roomNameAndC.entrySet()) {
            RoomTopoVo roomTopoVo = new RoomTopoVo();
            roomTopoVo.setRoomId(kv.getKey());
            Room room = roomService.getById(kv.getKey());
            roomTopoVo.setRoomName(room.getName());
            if (ObjectUtil.isNotNull(kv.getValue()) && !kv.getValue().isEmpty()) {
                roomTopoVo.setTopoNode(kv.getValue());
            }
            roomTopoVos.add(roomTopoVo);
        }
        return roomTopoVos;
    }

    @Override
    public List<CabinetTopoDetailVo> selectNodeByStationCabinetV2(String orgId) {
        List<CabinetTopoDetailVo> cabinetList = cabinetService.findByOrgIdV2(orgId);
        for (CabinetTopoDetailVo cabinet : cabinetList) {
            List<DetailCabinetVo> assetList = cabinetService.findDetailById(cabinet.getId());
            if (ObjectUtil.isNotNull(assetList) && !assetList.isEmpty()) {
                cabinet.setAssetList(assetList);
            }
        }
        return cabinetList;

    }

    @Override
    public Boolean deleteNetWorkAssetNodes(String nodeType, String assetId) {
        return topoVertexMapper.deleteNetWorkAssetTopoVertex(nodeType, assetId);
    }

    @Override
    public Boolean saveNodes(List<TopoVertex> list) {
        return this.saveBatch(list);
    }


    @Override
    public List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByCabnet(String nodeType, String orgId) {
        return topoVertexMapper.selectNodeAlarmLevelByCabnet(nodeType, orgId);
    }

    @Override
    public List<TopoPortStatus> findNetWorkPortStatus(String assetId) {
        return topoVertexMapper.findNetWorkPortStatus(assetId);
    }

    @Override
    public Boolean deleteNodes(String nodeType, String orgId) {
        return topoVertexMapper.deleteTopoVertex(nodeType, orgId);
    }

    /**
     * 查询网络拓扑端口状态
     *
     * @param paraMap
     * @return
     */
    @Override
    public List<TopoPortStatus> findPortStatus(Map<String, Object> paraMap) {
        List<String> orgIds = new ArrayList<String>();
        String orgId = paraMap.get("orgId").toString();
        orgIds.add(orgId);
        paraMap.put("orgIds", orgIds);
        return topoVertexMapper.findPortStatus(paraMap);

    }

    @Override
    public List<TopoVertexVo> selectLineNetTopo(String id) {
        SysOrg sysOrg = orgServ.getById(id);

        List<TopoVertexVo> topoVertexVos = null;
        if (StrUtil.isNotEmpty(sysOrg.getRemark()) && sysOrg.getRemark().contains("route:42+switch:201")) {
            // 只展示中心核心设备
            topoVertexVos = topoVertexMapper.selectLineCenterTopo(id);
        } else if (StrUtil.isNotEmpty(sysOrg.getRemark()) && sysOrg.getRemark().contains("switch:201")) {
            //中心核心设备+车站路由设备
            topoVertexVos = topoVertexMapper.selectLineCenterTopo(id);
            topoVertexVos.addAll(topoVertexMapper.selectLineRoutNetTopo(id));
        } else {
            topoVertexVos = topoVertexMapper.selectLineCenterTopo(id);
            topoVertexVos.addAll(topoVertexMapper.selectLineRoutNetTopo(id));
            topoVertexVos.addAll(topoVertexMapper.selectLineSwitchNetTopo(id));
        }
        List<TopoVertexVo> isNullList = new ArrayList<>();
        List<TopoVertexVo> noNullList = new ArrayList<>();

        for (TopoVertexVo topoVertexVo : topoVertexVos) {
            if (Objects.isNull(topoVertexVo.getName())) {
                isNullList.add(topoVertexVo);
            } else {
                noNullList.add(topoVertexVo);
            }
        }
        List<TopoVertexVo> collect = noNullList.stream().sorted(Comparator.comparing(TopoVertexVo::getName)).collect(Collectors.toList());

        if (!isNullList.isEmpty()) {
            collect.addAll(isNullList);
        }
        return collect;
    }

    @Override
    public List<TopoVertexAlarmLevelVo> selectLineNetTopoAlarmLevel(String id) {
        SysOrg sysOrg = orgServ.getById(id);
        List<TopoVertexAlarmLevelVo> topoVertexAlarmLevelVos = null;

        if (StrUtil.isNotEmpty(sysOrg.getRemark()) && sysOrg.getRemark().contains("route:42+switch:201")) {
            //只展示中心设备
            topoVertexAlarmLevelVos = topoVertexMapper.selectLineCenterTopoAlarmLevel(id);
        } else if (StrUtil.isNotEmpty(sysOrg.getRemark()) && sysOrg.getRemark().contains("switch:201")) {
            //屏蔽车站交换机
            topoVertexAlarmLevelVos = topoVertexMapper.selectLineCenterTopoAlarmLevel(id);
            topoVertexAlarmLevelVos.addAll(topoVertexMapper.selectLineRoutNetTopoAlarmLevel(id));
        } else {
            //中心核心设备+车站网络设备
            topoVertexAlarmLevelVos = topoVertexMapper.selectLineCenterTopoAlarmLevel(id);
            topoVertexAlarmLevelVos.addAll(topoVertexMapper.selectLineRoutNetTopoAlarmLevel(id));
            topoVertexAlarmLevelVos.addAll(topoVertexMapper.selectLineSwitchNetTopoAlarmLevel(id));
        }
        List<TopoVertexAlarmLevelVo> isNullList = new ArrayList<>();
        List<TopoVertexAlarmLevelVo> noNullList = new ArrayList<>();

        for (TopoVertexAlarmLevelVo topoVertexVo : topoVertexAlarmLevelVos) {
            if (Objects.isNull(topoVertexVo.getName())) {
                isNullList.add(topoVertexVo);
            } else {
                noNullList.add(topoVertexVo);
            }
        }
        List<TopoVertexAlarmLevelVo> collect = noNullList.stream().sorted(Comparator.comparing(TopoVertexAlarmLevelVo::getName)).collect(Collectors.toList());

        if (!isNullList.isEmpty()) {
            collect.addAll(isNullList);
        }
        return collect;
    }

    @Override
    public List<TopoPortStatus> findDownPort(String orgId) {
        return topoVertexMapper.findDownPort(orgId);
    }

    @Override
    public List<TopoVertexVo> selectPcTopoNodeByAsset(String nodeType, String orgId) {
        return topoVertexMapper.selectPcTopoNodeByAsset(nodeType, orgId, AssetModeConst.SERVER, null);
    }

    @Override
    public List<TopoVertexAlarmStatusVo> topoVertexStatus(String nodeType, String orgId) {

        return topoVertexMapper.topoVertexStatus(nodeType, Collections.singletonList(orgId));
    }

    @Override
    public List<AssetTargetVo> queryTarget(String assetId) {
        QueryWrapper<SysModuleConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("NAME", "config:topoType");
        List<SysModuleConfig> list = configService.list(queryWrapper);
        try {
            int value = Integer.parseInt(list.get(0).getValue());
            if (value == 1) {
                return topoVertexMapper.queryTargetAll(assetId);
            } else {
                return topoVertexMapper.queryTarget(assetId);
            }
        } catch (Exception e) {
            return topoVertexMapper.queryTarget(assetId);
        }
    }

    @Override
    public List<AssetTargetVo> queryPortIScorrect(AssetTargetVo assetTargetVo) {
        String local = assetTargetVo.getLocalPortIndex();
        String target = assetTargetVo.getTargetAssetId();
        String localId = assetTargetVo.getLocalAssetId();
        String targetId = assetTargetVo.getTargetAssetId();
        return topoVertexMapper.queryPortIScorrect(local, target, localId, targetId);

    }

    @Override
    public List<TopoPortStatus> findNetWorkDownPortStatus(String assetId) {
        return topoVertexMapper.findNetWorkDownPort(assetId);

    }

    /**
     * 查找拓扑图配置的设备连接信息
     *
     * @return
     */
    @Override
    public List<AssetLinkAssetVo> findMaAssetAndPort() {
        return topoVertexMapper.findMaAssetAndPort();
    }

    /**
     * 查询中心业务拓扑图
     *
     * @param orgId         组织ID
     * @param serviceTypeId 业务类型ID
     * @return BizTopoCenterVo
     */
    @Override
    public List<BizTopoCenterVo> findTopoCenterQuery(String orgId, String serviceTypeId) {
        return topoVertexMapper.findTopoCenterQuery(orgId, serviceTypeId);
    }

}

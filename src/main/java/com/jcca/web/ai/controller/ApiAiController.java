package com.jcca.web.ai.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.ai.vo.*;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.dao.CollectInterfacesMapper;
import com.jcca.web.collect.entity.*;
import com.jcca.web.collect.service.*;
import com.jcca.web.collect.service.bean.DiskVo;
import com.jcca.web.common.controller.bean.CpuResp;
import com.jcca.web.common.controller.bean.MemoryResp;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: ai访问接口
 * @author: sophia
 * @create: 2025/04/16 11:11
 **/
@RestController
@RequestMapping("/api/ai")
@Api(tags = "ai外部免密接口")
@Slf4j
public class ApiAiController {

    @Resource
    private AlarmInfoService alarmInfoServ;
    @Resource
    private CollectCpuService cpuService;
    @Resource
    private CollectMemoryService memoryService;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetLinkAssetService assetLinkAssetServ;
    @Resource
    private CollectDiskService diskService;
    @Resource
    private CollectNetworkCardService networkCardService;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    @Resource
    private SysOrgService orgService;
    @Resource
    private CollectInterfacesMapper interfaceMapper;

    /**
     * 获取组织信息
     *
     * @return
     */
    @PostMapping("/getOrgList")
    public ResultVo getOrgList(@RequestBody OrgAiVo org) {
        QueryWrapper<SysOrg> qw = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(org.getId())) {
            qw.eq("ID", getOrgId(org.getId()));
        }
        if (ObjectUtil.isNotNull(org.getTitle())) {
            qw.like("TITLE", org.getTitle());
        }
        if (ObjectUtil.isNotNull(org.getType())) {
            String type = org.getType();
            switch (type) {
                case "局":
                    qw.eq("TYPE", 1);
                    break;
                case "中心":
                    qw.eq("TYPE", 2);
                    break;
                case "线":
                    qw.eq("TYPE", 3);
                    break;
                case "站":
                    qw.eq("TYPE", 4);
                    break;
            }
        }
        if (ObjectUtil.isNotNull(org.getPid())) {
            qw.eq("PID", getOrgId(org.getPid()));
        }
        qw.eq("STATUS", 1);
        List<SysOrg> list = orgService.list(qw);
        if (list.size() > 50) {
            List<SysOrg> orgList = list.subList(0, 50);
            return ResultVoUtil.success("成功", orgList);
        }
        return ResultVoUtil.success("成功", list);
    }


    /**
     * 统计设备数量
     *
     * @return
     */
    @PostMapping("/countOrg")
    public ResultVo countOrg(@RequestBody OrgAiVo org) {
        QueryWrapper<SysOrg> qw = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(org.getType())) {
            String type = org.getType();
            switch (type) {
                case "局":
                    qw.eq("TYPE", 1);
                    break;
                case "中心":
                    qw.eq("TYPE", 2);
                    break;
                case "线":
                    qw.eq("TYPE", 3);
                    break;
                case "站":
                    qw.eq("TYPE", 4);
                    break;
            }
        }
        if (ObjectUtil.isNotNull(org.getPid())) {
            qw.eq("PID", getOrgId(org.getPid()));
        }
        qw.eq("STATUS", 1);
        int count = orgService.count(qw);
        return ResultVoUtil.success("", count);
    }


    /**
     * 统计组织个数
     *
     * @return
     */
    @PostMapping("/countAsset")
    public ResultVo countAsset(@RequestBody AssetAiVo assetVo) {
        QueryWrapper<Asset> qw = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(assetVo.getIp())) {
            qw.like("IP", assetVo.getIp());
        }
        if (ObjectUtil.isNotNull(assetVo.getName())) {
            qw.like("NAME", assetVo.getName());
        }
        if (ObjectUtil.isNotNull(assetVo.getAssetImage())) {
            qw.eq("ASSET_IMAGE", assetVo.getAssetImage());
        }

        if (ObjectUtil.isNotNull(assetVo.getOrgId())) {
            qw.eq("ORG_ID", getOrgId(assetVo.getOrgId()));
        }

        if (ObjectUtil.isNotNull(assetVo.getOrgName())) {
            qw.eq("ORG_ID", getOrgId(assetVo.getOrgName()));
        }
        if (ObjectUtil.isNotNull(assetVo.getAssetMode())) {
            String deskS = DictUtil.getKey("ASSET_MODE", assetVo.getAssetMode());
            if (!deskS.equals("")) {
                qw.eq("ASSET_MODE", deskS);
            }
        }
        qw.eq("IS_DEL", 1);
        int count = assetService.count(qw);
        return ResultVoUtil.success("", count);
    }


    /**
     * 查询设备信息
     *
     * @return
     */
    @PostMapping("/getAssetList")
    public ResultVo getAssetInfo(@RequestBody AssetAiVo assetVo) {
        QueryWrapper<Asset> qw = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(assetVo.getAssetId())) {
            qw.eq("ID", assetVo.getAssetId());
        }
        if (ObjectUtil.isNotNull(assetVo.getIp())) {
            qw.eq("IP", assetVo.getIp());
        }
        if (ObjectUtil.isNotNull(assetVo.getName())) {
            qw.like("NAME", assetVo.getName());
        }
        if (ObjectUtil.isNotNull(assetVo.getAssetImage())) {
            qw.eq("ASSET_IMAGE", assetVo.getAssetImage());
        }
        if (ObjectUtil.isNotNull(assetVo.getOrgId())) {
            qw.eq("ORG_ID", getOrgId(assetVo.getOrgId()));
        }

        if (ObjectUtil.isNotNull(assetVo.getOrgName())) {
            qw.eq("ORG_ID", getOrgId(assetVo.getOrgName()));
        }
        if (ObjectUtil.isNotNull(assetVo.getAssetMode())) {
            String deskS = DictUtil.getKey("ASSET_MODE", assetVo.getAssetMode());
            if (!deskS.equals("")) {
                qw.eq("ASSET_MODE", deskS);
            }
        }
        qw.eq("IS_DEL", 1);
        ArrayList<AssetVo> assetResps = new ArrayList<>();
        List<Asset> list = assetService.list(qw);
        Integer s = list.size();
        if (list.size() > 50) {
            s = 50;
        }
        for (int i = 0; i < s; i++) {
            AssetVo assetResp = new AssetVo();
            BeanUtils.copyProperties(list.get(i), assetResp);
            assetResps.add(assetResp);
        }
        return ResultVoUtil.success("", assetResps);
    }


    /**
     * 查询端口列表
     *
     * @return
     */
    @PostMapping("/getInterfaceList")
    public ResultVo getInterfaceList(@RequestBody AiVo aiVo) {
        if (ObjectUtil.isNotNull(aiVo.getAssetId())) {
            String assetId = getAssetId(aiVo.getAssetId());
            ArrayList<CollectInterfacesVo> collectInterfacesResps = new ArrayList<>();
            List<CollectInterfaces> interfaces = interfaceMapper.selectRealTimeData(assetId);
            for (CollectInterfaces portVo : interfaces) {
                CollectInterfacesVo collectInterfacesResp = new CollectInterfacesVo();
                BeanUtils.copyProperties(portVo, collectInterfacesResp);
                AssetLinkAsset linkAssetByAsset = assetLinkAssetServ.findLinkAssetByAsset(assetId, portVo.getPortIndex());
                if (ObjectUtil.isNotNull(linkAssetByAsset)) {
                    if (ObjectUtil.isNotNull(linkAssetByAsset.getLinkPort())) {
                        collectInterfacesResp.setPeerPort(linkAssetByAsset.getLinkPort());
                    }
                    if (ObjectUtil.isNotNull(linkAssetByAsset.getLinkAssetIp())) {
                        collectInterfacesResp.setPeerDevice(linkAssetByAsset.getLinkAssetIp());
                    }
                }
                collectInterfacesResps.add(collectInterfacesResp);
            }
            return ResultVoUtil.success("查询成功", collectInterfacesResps);
        } else {
            return ResultVoUtil.success("查询失败", "未找到指定设备");
        }
    }

    /**
     * AI查询告警信息
     *
     * @return
     */
    @PostMapping("/getAlarmList")
    public ResultVo getAlarmInfo(@RequestBody AiVo aiVo) {

        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(aiVo.getAssetId())) {
            queryWrapper.eq("ASSET_ID", aiVo.getAssetId());
        }
        if (ObjectUtil.isNotNull(aiVo.getContent())) {
            queryWrapper.like("DESCRIPTION", aiVo.getContent());
        }
        if (ObjectUtil.isNotNull(aiVo.getAlarmLevel())) {
            String alarmLevel = aiVo.getAlarmLevel();
            if (alarmLevel.contains("一") || alarmLevel.contains("1")) {
                queryWrapper.like("ALARM_LEVEL", 1);
            } else if (alarmLevel.contains("二") || alarmLevel.contains("2")) {
                queryWrapper.like("ALARM_LEVEL", 2);
            } else if (alarmLevel.contains("三") || alarmLevel.contains("3")) {
                queryWrapper.like("ALARM_LEVEL", 3);
            }
        }
        if (ObjectUtil.isNotNull(aiVo.getAlarmState())) {
            String alarmState = aiVo.getAlarmState();
            if (alarmState.contains("未") || alarmState.contains("没") || alarmState.contains("告警")) {
                queryWrapper.eq("ALARM_STATE", 1);
            } else {
                queryWrapper.eq("ALARM_STATE", 2);
            }

        }
        if (ObjectUtil.isNotNull(aiVo.getTime()) && aiVo.getTime() < 172800) {
            Date dateBefore = getDateBeforeSeconds(Long.valueOf(aiVo.getTime()).intValue());
            queryWrapper.ge("OCCUR_TIME", dateBefore);
        }
        queryWrapper.orderByAsc("OCCUR_TIME");
        List<AlarmInfo> list = alarmInfoServ.list(queryWrapper);
        if (list.size() > 50) {
            return ResultVoUtil.success("", list.subList(0, Math.min(list.size(), 50)));
        }
        return ResultVoUtil.success("", alarmInfoServ.list(queryWrapper));
    }


    /**
     * AI查询指定设备CPU
     *
     * @return
     */
    @PostMapping("/getCpuList")
    public ResultVo getCpuList(@RequestBody AiVo aiVo) {
        String assetId = getAssetId(aiVo.getAssetId());
        QueryWrapper<CollectCpu> cpuQueryWrapper = new QueryWrapper<CollectCpu>();
        cpuQueryWrapper.eq("ASSET_ID", assetId);
        if (ObjectUtil.isNotNull(aiVo.getTime()) && aiVo.getTime() < 172800) {
            Date dateBefore = getDateBeforeSeconds(Long.valueOf(aiVo.getTime()).intValue());
            cpuQueryWrapper.ge("COLLECT_TIME", dateBefore);
        }
        cpuQueryWrapper.orderByAsc("COLLECT_TIME");
        ArrayList<CpuResp> cpuResps = new ArrayList<>();
        List<CollectCpu> cpuData = cpuService.list(cpuQueryWrapper);
        for (CollectCpu cpu : cpuData) {
            CpuResp respCpu = new CpuResp();
            respCpu.setCollectDate(DateUtil.format(cpu.getCollectTime(), "yyyy-MM-dd HH:mm:ss"));
            respCpu.setCpuUsedRate(cpu.getCpuUsedRate());
            cpuResps.add(respCpu);
        }
        return ResultVoUtil.success("成功返回CPU", cpuResps);
    }


    /**
     * AI查询指定设备网卡信息
     *
     * @return
     */
    @PostMapping("/getNetworkList")
    public ResultVo getNetworkList(@RequestBody AiVo aiVo) {
        ArrayList<NetworkVo> resp = new ArrayList<>();
        String assetId = getAssetId(aiVo.getAssetId());
        QueryWrapper<CollectNetworkCard> netWorkAddressQueryWrapper = new QueryWrapper<CollectNetworkCard>();
        netWorkAddressQueryWrapper.eq("ASSET_ID", assetId);
        netWorkAddressQueryWrapper.orderByAsc("COLLECT_TIME");
        List<CollectNetworkCard> networkCards = networkCardService.list(netWorkAddressQueryWrapper);
        for (CollectNetworkCard netWorkAddress : networkCards) {
            NetworkVo networkResp = new NetworkVo();
            networkResp.setCollectDate(netWorkAddress.getCollectTime());
            networkResp.setIp(netWorkAddress.getIp());
            networkResp.setName(netWorkAddress.getName());
            networkResp.setMacAddress(netWorkAddress.getMacAddress());
            if (netWorkAddress.getStatus() == 1) {
                networkResp.setStatusStr("UP");
            }
            resp.add(networkResp);
        }
        return ResultVoUtil.success("成功返回网卡", resp);
    }

    /**
     * AI查询指定设备硬盘
     *
     * @return
     */
    @PostMapping("/getDiskList")
    public ResultVo getDiskList(@RequestBody AiVo aiVo) {
        ArrayList<DiskVo> resp = new ArrayList<>();
        String assetId = getAssetId(aiVo.getAssetId());
        QueryWrapper<CollectDisk> diskQueryWrapper = new QueryWrapper<CollectDisk>();
        diskQueryWrapper.eq("ASSET_ID", assetId);
        if (ObjectUtil.isNotNull(aiVo.getTime()) && aiVo.getTime() < 172800) {
            Date dateBefore = getDateBeforeSeconds(Long.valueOf(aiVo.getTime()).intValue());
            diskQueryWrapper.ge("COLLECT_TIME", dateBefore);
        }
        diskQueryWrapper.orderByAsc("COLLECT_TIME");
        List<CollectDisk> disks = diskService.list(diskQueryWrapper);
        for (CollectDisk disk : disks) {
            DiskVo diskResp = new DiskVo();
            diskResp.setCollectTime(disk.getCollectTime());
            diskResp.setMountPoint(disk.getMountPoint());
            diskResp.setUsedRate(disk.getUsedRate());
            resp.add(diskResp);
        }
        return ResultVoUtil.success("成功返回硬盘", resp);
    }

    /**
     * AI查询指定设备内存
     *
     * @return
     */
    @PostMapping("/getMemoryList")
    public ResultVo getMemoryList(@RequestBody AiVo aiVo) {
        ArrayList<MemoryResp> resp = new ArrayList<>();
        String assetId = getAssetId(aiVo.getAssetId());
        QueryWrapper<CollectMemory> memoryQueryWrapper = new QueryWrapper<CollectMemory>();
        memoryQueryWrapper.eq("ASSET_ID", assetId);
        if (ObjectUtil.isNotNull(aiVo.getTime()) && aiVo.getTime() < 172800) {
            Date dateBefore = getDateBeforeSeconds(Long.valueOf(aiVo.getTime()).intValue());
            memoryQueryWrapper.ge("COLLECT_TIME", dateBefore);
        }
        memoryQueryWrapper.orderByAsc("COLLECT_TIME");
        List<CollectMemory> memories = memoryService.list(memoryQueryWrapper);
        for (CollectMemory memory : memories) {
            MemoryResp memoryResp = new MemoryResp();
            memoryResp.setCollectDate(DateUtil.format(memory.getCollectTime(), "yyyy-MM-dd HH:mm:ss"));
            memoryResp.setMemUsedRate(memory.getMemUsedRate());
            resp.add(memoryResp);
        }
        return ResultVoUtil.success("成功返回内存", resp);
    }


    /**
     * AI查询指定设备数据信息
     *
     * @return
     */
    @PostMapping("/getAllInfo2")
    public ResultVo getAllInfo2(@RequestBody AiVo aiVo) {
        String[] types = aiVo.getTypes().toUpperCase().split(",");
        String assetId = aiVo.getAssetId();

        if (ObjectUtil.isNull(assetId)) {
            return ResultVoUtil.error("查询不到相关信息");
        }

        if (types.length == 0) {
            return ResultVoUtil.error("查询不到相关信息");
        }
        List<ResultResp> resultList = new ArrayList<ResultResp>();
        List<String> port = Arrays.asList(types);
        if (port.contains("CPU")) {
            System.out.println("调用CPU");
            QueryWrapper<CollectCpu> cpuQueryWrapper = new QueryWrapper<CollectCpu>();
            cpuQueryWrapper.eq("ASSET_ID", assetId);
            if (ObjectUtil.isNotNull(aiVo.getTime()) && aiVo.getTime() < 172800) {
                Date dateBefore = getDateBeforeSeconds(Long.valueOf(aiVo.getTime()).intValue());
                cpuQueryWrapper.ge("COLLECT_TIME", dateBefore);
            }

            cpuQueryWrapper.orderByAsc("COLLECT_TIME");
            List<CollectCpu> cpuData = cpuService.list(cpuQueryWrapper);
            for (CollectCpu cpu : cpuData) {
                CpuResp respCpu = new CpuResp();
                respCpu.setCollectDate(DateUtil.format(cpu.getCollectTime(), "yyyy-MM-dd HH:mm:ss"));
                respCpu.setCpuUsedRate(cpu.getCpuUsedRate());
                ResultResp resultResp = new ResultResp();
                resultResp.setCollectTime(cpu.getCollectTime());
                resultResp.setValue(respCpu.toString());
                resultList.add(resultResp);
            }
        }

        if (port.contains("MEMORY") || port.contains("内存")) {
            System.out.println("调用内存");
            QueryWrapper<CollectMemory> memoryQueryWrapper = new QueryWrapper<CollectMemory>();
            memoryQueryWrapper.eq("ASSET_ID", assetId);
            if (ObjectUtil.isNotNull(aiVo.getTime()) && aiVo.getTime() < 172800) {
                Date dateBefore = getDateBeforeSeconds(Long.valueOf(aiVo.getTime()).intValue());
                memoryQueryWrapper.ge("COLLECT_TIME", dateBefore);
            }
            memoryQueryWrapper.orderByAsc("COLLECT_TIME");
            List<CollectMemory> memories = memoryService.list(memoryQueryWrapper);
            for (CollectMemory memory : memories) {
                MemoryResp memoryResp = new MemoryResp();
                memoryResp.setCollectDate(DateUtil.format(memory.getCollectTime(), "yyyy-MM-dd HH:mm:ss"));
                memoryResp.setMemUsedRate(memory.getMemUsedRate());
                ResultResp resultResp = new ResultResp();
                resultResp.setCollectTime(memory.getCollectTime());
                resultResp.setValue(memoryResp.toString());
                resultList.add(resultResp);
            }
        }
        if (port.contains("NETWORK") || port.contains("网卡")) {
            System.out.println("调用网卡");
            QueryWrapper<CollectNetworkCard> netWorkAddressQueryWrapper = new QueryWrapper<CollectNetworkCard>();
            netWorkAddressQueryWrapper.eq("ASSET_ID", assetId);
            netWorkAddressQueryWrapper.orderByAsc("OCCUR_TIME");
            List<CollectNetworkCard> networkCards = networkCardService.list(netWorkAddressQueryWrapper);
            for (CollectNetworkCard netWorkAddress : networkCards) {
                NetworkVo networkResp = new NetworkVo();
                networkResp.setCollectDate(netWorkAddress.getCollectTime());
                ResultResp resultResp = new ResultResp();
                resultResp.setCollectTime(netWorkAddress.getCollectTime());
                resultResp.setValue(networkResp.toString());
                resultList.add(resultResp);
            }
        }

        if (port.contains("DISK") || port.contains("硬盘")) {
            System.out.println("调用DISK");
            QueryWrapper<AlarmInfo> alarmInfoQueryWrapper = new QueryWrapper<AlarmInfo>();
            alarmInfoQueryWrapper.eq("ASSET_ID", assetId);
            if (ObjectUtil.isNotNull(aiVo.getTime()) && aiVo.getTime() < 172800) {
                Date dateBefore = getDateBeforeSeconds(Long.valueOf(aiVo.getTime()).intValue());
                alarmInfoQueryWrapper.ge("OCCUR_TIME", dateBefore);
            }
            alarmInfoQueryWrapper.orderByAsc("OCCUR_TIME");
            List<AlarmInfo> alarmInfos = alarmInfoServ.list(alarmInfoQueryWrapper);
            for (AlarmInfo alarmInfo : alarmInfos) {
                AlarmVo alarmResp = new AlarmVo();
                alarmResp.setOccurTime(alarmInfo.getOccurTime());
                alarmResp.setDescription(alarmInfo.getDescription());
                if (alarmInfo.getAlarmState() == (byte) 1) {
                    alarmResp.setStatusStr("未恢复");
                } else {
                    alarmResp.setStatusStr("已恢复");
                }
                ResultResp resultResp = new ResultResp();
                resultResp.setCollectTime(alarmInfo.getOccurTime());
                resultResp.setValue(alarmResp.toString());
                resultList.add(resultResp);
            }
        }
        return ResultVoUtil.success("成功返回", resultList);
    }


    /**
     * 执行sql信息
     *
     * @return
     */
    @PostMapping("/executeSql")
    public ResultVo executeSql(@RequestBody AiVo aiVo) {
        String replaceSql = aiVo.getSql();
        System.out.println(replaceSql);
        // 1. 检查SQL是否为空
        if (StringUtils.isEmpty(replaceSql)) {
            return ResultVoUtil.error("SQL不能为空");
        }
        String trimmedSql = replaceSql.trim().toLowerCase();
        if (!trimmedSql.startsWith("select")) {
            return ResultVoUtil.error("只允许查询语句执行");
        }

        try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession()) {
            Connection connection = sqlSession.getConnection();

            // 2. 执行SQL并处理结果
            try (PreparedStatement preparedStatement = connection.prepareStatement(replaceSql)) {
                boolean hasResultSet = preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getResultSet()) {
                    List<Map<String, Object>> resultList = convertResultSetToList(resultSet);
                    sqlSession.commit();
                    return ResultVoUtil.success(resultList);
                }
            }
        } catch (SQLException e) {
            log.error("SQL执行异常: {}", replaceSql, e);
            return ResultVoUtil.error("SQL执行异常: " + e.getMessage());
        }
    }


    /**
     * AI获取所有设备信息
     *
     * @return
     */
    @GetMapping("/getAssetInfo2")
    public ResultVo getAssetInfo2() {
        QueryWrapper<Asset> qw2 = new QueryWrapper<>();
        qw2.eq("IS_DEL", 1);
        List<Asset> list = assetService.list(qw2).stream().map(a -> {
            String deskS = DictUtil.getValue("ASSET_MODE", a.getDesk().toString());
            a.setAssetVersion(deskS);
            return a;
        }).collect(Collectors.toList());
        return ResultVoUtil.success("", list);
    }


    /**
     * 获取组织信息
     *
     * @return
     */
    @GetMapping("/getOrg/{orgId}")
    public ResultVo getOrg(@PathVariable String orgId) {
        SysOrg org = orgService.getById(orgId);
        if (ObjectUtil.isNull(org)) {
            return ResultVoUtil.success(orgId + ":未知组织");
        }
        return ResultVoUtil.success("成功", org.getTitle());
    }


    /**
     * 获取当前时间往前推N秒的Date对象
     *
     * @param seconds 需要往前推的秒数
     * @return 往前推N秒后的Date对象
     */
    public Date getDateBeforeSeconds(int seconds) {
        // 获取当前时间的Calendar实例
        Calendar calendar = Calendar.getInstance();
        // 将当前时间减去指定的秒数
        calendar.add(Calendar.SECOND, -seconds);
        // 返回计算后的Date对象
        return calendar.getTime();
    }

    public String getAssetId(String assetStr) {
        if (ObjectUtil.isNotNull(assetService.getById(assetStr))) {
            return assetStr;
        }
        Asset asset = assetService.getOneByAllIp(assetStr);
        if (ObjectUtil.isNotNull(asset)) {
            return asset.getId();
        }

        QueryWrapper<Asset> qw = new QueryWrapper<>();
        qw.eq("NAME", assetStr);
        qw.eq("IS_DEL", 1);
        List<Asset> list = assetService.list(qw);
        if (ObjectUtil.isNotNull(list) && !list.isEmpty()) {
            return list.get(0).getId();
        }
        return "";
    }


    public String getOrgId(String orgStr) {
        if (ObjectUtil.isNotNull(orgService.getById(orgStr))) {
            return orgStr;
        }
        List<SysOrg> allByName = orgService.findAllByName(orgStr);
        if (!allByName.isEmpty()) {
            return allByName.get(0).getId();
        }
        return "";
    }


    /**
     * 将ResultSet转换为List<Map<String, Object>>
     */
    private List<Map<String, Object>> convertResultSetToList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i); // 使用getColumnLabel而不是getColumnName
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            resultList.add(row);
        }

        return resultList;
    }

}
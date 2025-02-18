package com.jcca.web.test.collect;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.quartz.alarm.bean.UnhealthyAsset;
import com.jcca.component.thresholds.bean.CollectInterfaceBean;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.entity.CollectDisk;
import com.jcca.web.collect.entity.CollectRoute;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectDiskService;
import com.jcca.web.collect.service.CollectDsService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.collect.service.bean.VlanPortBean;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.graph.service.TopoVertexService;
import com.jcca.web.statistics.service.HourInterfacesService;
import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件夹测试
 *
 * @author lyp
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CollectSpringTest {

    // 获取vlan下的物理地址和端口
    private static final List<String> VLAN_MIN = Arrays.asList("1.3.6.1.2.1.17.4.3.1.1", "1.3.6.1.2.1.17.4.3.1.2");
    // 获取端口索引
    private static final String VLAN_MIN_GET_PORT_INDEX = "1.3.6.1.2.1.17.1.4.1.2";
    private static final String VLAN_FLAG = "VL";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectDiskService diskService;
    @Resource
    private EventLogicService eventServ;
    @Resource
    private CollectCpuService collectCpuServ;
    @Resource
    private HourInterfacesService intefaceServ;
    @Resource
    private TopoVertexService topoVertexService;
    @Resource
    private CollectRouteService routeServ;
    @Resource
    private OutService outServ;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private CollectDsService dsService;

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource(name = "redisTransactionTemplate")
    private RedisTemplate redisTransactionTemplate;

    @Test
    public void testSyslog() throws Exception {
        SyslogEventInfoEntity syslogEventInfoEntity = new SyslogEventInfoEntity();
        syslogEventInfoEntity.setIp("192.168.73.88");
        syslogEventInfoEntity.setMessage("1 2025-02-14T09:36:04-00:00 XCC-7X04-J900V142 XCC-LOG - - - \n\tServer MTM: 7X04CTO1WW\n\n\n\tAlert Text: Security: Userid: USERID using default authentication had 1 login failures from WEB client at IP address 220.0.1.250.\n\tType of Alert: System - Remote Login\n\n\tSeverity: 4\n\tDate(m/d/y): 02/14/2025\n\tTime(h:m:s): 09:36:03\n\n\tContact: jcca\n\n\tLocation: jcca\n\tBMC Text ID: UnknownBMC\n\tBMC Serial Number: J900V142\n\tBMC UUID: 725FE55C404211EEB22472E2842E61EF\n\tEvent ID: 4000001000000000\n\tServiceable Event Indicator: Not Serviceable\n\tFRU list: Not available\n\tRoom ID: Not available\n\tRack ID: Not available\n\tLowest U-position: 1\n\tBlade Bay: Not available\n\tTest Alert: no\n\tAuxiliary Data: Not available\n\tCommon Event ID: FQXSPSE4002I\n\tEvent Type: 0\n\tReport Chain: XCC");
        syslogEventInfoEntity.setLevel(6);
        dataProcessManager.syslogEventHandlerRequest(syslogEventInfoEntity);
    }


	@Test
    public void testRaid(){
        List<UnhealthyAsset> unHealthyAsset = alarmInfoService.findUnHealthyAsset(2, 300);
        if (Objects.nonNull(unHealthyAsset) && !unHealthyAsset.isEmpty()) {
            for (UnhealthyAsset unhealthyAsset : unHealthyAsset) {
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setUniqueCode(EventUniqueCode.Unhealthy_1);
                eventReq.setOriginalMsg("资产" + 100 + "天内发生" + unhealthyAsset.getNum());
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                eventReq.setAssetId(unhealthyAsset.getAssetId());
                eventReq.setCreateTime(new Date());
                eventReq.setGroupFlag(EventGroupConstant.ALARM);
                try {
                    log.info("【不健康资产事件添加】：REQ:{}", JSONUtil.toJsonStr(eventReq));
                    eventServ.addEvent(eventReq);
                } catch (Exception e) {
                    log.error("【不健康资产事件添加!】：" + e.getMessage(), e);
                }
            }
        }

    }

	@Test
    public void testds(){
        List<CollectDS> drivesList = dsService.findByType("1589460697891344384", 3);
        Set<Integer> ySet = new HashSet<>();
        ArrayList<String> x_y = new ArrayList<>();
        int max_x = drivesList.stream().map(ds -> {
            ySet.add(ds.getYindex());
            x_y.add(ds.getYindex() + "," + ds.getXindex());
            return ds;
        }).max(Comparator.comparing(CollectDS::getXindex)).get().getXindex();

        ArrayList<String> index = new ArrayList<>();
        for (Integer y : ySet) {
            for (int i=1;i<=max_x ;i++){
                index.add(y+"," +i);
            }
        }
        index.removeAll(x_y);

        for (String s : index) {
            String[] split = s.split(",");
            CollectDS collectDS = new CollectDS();
            collectDS.setXindex(Integer.valueOf(split[1]));
            collectDS.setYindex(Integer.valueOf(split[0]));
            collectDS.setStatus(3);
        }
        System.out.println(1);


    }

    @Test
    public void testEquipment() throws Exception {
        Asset asset = new Asset();
        asset.setOsUser("cisco");
        asset.setAssetMode(201);
        asset.setIp("192.168.1.1");
        asset.setId("1438315118691094528");
        QueryWrapper<CollectRoute> queryWrapper = new QueryWrapper<CollectRoute>();
        queryWrapper.eq("ASSET_ID", asset.getId());

    }

    @Test
    public void test() throws Exception {
        redisService.set(OutConst.COLLECT_MASTER_URL, "192.168.51.157:9999");
        Asset asset = new Asset();
        asset.setOsUser("cisco");
        asset.setIp("192.168.51.1");
        List<VlanPortBean> exeVlan = exeVlan(asset, "Vl51");

        log.info(JSONUtil.toJsonStr(exeVlan));
    }

    /**
     * 获取vlan 下的物理端口索引和对端Mac地址
     *
     * @param asset
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
                continue;
            }

            for (VlanPortBean vlanPort : resp) {
                vlanPort.setPortIndex(value);
                respList.add(vlanPort);
            }
        }

        return respList;
    }

    @Test
    public void testTopoVertex() {
        topoVertexService.selectNodeAlarmLevelByAsset("net_topo", "1263726680338341889");
        topoVertexService.selectNodeAlarmLevelByCabnet("cabinet_topo", "1263726680338341889");
    }

    @Test
    public void testUpdate() {
        CollectDisk disk = new CollectDisk();
        disk.setAssetId("1271254428590149632");
        disk.setMountPoint("C:/d/a");

        disk.setCollectCode("0000000002");
        disk.setCollectTime(new Date());
        disk.setCreateTime(new Date());
        disk.setFree(100000L);
        disk.setFreeRate(12.1);
        disk.setId("1271254428590149601");
        disk.setTotal(2000000L);
        disk.setUsed(120000L);
        disk.setUsedRate(12.0);

        UpdateWrapper<CollectDisk> updateWrapper = new UpdateWrapper<CollectDisk>();
        updateWrapper.eq("ASSET_ID", disk.getAssetId());
        updateWrapper.eq("MOUNT_POINT", disk.getMountPoint());
        diskService.saveOrUpdate(disk, updateWrapper);
    }

    @Test
    public void testQuery() {
        DateTime parse = DateUtil.parse("2020-07-03 12:00:00", DatePattern.NORM_DATETIME_PATTERN);
        DateTime now = DateUtil.parse("2020-07-03 13:00:00", DatePattern.NORM_DATETIME_PATTERN);
        List<CollectCpu> statistics = collectCpuServ.statistics(parse.toJdkDate(), now.toJdkDate());
        System.out.println("共" + statistics.size());
        for (CollectCpu collectCpu : statistics) {
            System.out.println(
                    collectCpu.getAssetId() + "REAT:" + DateUtil.format(collectCpu.getCollectTime(), "HH:mm:ss") + "【"
                            + collectCpu.getCpuUsedRate() + "】");
        }
    }

    @Test
    public void maxQuery() {
        log.info("this is info");
        log.debug("this is debug");
        log.error("this is erro");
        Date lastCreateTime = intefaceServ.lastCreateTime();
        System.out.println(lastCreateTime);
    }

}

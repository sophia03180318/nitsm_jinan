package com.jcca.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.system.config.bean.SysModuleConfigReq;
import com.jcca.admin.system.dao.SysModuleConfigMapper;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.vo.AssetHealthDegreeModuleConf;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.controller.bean.AlarmVerifyBean;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web2.vo.SysModuleConfigVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统功能模块参数设置 服务实现类 * 系统功能模块参数设置 配置覆盖规则: 1.相同配置项,SERVICE_TYPE 为0为 1级配置
 * 2.相同配置项,SERVICE_TYPE 不为0 为2级配置 3.相同配置项,SERVICE_TYPE不为0并且ORG_ID不为0为3级配置
 * <p>
 * 相同配置项大级别覆盖小级别
 * </p>
 *
 * @author LuBan
 * @since 2021-01-05
 */
@Service
public class SysModuleConfigServiceImpl extends ServiceImpl<SysModuleConfigMapper, SysModuleConfig>
        implements SysModuleConfigService {

    public static final String SYS_ALARM_MODULE_CONFIG_KEY = "config:alarm_count";
    @Resource
    private StationService stationService;
    @Resource
    private RedisService redisService;
    @Resource
    private SysModuleConfigMapper configMapper;

    @Override
    public List<SysModuleConfig> getSysModuleConfigList(SysModuleConfigReq req) {
        Map<String, SysModuleConfig> configMap = new HashMap<>();

        // 一级配置
        QueryWrapper<SysModuleConfig> q1 = new QueryWrapper<SysModuleConfig>();
        q1.eq("SERVICE_TYPE", 0);
        List<SysModuleConfig> list = this.list(q1);

        list.forEach(e -> {
            configMap.put(e.getName(), e);
        });

        // 二级配置
        QueryWrapper<SysModuleConfig> q2 = new QueryWrapper<SysModuleConfig>();
        q2.eq("SERVICE_TYPE", req.getServiceType());
        q2.eq("ORG_ID", 0);
        List<SysModuleConfig> list2 = this.list(q2);
        if (CollUtil.isNotEmpty(list2)) {
            list2.forEach(e -> {
                configMap.put(e.getName(), e);
            });
        }

        // 三级配置
        boolean findThree = false;
        QueryWrapper<SysModuleConfig> q3 = new QueryWrapper<SysModuleConfig>();
        // 查询车站采集器组织
        if (StrUtil.isNotEmpty(req.getStationIp())) {
            QueryWrapper<Station> querystation = new QueryWrapper<>();
            querystation.eq("IP", req.getStationIp());
            Station station = stationService.getOne(querystation);
            if (station != null) {
                findThree = true;
                q3.eq("ORG_ID", station.getOrgId());
            }
        } else if (StrUtil.isNotEmpty(req.getOrgId())) {
            findThree = true;
            q3.eq("ORG_ID", req.getOrgId());
        }
        if (findThree) {
            List<SysModuleConfig> list3 = this.list(q3);
            if (CollUtil.isNotEmpty(list3)) {
                list3.forEach(e -> {
                    configMap.put(e.getName(), e);
                });
            }
        }

        List<SysModuleConfig> result = configMap.entrySet().stream().map(e -> e.getValue())
                .collect(Collectors.toList());
        ;
        return result;
    }

    @Override
    public String getStationCollectorMode() {
        QueryWrapper<SysModuleConfig> qw = new QueryWrapper<>();
        qw.eq("NAME", "config:stationCollectMode");
        List<SysModuleConfig> list = this.list(qw);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0).getValue();
        } else {
            return null;
        }
    }

    @Override
    public String getSyslogConfig(String registerKey, Integer assetMode) {
        Integer route = 42;
        Integer switchAsset = 201;

        if (StrUtil.isEmpty(registerKey)) {
            if (route.equals(assetMode)) {
                registerKey = "config:syslog:register:4:42";
            } else if (switchAsset.equals(assetMode)) {
                registerKey = "config:syslog:register:4:201";
            } else {
                registerKey = "config:syslog:register:1:183";
            }
        }

        QueryWrapper<SysModuleConfig> qw = new QueryWrapper<>();
        qw.eq("NAME", registerKey);
        SysModuleConfig one = getOne(qw);

        if (Objects.nonNull(one)) {
            return one.getValue();
        }

        if (route.equals(assetMode)) {
            registerKey = "%\\w*-\\d-\\w*:";
        } else if (switchAsset.equals(assetMode)) {
            registerKey = "%\\w*-\\d-\\w*:";
        } else {
            registerKey = "Event ID: \\w{16}";
        }

        return registerKey;
    }

    /**
     * 按照指定KEY获取配置信息
     *
     * @param name 指定KEY，（config:）后面的部分
     * @return 配置信息
     */
    @Override
    public SysModuleConfig getSysModuleConfig(String name) {
        SysModuleConfig config = null;
        Object o = redisService.get(name);
        if (Objects.isNull(o)) {
            QueryWrapper<SysModuleConfig> wrapper = Wrappers.query();
            wrapper.eq("name", name);
            List<SysModuleConfig> list = this.list(wrapper);
            if (CollectionUtil.isNotEmpty(list)) {
                config = list.get(0);
                redisService.set(name, config);
            }
        } else {
            config = (SysModuleConfig) o;
        }
        return config;
    }

    @Override
    public AssetHealthDegreeModuleConf getAssetHealthModultConf() {
        SysModuleConfig moduleConf = getSysModuleConfig("config:health_degree_config");
        AssetHealthDegreeModuleConf configObj = null;
        if (Objects.isNull(moduleConf)) {
            configObj = getDefaultConf();
        } else {
            String value = moduleConf.getValue();
            if (JSONUtil.isJson(value)) {
                configObj = JSONUtil.toBean(JSONUtil.parseObj(value), AssetHealthDegreeModuleConf.class);
            } else {
                configObj = getDefaultConf();
            }
        }

        return configObj;
    }

    private AssetHealthDegreeModuleConf getDefaultConf() {
        AssetHealthDegreeModuleConf configObj = new AssetHealthDegreeModuleConf();
        configObj.setOneLevelMaxScore(60);
        configObj.setTwoLevelMaxScore(30);
        configObj.setThreeLevelMaxScore(10);

        configObj.setOneLevelScore(5);
        configObj.setTwoLevelScore(2);
        configObj.setThreeLevelScore(1);

        return configObj;
    }

    @Override
    public void updateConfig(SysModuleConfig config) {
        redisService.remove(config.getName());

        updateById(config);
    }

    @Override
    public SysConfig getSysConfig() {
        SysModuleConfig config = this.getSysModuleConfig("config:sysConfig");
        if (Objects.isNull(config)) {
            SysConfig sysConfig = new SysConfig();
            SysModuleConfig config1 = new SysModuleConfig();
            config1.setId(MyIdUtil.getId());
            config1.setName("config:sysConfig");
            config1.setValue(JSONUtil.toJsonStr(sysConfig));
            config1.setDescription("broadcast:是否有语音播报\n" +
                    "showJcca:是否显示运维设备\n" +
                    "firstLevelfirstLevel:是否开启一级告警\n" +
                    "secondLevel:是否开启二级告警\n" +
                    "thirdLevel:是否开启三级告警\n" +
                    "continuous:是否开启连续播报(若关闭只播报新告警)\n" +
                    "popup:有新告警是否自动弹窗\n" +
                    "1. affirmStatus:yes   recoveredStatus:no   只播报所有未确认告警(与恢复状态无关)\n" +
                    "2. affirmStatus:no   recoveredStatus:yes   只播报所有未恢复告警(与确认状态无关)\n" +
                    "3. affirmStatus:yes   recoveredStatus:yes   播报所有未确认且未恢复的告警");
            config1.setOrgId("0");
            config1.setServiceType(3);
            this.save(config1);
            redisService.set("config:sysConfig", config1);
            return sysConfig;
        }
        try {
            SysConfig sysConfig = JSONUtil.parseObj(config.getValue()).toBean(SysConfig.class);
            return sysConfig;
        } catch (Exception e) {
            return new SysConfig();
        }

    }

    @Override
    public AlarmVerifyBean getAlarmVerifyValue() {
        AlarmVerifyBean resp = new AlarmVerifyBean();
        SysModuleConfig sysModuleConfig = getSysModuleConfig(SYS_ALARM_MODULE_CONFIG_KEY);
        if (Objects.isNull(sysModuleConfig)) {
            resp.setPingSize(0);
            resp.setProcessSize(0);
        } else {
            String value = sysModuleConfig.getValue();
            resp = JSONUtil.toBean(JSONUtil.parseObj(value), AlarmVerifyBean.class);
        }
        return resp;
    }

    @Override
    public List<SysModuleConfigVo> queryWebConfigList() {
        QueryWrapper<SysModuleConfig> q1 = new QueryWrapper<SysModuleConfig>();
        q1.isNotNull("WEB_CONF");
        q1.orderByAsc("TITLE");

        List<SysModuleConfig> list = list(q1);

        List<SysModuleConfigVo> voList = new ArrayList<>();
        for (SysModuleConfig sysModuleConfig : list) {
            String webConf = sysModuleConfig.getWebConf();
            JSONObject webConfObj = JSONUtil.parseObj(webConf);
            SysModuleConfigVo sysModuleConfigVo = JSONUtil.toBean(webConfObj, SysModuleConfigVo.class);
            sysModuleConfigVo.setKey(sysModuleConfig.getName());
            sysModuleConfigVo.setDefaultValue(sysModuleConfig.getValue());
            voList.add(sysModuleConfigVo);
        }

        return voList;
    }

    @Override
    public void updateConfigByName(String name, String value) {
        configMapper.updateByName( name,  value);
    }
}

package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.config.bean.SysModuleConfigReq;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.vo.AssetHealthDegreeModuleConf;
import com.jcca.web.asset.controller.bean.AlarmVerifyBean;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web2.vo.SysModuleConfigVo;

import java.util.List;


/**
 * <p>
 * 系统功能模块参数设置 服务类
 * </p>
 *
 * @author LuBan
 * @since 2021-01-05
 */
public interface SysModuleConfigService extends IService<SysModuleConfig> {
    List<SysModuleConfig> getSysModuleConfigList(SysModuleConfigReq req);

    /**
     * 车站设备采集器模式:0:(需要安装车站采集器,中心采集器获取车站采集器数据)1:不需要车站采集器,中心采集器直接采集车站设备
     *
     * @return
     */
    String getStationCollectorMode();

    /**
     * 获取syslog的正则配置信息
     *
     * @param registerKey
     * @param assetMode
     * @return
     */
    String getSyslogConfig(String registerKey, Integer assetMode);

    /**
     * 按照指定KEY获取配置信息
     *
     * @param name 指定KEY
     * @return 配置信息
     */
    SysModuleConfig getSysModuleConfig(String name);

    /**
     * 获取设备健康度配置
     *
     * @return
     */
    AssetHealthDegreeModuleConf getAssetHealthModultConf();

    void updateConfig(SysModuleConfig config);

    /**
     * 获取系统全局配置信息
     */
    SysConfig getSysConfig();

    /**
     * 获取值
     *
     * @param
     * @return
     */
    AlarmVerifyBean getAlarmVerifyValue();

    /**
     * 查询web界面相关配置项
     * @return
     */
    List<SysModuleConfigVo> queryWebConfigList();

    /**
     * 通过名称更新
     * @param name
     * @param value
     */
    void updateConfigByName(String name, String value);
}

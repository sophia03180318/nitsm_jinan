package com.jcca.web.config.controller;

import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.config.vo.SysConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ Author：sophia
 * @ Date：Created in 10:12 2023/6/28
 * @ Description:系统配置
 */
@Api(tags = "系统配置接口")
@RestController
@RequestMapping("/api/config")
public class SysModuleConfigController {
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private RedisService redisService;

    private String sysConfigName = "config:sysConfig";



    /**
     * 系统配置获取
     *
     * @return
     */
    @GetMapping("/query")
    @ApiOperation(value = "系统配置获取")
    @ActionLog(name = "系统配置", title = "系统配置获取", key = LogTypeConstant.QUERY)
    ResultVo<SysConfig> query() {
        SysConfig sysConfig = configService.getSysConfig();
        return   ResultVoUtil.success(sysConfig);
    }



    /**
     * 系统配置修改
     *
     * @param req
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "系统配置修改")
    @RequiresPermissions({"api:config:update"})
    @ActionLog(name = "系统配置", title = "系统配置修改", key = LogTypeConstant.MODIFY)
    ResultVo<String> update(@RequestBody SysConfig req) {
        SysModuleConfig config = configService.getSysModuleConfig(sysConfigName);
        if (Objects.isNull(config)) {
            SysModuleConfig config1 = new SysModuleConfig();
            config1.setId(MyIdUtil.getId());
            config1.setName(sysConfigName);
            config1.setValue(JSONUtil.toJsonStr(req));
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
            configService.save(config1);
            redisService.set(sysConfigName, config1);
            return ResultVoUtil.success("修改成功~！");
        }
        config.setValue(JSONUtil.toJsonStr(req));
        configService.updateById(config);
        redisService.set(sysConfigName, config);
        return ResultVoUtil.success("修改成功~！");
    }



}

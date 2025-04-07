package com.jcca.web2.controller;


import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.vo.SysModuleConfigVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Api(tags = "系统配置项V2")
@Slf4j
@RestController
@RequestMapping("/api/v2/sysModule")
public class SysModuleConfigControllerV2 {

    @Resource
    private SysModuleConfigService configServ;

    @GetMapping("/list")
    @ApiOperation("获取界面配置项")
    public ResultVo<Object> list(){
        List<SysModuleConfigVo> sysModuleConfigVos = configServ.queryWebConfigList();
        return ResultVoUtil.success(sysModuleConfigVos);
    }

    @PostMapping("/update")
    @ApiOperation("更新")
    public ResultVo<Object> update(@RequestBody SysModuleConfig conf){
        String name = conf.getName();
        String value = conf.getValue();
        if(StrUtil.isEmpty(name)){
            return ResultVoUtil.error("缺少唯一值");
        }
        if(StrUtil.isEmpty(value)){
            return ResultVoUtil.error("缺少配置值");
        }

        configServ.updateConfigByName(name,value);
        return ResultVoUtil.success();
    }

}

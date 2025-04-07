package com.jcca.web2.controller;


import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.vo.SysModuleConfigVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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


}

package com.jcca.web2.controller;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.jcca.admin.system.entity.VersionMsg;

import com.jcca.admin.system.service.VersionMsgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;

import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SqlInjectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@Api(tags = "车站V2")
@Slf4j
@RestController
@RequestMapping("/api/v2/stationVersion")
public class StationVersionControllerV2 {

    @Resource
    private VersionMsgService versionMsgService;

    @GetMapping("/list")
    @ApiOperation("获取车站版本列表")
    public ResultVo<Object> list(VersionMsg msg, Integer size, Integer page){
        // 获取用户列表
        IPage iPage = PagePlugin.startPage(page, size);
        QueryWrapper<VersionMsg> queryWrapper = new QueryWrapper<VersionMsg>();
        if (StrUtil.isNotEmpty(msg.getVersion())) {
            SqlInjectionUtils.formattingQueryWrapper(queryWrapper, "VERSION",msg.getVersion());
        }
        if (StrUtil.isNotEmpty(msg.getCreateDateStr())) {
            Date parse = DateUtil.parse(msg.getCreateDateStr(), "yyyy-MM-dd").toJdkDate();

            DateTime beginOfDay = DateUtil.beginOfDay(parse);
            DateTime endOfDay = DateUtil.endOfDay(parse);
            queryWrapper.ge("CREATE_DATE", beginOfDay.toJdkDate());
            queryWrapper.le("CREATE_DATE", endOfDay.toJdkDate());
        }

        queryWrapper.orderByDesc("CREATE_DATE");
        IPage page2 = versionMsgService.page(iPage, queryWrapper);

        return ResultVoUtil.success(page2);
    }


}

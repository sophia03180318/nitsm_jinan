package com.jcca.web2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.entity.SysDict;
import com.jcca.admin.system.service.SysDictService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.utils.ResultVoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 字典
 */
@Api(tags = "字典V2")
@RestController
@RequestMapping("/api/v2/dict")
public class DictControllerV2 {

    @Resource
    private SysDictService dictService;

    @GetMapping("/list")
    @ApiOperation("获取字典列表")
    public ResultVo<Object> list(SysDict dict, Integer page, Integer size){
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        if (dict.getName() != null) {
            wrapper.like("name", dict.getName());
        }
        if (dict.getTitle() != null) {
            wrapper.like("title", dict.getTitle());
        }
        if (dict.getStatus() == null) {
            wrapper.eq("status", StatusEnum.OK.getCode());
        }
        wrapper.orderByDesc("modify_time");
        iPage = dictService.page(iPage, wrapper);

        return ResultVoUtil.success(iPage);
    }




}

package com.jcca.web.cycles.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.cycles.entity.CyclesInfo;
import com.jcca.web.cycles.service.CyclesInfoService;
import com.jcca.web.cycles.service.bean.CyclesException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description: 周期
 * @author: Lvyp
 * @create: 2024/11/20 13:29
 */
@Api(tags = "维护计划周期计划")
@Slf4j
@RestController
@RequestMapping("/api/cycles")
public class CyclesController {

    @Resource
    private CyclesInfoService infoService;

    @GetMapping("/pageQuery")
    ResultVo pageQuery(Integer page, Integer size, String name) {
        IPage pageResult = infoService.pageQuery(page, size, name);
        return ResultVoUtil.success(pageResult);
    }

    @GetMapping("/queryInfo/{id}")
    ResultVo queryInfo(@PathVariable("id") String id) {
        return ResultVoUtil.success(infoService.queryInfoById(id));
    }


    @PostMapping("/save")
    ResultVo save(@RequestBody CyclesInfo saveReq) {
        try {
            infoService.saveInfo(saveReq);
        } catch (CyclesException e) {
            return ResultVoUtil.error(e.getCyclesErrorMsg());
        }
        return ResultVoUtil.success("保存成功");
    }

    @PostMapping("/update")
    ResultVo update(@RequestBody CyclesInfo updateReq) {
        try {
            infoService.updateInfo(updateReq);
        } catch (CyclesException e) {
            return ResultVoUtil.error(e.getCyclesErrorMsg());
        }
        return ResultVoUtil.success("修改成功");
    }

    @PostMapping("/delete/{id}")
    ResultVo delete(@PathVariable("id") String id) {
        try {
            infoService.deleteById(id);
        } catch (CyclesException e) {
            return ResultVoUtil.error(e.getCyclesErrorMsg());
        }
        return ResultVoUtil.success("删除成功");
    }

    @PostMapping("/updateStatus/{id}")
    ResultVo updateStatus(@PathVariable("id") String id) {
        CyclesInfo info = infoService.getById(id);
        Integer status = info.getStatus();
        if (status > 0) {
            info.setStatus(-1);
        } else {
            info.setStatus(1);
        }
        infoService.updateById(info);
        return ResultVoUtil.success("处理成功");
    }


}

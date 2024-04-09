package com.jcca.admin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author hanwone
 * @date 2018/10/19
 */
@Controller
@RequestMapping("/system/actionLog")
public class ActionLogController {

    @Resource
    private SysActionLogService actionLogService;

    /**
     * 列表页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:actionLog:index")
    public String index(Model model, SysActionLog actionLog, Integer page, Integer size) {
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<SysActionLog> wrapper = new QueryWrapper<>();
        wrapper.setEntity(actionLog);
        wrapper.orderByDesc("create_time");

        iPage = actionLogService.page(iPage, wrapper);
        // 封装数据
        model.addAttribute("list", iPage.getRecords());
        model.addAttribute("page", iPage);
        return "/system/actionLog/index";
    }

    /**
     * 跳转到详细页面
     */
    @GetMapping("/detail/{id}")
    @RequiresPermissions("system:actionLog:detail")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("actionLog", actionLogService.getById(id));
        return "/system/actionLog/detail";
    }

    /**
     * 删除指定的日志
     */
    @GetMapping("/status/delete")
    @RequiresPermissions("system:actionLog:status")
    @ResponseBody
    @ActionLog(name = "删除行为日志", title = "行为日志", key = LogTypeConstant.REMOVEE)
    public ResultVo delete(String id) {
        if (id != null) {
            actionLogService.removeById(id);
            return ResultVoUtil.success("删除日志成功");
        } else {
            actionLogService.removeAll();
            return ResultVoUtil.success("清空日志成功");
        }
    }
}

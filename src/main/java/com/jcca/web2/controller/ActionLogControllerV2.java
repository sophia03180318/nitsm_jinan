package com.jcca.web2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author hanwone
 * @date 2018/10/19
 */
@RestController
@RequestMapping("/api/v2/actionLog")
public class ActionLogControllerV2 {

    @Resource
    private SysActionLogService actionLogService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 列表页面
     */
    @PostMapping("/index")
    public ResultVo<Object> index(@RequestBody SysActionLog actionLog, Integer page, Integer size) {
        IPage iPage = PagePlugin.startPage(page, size);

        if (actionLog.getFlag()) {
            actionLog.setLogType(Byte.valueOf(LogTypeConstant.DEV));
        }
        QueryWrapper<SysActionLog> wrapper = getSysActionLogQueryWrapper(actionLog);
        if (Objects.nonNull(actionLog.getStartTime()) && Objects.nonNull(actionLog.getEndTime())) {
            wrapper.lt("CREATE_TIME", actionLog.getEndTime());
            wrapper.gt("CREATE_TIME", actionLog.getStartTime());
        }
        wrapper.orderByDesc("create_time");

        iPage = actionLogService.page(iPage, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", iPage.getRecords());
        map.put("total", iPage.getTotal());

        return ResultVoUtil.success(map);
    }

    private static QueryWrapper<SysActionLog> getSysActionLogQueryWrapper(SysActionLog actionLog) {
        QueryWrapper<SysActionLog> wrapper = new QueryWrapper<>();
        if (actionLog.getFlag()) {
            wrapper.eq("LOG_TYPE", LogTypeConstant.DEV);
        } else {
            if (!StringUtils.isEmpty(actionLog.getLogType())) {
                wrapper.eq("LOG_TYPE", actionLog.getLogType());
            }
        }
        if (!StringUtils.isEmpty(actionLog.getLogModel())) {
            wrapper.like("LOG_MODEL", actionLog.getLogModel());
        }
        if (!StringUtils.isEmpty(actionLog.getCreator())) {
            wrapper.like("CREATOR", actionLog.getCreator());
        }
        return wrapper;
    }

    @GetMapping("/detail/{id}")
    public ResultVo<Object> toDetail(@PathVariable("id") String id) {
        List<SysActionLogDetail> details = sysActionLogDetailService.listByActionLogId(id);
        return ResultVoUtil.success(details);
    }

    /**
     * 删除指定的日志
     */
    @GetMapping("/delete")
    @RequiresPermissions("api:v2:actionLog:delete")
    @ResponseBody
    @ActionLog(name = "删除行为日志", title = "行为日志", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> delete(String id) {
        if (id != null) {
            actionLogService.removeById(id);
            return ResultVoUtil.success("删除日志成功");
        } else {
            actionLogService.removeAll();
            return ResultVoUtil.success("清空日志成功");
        }
    }
}

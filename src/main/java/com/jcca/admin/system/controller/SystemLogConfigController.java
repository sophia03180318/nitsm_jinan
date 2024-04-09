package com.jcca.admin.system.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统日志输出配置
 */
@Controller
@RequestMapping("/system/logConfig")
public class SystemLogConfigController {


    @GetMapping("/index")
    @RequiresPermissions("system:logConfig:index")
    @ActionLog(name = "查看系统日志列表信息", title = "系统日志", key = LogTypeConstant.QUERY)
    public String index(Model model) {
        Map<String, Boolean> logErrorStatusMap = LogInputUtils.logErrorStatusMap;
        Map<String, Boolean> logInfoStatusMap = LogInputUtils.logInfoStatusMap;

        List<JSONObject> errorLogList = new ArrayList<JSONObject>();
        List<JSONObject> infoLogList = new ArrayList<JSONObject>();

        Set<String> errorKeys = logErrorStatusMap.keySet();
        for (String errorKey : errorKeys) {
            Boolean value = logErrorStatusMap.get(errorKey);
            JSONObject item = new JSONObject();
            item.put("name", errorKey);
            item.put("descStr", ServerTypeEnum.valueOf(errorKey).getMsg());
            item.put("value", value);
            errorLogList.add(item);
        }

        Set<String> infoKeys = logInfoStatusMap.keySet();
        for (String infoKey : infoKeys) {
            Boolean value = logInfoStatusMap.get(infoKey);
            JSONObject item = new JSONObject();
            item.put("name", infoKey);
            item.put("value", value);
            item.put("descStr", ServerTypeEnum.valueOf(infoKey).getMsg());
            infoLogList.add(item);
        }

        model.addAttribute("errorLogs", errorLogList);
        model.addAttribute("infoLogs", infoLogList);

        return "/system/logConfig/index";
    }

    /**
     * 设置
     * value 1开启日志 0关闭日志
     * type 取值：info、error
     * name 业务名称
     *
     * @param req
     * @return
     */
    @PostMapping("/setConf")
    @ResponseBody
    @ActionLog(name = "设置单个日志开关", title = "系统日志", key = LogTypeConstant.QUERY)
    public ResultVo setConf(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String type = reqJson.getStr("type");
        String name = reqJson.getStr("name");
        Integer value = reqJson.getInt("value");

        if (StrUtil.isEmpty(type) || StrUtil.isEmpty(name) || Objects.isNull(value)) {
            return ResultVoUtil.error("参数不全");
        }

        boolean status = value > 0;

        if ("info".equals(type)) {
            LogInputUtils.logInfoStatusMap.put(name, status);
        } else if ("error".equals(type)) {
            LogInputUtils.logErrorStatusMap.put(name, status);
        }

        return ResultVoUtil.success();
    }

    /**
     * 设置
     * 1开启日志 0关闭日志
     * type 取值：info、error
     *
     * @param req
     * @return
     */
    @PostMapping("/setAll")
    @ResponseBody
    @ActionLog(name = "批量设置日志开关", title = "系统日志", key = LogTypeConstant.QUERY)
    public ResultVo setAll(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String type = reqJson.getStr("type");
        Integer value = reqJson.getInt("value");

        if (StrUtil.isEmpty(type) || Objects.isNull(value)) {
            return ResultVoUtil.error("参数不全");
        }

        boolean status = value > 0;

        if ("info".equals(type)) {
            Set<String> infos = LogInputUtils.logInfoStatusMap.keySet();
            for (String infoKey : infos) {
                LogInputUtils.logInfoStatusMap.put(infoKey, status);
            }
        } else if ("error".equals(type)) {
            Set<String> errors = LogInputUtils.logErrorStatusMap.keySet();
            for (String errorKey : errors) {
                LogInputUtils.logErrorStatusMap.put(errorKey, status);
            }
        }

        return ResultVoUtil.success();
    }
}

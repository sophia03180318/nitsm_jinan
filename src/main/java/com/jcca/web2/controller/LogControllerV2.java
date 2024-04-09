package com.jcca.web2.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.entity.SysFunctionLog;
import com.jcca.web2.service.SysFunctionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * @author HanHW
 * @description 动态设置日志级别，默认为 INFO
 * @className LogController
 * @date 2023/9/22 14:59
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/log")
@Api(tags = "日志输出控制V2")
public class LogControllerV2 {

    private final String[] levels = {"OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL"};

    private LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    private Logger logger = loggerContext.getLogger(Web2Const.PACKAGE_NAME);

    @Resource
    private SysFunctionService functionService;

    @PostConstruct
    private void initFunctionLogMap() {
        functionService.getFunctionList();
    }

    /**
     * @description: 设置日志级别
     * @author: HanHW
     * @date: 2023/9/22 16:20
     * @param: [level] levels
     * @return: void
     **/
    @GetMapping("/setLevel")
    @ApiOperation("设置日志级别")
    @ActionLog(name = "设置日志级别", title = "日志输出控制", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> setLevel(String level) {
        level = level.toUpperCase();
        if (Arrays.asList(levels).contains(level)) {
            logger.setLevel(Level.toLevel(level));
            AppLogUtils.buildLogWarn(LogFunctionEnum.LOG_CONTROL, level, "日志级别变更为：" + level);
        }
        Map<String, String> map = new HashMap<>();
        map.put("level", level);
        return ResultVoUtil.success(map);
    }

    /**
     * @description: 查看日志级别
     * @author: HanHW
     * @date: 2023/9/22 16:21
     * @param: []
     * @return: 日志级别
     **/
    @GetMapping("/getLevel")
    @ApiOperation("查看日志级别")
    public ResultVo<Object> getLevel() {
        Map<String, String> map = new HashMap<>();
        map.put("level", logger.getLevel().levelStr);
        return ResultVoUtil.success(map);
    }

    /**
     * @description: 获取所有功能
     * @author: HanHW
     * @date: 2023/10/10 15:38
     * @param: []
     * @return: com.jcca.common.bean.ResultVo<java.lang.Object>
     **/
    @GetMapping("/getFunctionList")
    @ApiOperation("获取所有功能")
    public ResultVo<Object> getFunctionList() {

        List<SysFunctionLog> functionLogList = functionService.getFunctionList();

        return ResultVoUtil.success(functionLogList);
    }

    /**
     * @description: 设置功能日志开关
     * @author: HanHW
     * @date: 2023/10/10 16:15
     * @param: [status：状态，0关闭，1打开]
     * @return: com.jcca.common.bean.ResultVo<java.lang.Object>
     **/
    @GetMapping("/setFunction")
    @ApiOperation("设置功能日志开关")
    @ActionLog(name = "设置功能日志开关", title = "日志输出控制", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> setFunction(String code, String functionCode, Integer status) {

        functionService.setFunctionLog(code, functionCode, status);

        LogFunctionEnum one = LogFunctionEnum.getByCode(code);
        if (Objects.isNull(one)) {
            one = LogFunctionEnum.getByFunctionCode(functionCode);
        }
        if (Objects.nonNull(one)) {
            String msg = StatusConst.OK == status ? "日志已打开" : "日志已关闭";
            AppLogUtils.buildLogWarn(LogFunctionEnum.LOG_CONTROL, one.getFunction(), msg);
        }

        return ResultVoUtil.success();
    }
}

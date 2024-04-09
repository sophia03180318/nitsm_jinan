package com.jcca.admin.biz.controller;

import cn.hutool.core.util.NumberUtil;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author GodWone
 * @description 数据库数据转移备份
 * @className DataTransferController
 * @date 2022/11/14 11:47
 * @since 2.0.0.1
 */
@Slf4j
@Controller
@RequestMapping("/biz/data")
public class DataTransferController {

    @Value("${project.upload.file-path}")
    private String recoverPath;
    @Resource
    private SysActionLogService actionLogService;
    @Resource
    private SysModuleConfigService moduleConfigService;

    public static final String[] tables = {"ALARM_EVENT", "ALARM_EVENT_REL", "ALARM_INFO",
            "HOUR_CPU", "HOUR_INTERFACES", "HOUR_INTERFACES_ITEM", "HOUR_MEMORY", "HOUR_TEMP",
            "SYS_ACTION_LOG"};


    /**
     * 数据转存，将数据库中190天前数据存储为文件，并删除相应数据
     * 多次操作每天只生成一个文件
     * ALARM_EVENT、ALARM_EVENT_REL、ALARM_INFO
     * HOUR_CPU、HOUR_INTERFACES、HOUR_INTERFACES_ITEM、HOUR_MEMORY、HOUR_TEMP
     * SYS_ACTION_LOG
     *
     * @return success
     */
    @GetMapping("/transfer")
    @ResponseBody
    @RequiresPermissions("biz:data:transfer")
    @ActionLog(name = "删除并转存日志", title = "行为日志", key = LogTypeConstant.REMOVEE)
    public ResultVo transfer() {
        int amount = -190;
        SysModuleConfig moduleConfig = moduleConfigService.getSysModuleConfig("config:dataTransfer");
        if (Objects.nonNull(moduleConfig) && NumberUtil.isInteger(moduleConfig.getValue())) {
            amount = -Integer.parseInt(moduleConfig.getValue());
        } else {
            return ResultVoUtil.error("未配置config:dataTransfer或config:dataTransfer的值不为整数数字");
        }

        actionLogService.remove5000(tables, amount, recoverPath);

        if (log.isInfoEnabled()) {
            log.info("转存数据存放位置{}", recoverPath + "/transfer-data");
        }
        return ResultVoUtil.success("转存文件目录：" + recoverPath + "/transfer-data");
    }

    /**
     * 将上一次转存数据重新恢复至数据库
     * 恢复完成会删除文件
     *
     * @return success
     */
    @GetMapping("/recover")
    @ResponseBody
    @RequiresPermissions("biz:data:recover")
    @ActionLog(name = "恢复日志信息", title = "行为日志", key = LogTypeConstant.RECOVER)
    public ResultVo recover() {
        String path = recoverPath + "/transfer-data";
        File dict = new File(path);
        File[] fileList = dict.listFiles();
        assert fileList != null;
        Arrays.sort(fileList, (o1, o2) -> o2.getName().compareTo(o1.getName()));

        try {
            for (String table : tables) {
                for (File file : fileList) {
                    String[] nameDate = file.getName().split("-");
                    if (table.equals(nameDate[0])) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                        actionLogService.recoverData(nameDate[0], reader);

                        reader.close();

                        file.delete();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("恢复已删除数据异常-DataTransferController.recover");
        }

        return ResultVoUtil.success("恢复成功");
    }

}

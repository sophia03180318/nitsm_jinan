package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.SysFunctionLogMapper;
import com.jcca.web2.entity.SysFunctionLog;
import com.jcca.web2.service.SysFunctionService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author HanHW
 * @description 功能日志输出控制实现
 * @className SysFunctionServiceImpl
 * @date 2023/10/24 13:12
 * @since 2.1.0.0
 */
@Service
public class SysFunctionServiceImpl extends ServiceImpl<SysFunctionLogMapper, SysFunctionLog> implements SysFunctionService {
    /**
     * @description: 获取功能日志控制列表
     * @author: HanHW
     * @date: 2023/10/24 13:17
     * @param: []
     * @return: java.util.List<com.jcca.web2.entity.SysFunctionLog>
     **/
    @Override
    public List<SysFunctionLog> getFunctionList() {

        List<SysFunctionLog> list = this.list();
        if (list.isEmpty()) {
            LogFunctionEnum[] values = LogFunctionEnum.values();
            for (LogFunctionEnum value : values) {
                String code = value.getCode();
                String function = value.getFunction();
                String action = value.getAction();
                this.setLogValue(function, action, code, list);
            }

            this.setCache(list);

            this.saveBatch(list);
            return list;
        }

        Set<String> codeSet = new HashSet<>();
        for (SysFunctionLog log : list) {
            codeSet.add(log.getCode());
        }

        List<SysFunctionLog> logList = new ArrayList<>();
        LogFunctionEnum[] values = LogFunctionEnum.values();
        for (LogFunctionEnum value : values) {
            String code = value.getCode();
            String function = value.getFunction();
            String action = value.getAction();
            if (codeSet.contains(code)) {
                continue;
            }
            this.setLogValue(function, action, code, logList);
        }

        if (!CollectionUtils.isEmpty(logList)) {
            list.addAll(logList);

            this.saveBatch(logList);
        }

        this.setCache(list);

        return list;
    }

    // 将功能日志输出放入内存
    private void setCache(List<SysFunctionLog> list) {
        for (SysFunctionLog functionLog : list) {
            Web2Const.FUNCTION_LOG_MAP.put(functionLog.getCode(), functionLog.getStatus());
        }
    }

    /**
     * @description: 设置日志功能输出
     * CODE和FUNCTION_CODE不能同时为空；当同时存在时以CODE为准，因为CODE范围更小。
     * @author: HanHW
     * @date: 2023/10/24 14:10
     * @param: [functionLog]
     * @return: void
     */
    @Override
    public void setFunctionLog(String code, String functionCode, Integer status) {
        if (StringUtils.isEmpty(code) && StringUtils.isEmpty(functionCode)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        UpdateWrapper<SysFunctionLog> wrapper = Wrappers.update();
        if (!StringUtils.isEmpty(code)) {
            wrapper.eq("CODE", code);
            wrapper.set("STATUS", status);
            this.update(wrapper);

            this.getFunctionList(); // 更新缓存

            return;
        }

        if (!StringUtils.isEmpty(functionCode)) {
            wrapper.eq("FUNCTION_CODE", functionCode);
            wrapper.set("STATUS", status);

            this.update(wrapper);

            this.getFunctionList(); // 更新缓存
        }
    }


    private void setLogValue(String function, String action, String code, List<SysFunctionLog> list) {
        SysFunctionLog log = new SysFunctionLog();
        log.setId(MyIdUtil.getId());
        log.setCode(code);
        log.setFunction(function);
        log.setAction(action);
        log.setFunctionCode(code.substring(0, 2));
        log.setActionCode(code.substring(2));
        log.setStatus((int) StatusEnum.OK.getCode());
        log.setCreateTime(new Date());
        list.add(log);
    }
}

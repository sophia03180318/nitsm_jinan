package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.SysFunctionLog;

import java.util.List;

/**
 * @author HanHW
 * @description 功能日志输出控制
 * @className SysFunctionService
 * @date 2023/10/24 13:12
 * @since 2.1.0.0
 */
public interface SysFunctionService extends IService<SysFunctionLog> {

    /**
     * @description: 获取功能日志控制列表
     * @author: HanHW
     * @date: 2023/10/24 13:17
     * @param: []
     * @return: java.util.List<com.jcca.web2.entity.SysFunctionLog>
     **/
    List<SysFunctionLog> getFunctionList();

    /**
     * @description: 设置日志功能输出
     * @author: HanHW
     * @date: 2023/10/24 14:10
     * @param: [functionLog]
     * @return: void
     **/
    void setFunctionLog(String code, String functionCode, Integer status);
}

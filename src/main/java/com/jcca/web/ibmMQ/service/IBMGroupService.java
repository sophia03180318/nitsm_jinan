package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMGroup;

/**
 * @ Author：sophia
 * @ Date：Created in 11:47 2021/11/25
 * @ Description:
 */
public interface IBMGroupService extends IService<IBMGroup> {
    /**
     * 是否重名
     */
    boolean selectByName(String name, String connectId);

    /**
     * 根据名称获取ID
     */
    String selectIdByName(String name, String connectId);
}

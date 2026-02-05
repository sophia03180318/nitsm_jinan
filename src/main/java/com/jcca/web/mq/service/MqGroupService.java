package com.jcca.web.mq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.mq.entity.MqGroup;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:47 2021/11/25
 * @ Description:
 */
public interface MqGroupService extends IService<MqGroup> {
    /**
     * 是否重名
     */
    List<MqGroup> selectByName(String name, String connectId);

    List<MqGroup> selectByConnectId(String connectId);

}

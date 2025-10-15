package com.jcca.web.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.common.entity.DhFlag;

import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:00
 **/
public interface DhFlagService extends IService<DhFlag> {


    void savePub(String flag,String eventId);
}
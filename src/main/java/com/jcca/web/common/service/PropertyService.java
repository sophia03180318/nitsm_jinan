package com.jcca.web.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.common.entity.Property;

import java.util.List;

/**
 * @description: 属性
 * @author: sophia
 * @create: 2023/11/30 14:00
 **/
public interface PropertyService extends IService<Property> {
    List<Property> selectThreeDProperty();

}
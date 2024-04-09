package com.jcca.web.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.common.dao.PropertyMapper;
import com.jcca.web.common.entity.Property;
import com.jcca.web.common.service.PropertyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:10
 **/
@Service
public class PropertyServiceImpl extends ServiceImpl<PropertyMapper, Property> implements PropertyService {
    @Resource
    private PropertyMapper propertyMapper;
    @Override
    public List<Property> selectThreeDProperty() {
        return propertyMapper.selectThreeDProperty();
    }
}
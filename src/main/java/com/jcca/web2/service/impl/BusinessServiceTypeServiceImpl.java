package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web2.dao.BusinessServiceTypeMapper;
import com.jcca.web2.entity.BusinessServiceType;
import com.jcca.web2.service.BusinessServiceTypeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/01 10:42
 **/
@Service
public class BusinessServiceTypeServiceImpl extends ServiceImpl<BusinessServiceTypeMapper, BusinessServiceType> implements BusinessServiceTypeService {
    @Resource
    private BusinessServiceTypeMapper businessServiceTypeMapper;

    @Override
    public List<Asset> getAssetByType(String orgId, String type) {
        return businessServiceTypeMapper.getAssetByType(orgId, type);

    }

    @Override
    public Integer getMaxSort() {
        return businessServiceTypeMapper.getMaxSort();
    }

    @Override
    public String getTypeByName(String name) {
        List<String> ids = businessServiceTypeMapper.getTypeByName(name);
        if (!ids.isEmpty()){
            return ids.get(0);
        }
        return "";

    }
}
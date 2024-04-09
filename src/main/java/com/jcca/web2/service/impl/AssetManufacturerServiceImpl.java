package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.AssetManufacturerMapper;
import com.jcca.web2.entity.AssetManufacturer;
import com.jcca.web2.service.AssetManufacturerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/02 14:39
 **/
@Service
public class AssetManufacturerServiceImpl extends ServiceImpl<AssetManufacturerMapper, AssetManufacturer> implements AssetManufacturerService {

    @Resource
    private AssetManufacturerMapper assetManufacturerMapper;

    @Override
    public Map<Long, String> getManufacturerMap() {
        Map<Long, String> map = this.list().stream().collect(Collectors.toMap(AssetManufacturer::getId, AssetManufacturer::getName));
        return map;
    }

    @Override
    public Long getMaxId() {
        return assetManufacturerMapper.getMaxId();
    }
}
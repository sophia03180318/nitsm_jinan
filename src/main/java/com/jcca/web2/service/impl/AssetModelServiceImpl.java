package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.AssetModelMapper;
import com.jcca.web2.entity.AssetModel;
import com.jcca.web2.service.AssetModelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/02 16:14
 **/
@Service
public class AssetModelServiceImpl extends ServiceImpl<AssetModelMapper, AssetModel> implements AssetModelService {
    @Resource
    private AssetModelMapper modelMapper;

    @Override
    public Map<String, String> getModelMapByModeId(String modeId) {
        QueryWrapper<AssetModel> qw = new QueryWrapper<>();
        qw.eq("ASSET_MODE_ID", modeId);
        return this.list(qw).stream().collect(Collectors.toMap(AssetModel::getId, AssetModel::getModel));
    }

    /**
     * @description: 获取厂商型号列表
     * @author: HanHW
     * @date: 2023/12/4 11:18
     * @param: [manufacturerId]
     * @return: java.util.List<com.jcca.web2.entity.AssetModel>
     **/
    @Override
    public List<AssetModel> getByManufacturerId(String manufacturerId) {
        return modelMapper.getByManufacturerId(manufacturerId);
    }
}
package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.web2.dao.AssetModeMapper;
import com.jcca.web2.entity.AssetMode;
import com.jcca.web2.entity.AssetModel;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.AssetModelService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/02 16:14
 **/
@Service
public class AssetModeServiceImpl extends ServiceImpl<AssetModeMapper, AssetMode> implements AssetModeService {

    @Resource
    private AssetModeMapper assetModeMapper;
    @Resource
    private AssetModelService assetModelService;

    @Override
    public Map<Integer, String> getModeMap() {
        return this.list().stream().collect(Collectors.toMap(AssetMode::getCode, AssetMode::getName));
    }

    @Override
    public AssetMode getByCode(Integer code) {
        return assetModeMapper.getByCode(code);
    }

    /**
     * 获取厂商下的所有资产类型
     *
     * @param manufacturerId 厂商ID
     * @return
     */
    @Override
    public List<AssetMode> getManufacturerMode(String manufacturerId) {
        QueryWrapper<AssetModel> query1 = Wrappers.query();
        query1.eq("MANUFACTURER_ID", manufacturerId);
        List<AssetModel> list = assetModelService.list(query1);
        if (CollectionUtils.isEmpty(list)) {
            throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "该厂商没有对应的资产型号：" + manufacturerId);
        }
        Set<String> collect = list.stream().map(AssetModel::getAssetModeId).collect(Collectors.toSet());

        return assetModeMapper.selectBatchIds(collect);
    }

    /**
     * 由型号找资产类型
     *
     * @param modelId 型号ID
     * @return AssetMode
     */
    @Override
    public AssetMode getModelMode(String modelId) {
        AssetModel model = assetModelService.getById(modelId);
        if (Objects.isNull(model)) {
            throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "该型号没有对应的资产类型：" + modelId);
        }
        return assetModeMapper.selectById(model.getAssetModeId());
    }
}
package com.jcca.admin.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.PerformanceTargetMapper;
import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.service.PerformanceTargetService;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ Author：sophia
 * @ Date：Created in 9:59 2021/8/12
 * @ Description:
 */
@Service
public class PerformanceTargetServiceImpl extends ServiceImpl<PerformanceTargetMapper, PerformanceTarget> implements PerformanceTargetService {

    private static final Integer OTEHER_SYSTEM = -1;

    private static final String PUB_IMAGE = "PUB";

    @Resource
    private PerformanceTargetMapper performanceTargetMapper;
    @Resource
    private AssetService assetService;
    @Resource
    private SpecDictionaryService specDictionaryService;


    @Override
    public void setPIng(String id) {
        PerformanceTarget performanceTarget = performanceTargetMapper.selectById(id);
        if (ObjectUtil.isNotNull(performanceTarget.getPingTunnel()) && performanceTarget.getPingTunnel() == 1) {
            performanceTargetMapper.setPING(id, 0);
        } else {
            performanceTargetMapper.setPING(id, 1);
        }

    }

    /**
     * @description: 通过资产ID获取资产采集指标
     * @author: HanHW
     * @date: 2023/11/16 14:17
     * @param: [assetId]
     * @return: java.util.List<com.jcca.admin.system.entity.PerformanceTarget>
     */
    @Override
    public List<PerformanceTarget> getListByAssetIdV2(String assetId) {
        Asset asset = assetService.getById(assetId);

        Integer collectionType = asset.getCollectionType();
        String assetImage = asset.getAssetImage();
        Integer assetMode = asset.getAssetMode();
        Integer manufacturerId = asset.getManufacturerId();
        if (Objects.isNull(collectionType)) {
            collectionType = OTEHER_SYSTEM;
            asset.setCollectionType(OTEHER_SYSTEM);
        }

        SpecDictionary dict = specDictionaryService.queryDictByAsset(asset);

        //查询有无通用
        if (Objects.isNull(dict)) {
            asset.setAssetImage(PUB_IMAGE);
            dict = specDictionaryService.queryDictByAsset(asset);
        }

        if (Objects.isNull(dict)) {
            String msg = "SPEC_DICT 表缺少指标:[assetMode:" + assetMode + ",assetImage:" + assetImage + ",manufacturerId:" + manufacturerId + ",collectionType:" + collectionType + "]";
            AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_CONFIG, asset.getName(), msg);
            return new ArrayList<>();
        }

        QueryWrapper<PerformanceTarget> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SPEC_ID", dict.getSpecId());
        queryWrapper.eq("IS_AVAILABLE", StatusConst.OK);
        List<PerformanceTarget> targerList = list(queryWrapper);
        if (targerList.isEmpty()) {
            String msg = "PERFORMANCE_TARGET 表缺少指标:[assetMode:" + assetMode + ",assetImage:" + assetImage + ",manufacturerId:" + manufacturerId + ",collectionType:" + collectionType + "]";
            AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_CONFIG, asset.getName(), msg);
            return new ArrayList<>();
        }

        return targerList;
    }
}

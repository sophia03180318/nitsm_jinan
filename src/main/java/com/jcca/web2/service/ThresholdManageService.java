package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.ThresholdAssetListDto;
import com.jcca.web2.dto.ThresholdManageQuery;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.vo.AssetBaseInfoVo;
import com.jcca.web2.vo.ThresholdManageVo;

import java.util.List;

/**
 * @author HanHW
 * @description 阈值管理
 * @className ThresholdManageService
 * @date 2023/12/15 15:26
 * @since 2.1.0.0
 */
public interface ThresholdManageService extends IService<ThresholdManage> {

    /**
     * 获取阈值管理列表
     *
     * @param manage 参数
     * @return ThresholdManage
     */
    List<ThresholdManageVo> getList(ThresholdManageQuery manage);

    /**
     * 保存阈值数据
     *
     * @param manage 阈值数据
     */
    void add(ThresholdManage manage);

    /**
     * 获取资产ID 名称
     *
     * @param dto 查询条件
     * @return id  name
     */
    List<AssetBaseInfoVo> getAssetList(ThresholdAssetListDto dto);

    /**
     * 查询所有打量阈值
     *
     * @return ThresholdManageVo
     */
    List<ThresholdManageVo> batchList(ThresholdAssetListDto dto);
}

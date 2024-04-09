package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.dto.ThresholdAssetListDto;
import com.jcca.web2.dto.ThresholdManageQuery;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.vo.ThresholdManageVo;

import java.util.List;

/**
 * @author HanHW
 * @description 阈值管理
 * @className ThresholdManageMapper
 * @date 2023/12/15 15:28
 * @since 2.1.0.0
 */
public interface ThresholdManageMapper extends BaseMapper<ThresholdManage> {


    List<ThresholdManageVo> getList(ThresholdManageQuery manage);

    List<ThresholdManageVo> batchList(ThresholdAssetListDto dto);
}

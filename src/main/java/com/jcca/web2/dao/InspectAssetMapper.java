package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.InspectAsset;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectAssetMapper
 * @date 2025/5/19 17:34
 * @since 2.1.6.0
 */
public interface InspectAssetMapper extends BaseMapper<InspectAsset> {

    List<InspectAsset> getInspectAssets(@Param("assetIds") List<String> assetIds);
}

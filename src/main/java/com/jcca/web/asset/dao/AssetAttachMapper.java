package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.AssetAttach;
import org.apache.ibatis.annotations.Select;

/**
 * @author hanwone
 * @date 2020-04-26 15:35:31
 **/
public interface AssetAttachMapper extends BaseMapper<AssetAttach> {
    @Select("SELECT * from ASSET_ATTACH WHERE ASSET_ID=#{assetId}")
    AssetAttach getByAssetId(String assetId);
}

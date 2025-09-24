package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.AssetHidConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssetHidConfMapper extends BaseMapper<AssetHidConf> {


    /**
     * 通过资产和类型查询所有配置隐藏的信息
     * @param assetId
     * @param type
     * @return
     */
    List<AssetHidConf> getFlagListByAsset(@Param("assetId") String assetId,@Param("type") String type);

    /**
     * 通过 标记和资产ID查询是否存在配置的信息。
     * @param assetId
     * @param flag
     * @return
     */
    List<AssetHidConf> selectListByAssetAndFlag(@Param("assetId") String assetId,@Param("flag") String flag);

}

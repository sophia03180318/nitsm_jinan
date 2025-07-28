package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.web.asset.entity.Asset;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 14:53 2021/8/20
 * @ Description:
 */
public interface SpecDictionaryMapper extends BaseMapper<SpecDictionary> {

    @Select("SELECT * FROM SPEC_DICT WHERE ASSET_MODE = #{asset.assetMode} AND ASSET_IMAGE = #{asset.assetImage} " +
            "AND MANUFACTURER_ID = #{asset.manufacturerId} AND SYSTEM_TYPE = #{asset.collectionType}")
    SpecDictionary queryDictByAsset(@Param("asset") Asset asset);

    @Select("SELECT * FROM SPEC_DICT WHERE MANUFACTURER_ID = #{menufactureId}")
    List<SpecDictionary> findByManufacturerId(String menufactureId);
}

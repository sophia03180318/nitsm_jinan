package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.AssetModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 资产型号
 * @author: sophia
 * @create: 2023/11/01 10:20
 **/
public interface AssetModelMapper extends BaseMapper<AssetModel> {



    List<AssetModel> getByManufacturerId(String manufacturerId);

    AssetModel getByName(String assetImage);
}
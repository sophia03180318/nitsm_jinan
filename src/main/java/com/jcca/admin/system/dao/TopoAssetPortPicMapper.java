package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.TopoAssetPortPic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author syt
 * @date 2021-07-15 18:14:39
 **/
public interface TopoAssetPortPicMapper extends BaseMapper<TopoAssetPortPic> {

    @Delete("delete from TOPO_ASSET_PORT_PIC where ASSET_ID=#{assetId}")
    Boolean deleteAssetPortPic(String assetId);


    @Select("select * from TOPO_ASSET_PORT_PIC where ASSET_ID=#{assetId}")
    List<TopoAssetPortPic> queryAssetPortPic(String assetId);
}

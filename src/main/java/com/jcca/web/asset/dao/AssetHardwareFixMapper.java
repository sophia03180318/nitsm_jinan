package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.controller.bean.AssetHardwareFixReq;
import com.jcca.web.asset.entity.AssetHardwareFix;
import com.jcca.web.asset.vo.AssetHardwareFixExportVo;
import com.jcca.web.asset.vo.AssetHardwareFixVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-07-16 18:05:21
 **/
public interface AssetHardwareFixMapper extends BaseMapper<AssetHardwareFix> {


    List<AssetHardwareFixVo> findByPage(AssetHardwareFixReq req);

    List<AssetHardwareFixExportVo> findExport(AssetHardwareFixReq req);

    Long countItem(AssetHardwareFixReq req);

    @Select("select * from asset_hardware_fix where asset_id =#{assetId} order by CREATE_TIME desc")
    List<AssetHardwareFix> getByListAssetId(String assetId);

}

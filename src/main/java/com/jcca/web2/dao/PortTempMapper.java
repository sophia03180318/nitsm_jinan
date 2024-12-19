package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.dto.PortModelTemp;
import com.jcca.web2.entity.PortTemp;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 端口模板
 * @author: sophia
 * @create: 2023/11/01 10:20
 **/
public interface PortTempMapper extends BaseMapper<PortTemp> {
    @Select("select id ,model as name, path, ASSET_UNIT as unit from ASSET_MODEL where  ASSET_MODE_ID in (select  id from asset_mode where code = '201' or code ='42') order by name")
    List<PortModelTemp> getNetworkDevices();

    @Select("select * from PORT_TEMPLATE where MODEL_ID in (select id from ASSET_MODEL where  model =(select ASSET_IMAGE from asset where id=#{assetId} ))")
    List<PortTemp> getTemplateByAssetId(String assetId);

}
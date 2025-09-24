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
    List<PortModelTemp> getNetworkDevices();

    List<PortTemp> getTemplateByAssetId(String assetId);

}
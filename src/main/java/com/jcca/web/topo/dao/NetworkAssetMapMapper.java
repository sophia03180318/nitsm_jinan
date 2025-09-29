package com.jcca.web.topo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.topo.entity.NetworkAssetMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * topo 自动发现
 *
 * @author lyp
 */
@Mapper
public interface NetworkAssetMapMapper extends BaseMapper<NetworkAssetMap> {

    /**
     * 查询localip链接的远端设备端口
     *
     * @param localip
     * @param remoteip
     * @return
     */
    String queryPort(String localip, String remoteip);

    Integer countByAssetId(@Param("assetId") String assetId);

}

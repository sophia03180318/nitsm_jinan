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
    @Select("select e.REMOTE_PORT from M_NETWORK_ASSET_MAP e where e.LOCALHOST_IP=#{localip} and e.REMOTE_DEVICE_ID in (select t.LOCAL_DEVICE_ID from M_NETWORK_ASSET_MAP t where t.LOCALHOST_IP=#{remoteip})")
    String queryPort(String localip, String remoteip);

    @Select("select count(*) from M_NETWORK_ASSET_MAP e where e.LOCALHOST_IP = (select t.ip from asset t where t.id=#{assetId})")
    Integer countByAssetId(@Param("assetId") String assetId);

}

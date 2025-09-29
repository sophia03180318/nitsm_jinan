package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.bean.CollectNetworkCardVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 采集网卡数据
 *
 * @author Lvyp
 */
@Mapper
public interface CollectNetworkCardMapper extends BaseMapper<CollectNetworkCard> {

    /**
     * 查询实时网卡信息
     *
     * @param assetId
     */
    List<CollectNetworkCard> selectRealTimeData(@Param("assetId") String assetId);

    /**
     * 通过IP查询最后一次记录
     *
     * @param ip
     * @return
     */
    List<CollectNetworkCard> findLasterMacAddressByIp(@Param("ip") String ip);

    /**
     * 通过MAC地址查询命中的列表
     *
     * @param atPhysAddress
     * @return
     */
    CollectNetworkCard selectByMacAddress(@Param("atPhysAddress") String atPhysAddress);

    /**
     * 通过MAC地址查询命中的列表
     *
     * @param atPhysAddress
     * @return
     */
    List<CollectNetworkCard> selectByMacAddressAndAssetId(@Param("atPhysAddress") String atPhysAddress, @Param("assetId") String assetId);


    List<CollectNetworkCardVo> getRealTimeDataAllCenterPc();

    /**
     * 更新网卡状态
     * @param assetId
     * @param ip
     * @return
     */
    Integer updateNetCardStatus(@Param("assetId") String assetId, @Param("ip") String ip, @Param("status") Byte status, @Param("seachStatus") Byte seachStatus);

    CollectNetworkCard findByNetworkName(String networkName, String assetId);
}

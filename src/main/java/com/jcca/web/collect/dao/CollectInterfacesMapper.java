package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.graph.vo.StatisticsInfoVo;
import com.jcca.web.graph.vo.TopoPortInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 硬件端口
 *
 * @author Lvyp
 */
@Mapper
public interface CollectInterfacesMapper extends BaseMapper<CollectInterfaces> {

    /**
     * 查询资产当前对应最新的数据
     *
     * @param assetId
     * @return
     */
    @Select("SELECT * FROM COLLECT_INTERFACES b WHERE b.COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code FROM COLLECT_INTERFACES WHERE ASSET_ID=#{assetId}) order by b.PORT_INDEX_RANK")
    List<CollectInterfaces> selectRealTimeData(@Param("assetId") String assetId);

    /**
     * 汇总开始-结束时间之间的数据
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("SELECT MAX(ID) AS ID,MAX(COLLECT_TIME) AS COLLECT_TIME,MAX(to_number(COLLECT_CODE)) AS COLLECT_CODE,ASSET_ID,ROUND(AVG(PORT_IN)) AS PORT_IN,ROUND(AVG(PORT_OUT)) AS PORT_OUT,ROUND(AVG(DISCARD_PACKETS_IN)) AS DISCARD_PACKETS_IN,ROUND(AVG(DISCARD_PACKETS_OUT)) AS DISCARD_PACKETS_OUT,ROUND(AVG(NO_UNICAST_PACKETS_IN)) AS NO_UNICAST_PACKETS_IN,ROUND(AVG(NO_UNICAST_PACKETS_OUT)) AS NO_UNICAST_PACKETS_OUT,ROUND(AVG(UNICAST_PACKETS_IN)) AS UNICAST_PACKETS_IN,ROUND(AVG(UNICAST_PACKETS_OUT)) AS UNICAST_PACKETS_OUT,ROUND(AVG(ERROR_CODE_IN)) AS ERROR_CODE_IN,ROUND(AVG(ERROR_CODE_OUT)) AS ERROR_CODE_OUT,ROUND(AVG(PORT_SPEED),2) AS PORT_SPEED,ROUND(AVG(PORT_IN_SPEED),0) AS PORT_IN_SPEED,ROUND(AVG(PORT_OUT_SPEED),0) AS PORT_OUT_SPEED,ROUND(AVG(LOSE_PACKETS_OUT_RATE),2) AS LOSE_PACKETS_OUT_RATE,ROUND(AVG(LOSE_PACKETS_IN_RATE),2) AS LOSE_PACKETS_IN_RATE,ROUND(AVG(ERRO_CODE_OUT_RATE),2) AS ERRO_CODE_OUT_RATE,ROUND(AVG(ERRO_CODE_IN_RATE),2) AS ERRO_CODE_RATE_OUT,MAX(CREATE_TIME) AS CREATE_TIME FROM COLLECT_INTERFACES WHERE COLLECT_TIME BETWEEN #{startDate} AND #{endDate} GROUP BY ASSET_ID")
    List<CollectInterfaces> statisticsGroupByAsset(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Select(" SELECT CRC_ERRORS as crcErrors ,status,port_name as portName,PORT_INDEX_RANK as portIndexRank,link_phy_address as phyAddress,round(port_speed/1000/1000) as portSpeed,b.TX_POWER as txPower,b.RX_POWER as rxPower FROM COLLECT_INTERFACES b WHERE b.COLLECT_CODE=#{collectCode} and (b.PORT_INDEX=#{portName} or b.PORT_NAME=#{portName})")
    TopoPortInfoVo selectPortIndex(@Param("assetId") String assetId, @Param("portName") String portName, @Param("collectCode") String collectCode);

    /**
     * 查询资产
     *
     * @param remoteIp
     * @return
     */
    @Select("select * from COLLECT_INTERFACES t where t.LINK_IP=#{remoteIp}")
    List<CollectInterfaces> findByLinkIp(@Param("remoteIp") String remoteIp);

    /**
     * 查询端口流入速率 单位换算为 bits/sec
     *
     * @param assetId
     * @param portIndex
     * @return
     */

    @Select("select * from ( select round(PORT_IN_SPEED/8000,2) as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portIndex} order by COLLECT_TIME desc) where rownum<#{rownum}")
    List<StatisticsInfoVo> selectLinePortIn(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);

    /**
     * 查询端口流出速率 单位换算为 bits/sec
     *
     * @param assetId
     * @param portIndex
     * @return
     */
    @Select("select * from ( select round(PORT_OUT_SPEED/8000,2) as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portIndex} order by COLLECT_TIME desc) where rownum<#{rownum}")
    List<StatisticsInfoVo> selectLinePortOut(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);


    /**
     * 通过资产ID和端口排序查找端口采集信息
     *
     * @param assetId
     * @param portIndexRank
     * @return
     */
    @Select("select * from COLLECT_INTERFACES e where e.id = (select max(id) from COLLECT_INTERFACES where ASSET_ID=#{assetId} and PORT_INDEX_RANK = #{portIndexRank} )")
    CollectInterfaces selectByAssetIdAndPortRank(@Param("assetId") String assetId, @Param("portIndexRank") String portIndexRank);


    @Select("select * from ( select RX_POWER as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portIndex} and RX_POWER is not null order by COLLECT_TIME desc) where rownum<#{rownum}")
    List<StatisticsInfoVo> selectLinePortDbmIn(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);


    @Select("select * from ( select TX_POWER as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portIndex} and TX_POWER is not null order by COLLECT_TIME desc) where rownum<#{rownum}")
    List<StatisticsInfoVo> selectLinePortDbmOut(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);

    @Select("select * from collect_interfaces where ASSET_ID=#{assetId} and PORT_NAME=#{portName}")
    List<CollectInterfaces> selectByAssetAndPortName(@Param("assetId") String assetId, @Param("portName") String portName);

    @Select("select portName from collect_interfaces where ASSET_ID=#{assetId} and port_index=#{portFullName} ")
    List<String> selectPortNameByFullName(@Param("assetId") String assetId, @Param("portFullName") String portFullName);



    @Select("select i.asset_id assetId, i.port_in portIn, i.port_out portOut, i.port_index portIndex, i.status portStatus, i.port_index_rank portIndexRank, '2' as maOrAt, " +
            "r.port_ip portIp,  r.at_asset_id atAssetId, r.at_port_index_name atPortIndexName, r.at_name atName, r.at_net_address atNetAddress, " +
            "r.at_phys_address atPhysAddress from collect_interfaces i " +
            "left join collect_route r on i.asset_id = r.asset_id and i.port_index = r.port_index_name " +
            "where i.port_type in (6,18,22,23,39,56,135) and port_index not like 'unrouted%' " +
            "and i.collect_code in (select MAX(collect_code) from collect_interfaces group by asset_id) order by i.asset_id, i.port_index_rank")


    List<AssetLinkAssetVo> findAtAssetAndPort();
}

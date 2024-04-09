package com.jcca.web.statistics.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.graph.vo.StatisticsInfoVo;
import com.jcca.web.statistics.entity.HourInterfacesItem;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 端口明细一小时统计
 *
 * @author Lvyp
 */
public interface HourInterfacesItemMapper extends BaseMapper<HourInterfacesItem> {

    /**
     * 查询最大的创建时间
     *
     * @return
     */
    @Select("SELECT MAX(CREATE_TIME) FROM HOUR_INTERFACES_ITEM")
    Date maxCreateDate();


    /**
     * 端口流入量查询
     *
     * @param assetId
     * @return
     */
    @Select("select * from ( select round(port_in_count/8/1024/1024,2) as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES" +
            " where ASSET_ID = #{assetId} and port_index= #{portName} order by COLLECT_TIME desc) where rownum<30")
    List<StatisticsInfoVo> portIns(String assetId, String portName);

    /**
     * 端口流出量查询
     *
     * @param assetId
     * @return
     */
    @Select("select * from( select round(port_out_count/8/1024/1024,2) as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES" +
            " where ASSET_ID = #{assetId} and port_index= #{portName} order by COLLECT_TIME desc) where rownum<30")
    List<StatisticsInfoVo> portOuts(String assetId, String portName);

    /**
     * 端口流入丢包数
     */
    @Select("select * from( select LOSE_PACKETS_IN_RATE as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portName} order by COLLECT_TIME desc) where rownum<30")
    List<StatisticsInfoVo> discardPackageIns(String assetId, String portName);


    /**
     * 端口流出丢包数
     */
    @Select(" select * from (select LOSE_PACKETS_OUT_RATE as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portName} order by COLLECT_TIME desc) where  rownum<30")
    List<StatisticsInfoVo> discardPackageOuts(String assetId, String portName);

    /**
     * 端口流入误码数
     */
    @Select("select * from  (select ERRO_CODE_IN_RATE as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portName} order by COLLECT_TIME desc) where  rownum<30")
    List<StatisticsInfoVo> errorCodeIns(String assetId, String portName);

    /**
     * 端口流出误码数
     */
    @Select(" select * from  (select ERRO_CODE_OUT_RATE as value ,COLLECT_TIME as endTime from COLLECT_INTERFACES where ASSET_ID = #{assetId} and port_index= #{portName} order by COLLECT_TIME desc) where  rownum<30")
    List<StatisticsInfoVo> errorCodeOuts(String assetId, String portName);

}

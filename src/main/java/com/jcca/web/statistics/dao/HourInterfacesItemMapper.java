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
    Date maxCreateDate();


    /**
     * 端口流入量查询
     *
     * @param assetId
     * @return
     */
    List<StatisticsInfoVo> portIns(String assetId, String portName);

    /**
     * 端口流出量查询
     *
     * @param assetId
     * @return
     */
    List<StatisticsInfoVo> portOuts(String assetId, String portName);

    /**
     * 端口流入丢包数
     */
    List<StatisticsInfoVo> discardPackageIns(String assetId, String portName);


    /**
     * 端口流出丢包数
     */
    List<StatisticsInfoVo> discardPackageOuts(String assetId, String portName);

    /**
     * 端口流入误码数
     */
    List<StatisticsInfoVo> errorCodeIns(String assetId, String portName);

    /**
     * 端口流出误码数
     */
    List<StatisticsInfoVo> errorCodeOuts(String assetId, String portName);

}

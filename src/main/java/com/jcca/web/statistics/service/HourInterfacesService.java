package com.jcca.web.statistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.graph.vo.StatisticsInfoVo;
import com.jcca.web.statistics.entity.HourInterfaces;

import java.util.Date;
import java.util.List;

/**
 * 端口1小时统计
 *
 * @author Lvyp
 */
public interface HourInterfacesService extends IService<HourInterfaces> {

    /**
     * 获取最后一次统计的时间
     *
     * @return
     */
    Date lastCreateTime();

    /**
     * 按时间区间查询资产端口流量折线数据
     *
     * @param assetId
     * @param startDate
     * @param endDate
     * @return
     */
    List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate);


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
     *
     * @param assetId
     * @return
     */

    List<StatisticsInfoVo> discardPackageIns(String assetId, String portName);

    /**
     * 端口流出丢包数
     *
     * @param assetId
     * @return
     */
    List<StatisticsInfoVo> discardPackageOuts(String assetId, String portName);


    /**
     * 端口流入误码数
     *
     * @param assetId
     * @return
     */
    List<StatisticsInfoVo> errorCodeIns(String assetId, String portName);

    /**
     * 端口流出误码数
     *
     * @param assetId
     * @return
     */
    List<StatisticsInfoVo> errorCodeOuts(String assetId, String portName);
}

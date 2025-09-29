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
    List<CollectInterfaces> selectRealTimeData(@Param("assetId") String assetId);

    /**
     * 汇总开始-结束时间之间的数据
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<CollectInterfaces> statisticsGroupByAsset(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    TopoPortInfoVo selectPortIndex(@Param("assetId") String assetId, @Param("portName") String portName, @Param("collectCode") String collectCode);

    /**
     * 查询资产
     *
     * @param remoteIp
     * @return
     */
    List<CollectInterfaces> findByLinkIp(@Param("remoteIp") String remoteIp);

    /**
     * 查询端口流入速率 单位换算为 bits/sec
     *
     * @param assetId
     * @param portIndex
     * @return
     */

    List<StatisticsInfoVo> selectLinePortIn(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);

    /**
     * 查询端口流出速率 单位换算为 bits/sec
     *
     * @param assetId
     * @param portIndex
     * @return
     */
    List<StatisticsInfoVo> selectLinePortOut(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);


    /**
     * 通过资产ID和端口排序查找端口采集信息
     *
     * @param assetId
     * @param portIndexRank
     * @return
     */
    CollectInterfaces selectByAssetIdAndPortRank(@Param("assetId") String assetId, @Param("portIndexRank") String portIndexRank);


    List<StatisticsInfoVo> selectLinePortDbmIn(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);


    List<StatisticsInfoVo> selectLinePortDbmOut(@Param("assetId") String assetId, @Param("portIndex") String portIndex, @Param("rownum") Integer rownum);

    List<CollectInterfaces> selectByAssetAndPortName(@Param("assetId") String assetId, @Param("portName") String portName);

    List<String> selectPortNameByFullName(@Param("assetId") String assetId, @Param("portFullName") String portFullName);

    List<AssetLinkAssetVo> findAtAssetAndPort();
}

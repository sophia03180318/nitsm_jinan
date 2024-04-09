package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.graph.vo.StatisticsInfoVo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 硬件端口
 *
 * @author Lvyp
 */
public interface CollectInterfacesService extends IService<CollectInterfaces> {

    /**
     * 更新实时数据
     *
     * @param datas
     */
    void updateRealTimeData(List<CollectInterfaces> datas);

    /**
     * 查询所有的端口
     *
     * @param assetId
     * @return
     */
    List<CollectInterfaces> getRealTimeData(String assetId);

    /**
     * 获取数据库中保存的端口的状态信息
     * @param assetId
     * @return
     */
    Map<Integer,Boolean> getDbInterfacesStatus(String assetId);

    /**
     * 获取告警匹配码
     *
     * @param interfaces
     * @return
     */
    String getAlarmCode(CollectInterfaces interfaces, String type);

    /**
     * 删除最新一条数据往前两小时的数据
     *
     * @param hour
     * @return
     */
    Boolean removeBeforeData(Integer hour);

    /**
     * 根据时间对资产的端口数据进行汇总
     *
     * @param lastCreateDate
     * @param now
     * @return
     */
    List<CollectInterfaces> statistics(Date lastCreateDate, Date now);

    /**
     * 查询通过端口的LINKIP
     *
     * @param remoteIp
     * @return
     */
    CollectInterfaces findInterfaceByLinkIp(String remoteIp);

    /**
     * 折线图 端口流入
     *
     * @param assetId
     * @param portName
     * @return
     */
    List<StatisticsInfoVo> selectLinePortIn(String assetId, String portName);

    /**
     * 折线图 端口光功率
     *
     * @param assetId
     * @param portName
     * @return
     */
    List<StatisticsInfoVo> selectLinePortDbmIn(String assetId, String portName);

    /**
     * 折线图 端口光功率
     *
     * @param assetId
     * @param portName
     * @return
     */
    List<StatisticsInfoVo> selectLinePortDbmOut(String assetId, String portName);

    /**
     * 折线图 端口流出
     *
     * @param string
     * @param portName
     * @return
     */
    List<StatisticsInfoVo> selectLinePortOut(String string, String portName);

    /**
     * 查询端口信息
     *
     * @param assetId
     * @param portIndexRank
     * @return
     */
    CollectInterfaces findByAssetIdAndPortRank(String assetId, String portIndexRank);

    /**
     * 计算端口最新流量KB/S
     *
     * @param type 1流入 -1 流出
     * @return
     */
    Double getPortFlow(String assetId, String portIndex, Integer type);

    List<CollectInterfaces> filterPort(String assetId);

    /**
     * 查询端口信息
     * @param assetId
     * @param portName
     * @return
     */
    List<CollectInterfaces> selectByAssetAndPortName(String assetId, String portName);

    /**
     * 查询
     *
     * @param assetId
     * @param portFullName
     * @return
     */
    String getPortNameByFullName(String assetId, String portFullName);
}

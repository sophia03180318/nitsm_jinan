package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.enums.CollectNetCardStatus;

import java.util.List;

/**
 * 采集网卡数据
 *
 * @author Lvyp
 */
public interface CollectNetworkCardService extends IService<CollectNetworkCard> {

    /**
     * 更新实时数据
     *
     * @param entityList
     */
    void updateRealTimeData(List<CollectNetworkCard> entityList);

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectNetworkCard> getRealTimeData(String assetId);

    /**
     * 生成告警编号
     *
     * @param net
     * @return
     */
    String getAlarmCode(CollectNetworkCard net);

    /**
     * 批量更新
     *
     * @param entityList 资产id必须相同。
     */
    void updateBatchByAssetId(List<CollectNetworkCard> entityList);

    /**
     * 通过MAC地址查找网卡信息
     *
     * @param atPhysAddress
     * @return
     */
    CollectNetworkCard findByMacAddress(String atPhysAddress);


    /**
     * 通过网卡名名查找网卡信息
     *
     * @param networkName
     * @return
     */
    CollectNetworkCard findByNetworkName(String networkName,String assetId);


    /**
     * 查询网卡
     * @param macAddr
     * @param assetId
     * @return
     */
    List<CollectNetworkCard> selectByMacAddressAndAssetId(String macAddr, String assetId);

    /**
     * 更新网卡状态
     * 只更新UP或者DOWN状态未知状态不会更新
     *
     * @param assetId
     * @param ip
     * @param status
     */
    Integer updateNetCardStatus(String assetId, String ip, CollectNetCardStatus status);

    /**
     * 通过资产ID更新
     *
     * @param net
     */
    void updateByAssetId(CollectNetworkCard net);

    /**
     * 通过IP查询网卡
     * @param linkAssetIp
     * @return
     */
    CollectNetworkCard getOneByIp(String linkAssetIp);
}

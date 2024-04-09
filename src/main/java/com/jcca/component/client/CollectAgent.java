package com.jcca.component.client;

import com.jcca.component.client.bean.CollectNodesMsg;
import com.jcca.component.client.bean.CollectTestReq;
import com.jcca.component.client.bean.CollectTestResp;
import com.jcca.component.client.bean.CollectorCenterAssetMsg;
import com.jcca.component.client.enums.RealTimePingStatusEnum;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.web.asset.entity.Asset;

import java.io.IOException;
import java.util.List;

/**
 * 采集器客户端
 *
 * @author Lvyp
 */
public interface CollectAgent {

    /**
     * 向主采集器发送车站采集器变动信息
     */
    void sendDeleteStaion(String Url) throws CollectAgencyException;

    /**
     * 从主节点获取当前各个节点状态
     *
     * @return
     * @throws CollectAgencyException
     */
    List<CollectNodesMsg> getCollectNodeMsg() throws CollectAgencyException;

    /**
     * 采集器上的资产分配信息
     *
     * @return
     * @throws CollectAgencyException
     */
    List<CollectorCenterAssetMsg> getCenterAssetList() throws CollectAgencyException;

    /**
     * 实时ping
     *
     * @param asset
     * @return
     */
    RealTimePingStatusEnum realTimePing(Asset asset) throws IOException;

    /**
     * 向中心采集器发送Post消息并获取返回信息
     * @param uri
     * @param body
     * @param readTimeOut
     * @return
     * @throws CollectAgencyException
     */
    String sendPostToCenter(String uri, String body, int readTimeOut) throws CollectAgencyException;
}

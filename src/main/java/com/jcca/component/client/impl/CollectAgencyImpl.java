package com.jcca.component.client.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.RestBean;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.TestIpUtil;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.bean.CollectNodesMsg;
import com.jcca.component.client.bean.CollectorCenterAssetMsg;
import com.jcca.component.client.enums.RealTimePingStatusEnum;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.component.client.exception.CollectAgencyException.CollectAgencyEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.nodes.dao.CenterNodesMapper;
import com.jcca.web.nodes.entity.CenterNodes;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 采集器客户端实现
 *
 * @author Lvyp
 */
@Service
public class CollectAgencyImpl implements CollectAgent {

    private static final String GET_NODES_MSG_URL = "/monitor/collectNodeMonitorList";
    private static final String GET_CENTER_ASSET_MSG_URL = "/monitor/getNodeAsset";
    private static final String REAL_TIME_PING_URL = "/business/currAssetPingState";
    private static final String REAL_TIME_PING_STATION_URL = "/business/runItem";
    private static final String DELETE_STATION_URI = "/collectNode/deleteCollector";

    @Resource
    private RedisService redisService;
    @Resource
    private SysOrgService orgServ;
    @Resource
    private StationService stationServ;
    @Resource
    private CenterNodesMapper centerNodesMapper;


    @Override
    public void sendDeleteStaion(String url) throws CollectAgencyException {
        JSONObject reqJson = new JSONObject();
        reqJson.put("data", url);
        String reqStr = JSONUtil.toJsonStr(reqJson);
        sendPostToCenter(DELETE_STATION_URI, reqStr, 10 * 1000);
    }

    @Override
    public List<CollectNodesMsg> getCollectNodeMsg() throws CollectAgencyException {
        JSONObject reqJson = new JSONObject();
        reqJson.put("page", 1);
        reqJson.put("limit", 50);
        reqJson.put("search", "");

        String reqStr = JSONUtil.toJsonStr(reqJson);

        String body = sendPostToCenter(GET_NODES_MSG_URL, reqStr, 15 * 1000);

        JSONObject respJson = JSONUtil.parseObj(body);
        String code = respJson.getStr("code");
        String msg = respJson.getStr("msg");
        String data = respJson.getStr("data");

        if (!"0".equals(code)) {
            AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, "ITSM从采集器主节点获取节点状态失败", msg);
            throw new CollectAgencyException(code, msg);
        }

        JSONObject dataJson = JSONUtil.parseObj(data);
        JSONArray list = dataJson.getJSONArray("list");

        List<CollectNodesMsg> msgList = JSONUtil.toList(list, CollectNodesMsg.class);
        for (CollectNodesMsg collectNodesMsg : msgList) {
            String[] split = collectNodesMsg.getNodeUrl().split(":");
            if (split.length == 2) {
                collectNodesMsg.setNodeIp(split[0].trim());
            } else if (split.length == 3) {
                String trim = split[1].trim();
                String[] proxyUrl = trim.split("/");
                if (proxyUrl.length == 2) {
                    collectNodesMsg.setNodeIp(proxyUrl[1]);
                }
            }
        }

        return msgList;
    }


    @Override
    public List<CollectorCenterAssetMsg> getCenterAssetList() throws CollectAgencyException {
        JSONObject reqJson = new JSONObject();
        reqJson.put("page", 1);
        reqJson.put("limit", 10000);
        reqJson.put("search", "");

        String reqStr = JSONUtil.toJsonStr(reqJson);

        String body = sendPostToCenter(GET_CENTER_ASSET_MSG_URL, reqStr, 15 * 1000);
        if (StrUtil.isEmpty(body)) {
            throw new CollectAgencyException(CollectAgencyEnum.RESPONSE_FORMAT_ERRO);
        }
        JSONObject respJson = JSONUtil.parseObj(body);
        String code = respJson.getStr("code");
        String msg = respJson.getStr("msg");
        String data = respJson.getStr("data");

        if (!"0".equals(code)) {
            AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, "ITSM从采集器主节点获取资产信息列表失败", msg);
            throw new CollectAgencyException(code, msg);
        }

        JSONArray list = JSONUtil.parseArray(data);

        return JSONUtil.toList(list, CollectorCenterAssetMsg.class);
    }


    @Override
    public RealTimePingStatusEnum realTimePing(Asset asset) throws IOException {
        List<String> needPingIp = new ArrayList<String>();
        String ip = asset.getIp();
        String ip2 = asset.getIp2();
        if (StrUtil.isEmpty(ip) && StrUtil.isEmpty(ip2)) {
            return RealTimePingStatusEnum.UNKNOW;
        }
        if (StrUtil.isNotEmpty(ip)) {
            needPingIp.add(ip);
        }

        if (StrUtil.isNotEmpty(ip2)) {
            needPingIp.add(ip2);
        }

        SysOrg sysorg = orgServ.getById(asset.getOrgId());
        // 无组织设备
        if (Objects.isNull(sysorg)) {
            return itsmSendPing(needPingIp);
        }

        // 中心设备
        JSONObject reqJson = new JSONObject();
        if (OrgTypeEnum.CENTER.getCode() == sysorg.getType()) {
            return realTimePingCenterAsset(needPingIp, reqJson);
        } else if (OrgTypeEnum.STATION.getCode() == sysorg.getType()) {
            // 车站
            return realtimePingStation(needPingIp, sysorg, reqJson);
        }

        // 其他设备
        return itsmSendPing(needPingIp);
    }

    /**
     * 发送消息到中心采集器主节点
     *
     * @return
     */
    @Override
    public String sendPostToCenter(String uri, String body, int readTimeOut) throws CollectAgencyException {
        Object masterIp = redisService.get(OutConst.COLLECT_MASTER_URL);
        if (Objects.isNull(masterIp)) {
            throw new CollectAgencyException(CollectAgencyEnum.GET_MASTER_IP_ERRO);
        }
        String[] masterArray = masterIp.toString().split(":");
        String masterIpStr = masterArray[0];
        String masterPortStr = masterArray[1];

        List<String> ipList = Collections.singletonList(masterIpStr);
        CenterNodes centerNodes = centerNodesMapper.selectNodes(masterIpStr);
        if (Objects.nonNull(centerNodes)) {
            ipList = new ArrayList<String>();
            if (StrUtil.isNotEmpty(centerNodes.getNodeIp1())) {
                ipList.add(centerNodes.getNodeIp1());
            }
            if (StrUtil.isNotEmpty(centerNodes.getNodeIp2())) {
                ipList.add(centerNodes.getNodeIp2());
            }
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "ITSM向中心采集器发送请求", uri + " " + body);
        for (String ip : ipList) {
            try {
                String respBody = HttpUtil.createPost("http://" + ip + ":" + masterPortStr + uri).body(body).contentType(ContentType.APPLICATION_JSON.getMimeType()).setConnectionTimeout(5 * 1000).setReadTimeout(readTimeOut).execute().body();
                AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "ITSM请求中心采集器响应", respBody);
                return respBody;
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.ITSM_TO_COLLECTOR, "ITSM请求中心采集器主节点异常：" + ip, e.getMessage());
            }
        }

        throw new CollectAgencyException(CollectAgencyEnum.GET_MASTER_NET_ERRO);
    }

    /**
     * 实时ping 中心设备
     *
     * @param needPingIp
     * @param reqJson
     * @return
     */
    private RealTimePingStatusEnum realTimePingCenterAsset(List<String> needPingIp, JSONObject reqJson) {
        for (String item : needPingIp) {
            String requestIp = "";
            try {
                requestIp = getRequestIp() + REAL_TIME_PING_URL;
            } catch (CollectAgencyException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, "采集客户端实时PING异常", e);
                continue;
            }
            AppLogUtils.buildLogInfo(LogFunctionEnum.OUT_API, "采集客户端", requestIp);

            reqJson.put("ip", item);
            try {
                String sendPost = sendPostToCenter(REAL_TIME_PING_URL, reqJson.toString(), 10 * 1000);
                JSONObject body = JSONUtil.parseObj(sendPost);
                String code = body.getStr("code");
                int data = body.getInt("data");

                if ("0".equals(code)) {
                    if (0 == data) {
                        return RealTimePingStatusEnum.DOWN;
                    }
                    return RealTimePingStatusEnum.UP;
                }
            } catch (CollectAgencyException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, "采集客户端实时PING异常", e);
            }
        }

        return RealTimePingStatusEnum.UNKNOW;
    }

    /**
     * 实时从车站采集器发起ping
     *
     * @param needPingIp
     * @param sysorg
     * @param reqJson
     * @return
     */
    private RealTimePingStatusEnum realtimePingStation(List<String> needPingIp, SysOrg sysorg, JSONObject reqJson) throws IOException {
        Station station = stationServ.getById(sysorg.getId());
        if (Objects.isNull(station)) {
            return itsmSendPing(needPingIp);
        }

        int size = needPingIp.size();
        int trySize = 1;
        for (String item : needPingIp) {
            reqJson.put("assetIp", item);
            reqJson.put("runType", "ping");

            try {
                String sendPost = stationServ.sendPostToStation(REAL_TIME_PING_STATION_URL, sysorg.getId(), reqJson.toString());
                if (!JSONUtil.isJson(sendPost)) {
                    throw new CollectAgencyException(CollectAgencyEnum.RESPONSE_FORMAT_ERRO);
                }
                JSONObject body = JSONUtil.parseObj(sendPost);
                RestBean restbean = JSONUtil.toBean(body, RestBean.class);
                if (RestBean.SUCCESS.equals(restbean.getCode())) {
                    return RealTimePingStatusEnum.UP;
                } else if (size == trySize) {
                    //所有IP均已尝试
                    return RealTimePingStatusEnum.DOWN;
                }
                trySize++;
            } catch (CollectAgencyException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, "实时从车站采集器发起PING异常", e);
            }
        }

        return RealTimePingStatusEnum.UNKNOW;
    }

    /**
     * 实时从ITSM发起ping
     *
     * @param needPingIp
     * @return
     */
    private RealTimePingStatusEnum itsmSendPing(List<String> needPingIp) throws IOException {
        for (String item : needPingIp) {
            Boolean result = TestIpUtil.ping(item, 3);
            if (result) {
                return RealTimePingStatusEnum.UP;
            }
        }
        return RealTimePingStatusEnum.DOWN;
    }

    /**
     * 获取请求的IP地址
     *
     * @return
     * @throws CollectAgencyException
     */
    private String getRequestIp() throws CollectAgencyException {
        Object masterIp = redisService.get(OutConst.COLLECT_MASTER_URL);
        if (Objects.isNull(masterIp)) {
            throw new CollectAgencyException(CollectAgencyEnum.GET_MASTER_IP_ERRO);
        }
        return masterIp.toString();
    }

}

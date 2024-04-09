package com.jcca.component.thresholds.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.web.collect.entity.CollectCluster;
import com.jcca.web.collect.service.CollectClusterService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 处理集群采集回来的数据
 * @author lyp
 */
@Slf4j
@Component
public class DisposeClusterAdapterImpl  implements CollectAdapter {

    /**
     * 主备服务器状态
     * key: serverName value：当前状态
     */
    private static Map<String,Boolean> serverStatusMap = new HashMap<>();
    /**
     * 当前主节点
     * key:assetId value : serverName
     */
    private static Map<String,String> serverMaster = new HashMap<>();
    /**
     * 服务
     */
    private static final String SERVER_ROLE_MAIN = "main";
    private static final String NODE_STATUS_NORMAL = "OK";

    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private CollectClusterService clusterServ;



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void dispose(JSONArray data) {
        if(LogInputUtils.inputInfo(ServerTypeEnum.DISPOSE_CLUSTER)){
            log.info("处理集群消息："+data.toString());
        }
        List<CollectCluster> collectClusters = JSONUtil.toList(data, CollectCluster.class);
        if(Objects.isNull(collectClusters)||collectClusters.isEmpty()){
            return ;
        }
        List<CollectCluster> saveEntity = new ArrayList<CollectCluster>();
        for (CollectCluster collectCluster : collectClusters) {
            if(StrUtil.isEmpty(collectCluster.getServerName())||StrUtil.isEmpty(collectCluster.getServerIp())){
                continue ;
            }
            String serverRole = collectCluster.getServerRole();
            //分析主备是否切换，切换的话需要上报切换告警
            extracted(collectCluster, serverRole);
            //AB机状态告警
            extractedStatus(collectCluster);

            Date date = new Date();
            date.setTime(Long.valueOf(collectCluster.getCollectTimeStr()));
            collectCluster.setCollectTime(date);
            collectCluster.setId(MyIdUtil.getId());
            saveEntity.add(collectCluster);
        }
        //保存
        if(!saveEntity.isEmpty()){
            QueryWrapper<CollectCluster> queryWrapper = new QueryWrapper<CollectCluster>();
            queryWrapper.eq("ASSET_ID",saveEntity.get(0).getAssetId());
            clusterServ.remove(queryWrapper);
            clusterServ.saveBatch(saveEntity);
        }
    }

    /**
     * 处理节点状态告警
     * @param collectCluster
     */
    private void extractedStatus(CollectCluster collectCluster) {
        String key = collectCluster.getAssetId() + collectCluster.getServerName();
        Boolean serverStatus = serverStatusMap.get(key);
        Boolean nodeStatus = NODE_STATUS_NORMAL.equals(collectCluster.getStatus());
        if (Objects.isNull(serverStatus)) {
            serverStatusMap.put(key, nodeStatus);
        }
        if (!nodeStatus.equals(serverStatus)) {
            //状态变动
            Date date = new Date();
            date.setTime(Long.parseLong(collectCluster.getCollectTimeStr()));

            CreateEventReq createEventReq = new CreateEventReq();
            createEventReq.setFlag("集群状态变动");
            createEventReq.setAssetId(collectCluster.getAssetId());
            createEventReq.setBaseValue(collectCluster.getServerName());
            createEventReq.setCollectValue(collectCluster.getStatus());
            createEventReq.setUniqueCode(EventUniqueCode.SERVER_STATUS);
            createEventReq.setGroupFlag(EventGroupConstant.SERVER_STATUS);
            createEventReq.setCreateTime(date);
            try {
                if (Objects.nonNull(nodeStatus)) {
                    if (nodeStatus) {
                        //状态恢复
                        createEventReq.setOriginalMsg(String.format("节点:%s 状态：%s  ; 状态正常！", collectCluster.getServerName(), collectCluster.getStatus()));
                        createEventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                    } else {
                        //告警
                        createEventReq.setOriginalMsg(String.format("节点:%s 状态：%s  ; 状态异常！", collectCluster.getServerName(), collectCluster.getStatus()));
                        createEventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                    }
                    if (LogInputUtils.inputInfo(ServerTypeEnum.DISPOSE_CLUSTER)) {
                        log.error("集群状态变动事件上送事件信息：" + JSONUtil.toJsonStr(createEventReq));
                    }
                    eventLogicServ.addEvent(createEventReq);
                }

                serverStatusMap.put(key, nodeStatus);
            } catch (Exception e) {
                log.error("集群节点状态事件上送失败："+e.getMessage(),e);
            }

        }
    }

    /**
     * 主备切换判定逻辑
     * @param collectCluster
     * @param serverRole
     * @throws Exception
     */
    private void extracted(CollectCluster collectCluster, String serverRole){
        if(SERVER_ROLE_MAIN.equals(serverRole)){
            String value = serverMaster.get(collectCluster.getAssetId());
            if(StrUtil.isEmpty(value)){
                //如果没有，记录当前的  不上送事件。不能从数据库取，如果从数据库取的话不能断定中间是否发生过切换了已经。
                serverMaster.put(collectCluster.getAssetId(), collectCluster.getServerName());
            }else if(!value.equals(collectCluster.getServerName())){
                //上主备切换事件
                Date date = new Date();
                date.setTime(Long.parseLong(collectCluster.getCollectTimeStr()));

                CreateEventReq createEventReq = new CreateEventReq();
                createEventReq.setOriginalMsg(String.format("集群主备节点发生切换，由%s 机切换为了%s 机！",value, collectCluster.getServerName()));
                createEventReq.setCollectValue(collectCluster.getServerName());
                createEventReq.setFlag("集群主备切换");
                createEventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
                createEventReq.setAssetId(collectCluster.getAssetId());
                createEventReq.setBaseValue(value);
                createEventReq.setUniqueCode(EventUniqueCode.MAIN_BACKUP_CHANGE);
                createEventReq.setGroupFlag(EventGroupConstant.MAIN_BACKUP_CHANGE);
                createEventReq.setCreateTime(date);
                try {
                    if (LogInputUtils.inputInfo(ServerTypeEnum.DISPOSE_CLUSTER)) {
                        log.error("集群主备切换事件上送事件信息：" + JSONUtil.toJsonStr(createEventReq));
                    }
                    eventLogicServ.addEvent(createEventReq);
                    serverMaster.put(collectCluster.getAssetId(), collectCluster.getServerName());
                } catch (Exception e) {
                    log.error("集群主备切换事件上送失败：" + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.CLUSTER;
    }
}




package com.jcca.dataProcessing.DataFilter.node;

import com.jcca.common.exception.ResultException;
import com.jcca.component.client.bean.CollectNodesMsg;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectNodeEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component("collectNodeStatusHandler")
public class CollectNodeStatusHandler extends IFilterHandler<CollectNodeEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectNodeEntity node) throws Exception {
        String descStr = "";
        String eventRedisKey = "";
        String eventMapKey = node.getAssetId();
        String statusStr = CollectNodesMsg.DOWN.equals(node.getNodeState()) ? "已掉线" : "状态正常";

        if (CollectNodesMsg.IS_CENTER.equals(node.getNodeType())) {
            eventRedisKey = StatusInfoChangeTypeEnum.event_jcca_center.getCode();
            descStr = String.format(StatusInfoChangeTypeEnum.event_jcca_center.getDescr(), node.getNodeName(),statusStr, node.getNodeIp());
        } else {
            eventRedisKey = StatusInfoChangeTypeEnum.event_jcca_station.getCode();
            descStr = String.format(StatusInfoChangeTypeEnum.event_jcca_station.getDescr(), node.getNodeName(),statusStr, node.getNodeIp());
        }
        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setFlag(node.getAssetId());
        alarmTempReq.setAssetIp(node.getAssetIp());
        alarmTempReq.setAssetName(node.getAssetName());
        alarmTempReq.setOrgMsg(descStr);
        alarmTempReq.setCollectValue(statusStr);

        Integer status = CollectNodesMsg.DOWN.equals(node.getNodeState()) ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(node.getAssetId(), new ChangeInfo(), eventRedisKey, eventMapKey,status ,alarmTempReq,node.getInspectRecordId());
        if (event != null) {
            //被事件信息截取
            event.setDescLog(descStr);
            event.setCollectTime(new Date());
            this.dispatureEvent(event);
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}

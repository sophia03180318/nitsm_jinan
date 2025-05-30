package com.jcca.dataProcessing.support;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web2.dto.xunjian.XunjianDataDto;
import lombok.Data;

import java.util.Date;


/**
 * 事件类，向后传递的事件信息
 */
@Data
public class IEvent {
    /**
     * 变动信息
     */
    private ChangeInfo info;//具体性能信息
    private String eventRedisKey;//事件Code码
    private String mapKey;
    /**
     * 事件状态
     */
    private Integer status;//事件状态
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 创建时间
     */
    private Date collectTime;
    /**
     * 对应知识库信息
     */
    private EventAlarmLevelBaseEntity eventAlarmLevelBaseEntity;
    /**
     * 描述信息
     */
    private String descStr;
    /**
     * 进程恢复时进程的id号
     */
    private String recoveryProcessIdDescr;
    /**
     * 模板参数
     */
    private AlarmTempReq alarmTempReq;
    /**
     * 巡检数据
     */
    private String inspectRecordId;

    public IEvent() {
    }

    public IEvent(String assetId, ChangeInfo info, String eventRedisKey, String mapKey, Integer status,String inspectRecordId) {
        this.info = info;
        this.eventRedisKey = eventRedisKey;
        this.mapKey = mapKey;
        this.status = status;
        this.assetId = assetId;
        this.collectTime = info.getCollectTime();
        this.inspectRecordId=inspectRecordId;
    }

    public IEvent(String assetId, ChangeInfo info, String eventRedisKey, String mapKey, Integer status,AlarmTempReq alarmTempReq,String inspectRecordId) {
        this.info = info;
        this.eventRedisKey = eventRedisKey;
        this.mapKey = mapKey;
        this.status = status;
        this.assetId = assetId;
        this.collectTime = info.getCollectTime();
        this.alarmTempReq = alarmTempReq;
        this.inspectRecordId=inspectRecordId;
    }


    public void setDescStr(String descStr) {
        this.descStr = descStr + "当前状态：" + EventLevelEnum.getMsgByCode(this.getStatus());
    }

    public void setDescLog(String descStr) {
        this.descStr = descStr;
    }

    public void setDescLogAndMessageId(String descStr, String messageId) {
        this.descStr = descStr + "," + "当前事件ID：" + messageId + "。";
    }
}

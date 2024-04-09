package com.jcca.dataProcessing.manager.bean;

import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.asset.entity.Asset;
import lombok.Data;

/**
 * @description: 保存告警响应信息
 * @author: Lvyp
 * @create: 2023/12/20 17:33
 */
@Data
public class SaveAlarmResp {

    /**
     * 是否需要推送
     */
    private Boolean needSendToWeb;
    /**
     * needSendToWeb true的话有值
     */
    private AlarmInfo newAlarm;
    /**
     * needSendToWeb true的话有值
     */
    private Asset asset;

}

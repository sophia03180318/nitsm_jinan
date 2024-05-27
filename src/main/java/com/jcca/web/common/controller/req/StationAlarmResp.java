package com.jcca.web.common.controller.req;

import lombok.Data;

/**
 * @description: 推送告警后响应信息
 * @author: Lvyp
 * @create: 2024/04/28 17:51
 */
@Data
public class StationAlarmResp {

    /**
     * 告警推送状态枚举
     */
    public enum  PushAlarmStatusEnum {

        /**
         * 接收成功
         */
        SUCCESS,
        /**
         * 不允许此类告警
         * 如果收到此状态，车站需要删除当前告警
         */
        REFUSE;
    }


    private Long id;
    /**
     * ITSM的ID
     */
    private String itsmId;
    /**
     * 状态
     * PushAlarmStatusEnum
     */
    private String status;


}

package com.jcca.web2.dto;

import lombok.Data;

/**
 * @description: 事件分页请求参数
 * @author: Lvyp
 * @create: 2023/11/23 13:58
 */
@Data
public class EventPageDto extends PageDto {

    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 事件等级
     * -1异常
     * 0通知
     * 1正常
     * 2警告
     */
    private Integer eventLevel;
    /**
     * 原始内容
     */
    private String eventMsg;
    /**
     * 唯一标识、匹配码
     */
    private String uniqueCode;
    /**
     * 时间类型ID
     */
    private String eventTypeId;
    /**
     * 事件类型
     * 1、已知
     * 2、未知
     * 3、所有
     * 4、没有配置级别的syslog事件
     */
    private Integer eventType;

}

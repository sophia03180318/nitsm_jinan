package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 事件分页请求结果
 * @author: Lvyp
 * @create: 2023/11/23 14:12
 */
@Data
public class EventPageVo {


    private String eventId;
    /**
     * 事件在白名单的数量
     */
    private Integer whiteRoleSize;
    /**
     * 事件类型
     */
    private String eventType;
    /**
     * 事件类型ID
     */
    private String eventTypeId;
    /**
     * 事件等级
     */
    private Integer eventLevel;
    /**
     * 唯一码标识
     */
    private String uniqueCode;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产IP1
     */
    private String assetIp1;
    /**
     * 资产IP2
     */
    private String assetIp2;
    /**
     * 事件信息
     */
    private String eventMsg;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 标记
     */
    private String flag;
    /**
     * 产生时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}

package com.jcca.web2.dto;

import lombok.Data;

/**
 * @description: 白名单分页查询
 * @author: Lvyp
 * @create: 2023/11/30 16:05
 */
@Data
public class WhitePageQueryDto extends PageDto {

    /**
     * 设备名称
     */
    private String assetName;
    /**
     * 事件唯一码
     */
    private String alarmCode;
    /**
     * 事件类型ID
     */
    private String eventTypeId;


}

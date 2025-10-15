package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @description: 告警规则白名单添加
 * @author: Lvyp
 * @create: 2023/11/30 11:37
 */
@Data
public class AlarmWhitelistAddDto {

    private String id;
    /**
     * 资产ID
     */
    @NotEmpty(message = "缺少资产ID")
    private List<String> assetId;
    /**
     * 一些告警的特殊设定值
     * 如：进程告警：放入进程名称
     * 端口告警 ：放入端口索引
     * 网卡告警：放入网卡名称
     */
    private String flag;

    /**
     * 事件匹配码
     */
    @NotEmpty(message = "缺少事件唯一码")
    private String alarmCode;

    /**
     * 事件类型ID
     */
    @NotEmpty(message = "缺少事件类型ID")
    private String eventTypeId;

    /**
     * 标识
     * 0 普通
     * 1 批量
     */
    private int typeMark;
}

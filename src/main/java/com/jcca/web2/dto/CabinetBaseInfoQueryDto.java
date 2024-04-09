package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description: 查询机柜详情入参
 * @author: Lvyp
 * @create: 2023/10/24 14:33
 */
@Data
public class CabinetBaseInfoQueryDto {

    /**
     * 机柜ID
     */
    @NotEmpty(message = "机柜ID不能为空")
    private String cabinetId;
    /**
     * 告警确认状态
     * 1未确认  2已确认
     */
    private Integer status;
    /**
     * 告警状态
     * 1、告警  2恢复
     */
    private Integer alarmState;
    /**
     * 告警确认状态筛选关系
     * 1and关系
     * 2or关系
     */
    @NotNull(message = "必须传入筛选关系")
    private Integer statusLogical;

    /**
     * 是否查询JCCA设备
     * 1查2不查
     */
    private Integer showJcca;
    /**
     * 用户管辖的设备ID列表
     */
    private List<String> assetIdList;
    /**
     * 天窗告警标记，1正常时段告警，2天窗时段告警
     * AlarmBlankConst
     */
    private Byte blank;

    private String userName;
}

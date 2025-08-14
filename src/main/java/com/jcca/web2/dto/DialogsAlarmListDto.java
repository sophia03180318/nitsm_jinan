package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description: 弹框中告警列表查询
 * @author: Lvyp
 * @create: 2023/11/16 12:11
 */
@Data
public class DialogsAlarmListDto {

    /**
     * 设备型号
     */
    private Integer assetDesk;
    /**
     * 设备ID
     */
    private String assetId;
    /**
     * 机柜ID
     */
    private String cabinetId;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 确认状态
     * AlarmStatusEnum
     * 1未确认  2已确认
     */
    private Integer status;
    /**
     * 告警状态
     * AlarmStateEnum
     * 1告警  2恢复
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
     * 查询告警包含级别
     */
    private List<Integer> alarmLevelList;
    /**
     * 监控项目code
     * 对应是alarmInfo表中的eventCategroy
     */
    private String eventCategory;
    /**
     * 天窗告警标记，1正常时段告警，2天窗时段告警
     * AlarmBlankConst
     */
    private Byte blank;
    /**
     * 鉴权User 如果不传会使用系统当前登录用户，
     * 传入的话会以传入为准
     */
    private String userName;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;

}

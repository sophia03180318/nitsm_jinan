package com.jcca.web2.dto;

import lombok.Data;

import java.util.List;

/**
 * @description: 告警列表分页查询条件
 * @author: Lvyp
 * @create: 2023/11/22 09:18
 */
@Data
public class AlarmPageDto extends PageDto {
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
     * 标题
     */
    private String title;
    /**
     * 告警发生开始时间
     */
    private String startTime;
    /**
     * 告警发生结束时间
     */
    private String endTime;
    /**
     * 1告警，2恢复
     */
    private Integer alarmState;
    /**
     * 1未确认  2已确认
     */
    private Integer status;
    /**
     * 0未知告警
     * 1一级告警
     * 2二级告警
     * 3三级告警
     * 4信息通知
     */
    private Integer alarmLevel;
    /**
     * 告警项的code
     */
    private String alarmCode;
    /**
     * 0未知告警
     * 1一级告警
     * 2二级告警
     * 3三级告警
     * 4信息通知
     */
    private List<Integer> alarmLevelList;
    /**
     * 资产小型号筛选集合
     */
    private List<Integer> assetDeskList;
    /**
     * 告警项的code
     */
    private List<String> alarmCodeList;
    /**
     * 是否天窗告警
     * 1不是2是
     */
    private Integer blank;
    /**
     * 用户管辖的设备ID列表
     */
    private List<String> assetIdList;
    /**
     * 组织 id 列表
     */
    private List<String> orgIdList;
    /**
     * 是否显示中航设备告警
     * 2不显示1显示
     */
    private Integer showJcca;
    /**
     * 分组统计的字段
     */
    private String groupField;
    /**
     * 报表生成
     * 增加的字段
     */
    private Integer assetMode;

    /**
     * 告警内容
     * */
    private String content;

}

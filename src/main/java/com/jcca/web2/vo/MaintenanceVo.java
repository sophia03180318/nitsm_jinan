package com.jcca.web2.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

/**
 * @description: 维护计划
 * @author: sophia
 * @create: 2023/11/16 16:20
 **/
@Data
public class MaintenanceVo {

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 组织ID
     */
    @NotEmpty(message = "组织ID不能为空")
    private String orgId;

    /**
     * 影响组织ID
     */
    @NotEmpty(message = "影响组织不能为空")
    private List<String> influenceOrgIds;

    /**
     * 开始时间
     */
    @NotEmpty(message = "开始时间不能为空")
    private String startTime;

    /**
     * 结束时间
     */
    @NotEmpty(message = "结束时间不能为空")
    private String endTime;

    /**
     * 施工等级
     */
    private String planLevel;

    /**
     * 电务段
     */
    private String dianWuDuan;

    /**
     * 工号
     */
    private String jobNumber;

    /**
     * CTC/TDCS
     */
    private String model;

    /**
     * 联锁
     */
    private String chain;

    /**
     * 监测
     */
    private String monitor;

    /**
     * 施工内容
     */
    private String content;

    /**
     * 软件人员
     */
    private String software;

    /**
     * 现场人员
     */
    private String fieldForce;

    /**
     * 修改表
     */
    private String amend;

    /**
     * 备注
     */
    private String remark;

    /**
     * 施工状态
     */
    private String status;

    /**
     * 施工类型
     */
    private String type;

    /**
     * 调试记录
     */
    private String debug;

    /**
     * 创建时间
     */
    private Date createTime;

}
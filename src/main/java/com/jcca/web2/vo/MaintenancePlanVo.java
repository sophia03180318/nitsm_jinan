package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 维护计划
 * @author: sophia
 * @create: 2023/11/16 16:20
 **/
@Data
public class MaintenancePlanVo {
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 组织  以,分割
     */
    private String orgName;

    /**
     * 开始时间
     */
    private String startTimeStr;

    /**
     * 结束时间
     */
    private String endTimeStr;

    /**
     * 施工等级
     */
    private String level;

    /**
     * 电务段
     */
    private String station;

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
     * 错误日志
     */
    private String log;


}
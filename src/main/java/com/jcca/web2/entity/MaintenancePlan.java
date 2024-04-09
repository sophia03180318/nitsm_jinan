package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

/**
 * @description: 维护计划
 * @author: sophia
 * @create: 2023/11/16 16:20
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("MAINTENANCE_PLAN")
public class MaintenancePlan extends Model<MaintenancePlan> {
    private static final long serialVersionUID = -3238172374561917680L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 组织ID
     */
    @TableField("ORG_ID")
    private String orgId;

    /**
     * 影响组织  以,分割
     */
    @TableField("INFLUENCE_ORG")
    private String influenceOrg;
    @TableField(exist = false)
    private List<String> influenceOrgList;

    /**
     * 开始时间
     */
    @TableField(value = "START_TIME", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @TableField(value = "END_TIME", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date endTime;

    /**
     * 施工等级
     */
    @TableField("PLAN_LEVEL")
    private String planLevel;

    /**
     * 电务段
     */
    @TableField("DIAN_WU_DUAN")
    private String dianWuDuan;

    /**
     * 工号
     */
    @TableField("JOB_NUMBER")
    private String jobNumber;

    /**
     * CTC/TDCS
     */
    @TableField("MODEL")
    private String model;

    /**
     * 联锁
     */
    @TableField("CHAIN")
    private String chain;

    /**
     * 监测
     */
    @TableField("MONITOR")
    private String monitor;

    /**
     * 施工内容
     */
    @TableField("CONTENT")
    private String content;

    /**
     * 软件人员
     */
    @TableField("SOFTWARE")
    private String software;

    /**
     * 现场人员
     */
    @TableField("FIELD_FORCE")
    private String fieldForce;

    /**
     * 修改表
     */
    @TableField("AMEND")
    private String amend;

    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;

    /**
     * 施工状态
     */
    @TableField("STATUS")
    private String status;

    /**
     * 施工类型
     */
    @TableField("TYPE")
    private String type;

    /**
     * 调试记录
     */
    @TableField("DEBUG")
    private String debug;

    /**
     * 年
     */
    @TableField("OCCUR_YEAR")
    private Integer occurYear;

    /**
     * 月
     */
    @TableField("OCCUR_MONTH")
    private Integer occurMonth;

    /**
     * 日
     */
    @TableField("OCCUR_DAY")
    private Integer occurDay;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;

    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;

    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}
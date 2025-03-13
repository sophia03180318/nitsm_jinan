package com.jcca.web2.dto;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description: 告警处理请求参数
 * @author: Lvyp
 * @create: 2023/11/16 16:26
 */
@Data
public class DisposeAlarmDto {

    /**
     * 告警ID列表
     */
    @NotNull(message = "告警ID列表不可空")
    @Size(min = 1, message = "至少需要一个告警ID")
    private List<String> alarmIdList;
    /**
     * 告警备注信息
     */
    @Length(max = 128, message = "长度超出接口限制")
    private String remark;
    /**
     * 告警处理人
     */
    private String confirmor;
    /**
     * 故障发生时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;
    /**
     * 故障现象
     */
    private String description;
    /**
     * 故障原因
     */
    @Length(max = 512, message = "长度超出接口限制")
    private String reason;
    /**
     * 天窗开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
     * 天窗结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String endTime;
    /**
     * 施工计划
     */
    private String name;
    /**
     * 施工地点
     */
    private String place;
    /**
     * 施工人
     */
    private String operator;
    /**
     * 配合人
     */
    private String cooperator;
    /**
     * 影响设备列表
     */
    private List<String> influence;
    /**
     * 影响范围所属组织ID
     */
    private String orgId;


    /**
     * 是否需要处理转故障
     *
     * @return
     */
    public boolean needDisposeBroken() {
        if (Objects.isNull(this.occurTime) && StrUtil.isEmpty(this.description) && StrUtil.isEmpty(this.reason)) {
            return false;
        }
        return true;
    }

    /**
     * 是否需要处理施工计划
     *
     * @return
     */
    public boolean needDisposeRecord() {
        boolean noValue = Objects.isNull(this.startTime) && Objects.isNull(this.endTime) && StrUtil.isEmpty(this.name)
                && StrUtil.isEmpty(this.place) && StrUtil.isEmpty(this.operator) && StrUtil.isEmpty(this.cooperator);
        if (noValue) {
            return false;
        }

        return true;
    }

    /**
     * 校验故障参数是否符合要求
     *
     * @return
     */
    public boolean brokenVerify() {
        if (Objects.isNull(this.occurTime) && StrUtil.isEmpty(this.description) && StrUtil.isEmpty(this.reason)) {
            return true;
        }
        if (Objects.isNull(this.occurTime)) {
            return false;
        }
        if (Objects.isNull(this.description)) {
            return false;
        }
        return true;
    }

    /**
     * 验证施工计划参数是否符合要求
     *
     * @return
     */
    public boolean recordVerify() {
        boolean noUsed = Objects.isNull(this.startTime) && Objects.isNull(this.endTime) && StrUtil.isEmpty(this.name)
                && StrUtil.isEmpty(this.place) && StrUtil.isEmpty(this.operator) && StrUtil.isEmpty(cooperator);
        if (noUsed) {
            return true;
        }

        if (Objects.isNull(this.startTime)) {
            return false;
        }
        if (Objects.isNull(this.endTime)) {
            return false;
        }
        if (StrUtil.isEmpty(this.name)) {
            return false;
        }
        return true;
    }

}

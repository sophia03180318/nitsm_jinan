package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.util.Date;

/**
 * @description: 日志输出控制
 * @author: HanHW
 * @date: 2023/10/24 12:07
 **/
@TableName("SYS_FUNCTION_LOG")
public class SysFunctionLog extends Model<SysFunctionLog> implements java.io.Serializable {
    private static final long serialVersionUID = 8805262923757215318L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    // 功能码  functionCode + actionCode
    private String code;

    // LogFunctionConstant 前两位
    private String functionCode;

    // LogFunctionConstant 后两位
    private String actionCode;

    // LogFunctionEnum
    @TableField(value = "FUNCTION_NAME")
    private String functionName;

    @TableField(value = "ACTION_NAME")
    private String actionName;

    private Integer status;

    private String remark;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getFunction() {
        return functionName;
    }

    public void setFunction(String function) {
        this.functionName = function;
    }

    public String getAction() {
        return actionName;
    }

    public void setAction(String action) {
        this.actionName = action;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}
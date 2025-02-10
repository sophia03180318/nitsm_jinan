package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author HanHW
 * @description 行为日志明细
 * @className SysActionLogDetail
 * @date 2024/4/10 11:12
 * @since 2.1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("SYS_ACTION_LOG_DETAIL")
public class SysActionLogDetail extends Model<SysActionLogDetail> {

    private static final long serialVersionUID = -1053764543614367100L;

    @TableId
    private String id;

    private String actionLogId;
    private String assetId;
    private String itemId;
    private String itemIdType;
    private String description;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.UPDATE)
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.UPDATE)
    private String modifier;
}

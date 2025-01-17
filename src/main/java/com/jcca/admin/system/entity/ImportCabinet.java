package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ Author：sophia
 * @ Date：Created in 11:13 2021/8/12
 * @ Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("IMPORT_CABINET")
public class ImportCabinet extends Model<ImportCabinet> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 机柜名称
     */

    @TableField("NAME")
    @NotEmpty(message = "机柜名称不能为空")
    private String name;
    /**
     * 机柜编码
     */
    @TableField("CODE")
    @Digits(message = "机柜编号最多为4位正整数", integer = 4, fraction = 0)
    private String code;
    /**
     * 机房名称
     */
    @TableField("ROOM_NAME")
    @NotEmpty(message = "所属机房不能为空")
    private String roomName;
    /**
     * 组织名称
     */
    @TableField("ORG_NAME")
    @NotEmpty(message = "所属组织不能为空")
    private String orgName;

    /**
     * 横向索引
     */
    @TableField("ROW_INDEX")
    @NotNull(message = "横向索引不能为空")
    private String rowIndex;
    /**
     * 纵向索引
     */
    @TableField("COLUMN_INDEX")
    @NotNull(message = "纵向索引不能为空")
    private String columnIndex;

    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;

    /**
     * 创建者
     */
    @TableField(value = "CREATOR")
    private String creator;

    /**
     * 错误日志
     */
    @TableField(value = "ERROR_LOG")
    private String errorLog;

    /**
     * 执行状态
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 二维码识别码
     */
    @TableField("QR_CODE_NUM")
    private String qrCodeNum;

    @TableField(exist = false)
    private Integer page;
    @TableField(exist = false)
    private Integer size;
}

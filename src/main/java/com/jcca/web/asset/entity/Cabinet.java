package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 机柜表
 *
 * @author hanwone
 * @date 2020-04-26 15:48:43
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("cabinet")
public class Cabinet extends Model<Cabinet> implements java.io.Serializable {

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
    @NotNull(message = "机柜编号不能为空")
    private Integer code;

    /**
     * 组织名称链
     */
    @TableField(exist = false)
    private String orgName;

    /**
     * 所属组织Id
     */
    @TableField(exist = false)
    private String orgId;
    /**
     * 页面所筛选的组织树回传id 与机柜无直接关系
     */
    @TableField(exist = false)
    private String orgTreeId;

    /**
     * 机房ID
     */
    @TableField("ROOM_ID")
    @NotEmpty(message = "所属机房不能为空")
    private String roomId;
    /**
     * 横向索引
     */
    @TableField("ROW_INDEX")
    @NotNull(message = "横向索引不能为空")
    private Integer rowIndex;
    /**
     * 纵向索引
     */
    @TableField("COLUMN_INDEX")
    @NotNull(message = "纵向索引不能为空")
    private Integer columnIndex;

    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
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
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    /**
     * 二维码识别码
     */
    @TableField("QR_CODE_NUM")
    private String qrCodeNum;

    /**
     * 机房名称
     * */
    @TableField(exist = false)
    private String roomName;

}
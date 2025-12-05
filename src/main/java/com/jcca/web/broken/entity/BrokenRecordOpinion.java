package com.jcca.web.broken.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName BrokenRecordOpinion
 * @Description 故障记录处理意见
 * @Date 2020/6/28 17:36
 * @Author hanwone
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("BROKEN_RECORD_OPINION")
public class BrokenRecordOpinion extends Model<BrokenRecordOpinion> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 故障记录ID
     */
    @TableField("BROKEN_RECORD_ID")
    private String brokenRecordId;
    /**
     * 处理意思
     */
    @TableField("OPINION")
    private String opinion;
    /**
     * 数据状态 StatusConst
     */
    @TableField("STATUS")
    private Byte status;
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

    @TableField(exist = false)
    private String assetId;
}

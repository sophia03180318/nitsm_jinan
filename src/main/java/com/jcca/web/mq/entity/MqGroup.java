package com.jcca.web.mq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:23 2021/11/25
 * @ Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("MQ_GROUP")
public class MqGroup extends Model<MqGroup> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 业务组名称
     */
    @NotEmpty(message = "请输入名称")
    @TableField("NAME")
    private String name;

    /**
     * 管理器ID
     */
    @TableField("CONNECT_ID")
    private String connectId;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;

    /**
     * 组内包含的monitor
     */
    @TableField(exist = false)
    private List<MqMonitor> monitorList;

}

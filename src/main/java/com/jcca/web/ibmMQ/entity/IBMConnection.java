package com.jcca.web.ibmMQ.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("IBMMQ_QMGR_CONNECTION")
public class IBMConnection extends Model<IBMConnection> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 主机名字
     */
    @TableField("connect_name")
    private String connectName;
    /**
     * 管道名称
     */
    @TableField("channel_name")
    private String channelName;
    /**
     * 主机名称
     */
    @TableField("connect_host")
    private String connectHost;
    /**
     * 主机端口
     */
    @TableField("connect_port")
    private Integer connectPort;
    /**
     * 用户ID
     */
    @TableField("USER_Id")
    private String userId;
    /**
     * 描述信息
     */
    @TableField("des_cription")
    private String description;

    /**
     * 状态
     */
    @TableField("status")
    private String status;


}
package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author HanHW
 * @description 用户登录控制
 * @className SysLoginConf
 * @date 2023/10/26 10:10
 * @since 2.1.0.0
 */
@Data
@TableName("SYS_LOGIN_CONF")
public class SysLoginConf {

    // 用户ID
    @TableId(value = "USER_ID", type = IdType.ID_WORKER_STR)
    private String userId;
    // 用户名称
    private String username;
    // 尝试次数
    private Integer trySize;
    // 最大允许错误次数
    private Integer maxSize;
    // 锁定时间
    private Integer lockTime;
    // 最后一次登录时间
    private String lastTime;
    // 允许登录的IP
    private String ipList;
    // 用户状态 1正常，2锁定 StatusConst
    private Byte status;
    // 备注
    private String remark;
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "MODIFY_TIME", fill = FieldFill.UPDATE)
    private Date modifyTime;

    @TableField(exist = false)
    private List<String> ips;
}

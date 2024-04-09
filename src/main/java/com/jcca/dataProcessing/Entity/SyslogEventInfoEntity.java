package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Zhaozheng
 * @description TODO 事件信息类
 * @className EventInfoEntity
 * @date 2023/11/28 13:15
 * @since 2.1.0.0
 */
@Data
public class SyslogEventInfoEntity extends CommonEntity implements Serializable {
    //获取到的IP地址信息
    private String ip;
    //告警信息
    private String message;

    private Integer level;
}

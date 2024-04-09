package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * @author Zhaozheng
 * @description TODO
 * @className MQMonitorEntity
 * @date 2024/1/6 10:39
 * @since 2.1.0.0
 */
@Data
public class MQMonitorEntity extends CommonEntity {
    private Integer status;
    private String message;
    private String name;

}

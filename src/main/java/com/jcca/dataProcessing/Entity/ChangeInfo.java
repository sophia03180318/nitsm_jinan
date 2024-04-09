package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 变通信息基础类
 * @className BaseInfo
 * @date 2023/10/26 10:54
 * @since 2.1.0.0
 */
@Data
public class ChangeInfo {
    /**
     * redis对应的key
     */
    private String redisKey;
    /**
     * 对应名称
     */
    private String mapKey;

    /**
     * 对应的名称
     */
    private String name;
    /**
     * 对应值
     */
    private Object value;
    /**
     * 采集事件
     */
    private Date collectTime;

    /**
     * 是否已经被事件获取
     */
    private Boolean isEvent;

    /**
     * 事件信息
     */
    private EventInfo eventInfo;
    /**
     * 性能数据是否有变化
     */
    private Boolean isChange;

}

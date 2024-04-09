package com.jcca.dataProcessing.manager.threshold;

import lombok.Data;


/**
 * 阈值事件
 */
@Data
public class Event {
    private String code;
    private String redisKey;
    private String mapKey;

}

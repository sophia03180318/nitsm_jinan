package com.jcca.web.ai.vo;

import lombok.Data;

/**
 * @description: ai回答需要
 * @author: sophia
 * @create: 2025/03/20 17:14
 **/
@Data
public class AiVo {
    /*
     * 设备名称
     * */
    private String assetId;

    /*
     * 需要的相关信息
     * */
    private String types;

    /*
     * 时间
     * */
    private Long time;

    private String sql;


    private String content;

    private String alarmLevel;

    //是否恢复
    private String alarmState;

    //端口名称
    private String portName;


}
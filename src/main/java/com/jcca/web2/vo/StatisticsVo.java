package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 数据统计
 * @className StatisticsVoV2
 * @date 2023/10/26 15:56
 * @since 2.1.0.0
 */
@Data
public class StatisticsVo {

    private String id;

    private String name;

    private Integer alarmLevel;

    private Long total;

    private Long alarmTotal;

}

package com.jcca.web.statistics.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName StatisticsAlarmVo
 * @Description 告警次数统计
 * @Date 2020/7/20 10:17
 * @Author hanwone
 */
@Data
public class StatisticsAlarmVo implements Serializable {
    private static final long serialVersionUID = 7337239349402240696L;
    /**
     * 数量
     */
    private Long total;
    /**
     * 名称
     */
    private String name;

    private String id;

    private String orgId;
}

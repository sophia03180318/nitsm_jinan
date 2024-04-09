package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 汇总信息vo
 * @author: Lvyp
 * @create: 2023/11/22 09:31
 */
@Data
public class AlarmPageStatisticsVo {

    /**
     * 项
     */
    private String key;
    /**
     * 值
     */
    private Integer value;

    private String code;

}

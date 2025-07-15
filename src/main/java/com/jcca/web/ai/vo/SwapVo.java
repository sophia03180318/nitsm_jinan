package com.jcca.web.ai.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 交换空间数据
 */
@Data
public class SwapVo implements Serializable {


    private Double swapUsedRate;
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private Date collectDate;

    @Override
    public String toString() {
        return "{交换空间使用率:" + swapUsedRate + "%" +
                ",时间:" + collectDate + "}";
    }
}

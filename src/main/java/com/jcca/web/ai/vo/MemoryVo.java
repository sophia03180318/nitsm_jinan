package com.jcca.web.ai.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 内存信息
 */
@Data
public class MemoryVo implements Serializable {

    private Date collectDate;

    private Double memUsedRate;

    @Override
    public String toString() {
        return "{内存使用率:" + memUsedRate + "%" +
                ",时间:" + collectDate + "}";
    }

}

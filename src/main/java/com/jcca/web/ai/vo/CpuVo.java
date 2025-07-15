package com.jcca.web.ai.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * CPU数据
 */
@Data
public class CpuVo implements Serializable {


    private Double cpuUsedRate;
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private Date collectDate;

    @Override
    public String toString() {
        return "{CPU使用率:" + cpuUsedRate + "%" +
                ",时间:" + collectDate + "}";
    }
}

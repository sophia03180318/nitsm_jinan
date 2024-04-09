package com.jcca.web.asset.vo;

import lombok.Data;

/**
 * @ClassName AvgVo
 * @Description 平均值统计
 * @Date 2020/7/7 10:04
 * @Author hanwone
 */
@Data
public class AvgVo {

    /**
     * cpu使用率平均值
     */
    private String avgCpu;
    /**
     * 内存使用率平均值
     */
    private String avgMem;
    /**
     * 交换空间使用率平均值
     */
    private String avgSwap;
}

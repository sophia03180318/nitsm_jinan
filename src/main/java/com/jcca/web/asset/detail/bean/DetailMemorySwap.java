package com.jcca.web.asset.detail.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName DetailMemorySwap
 * @Description 资产详情----内存 交换空间信息
 * @Date 2020/6/22 17:35
 * @Author hanwone
 */
@Data
public class DetailMemorySwap {
    /**
     * 内存总量
     */
    private String memTotalStr;
    /**
     * 内存已使用
     */
    private String memUsedStr;
    /**
     * 内存使用率
     */
    private Double memUsedRate;
    /**
     * 交换空间总量
     */
    private String swapTotalStr;
    /**
     * 交换空间已使用
     */
    private String swapUsedStr;
    /**
     * 交换空间使用率
     */
    private Double swapUsedRate;
    /**
     * 采集时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
}

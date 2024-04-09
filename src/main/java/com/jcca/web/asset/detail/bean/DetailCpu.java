package com.jcca.web.asset.detail.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName DetailCpu
 * @Description 资产详情----CPU信息
 * @Date 2020/6/22 17:25
 * @Author hanwone
 */
@Data
public class DetailCpu {
    /**
     * cpu个数
     */
    private Integer cpuNumber;
    /**
     * cpu核数
     */
    private Integer cpuCoreNumber;
    /**
     * cpu型号
     */
    private String cpuModel;
    /**
     * cpu频率
     */
    private String cpuFrequency;
    /**
     * cpu使用率
     */
    private Double cpuUsedRate;
    /**
     * 采集时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
}

package com.jcca.web.asset.detail.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName AssetLine
 * @Description 实时折线图
 * @Date 2020/6/29 16:53
 * @Author hanwone
 */
@Data
public class AssetLine {
    /**
     * 采集到的数据值
     */
    private String data;
    /**
     * 数据值对应的时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "GMT+8")
    private Date time;
    /**
     * 交换空间使用率
     */
    private String swapUsedRate;
}

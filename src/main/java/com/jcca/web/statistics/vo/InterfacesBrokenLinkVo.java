package com.jcca.web.statistics.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 端口折线1小时汇总
 *
 * @author Lvyp
 */
@Data
public class InterfacesBrokenLinkVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 汇总的时间点
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    /**
     * 端口接收率 百分比-数字格式
     * 流入率
     */
    private Double portRateIn;
    /**
     * 端口发送率 百分比-数字格式
     * 流出率
     */
    private Double portRateOut;
}

package com.jcca.web.statistics.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * CPU 折线数据
 *
 * @author Lvyp
 */
@Data
public class CpuBrokenLinkVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 统计开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDay;
    /**
     * 统计结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDay;
    /**
     * CPU使用率
     */
    private String cpuUsedRate;

}

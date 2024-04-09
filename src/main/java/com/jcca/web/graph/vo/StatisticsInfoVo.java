package com.jcca.web.graph.vo;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/24 14:44
 */
@Data
public class StatisticsInfoVo {
    private Date endTime;
    private Double value;
    /**
     * M/s
     */
    private Double speed;

    public String getEndTime() {
        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        return df.format(this.endTime);
    }

    public Date getEndDate() {
        return this.endTime;
    }

}

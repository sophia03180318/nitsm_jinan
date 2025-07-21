package com.jcca.web.cycles.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 影响组织
 * @author: Lvyp
 * @create: 2024/11/20 09:39
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cycles_times")
public class CyclesTimes extends Model<CyclesTimes> implements java.io.Serializable {

    /**
     * id
     */
    private String id;
    /**
     * 周期信息ID
     */
    private String cyclesInfoId;
    /**
     * 周
     */
    private Integer weeks;
    /**
     * 开始时间 HHmmss
     */
    private Integer startTime;
    /**
     * 结束时间 HHmmss
     */
    private Integer endTime;

}

package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统数据
 *
 * @author Lvyp
 */
@Data
public class CollectSystemTimeEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 时长 单位毫秒
     */
    private Long timeSpan;
    /**
     * 设备运行时长
     */
    private Long timeduration;
    /**
     * 系统当前时间
     */
    private Date systemDate;
}

package com.jcca.component.thresholds.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统数据
 *
 * @author Lvyp
 */
@Data
public class CollectSystemTimeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间不能空")
    private String collectTime;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不能空")
    private String assetId;
    /**
     * 时长 单位毫秒
     */
    @NotNull(message = "时间差空")
    private Long timeSpan;
    /**
     * 设备运行时长
     */
    private Long timeduration;
    /**
     * 系统当前时间
     */
    @NotNull(message = "系统时间空")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss:SSS", timezone = "GMT+8")
    private Date systemDate;
}

package com.jcca.web.asset.controller.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 新增
 *
 * @author Lvyp
 */
@Data
public class ThresholdProcessUpdateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    @NotEmpty(message = "请选择修改的阈值")
    private String id;
    /**
     * cpu阈值
     */
    @Pattern(regexp = "^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$", message = "cpu阈值配置不正确")
    @DecimalMin(value = "0.01", message = "cpu阈值最小值为0.01")
    private String thresholdCpu;
    /**
     * 内存阈值
     */
    @Pattern(regexp = "^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$", message = "内存阈值不正确")
    @DecimalMin(value = "0.01", message = "内存阈值最小值为0.01")
    private String thresholdMemory;

    private String remark;
    /**
     * 进程名称
     */
    private String processName;

}

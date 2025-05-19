package com.jcca.web2.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: hhw
 * @description: XunjianJobDto 主要是用来
 * @date: 2025-05-18  17:12
 * @since: 2.1.6.0
 */
@Data
public class XunjianJobDto {


    private String id;
    private String operator;
    /**
     * 周期时间
     */
    private String cronTimes;
    private String jobId;
    @Length(min = 1, max = 100)
    private String jobName;
    /**
     * 任务类型，1手动巡检，2周期巡检
     */
    @NotNull(message = "任务类型不能为空")
    private Integer autoFlag;
    /**
     * 执行策略，1手动执行，2立即执行
     */
    @NotNull(message = "执行策略不能为空")
    private Integer startNow;
    private String remark;

    @NotEmpty(message = "巡检资产不能为空")
    private List<String> assetIds;
    @NotEmpty(message = "巡检指标不能为空")
    private List<String> targetIds;
}

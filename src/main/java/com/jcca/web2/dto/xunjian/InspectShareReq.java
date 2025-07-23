package com.jcca.web2.dto.xunjian;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author: hhw
 * @description: InspectShareReq 主要是用来
 * @date: 2025-07-17  14:05
 * @since: 2.1.8.0
 */
@Data
public class InspectShareReq {

    @NotEmpty(message = "任务ID不能为空")
    private String jobId;
    @NotEmpty(message = "巡检记录ID不能为空")
    private String inspectRecordId;
    @NotEmpty(message = "查看人不能为空")
    private String usernames;
}

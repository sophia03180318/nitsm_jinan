package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description: 更新告警规则
 * @author: Lvyp
 * @create: 2023/11/29 15:49
 */
@Data
public class UpdateAlarmRepoDto extends AddAlarmRepoDto {

    /**
     * 规则的ID
     */
    @NotEmpty(message = "规则ID不能空")
    private String id;


}

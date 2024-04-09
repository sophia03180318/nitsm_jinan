package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description: 更新图表配置请求参数
 * @author: Lvyp
 * @create: 2023/11/13 09:15
 */
@Data
public class UpdateBoardsConfigDto {

    /**
     * 图表ID
     */
    @NotEmpty(message = "缺少图表模板ID")
    private String boardId;
    /**
     * 配置信息
     */
    @NotEmpty(message = "缺少图表配置信息")
    private String configs;

}

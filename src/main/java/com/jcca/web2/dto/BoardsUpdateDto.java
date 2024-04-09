package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description: 更新模板请求参数
 * @author: Lvyp
 * @create: 2023/11/13 09:47
 */
@Data
public class BoardsUpdateDto {

    /**
     * 模板ID
     */
    @NotEmpty(message = "模板ID不可空")
    private String boardId;
    /**
     * 名称
     */
    private String name;
    /**
     * tag
     */
    private String tags;

    /**
     * 是否公开
     * 0不公开 1公开
     */
    private Integer pub;
    /**
     * 是否隐藏
     * 0不隐藏 1隐藏
     */
    private Integer hide;
    /**
     * 是否启用
     * 0不启用 1启用
     */
    private Integer enable;
    /**
     * 设备类型
     */
    private Integer assetMode;
}

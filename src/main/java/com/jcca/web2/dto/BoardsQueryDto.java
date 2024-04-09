package com.jcca.web2.dto;

import lombok.Data;

/**
 * @description: 查询图标模板
 * @author: Lvyp
 * @create: 2023/11/10 17:39
 */
@Data
public class BoardsQueryDto extends PageDto {

    private String name;

    private String tag;

}

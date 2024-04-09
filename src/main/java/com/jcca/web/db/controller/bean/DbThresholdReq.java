package com.jcca.web.db.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName DbThresholdReq
 * @Description 数据库表空间阈值
 * @Date 2020/9/3 11:23
 * @Author hanwone
 */
@Data
public class DbThresholdReq {

    @NotEmpty(message = "数据ID不能为空")
    private String id;

    private Integer tablespaceThreshold;
}

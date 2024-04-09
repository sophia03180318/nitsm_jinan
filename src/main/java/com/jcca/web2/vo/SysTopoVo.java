package com.jcca.web2.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description: 拓扑图
 * @author sophia
 * @create: 2023/10/20 09:45
 **/
@Data
public class SysTopoVo implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "拓扑图种类不能为空")
    private String category;
    private String xml;
    private String orgId;
    private String filename;
    private String assetId;
}
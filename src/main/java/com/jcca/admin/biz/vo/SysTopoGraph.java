package com.jcca.admin.biz.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author hanwone
 * @date 2020-04-30 15:53:07
 **/
@Data
public class SysTopoGraph implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "拓扑图种类不能为空")
    private String category;
    private String xml;
    private String orgId;
    private String filename;
    private String assetId;
}
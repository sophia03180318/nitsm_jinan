package com.jcca.web.asset.controller.bean;

import lombok.Data;

/**
 * @ClassName AssetProcessVo
 * @Author syt
 * @Date 2021/10/28  16:56
 */
@Data
public class AssetProcessTreeVo {
    private String id;
    private String pId;
    private Byte status;
    private String title;
    private Integer type;
    private String orgName;
    private String orgId;
    private String serviceTypeId;
}

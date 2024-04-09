package com.jcca.admin.system.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/18 14:23
 */
@Data
public class AssetPortVo {
    private String portIndex;
    private String assetId;
    private String portType;
    private String portName;
    private String shortName;
    private Integer pointX;
    private Integer pointY;
    private String vlanId;
    private String NodeId;
    private String parentId;
    private Integer status;
    private Integer isPort;
    private String portMode;
    //缩略名称 即 Gi/0/21
    private String portSlugName;
    // 旋转角度 syt 2021/9/28
    private Integer rotation;
    private Date updateDate;

    private Boolean show;

    private List<AssetPortVo> sonList;

}

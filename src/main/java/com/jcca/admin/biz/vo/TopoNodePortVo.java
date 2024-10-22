package com.jcca.admin.biz.vo;

import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/20 10:36
 */
@Data
public class TopoNodePortVo {
    private String assetId;
    private String nodeId;
    private Integer pointX;
    private Integer pointY;
    private String type;
    private String content;
    private String portIndex;
    private String parentId;
    private String categoryPic;
    private String pictureImg;
    private Integer height;
    private Integer width;
    private String portMode;
    // 旋转角度 syt 20201/9/28
    private Integer rotation;
    /**
     * 模板上配置的名字
     */
    private String tempPortName;
}

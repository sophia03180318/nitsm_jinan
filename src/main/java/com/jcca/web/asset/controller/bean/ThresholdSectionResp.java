package com.jcca.web.asset.controller.bean;

import lombok.Data;

@Data
public class ThresholdSectionResp {

    private String sectionId;
    /**
     * 阈值类型
     * ThresholdSectionEnum
     */
    private String type;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 标识
     * 端口索引之类的
     */
    private String flag;
    /**
     * 设定的最小值
     */
    private Double minPrice;
    /**
     * 设定的最大值
     */
    private Double maxPrice;
    /**
     * 1启用
     * -1禁用
     */
    private Integer status;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

}

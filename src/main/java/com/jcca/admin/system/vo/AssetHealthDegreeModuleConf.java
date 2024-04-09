package com.jcca.admin.system.vo;

import lombok.Data;

/**
 * 资产健康度管理配置
 *
 * @author lyp
 */
@Data
public class AssetHealthDegreeModuleConf {
    /**
     * 一级最大得分
     */
    private Integer oneLevelMaxScore;
    /**
     * 二级最大得分
     */
    private Integer twoLevelMaxScore;
    /**
     * 三级最大得分
     */
    private Integer threeLevelMaxScore;
    /**
     * 一级告警单条记录得分
     */
    private Integer oneLevelScore;
    /**
     * 二级告警单条记录得分
     */
    private Integer twoLevelScore;
    /**
     * 三级告警单条记录得分
     */
    private Integer threeLevelScore;

}

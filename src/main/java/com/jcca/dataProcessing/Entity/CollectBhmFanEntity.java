package com.jcca.dataProcessing.Entity;


import lombok.Data;

import java.io.Serializable;

/**
 * 采集管理口的CPU信息
 */
@Data
public class CollectBhmFanEntity extends CommonEntity implements Serializable {

    /**
     * 名称
     */
    private String name;
    /**
     * 成员ID 代表风扇的槽位
     * 第几个风扇
     */
    private String memberId;
    /**
     * 部件编号
     */
    private String partNumber;
    /**
     * 当前转速
     */
    private Integer reading;
    /**
     * 转速单位
     */
    private String readingUnits;
    /**
     * 状态
     */
    private ReadFishStatusEntity status;
    /**
     * 最大转速
     *  "RPM"(转 / 分钟)
     */
    private Integer maxReadingRange;
    /**
     * 最小转速
     * "RPM"(转 / 分钟)
     */
    private Integer minReadingRange;



}

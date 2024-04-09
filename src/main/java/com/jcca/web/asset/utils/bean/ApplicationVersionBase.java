package com.jcca.web.asset.utils.bean;

import lombok.Data;

/**
 * 应用版本信息
 *
 * @author Lvyp
 */
@Data
public class ApplicationVersionBase {
    /**
     * 版本
     */
    private String version;
    /**
     * 开始时间
     */
    private String startDateStr;
    /**
     * 结束时间
     */
    private String endDateStr;
    /**
     * 更换原因
     */
    private String changeRemark;
    /**
     * 厂家签名
     */
    private String signature;
    /**
     * 中心签名
     */
    private String centerSignature;

}

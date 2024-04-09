package com.jcca.web.asset.utils.bean;

import lombok.Data;

/**
 * 硬件信息
 *
 * @author Lvyp
 */
@Data
public class HardwareBase {

    /**
     * 更换名称
     */
    private String name;
    /**
     * 更换备注
     */
    private String changeRemark;
    /**
     * 更换时间
     */
    private String changeDateStr;
    /**
     * 厂家签名
     */
    private String signature;
    /**
     * 中心签名
     */
    private String centerSignature;

}

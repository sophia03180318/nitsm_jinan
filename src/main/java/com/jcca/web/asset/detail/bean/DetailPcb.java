package com.jcca.web.asset.detail.bean;

import lombok.Data;

/**
 * @ClassName DetailPcb
 * @Description 资产详情----板卡信息
 * @Date 2020/6/23 13:25
 * @Author hanwone
 */
@Data
public class DetailPcb {

    /**
     * 板卡编号
     */
    private String serialNumber;
    /**
     * 板卡索引
     */
    private String pcbIndex;
    /**
     * 板卡名称
     */
    private String name;
    /**
     * 型号名称
     */
    private String modelName;
    /**
     * 板卡描述
     */
    private String descStr;
}

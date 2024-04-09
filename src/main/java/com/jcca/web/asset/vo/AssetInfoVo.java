package com.jcca.web.asset.vo;

/**
 * @ Author：sophia
 * @ Date：Created in 16:23 2021/7/6
 * @ Description:
 */

import com.jcca.web.asset.entity.Asset;
import lombok.Data;

@Data
public class AssetInfoVo {
    /**
     * 是否正确
     */
    private boolean Judge;
    /**
     * 报错信息
     */
    private String Info;
    /**
     * 资产对象
     */
    private Asset asset;
    /**
     * 值
     */
    private int intData;

}

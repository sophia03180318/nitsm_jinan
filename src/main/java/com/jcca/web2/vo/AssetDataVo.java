package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 资产数据VO
 * @author: Lvyp
 * @create: 2023/11/13 13:27
 */
@Data
public class AssetDataVo {
    /**
     * 前端可取的属性
     */
    private BoardObjVo metric;
    /**
     * map key时间戳,value 值
     */
    private Object values;

}

package com.jcca.web2.vo;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * @description: 资产状态详情
 * @author: Lvyp
 * @create: 2023/12/28 11:21
 */
@Data
public class AssetStatusDetailVo {

    /**
     * 详情
     */
    private List<?> dataList;
    /**
     * 表头列表
     */
    private List<JSONObject> titleList;

}

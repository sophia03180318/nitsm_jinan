package com.jcca.web.asset.detail;

import com.jcca.common.bean.ResultVo;
import com.jcca.web.asset.entity.Asset;

/**
 * @ClassName DetailAdapter
 * @Description 资产详情适配器
 * @Date 2020/6/22 13:37
 * @Author hanwone
 */
public interface DetailAdapter {

    String getCode();

    /**
     * 资产详情
     * @param asset
     * @return
     */
    ResultVo handle(Asset asset);

    /**
     * topo点击的时候出来的右侧框
     * @param asset
     * @return
     */
    ResultVo getAssetGeneralInfo(Asset asset);

}

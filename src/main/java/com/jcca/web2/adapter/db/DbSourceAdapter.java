package com.jcca.web2.adapter.db;

import com.jcca.web2.dto.ExeSqlQuery;
import com.jcca.web2.vo.AssetDataVo;

import java.util.List;

/**
 * @description: 数据源适配器
 * @author: Lvyp
 * @create: 2023/11/01 17:41
 */
public interface DbSourceAdapter {

    /**
     * 适配码
     *
     * @return
     */
    public String getAdapterCode();

    /**
     * 执行命令
     *
     * @param query
     * @return
     */
    public List<AssetDataVo> exeCommand(ExeSqlQuery query);

}

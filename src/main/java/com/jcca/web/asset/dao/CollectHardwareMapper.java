package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.CollectHardware;
import org.apache.ibatis.annotations.Select;

/**
 * @author: hhw
 * @description: CollectHardwareMapper 主要是用来
 * @date: 2025-08-25  15:11
 * @since: 2.1.9.0
 */
public interface CollectHardwareMapper extends BaseMapper<CollectHardware> {

    CollectHardware findByAssetId(String assetId);
}

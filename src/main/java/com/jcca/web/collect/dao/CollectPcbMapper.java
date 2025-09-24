package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectPcb;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 板卡采集信息
 *
 * @author Lvyp
 */
@Mapper
public interface CollectPcbMapper extends BaseMapper<CollectPcb> {

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectPcb> selectRealTimeData(String assetId);

    List<CollectPcb> getPcdInfoByAsset(String assetId);
}

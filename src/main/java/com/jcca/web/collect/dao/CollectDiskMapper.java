package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectDisk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 采集磁盘信息
 *
 * @author Lvyp
 */
@Mapper
public interface CollectDiskMapper extends BaseMapper<CollectDisk> {


    /**
     * 查询资产当前对应最新的数据
     *
     * @param assetId
     * @return
     */
    List<CollectDisk> selectRealTimeData(@Param("assetId") String assetId);

}

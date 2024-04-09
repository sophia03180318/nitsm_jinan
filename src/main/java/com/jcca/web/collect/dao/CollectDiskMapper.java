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
    @Select("SELECT * FROM COLLECT_DISK b WHERE b.COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code FROM COLLECT_DISK WHERE ASSET_ID=#{assetId})")
    List<CollectDisk> selectRealTimeData(@Param("assetId") String assetId);

}

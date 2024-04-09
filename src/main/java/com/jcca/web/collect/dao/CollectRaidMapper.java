package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectRaid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 0:32 2022/6/9
 * @ Description:
 */
@Mapper
public interface CollectRaidMapper extends BaseMapper<CollectRaid> {

    @Select("select a.* from COLLECT_RAID a where COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code  from COLLECT_RAID where ASSET_ID=#{assetId} and DISK_TYPE=#{type}) and ASSET_ID=#{assetId} and DISK_TYPE=#{type}")
    List<CollectRaid> findByType(String assetId, int type);

    @Select("select a.* from COLLECT_RAID a where COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code  from COLLECT_RAID where ASSET_ID=#{assetId} and GROUP_ID=#{groupId}  and DISK_TYPE=#{type}) and ASSET_ID=#{assetId} and DISK_TYPE=#{type}")
    List<CollectRaid> findByGroup(String assetId, String groupId, int type);

    @Select("SELECT MAX(COLLECT_TIME) AS code  from COLLECT_RAID where ASSET_ID=#{assetId}")
    Date findLastTime(String assetId);

    @Select("select * from COLLECT_RAID where DISK_TYPE =4 and COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code  from COLLECT_RAID where ASSET_ID=#{assetId} and DISK_TYPE= 4) and PARENT_ORG_ID in (select DISTINCT(REAL_ID) from COLLECT_RAID where ASSET_ID=#{assetId} and DISK_TYPE= 1)")
    List<CollectRaid> findDrive(String assetId);

    @Select("SELECT MAX(COLLECT_TIME) AS code  from COLLECT_RAID")
    Date getLastTime();
}

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

    List<CollectRaid> findByType(String assetId, int type);

    List<CollectRaid> findByGroup(String assetId, String groupId, int type);

    Date findLastTime(String assetId);

    List<CollectRaid> findDrive(String assetId);

    Date getLastTime();
}

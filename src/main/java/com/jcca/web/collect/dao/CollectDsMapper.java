package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectDS;
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
public interface CollectDsMapper extends BaseMapper<CollectDS> {

    List<CollectDS> findByType(String assetId, int type);

    Date findLastTime(String assetId);

    List<CollectDS> findByArray(String arrayId);


    List<CollectDS> findByDrives(String assetId, int index);
}

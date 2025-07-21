package com.jcca.web.broken.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.broken.entity.BrokenRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 故障记录
 *
 * @author lyp
 */
@Mapper
public interface BrokenRecordMapper extends BaseMapper<BrokenRecord> {

    @Select("select * from BROKEN_RECORD where asset_id =#{asset_id} order by CREATE_TIME desc")
    List<BrokenRecord> getListByAssetId(String assetId);
}

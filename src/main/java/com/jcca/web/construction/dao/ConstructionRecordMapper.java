package com.jcca.web.construction.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.construction.entity.ConstructionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 施工记录
 *
 * @author lyp
 */
@Mapper
public interface ConstructionRecordMapper extends BaseMapper<ConstructionRecord> {
    List<ConstructionRecord> getConstructionRecord(String alarmId);
}

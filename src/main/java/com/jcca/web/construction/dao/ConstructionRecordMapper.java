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
    @Select("SELECT c.* FROM   construction_record c JOIN   alarm_info a ON a.id = #{alarmId}  WHERE  c.influence LIKE '%' || a.asset_id || '%'  AND  a.occur_time BETWEEN c.start_time AND c.end_time order by c.MODIFY_TIME desc")
    List<ConstructionRecord> getConstructionRecord(String alarmId);
}

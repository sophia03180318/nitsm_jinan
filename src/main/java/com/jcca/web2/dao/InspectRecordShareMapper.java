package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.InspectRecordShare;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.vo.InspectShareVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: hhw
 * @description: InspectRecordShareMapper 主要是用来
 * @date: 2025-07-17  13:26
 * @since: 2.1.8.0
 */
public interface InspectRecordShareMapper extends BaseMapper<InspectRecordShare> {

    @Select("select distinct x.* from inspect_record_share s left join xunjian_schedule x on s.job_id =  x.job_id where s.viewer = #{username}")
    List<XunjianSchedule> findByViewer(String username);

    @Select("SELECT DISTINCT R.ID, R.INSPECT_TIME AS NAME, '2' isShare FROM INSPECT_RECORD R " +
            "RIGHT JOIN INSPECT_RECORD_SHARE S ON R.ID = S.INSPECT_RECORD_ID " +
            "WHERE R.INSPECT_CODE = #{jobId} AND S.VIEWER = #{username} ORDER BY R.INSPECT_TIME DESC")
    List<InspectShareVo> findShareRecord(String jobId, String username);
}

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

    List<XunjianSchedule> findByViewer(String username);

    List<InspectShareVo> findShareRecord(String jobId, String username);
}

package com.jcca.web.broken.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.broken.entity.BrokenRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 故障记录
 *
 * @author lyp
 */
@Mapper
public interface BrokenRecordMapper extends BaseMapper<BrokenRecord> {

}

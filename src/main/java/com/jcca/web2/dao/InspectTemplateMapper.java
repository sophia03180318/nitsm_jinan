package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.InspectTemplate;
import com.jcca.web2.vo.InspectTemplateVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InspectTemplateMapper extends BaseMapper<InspectTemplate> {
    /**
     * 查询用户对应得巡检模板
     *
     * @param userId
     * @return
     */
    List<InspectTemplateVo> queryInspectTemplateData(@Param(value = "userId") String userId);

}

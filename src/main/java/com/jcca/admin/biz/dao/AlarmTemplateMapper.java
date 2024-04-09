package com.jcca.admin.biz.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.biz.entity.AlarmTemplate;
import org.apache.ibatis.annotations.Select;

/**
 * @author hanwone
 * @date 2020-04-23 15:52:15
 **/
public interface AlarmTemplateMapper extends BaseMapper<AlarmTemplate> {


    /**
     * 通过类别查询模板
     *
     * @param category
     * @return
     */
    @Select(value = "select * from alarm_template where category = #{category}")
    AlarmTemplate selectByCategory(String category);
}

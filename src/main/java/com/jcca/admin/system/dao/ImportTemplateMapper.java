package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.ImportTemplate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 17:08 2021/6/22
 * @ Description:
 */
public interface ImportTemplateMapper extends BaseMapper<ImportTemplate> {


    /*根据名称查询模板*/
    @Select("select * from import_template where TEMPLATE_NAME = #{templateName}")
    List<ImportTemplate> selectByTemplateName(@Param("templateName") String templateName);


}

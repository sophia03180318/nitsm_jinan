package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.ImportTemplate;
import com.jcca.admin.system.vo.Template;

/**
 * @ Author：sophia
 * @ Date：Created in 16:23 2021/6/22
 * @ Description:
 */
public interface ImportTemplateService extends IService<ImportTemplate> {
    ImportTemplate findById(String id);

    /*添加模板*/
    int createTemplate(ImportTemplate template);

    /*删除模板*/
    int deleteTemplateById(String id);

    /*编辑模板*/
    int updateTemplate(ImportTemplate template);

    /*模板是否重名*/
    boolean repeatByUsername(String name, String id);

    /*类型转换 template->importTemplate*/
    ImportTemplate transformImportTemplate(Template template);

    /*类型转换 importTemplate->template*/
    Template transformTemplate(ImportTemplate importTemplate);


}

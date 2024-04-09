package com.jcca.admin.system.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.ImportTemplateMapper;
import com.jcca.admin.system.entity.ImportTemplate;
import com.jcca.admin.system.service.ImportTemplateService;
import com.jcca.admin.system.util.TemplateExportUtil;
import com.jcca.admin.system.vo.Template;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ Author：sophia
 * @ Date：Created in 17:06 2021/6/22
 * @ Description:
 */
@Service
@Slf4j
public class ImportTemplateImpl extends ServiceImpl<ImportTemplateMapper, ImportTemplate> implements ImportTemplateService {

    @Resource
    private ImportTemplateMapper importTemplateMapper;

    @Override
    public ImportTemplate findById(String id) {
        return importTemplateMapper.selectById(id);
    }

    @Override
    public int createTemplate(ImportTemplate template) {
        try {
            importTemplateMapper.insert(template);
            log.info("成功添加模板");
            return 1;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return -1;
    }

    @Override
    public int deleteTemplateById(String id) {

        try {
            int i = importTemplateMapper.deleteById(id);
            if (i == 1) {
                log.info("成功删除id为[" + id + "]的模板");
            } else {
                log.info("不存在id为[" + id + "]的模板");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return -1;
        }
        return 1;
    }

    @Override
    public int updateTemplate(ImportTemplate importTemplate) {
        String id = importTemplate.getId();

        try {
            int i = importTemplateMapper.updateById(importTemplate);
            log.info("成功修改id为[" + id + "]的模板");
            return 1;
        } catch (Exception e) {
            log.error(e.getMessage());
            return -1;
        }
    }

    @Override

    public boolean repeatByUsername(String templateName, String id) {
        List<ImportTemplate> list = importTemplateMapper.selectByTemplateName(templateName);
        if (list.size() > 0) {
            String id2 = list.get(0).getId();
            if (!list.get(0).getId().equals(id)) {
                return true;
            }
        }


        return false;
    }

    @Override
    public ImportTemplate transformImportTemplate(Template template) {
        ImportTemplate importTemplate = new ImportTemplate();
        importTemplate.setId(template.getId());
        importTemplate.setTemplateName(template.getTemplateName());
        importTemplate.setType(template.getType());
        importTemplate.setRemark(template.getRemark());
        importTemplate.setCreateTime(template.getCreateTime());
        importTemplate.setModifyTime(template.getModifyTime());

        ArrayList<String> fielName = new ArrayList<>();
        Collections.addAll(fielName, "serialVersionUID", "id", "templateName", "type", "remark", "createTime", "modifyTime");
        String auttributeStr = "";
        try {
            auttributeStr = TemplateExportUtil.getValuesStr(template, fielName);

        } catch (NoSuchFieldException e) {
            log.error(e.getMessage());
        } catch (IllegalAccessException e) {
            log.error(e.getMessage());
        }
        importTemplate.setAttribute(auttributeStr);
        return importTemplate;
    }

    @Override
    public Template transformTemplate(ImportTemplate importTemplate) {
        String attribute = importTemplate.getAttribute();
        Template template = new Template();
        template.setId(importTemplate.getId());
        template.setTemplateName(importTemplate.getTemplateName());
        template.setType(importTemplate.getType());
        template.setRemark(importTemplate.getRemark());
        template.setCreateTime(importTemplate.getCreateTime());
        template.setModifyTime(importTemplate.getModifyTime());

        Map<String, String> template_kv = DictUtil.value("TEMPLATE_KV");
        for (Map.Entry<String, String> kv : template_kv.entrySet()) {
            if (attribute.contains(kv.getValue())) {
                TemplateExportUtil.setFieldValues(template, kv.getKey(), kv.getKey());
            }
        }
        return template;
    }


}

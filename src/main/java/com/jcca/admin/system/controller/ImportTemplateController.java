package com.jcca.admin.system.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jcca.admin.system.entity.ImportTemplate;
import com.jcca.admin.system.service.ImportTemplateService;
import com.jcca.admin.system.util.TemplateExportUtil;
import com.jcca.admin.system.vo.Template;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 17:51 2021/6/22
 * @ Description:
 */
@Slf4j
@Controller
@RequestMapping("/system/export")
public class ImportTemplateController {
    @Resource
    private ImportTemplateService importTemplateService;

    /**
     * 模板页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:export:index")
    @ActionLog(name = "查看模板列表", title = "模板导出", key = LogTypeConstant.QUERY)
    public String index(Model model, ImportTemplate importTemplate, Integer size, Integer page) {

        /*获取模板列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<ImportTemplate> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(importTemplate.getTemplateName())) {
            wrapper.like("TEMPLATE_NAME", importTemplate.getTemplateName());
        }
/*        if (StringUtils.isNotEmpty(importTemplate.getType())) {
            wrapper.like("type", importTemplate.getType());
        }*/
        wrapper.ne("ID", "1131");
        wrapper.ne("ID", "1132");

        wrapper.orderByDesc("MODIFY_TIME");
        iPage = importTemplateService.page(iPage, wrapper);
        List<ImportTemplate> records = iPage.getRecords();
        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/system/export/index";
    }

    /**
     * 跳转到模板添加页面
     */
    @GetMapping("/add")
    @RequiresPermissions("system:export:add")
    public String toAdd() {
        return "/system/export/add";
    }

    /**
     * 跳转到模板编辑页面
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:export:edit")
    public String toEdit(@PathVariable("id") String id, Model model) {
        Template template = importTemplateService.transformTemplate(importTemplateService.getById(id));
        model.addAttribute("template", template);
        return "/system/export/add";
    }

    /**
     * 跳转到模板属性详细页面
     */
    @GetMapping("/detail/{id}")
    @RequiresPermissions("system:export:detail")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("template", importTemplateService.getById(id));
        //在这转换成 template类   让详细属性 分列展示
        return "/system/export/detail";
    }

    /**
     * 导出指定模板
     */
    @GetMapping("/exportTemplate/{id}")
    @RequiresPermissions("system:export:exportTemplate")
    @ActionLog(name = "导出模板文件", title = "模板导出", key = LogTypeConstant.DOWNLOAD)
    public void exportExcel(@PathVariable("id") String id, HttpServletResponse response) {
        Template template = importTemplateService.transformTemplate(importTemplateService.findById(id));

        try {
            String excelName = template.getTemplateName();
            template.setTemplateName(null);
            template.setId(null);
            template.setType(null);
            template.setRemark(null);
            template.setCreateTime(null);
            template.setModifyTime(null);
            ArrayList<String> attributeList = TemplateExportUtil.isAllFieldNotNull(template);
            SXSSFWorkbook excel = TemplateExportUtil.createExcel(attributeList);
            DispatchRecordExcelUtil.responseBody(excel, response, excelName);
        } catch (Exception e) {

        }
    }


    @ResponseBody
    @RequestMapping("/deleteById")
    @RequiresPermissions("system:export:deleteById")
    @ActionLog(name = "删除模板", title = "模板导出", key = LogTypeConstant.REMOVEE)
    public ResultVo deleteById(String id) {

        int i = importTemplateService.deleteTemplateById(id);
        if (i == 1) {
            return ResultVoUtil.success("模板删除成功");
        }
        return ResultVoUtil.error("模板删除失败");
    }

    /**
     * 保存添加/修改的数据
     *
     * @param template 实体对象
     */
    @PostMapping("/save")
    @RequiresPermissions({"system:export:add", "system:export:edit"})
    @ResponseBody
    @ActionLog(name = "保存模板", title = "模板导出", key = LogTypeConstant.ADD)
    public ResultVo save(Template template) {

        // 判断模板名称是否为空
        if (ObjectUtil.isNull(template.getTemplateName()) || template.getTemplateName().trim().isEmpty()) {
            throw new ResultException(ResultEnum.TEMPLATE_NAME_NULL);
        }

/*        // 判断类型是否为空
        if (ObjectUtil.isNull(template.getType()) || template.getType().trim().isEmpty() ){
            throw new ResultException(ResultEnum.TEMPLATE_TYPE_NULL);
        }*/

        if (template.getId() == null) {
            // 判断模板名称是否重复
            if (importTemplateService.repeatByUsername(template.getTemplateName(), template.getId())) {
                throw new ResultException(ResultEnum.TEMPLATE_NAME_EXIST);
            }
        }
        template.setType("1");
        // 保存数据时 转化为entity类 importTemplate
        importTemplateService.saveOrUpdate(importTemplateService.transformImportTemplate(template));
        return ResultVoUtil.SAVE_SUCCESS;
    }


}
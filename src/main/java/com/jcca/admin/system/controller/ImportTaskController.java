package com.jcca.admin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.entity.ImportTemplate;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.web.asset.entity.AssetImportTask;
import com.jcca.web.asset.service.AssetImportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 14:09 2021/7/16
 * @ Description:
 */

@Slf4j
@Controller
@RequestMapping("/system/task")
public class ImportTaskController {
    @Resource
    AssetImportTaskService assetImportTaskService;


    /**
     * 任务日志页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:task:index")
    public String index(Model model, AssetImportTask assetImportTask, Integer size, Integer page) {

        /*获取模板列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<AssetImportTask> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("CREATE_TIME");

        iPage = assetImportTaskService.page(iPage, wrapper);

        List<ImportTemplate> records = iPage.getRecords();
        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/system/task/index";
    }


}

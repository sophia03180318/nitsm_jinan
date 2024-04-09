package com.jcca.admin.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SoftwareType;
import com.jcca.admin.system.service.SoftwareTypeService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.PromptInfo;
import com.jcca.web.asset.service.PromptInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ Author：syt
 * @ Date：Created in 14:09 2021/11/9
 * @ Description:
 */

@Slf4j
@Controller
@RequestMapping("/system/software")
public class SoftwareTypeController {

    @Resource
    private SoftwareTypeService softwareTypeService;
    @Resource
    private PromptInfoService promptInfoService;
//    @Resource
//    private CodeService codeServ;


    /**
     * 软件类型页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:software:index")
    @ActionLog(name = "查看软件类型列表", title = "软件类型", key = LogTypeConstant.QUERY)
    public String index(Model model, Integer size, Integer page, SoftwareType softwareType) {

        /*获取软件类型列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<SoftwareType> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotEmpty(softwareType.getName())) {
            wrapper.like("NAME", softwareType.getName());
        }
        wrapper.orderByDesc("CREATE_TIME");

        iPage = softwareTypeService.page(iPage, wrapper);

        List<SoftwareType> records = iPage.getRecords();

        for (SoftwareType record : records) {
            // 获取每种类型的提示信息
            QueryWrapper<PromptInfo> query = Wrappers.query();
            query.eq("SOFTWARETYPE_ID", record.getId());
            List<String> promptInfos = promptInfoService.list(query).stream().map(PromptInfo::getValue).collect(Collectors.toList());
            record.setProcesses(promptInfos.toString());

        }
        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/system/software/index";
    }

    /**
     * 跳转到添加页面
     */
    @GetMapping("/add")
    @RequiresPermissions("system:software:add")
    public String toAdd() {
        return "/system/software/add";
    }

    /**
     * 跳转到编辑页面
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:software:edit")
    public String toEdit(@PathVariable("id") String id, Model model) {
        QueryWrapper<SoftwareType> query = Wrappers.query();
        query.eq("ID", id);
        SoftwareType one = softwareTypeService.getOne(query);

        model.addAttribute("softwareType", one);
        return "/system/software/add";
    }

    /**
     * 保存添加/修改的数据
     */
    @PostMapping({"/add", "/edit"})
    @RequiresPermissions({"system:software:add", "system:software:edit"})
    @ResponseBody
    @ActionLog(name = "新增或修改软件类型", title = "软件类型", key = LogTypeConstant.MODIFY)
    public ResultVo save(SoftwareType softwareType) {
        // 创建信息
        softwareType.setCreator(ShiroUtil.getSubject().getUsername());
        softwareType.setCreateTime(new Date());
        if (StrUtil.isNotBlank(softwareType.getId())) {
            // 编辑
            softwareTypeService.updateById(softwareType);
        } else {
            // 添加
            List<String> s = softwareTypeService.list().stream().map(SoftwareType::getName).collect(Collectors.toList());
            if (CollUtil.contains(s, softwareType.getName())) {
                return ResultVoUtil.error("当前名称【" + softwareType.getName() + "】与已有名称重复，请核实后再添加");
            }
            softwareType.setId(MyIdUtil.getId());
            softwareTypeService.save(softwareType);
        }
        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除配置缓存数据
     */
    @GetMapping({"/remove"})
    @RequiresPermissions({"system:software:remove"})
    @ResponseBody
    @ActionLog(name = "删除软件类型", title = "软件类型", key = LogTypeConstant.REMOVEE)
    public ResultVo remove(@RequestParam("id") String id) {
        softwareTypeService.removeById(id);
        return ResultVoUtil.SAVE_SUCCESS;
    }

}

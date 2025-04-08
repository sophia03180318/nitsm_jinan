package com.jcca.admin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.entity.SysDict;
import com.jcca.admin.system.service.SysDictService;
import com.jcca.admin.system.validator.DictValid;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.StatusUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hanwone
 * @date 2018/8/14
 */
@Controller
@RequestMapping({"/system/dict","/api/v2/system/dict"})
public class DictController {

    @Resource
    private SysDictService dictService;

    /**
     * 列表页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:dict:index")
    @ActionLog(name = "查看字典列表", title = "字典管理", key = LogTypeConstant.QUERY)
    public String index(Model model, SysDict dict, Integer page, Integer size) {
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        if (dict.getName() != null) {
            wrapper.like("name", dict.getName());
        }
        if (dict.getTitle() != null) {
            wrapper.like("title", dict.getTitle());
        }
        if (dict.getStatus() == null) {
            wrapper.eq("status", StatusEnum.OK.getCode());
        }
        wrapper.orderByDesc("modify_time");
        iPage = dictService.page(iPage, wrapper);
        // 封装数据
        model.addAttribute("list", iPage.getRecords());
        model.addAttribute("page", iPage);
        return "/system/dict/index";
    }

    /**
     * 跳转到添加页面
     */
    @GetMapping("/add")
    @RequiresPermissions("system:dict:add")
    public String toAdd() {
        return "/system/dict/add";
    }

    /**
     * 跳转到编辑页面
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:dict:edit")
    public String toEdit(@PathVariable("id") String id, Model model) {
        model.addAttribute("dict", dictService.getById(id));
        return "/system/dict/add";
    }

    /**
     * 保存添加/修改的数据
     *
     * @param valid 验证对象
     */
    @PostMapping({"/add", "/edit"})
    @RequiresPermissions({"system:dict:add", "system:dict:edit"})
    @ResponseBody
    @ActionLog(name = "新增或修改字典", title = "字典管理", key = LogTypeConstant.ADD)
    public ResultVo save(@Validated DictValid valid, SysDict dict) {
        // 清除字典值两边空格
        dict.setValue(dict.getValue().trim());

        // 复制保留无需修改的数据
        if (dict.getId() != null) {
            SysDict beDict = dictService.getById(dict.getId());
            EntityBeanUtil.copyProperties(beDict, dict);
        } else {
            // 判断字典标识是否重复
            if (dictService.repeatByName(dict)) {
                throw new ResultException(ResultEnum.DICT_EXIST);
            }
        }

        // 保存数据
        dict.setStatus(StatusEnum.OK.getCode());
        dictService.saveOrUpdate(dict);
        if (dict.getId() != null) {
            DictUtil.clearCache(dict.getName());
        }
        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 跳转到详细页面
     */
    @GetMapping("/detail/{id}")
    @RequiresPermissions("system:dict:detail")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("dict", dictService.getById(id));
        return "/system/dict/detail";
    }

    /**
     * 设置一条或者多条数据的状态
     */
    @RequestMapping("/status/{param}")
    @RequiresPermissions("system:dict:status")
    @ResponseBody
    @ActionLog(name = "修改字典状态", title = "字典管理", key = LogTypeConstant.MODIFY)
    public ResultVo status(
            @PathVariable("param") String param,
            @RequestParam(value = "ids", required = false) List<String> ids) {
        // 更新状态
        StatusEnum statusEnum = StatusUtil.getStatusEnum(param);
        if (dictService.updateStatus(statusEnum, ids)) {
            return ResultVoUtil.success(statusEnum.getMessage() + "成功");
        } else {
            return ResultVoUtil.error(statusEnum.getMessage() + "失败，请重新操作");
        }
    }
}

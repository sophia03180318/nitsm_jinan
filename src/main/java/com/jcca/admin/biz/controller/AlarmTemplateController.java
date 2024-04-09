package com.jcca.admin.biz.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.biz.entity.AlarmTemplate;
import com.jcca.admin.biz.service.AlarmTemplateService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName BizMsgTemplateController
 * @Description 消息模板
 * @Date 2020/4/23 16:06
 * @Author hanwone
 */
@Controller
@RequestMapping("/system/msg")
public class AlarmTemplateController {

    @Resource
    private AlarmTemplateService templateService;

    @Resource
    private RedisService redisService;

    /**
     * 消息模板首页
     *
     * @param model
     * @param template
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/index")
    @RequiresPermissions({"system:msg:index"})
    public String index(Model model, AlarmTemplate template, Integer page, Integer size) {
        IPage iPage = PagePlugin.startPage(page, size);
        QueryWrapper<AlarmTemplate> wrapper = new QueryWrapper<>();

        wrapper.eq("status", StatusEnum.OK.getCode());
        wrapper.orderByDesc("modify_time");
        if (StrUtil.isNotEmpty(template.getTitle())) {
            wrapper.like("title", template.getTitle());
        }
        if (StrUtil.isNotEmpty(template.getName())) {
            wrapper.like("name", template.getName());
        }
        if (StrUtil.isNotEmpty(template.getCategory())) {
            wrapper.like("category", template.getCategory());
        }

        iPage = templateService.page(iPage, wrapper);
        // 封装数据
        model.addAttribute("list", iPage.getRecords());
        model.addAttribute("page", iPage);

        return "/biz/msg/index";
    }

    /**
     * 跳转到添加页面
     *
     * @return
     */
    @GetMapping("/add")
    @RequiresPermissions("system:msg:add")
    public String add() {
        return "/biz/msg/add";
    }

    /**
     * 跳转到编辑页面
     *
     * @return
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:msg:edit")
    public String edit(@PathVariable(value = "id") String id, Model model) {
        model.addAttribute("msg", templateService.getById(id));
        return "/biz/msg/add";
    }

    /**
     * 保存消息模板
     *
     * @param msg
     * @return
     */
    @PostMapping("/save")
    @ResponseBody
    @RequiresPermissions("system:msg:save")
    public ResultVo save(@Validated AlarmTemplate msg) {

        AlarmTemplate otherPlate = templateService.findByCategory(msg.getCategory());
        if (msg.getId() == null) {
            // 检查是否存在相同类别
            if (otherPlate != null) {
                return ResultVoUtil.error("模板类别已存在");
            }
            templateService.save(msg);
        } else {
            if (otherPlate != null && !otherPlate.getId().equals(msg.getId())) {
                return ResultVoUtil.error("模板类别已存在");
            }

            AlarmTemplate template = templateService.getById(msg.getId());
            String[] fields = {"createTime", "creator"};
            EntityBeanUtil.copyProperties(template, msg, fields);
            // 保存数据
            msg.setStatus(StatusEnum.OK.getCode());
            templateService.saveOrUpdate(msg);

        }
        redisService.remove(RedisCacheConst.ALARM_CATEGORY_PRE + msg.getCategory());
        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 跳转到详细页面
     */
    @GetMapping("/detail/{id}")
    @RequiresPermissions("system:msg:detail")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("msg", templateService.getById(id));
        return "/biz/msg/detail";
    }

    /**
     * 设置数据的状态
     */
    @GetMapping("/status/{param}")
    @RequiresPermissions("system:msg:del")
    @ResponseBody
    public ResultVo updateStatus(
            @PathVariable("param") String param,
            @RequestParam(value = "ids", required = false) List<String> ids) {

        // 更新状态
        if (templateService.updateStatus(param, ids)) {
            return ResultVoUtil.success("更新成功");
        } else {
            return ResultVoUtil.error("更新失败，请重新操作");
        }
    }
}

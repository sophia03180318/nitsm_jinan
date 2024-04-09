package com.jcca.admin.biz.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName GlobalConfigController
 * @Description 配置操作
 * @Author wone
 * @Date 2021/1/8 13:34
 * @Version ITSM2.0
 **/
@Controller
@RequestMapping("/global/config")
public class GlobalConfigController {

    private static final String MAIN_MENU_CONF_KEY = "config:main_menu";

    @Resource
    private SysModuleConfigService configService;

    @Resource
    private RedisService redisService;

    @GetMapping("/getIndexMenu")
    @ResponseBody
    public String getIndexMenu() {
        QueryWrapper<SysModuleConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("name", MAIN_MENU_CONF_KEY);
        List<SysModuleConfig> configs = configService.list(wrapper);
        if (configs.isEmpty()) {
            return "topo";
        }
        return configs.get(0).getValue();
    }

    /**
     * 去配置页面
     *
     * @return
     */
    @GetMapping("/index")
    @RequiresPermissions("global:config:index")
    @ActionLog(name = "获取配置信息", title = "系统配置", key = LogTypeConstant.QUERY)
    public String index(Model model, SysModuleConfig config, Integer page, Integer size) {
        IPage<SysModuleConfig> iPage = PagePlugin.startPageT(page, size, SysModuleConfig.class);

        QueryWrapper<SysModuleConfig> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(config.getName())) {
            wrapper.like("name", config.getName());
        }
        wrapper.orderByDesc("create_time");
        iPage = configService.page(iPage, wrapper);

        List<SysModuleConfig> records = iPage.getRecords();
        for (SysModuleConfig record : records) {
            if (Objects.isNull(redisService.get(record.getName()))) {
                redisService.set(record.getName(), record);
            }
        }

        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);

        return "/biz/config/index";
    }

    /**
     * 跳转到添加页面
     */
    @GetMapping("/add")
    @RequiresPermissions("global:config:add")
    public String toAdd() {
        return "/biz/config/add";
    }

    /**
     * 跳转到编辑页面
     */
    @GetMapping("/edit/{name}")
    @RequiresPermissions("global:config:edit")
    public String toEdit(@PathVariable("name") String name, Model model) {
        Object o = redisService.get(name);
        if (Objects.isNull(o)) {
            QueryWrapper<SysModuleConfig> query = Wrappers.query();
            query.eq("name", name);
            o = configService.getOne(query);
        }

        model.addAttribute("config", o);
        return "/biz/config/add";
    }

    /**
     * 保存添加/修改的数据
     */
    @PostMapping({"/add", "/edit"})
    @RequiresPermissions({"global:config:add", "global:config:edit"})
    @ResponseBody
    @ActionLog(name = "保存配置信息", title = "系统配置", key = LogTypeConstant.ADD)
    public ResultVo save(SysModuleConfig config) {

        config.setCreator(ShiroUtil.getSubject().getUsername());
        config.setCreateTime(new Date());
        if (StrUtil.isNotBlank(config.getId())) {
            configService.updateById(config);
        } else {
            if (!config.getName().startsWith("config")) {
                return ResultVoUtil.error("配置项key必须以[config]开头,请核实后再添加");
            }

            Object o = redisService.get(config.getName());
            if (Objects.nonNull(o)) {
                return ResultVoUtil.error("当前key[" + config.getName() + "]与已有key重复,请核实后再添加");
            }

            config.setId(MyIdUtil.getId());
            configService.save(config);
        }

        redisService.set(config.getName(), config);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除配置缓存数据
     */
    @GetMapping({"/remove"})
    @RequiresPermissions({"global:config:remove"})
    @ResponseBody
    @ActionLog(name = "删除配置信息", title = "系统配置", key = LogTypeConstant.REMOVEE)
    public ResultVo remove(@RequestParam("name") String name) {

        SysModuleConfig o = (SysModuleConfig) redisService.get(name);
        configService.removeById(o);

        redisService.remove(name);

        return ResultVoUtil.SAVE_SUCCESS;
    }
}

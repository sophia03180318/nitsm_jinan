package com.jcca.admin.system.controller;

import com.jcca.common.utils.HttpServletUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopoController {

    /**
     * 主页
     */
    @GetMapping("/topo")
    @RequiresPermissions("topo")
    public String index(Model model) {
        String search = HttpServletUtil.getRequest().getQueryString();
        model.addAttribute("search", search);
        return "/system/topo/index";
    }

}

package com.jcca.admin.system.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.service.PerformanceTargetService;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.admin.system.vo.CommanResultVo;
import com.jcca.admin.system.vo.MinuteVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web2.entity.AssetManufacturer;
import com.jcca.web2.service.AssetManufacturerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @ Author：sophia
 * @ Date：Created in 9:24 2021/8/20
 * @ Description:
 */
@Slf4j
@Controller
@RequestMapping("/system/collector")
public class CollectorController {

    @Resource
    private SpecDictionaryService specDictionaryService;
    @Resource
    private PerformanceTargetService performanceTargetService;
    @Resource
    private OutService outService;
    @Resource
    private AssetManufacturerService manufacturerService;

    @PostMapping("/manufacturerList")
    @ResponseBody
    public ResultVo manufacturerList(){
        List<AssetManufacturer> list = manufacturerService.list();
        return ResultVoUtil.success(list);
    }


    /**
     * 采集配置列表
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/index")
    @RequiresPermissions("system:collector:index")
    @ActionLog(name = "查看采集配置", title = "采集配置", key = LogTypeConstant.QUERY)
    public String index(Model model, SpecDictionary specDictionary, Integer size, Integer page) {
        /*获取模板列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<SpecDictionary> wrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(specDictionary.getAssetMode()) && StringUtils.isNotEmpty(specDictionary.getAssetMode().toString().trim())) {
            wrapper.eq("ASSET_MODE", specDictionary.getAssetMode());
        }
        if (StringUtils.isNotEmpty(specDictionary.getAssetImage())) {
            wrapper.eq("ASSET_IMAGE", specDictionary.getAssetImage());
        }
        if (StringUtils.isNotEmpty(specDictionary.getManufacturerId())) {
            //改成从表里查
            wrapper.eq("MANUFACTURER_ID", specDictionary.getManufacturerId());
        }
        if (ObjectUtil.isNotNull(specDictionary.getSpecId()) && StringUtils.isNotEmpty(specDictionary.getSpecId().toString().trim())) {
            wrapper.eq("SPEC_ID", specDictionary.getSpecId());
        }
        wrapper.orderByDesc("CREATE_DATE");
        iPage = specDictionaryService.page(iPage, wrapper);
        List<SpecDictionary> records = iPage.getRecords();

        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/system/collector/index";
    }

    /**
     * 跳转到模板添加页面
     */
    @GetMapping("/add")
    public String toAdd() {
        return "/system/collector/add";
    }

    /**
     * 保存添加/修改的数据
     *
     * @param specDictionary 实体对象
     */
    @PostMapping("/save")
    @ResponseBody
    @ActionLog(name = "保存采集配置", title = "采集配置", key = LogTypeConstant.ADD)
    public ResultVo save(SpecDictionary specDictionary) {

        // 判断适配类型是否为空
        if (ObjectUtil.isNull(specDictionary.getAssetMode()) || specDictionary.getAssetMode().toString().length() < 1) {
            throw new ResultException(ResultEnum.COLLECTOR_ASSET_MDOE);
        }

        // 判断适配型号是否为空
        if (ObjectUtil.isNull(specDictionary.getAssetImage()) || specDictionary.getAssetImage().toString().length() < 1) {
            throw new ResultException(ResultEnum.COLLECTOR_ASSET_IMAGE);
        }
        // 判断适配厂家是否为空
        if (ObjectUtil.isNull(specDictionary.getManufacturerId()) || specDictionary.getManufacturerId().toString().length() < 1) {
            throw new ResultException(ResultEnum.COLLECTOR_ASSET_FACTORY);
        }
        // 判断操作系统是否为空
        if (ObjectUtil.isNull(specDictionary.getSystemType()) || specDictionary.getSystemType().toString().length() < 1) {
            throw new ResultException(ResultEnum.COLLECTOR_SYSTEM_TYPE);
        }
        // 判断执行代码是否为空
        if (ObjectUtil.isNull(specDictionary.getSpecId()) || specDictionary.getSpecId().toString().length() < 1) {
            throw new ResultException(ResultEnum.COLLECTOR_SPEC_DICT);
        }
        if (ObjectUtil.isNull(specDictionary.getId())) {
            specDictionary.setCreateDate(new Date());
        }
        specDictionaryService.saveOrUpdate(specDictionary);
        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 跳转到模板编辑页面
     */
    @GetMapping("/edit/{id}")
    public String toEdit(@PathVariable("id") String id, Model model) {
        SpecDictionary specDictionary = specDictionaryService.getById(id);

        String manufacturerId = specDictionary.getManufacturerId();
        AssetManufacturer manufacturer = manufacturerService.getById(manufacturerId);
        if(Objects.nonNull(manufacturer)){
            model.addAttribute("manufacturer", manufacturer);
        }
        model.addAttribute("template", specDictionary);
        return "/system/collector/add";
    }


    @ResponseBody
    @RequestMapping("/deleteById")
    @ActionLog(name = "删除采集配置", title = "采集配置", key = LogTypeConstant.REMOVEE)
    public ResultVo deleteById(String id) {
        boolean b = specDictionaryService.removeById(id);
        if (b) {
            return ResultVoUtil.success("模板删除成功");
        }
        return ResultVoUtil.error("模板删除失败");
    }


    /**
     * 采集配置列表
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/specIndex")
    @RequiresPermissions("system:collector:specIndex")
    public String specIndex(Model model, PerformanceTarget performanceTarget, Integer size, Integer page) {
        /*获取模板列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<PerformanceTarget> wrapper = new QueryWrapper<>();

        if (ObjectUtil.isNotNull(performanceTarget.getTargetDescription()) && StringUtils.isNotEmpty(performanceTarget.getTargetDescription())) {
            wrapper.like("TARGET_DESCRIPTION", performanceTarget.getTargetDescription());
        }

        if (ObjectUtil.isNotNull(performanceTarget.getComman()) && StringUtils.isNotEmpty(performanceTarget.getComman())) {
            wrapper.like("COMMAND", performanceTarget.getComman());
        }
        if (ObjectUtil.isNotNull(performanceTarget.getTargetHandle()) && StringUtils.isNotEmpty(performanceTarget.getTargetHandle())) {
            wrapper.like("TARGET_HANDLE", performanceTarget.getTargetHandle());
        }
        if (ObjectUtil.isNotNull(performanceTarget.getPingTunnel()) && StringUtils.isNotEmpty(performanceTarget.getPingTunnel().toString().trim())) {
            if (performanceTarget.getPingTunnel() == 1) {
                wrapper.like("PING_TUNNEL", performanceTarget.getPingTunnel());
            } else {
                wrapper.ne("PING_TUNNEL", 1).or().isNull("PING_TUNNEL");
            }

        }
        wrapper.orderByDesc("CREATE_TIME");
        iPage = performanceTargetService.page(iPage, wrapper);
        List<PerformanceTarget> records = iPage.getRecords();
        ArrayList<PerformanceTarget> performanceTargets = new ArrayList<PerformanceTarget>();
        for (PerformanceTarget record : records) {
            Integer pingTunnel = record.getPingTunnel();
            if (ObjectUtil.isNull(pingTunnel) || pingTunnel != 1) {
                record.setPingTunnel(0);
            }
            performanceTargets.add(record);
        }
        // 封装数据
        model.addAttribute("list", performanceTargets);
        model.addAttribute("page", iPage);
        return "/system/collector/specIndex";
    }

    @ResponseBody
    @RequestMapping("/setPing")
    @ActionLog(name = "PING采集配置", title = "采集配置", key = LogTypeConstant.MODIFY)
    public ResultVo setPing(String id) {
        try {
            performanceTargetService.setPIng(id);
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_COLLECTOR)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_COLLECTOR, ErrorCodeEnum.SYSTEM_COLLECTOR_PING, "", "设置ping隧道失败"));
            }
        }

        return ResultVoUtil.success();
    }


    /**
     * 跳转更改时间页面
     */
    @GetMapping("/setTime/{id}")
    public String setTime(@PathVariable("id") String id, Model model) {
        PerformanceTarget performanceTarget = performanceTargetService.getById(id);
        String cronExpress = performanceTarget.getCronExpress();
        MinuteVo minuteVo = new MinuteVo();
        minuteVo.setId(id);
        minuteVo.setPrefix(cronExpress.substring(0, 4));
        minuteVo.setSuffix(cronExpress.substring(5, cronExpress.length()));
        minuteVo.setMinute(cronExpress.substring(4, 5));
        model.addAttribute("minute", minuteVo);
        return "/system/collector/setTime";
    }


    @ResponseBody
    @PostMapping("/saveTime")
    @ActionLog(name = "修改采集时间配置", title = "采集配置", key = LogTypeConstant.MODIFY)
    public ResultVo setTime(MinuteVo minte) {
        try {
            if (ObjectUtil.isNull(minte.getMinute())) {
                throw new ResultException(ResultEnum.COLLECTOR_NULL);
            }

            int min = Integer.parseInt(minte.getMinute().trim());
            if (min < 1 || min > 59) {
                throw new ResultException(ResultEnum.COLLECTOR_NUM);
            }
            PerformanceTarget performanceTarget = performanceTargetService.getById(minte.getId());
            performanceTarget.setCronExpress("0 0/" + min + " * * * ? *");
            performanceTargetService.saveOrUpdate(performanceTarget);
        } catch (NumberFormatException e) {
            throw new ResultException(ResultEnum.COLLECTOR_NUM);
        }
        return ResultVoUtil.success();
    }

    /**
     * 跳转测试链接页面
     */
    @GetMapping("/testSnmp/{id}")
    public String testSnmp(@PathVariable("id") String id, Model model) {
        MinuteVo minuteVo = new MinuteVo();
        minuteVo.setId(id);
        model.addAttribute("perform", minuteVo);
        return "/system/collector/testSnmp";
    }

    /**
     * 跳转测试链接页面
     */
    @RequestMapping("/testSnmp")
    public String testSnmp(MinuteVo perform, Model model) {
        PerformanceTarget performanceTarget = performanceTargetService.getById(perform.getId());
        String[] commans = performanceTarget.getComman().split("\\|");

        ArrayList<CommanResultVo> commanResultVos = new ArrayList<>();


        BusinessGetSnmpResultReq businessGetSnmpResultReq = new BusinessGetSnmpResultReq();
        businessGetSnmpResultReq.setIp(perform.getIp());
        businessGetSnmpResultReq.setCommunity(perform.getCommunity());
        //GET\WALK
        businessGetSnmpResultReq.setType(perform.getType());
        try {
            for (String comman : commans) {
                businessGetSnmpResultReq.setMib(comman);
                CommanResultVo commanResultVo = new CommanResultVo();
                commanResultVo.setComman(comman);
                BusinessGetSnmpResultResp snmpResult = outService.getSnmpResult(businessGetSnmpResultReq);
                commanResultVo.setComman(comman);
                commanResultVo.setResult(snmpResult.getResultList());
                commanResultVo.setMsg(snmpResult.getMsg());
                commanResultVos.add(commanResultVo);
            }

        } catch (Exception e) {
            throw new RuntimeException("跳转测试链接页面异常-CollectorController.testSnmp");
        }
        model.addAttribute("list", commanResultVos);
        return "/system/collector/snmpResult";
    }

}

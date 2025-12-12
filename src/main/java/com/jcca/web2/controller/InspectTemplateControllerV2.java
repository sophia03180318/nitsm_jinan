package com.jcca.web2.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.dto.xunjian.InspectTemplateDto;
import com.jcca.web2.entity.InspectTemplate;
import com.jcca.web2.service.InspectTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lifp
 * @version 1.0
 * @description: 巡检模板
 * @date 2025-10-09 星期四 10:34:22
 */
@RestController
@RequestMapping("/api/v2/template")
@Api(tags = "巡检管理V2")
public class InspectTemplateControllerV2 {

    private final static Logger logger = LoggerFactory.getLogger(InspectTemplateControllerV2.class);

    @Resource
    private InspectTemplateService inspectTemplateService;

    @PostMapping("/add")
    @ApiOperation("新增智能巡检模板")
    public ResultVo<Object> addTemplate(@RequestBody InspectTemplateDto addTemplate) {
        try {
            ResultVo<Object> resultVo = verificationParam(addTemplate);
            if (resultVo.getCode() != 200) {
                return resultVo;
            }

            SysUser subject = ShiroUtil.getSubject();
            String templateCode = MyIdUtil.getId();

            Integer nameCount = inspectTemplateService.lambdaQuery().eq(InspectTemplate::getUserId, subject.getId()).eq(InspectTemplate::getTemplateName, addTemplate.getTemplateName()).count();
            if (nameCount > 0) {
                return ResultVoUtil.error("名字不能重复");
            }

            List<String> templateCodes = inspectTemplateService.lambdaQuery()
                    .eq(InspectTemplate::getUserId, subject.getId())
                    .list()
                    .stream()
                    .map(InspectTemplate::getTemplateCode)
                    .distinct()
                    .collect(Collectors.toList());

            int templateCount = templateCodes.size();

            if (templateCount >= 8) {
                return ResultVoUtil.error("个人模板数不能超过8个");
            }

            for (String assetId : addTemplate.getAssetData()) {
                InspectTemplate template = new InspectTemplate();
                template.setTemplateName(addTemplate.getTemplateName());
                template.setAssetId(assetId);
                template.setTemplateCode(templateCode);
                template.setUserId(subject.getId());
                template.setCreator(subject.getUsername());
                template.setCreateTime(new Date());
                inspectTemplateService.save(template);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVoUtil.error("添加模板失败");
        }
        return ResultVoUtil.success();
    }

    @PostMapping("/update")
    @ApiOperation("修改智能巡检模板")
    public ResultVo<Object> updateTemplate(@RequestBody InspectTemplateDto updateTemplate) {
        try {
            ResultVo<Object> resultVo = verificationParam(updateTemplate);
            if (resultVo.getCode() != 200) {
                return resultVo;
            }

            if (StringUtils.isEmpty(updateTemplate.getTemplateCode())) {
                return ResultVoUtil.error("修改模板失败，缺少关键参数[templateCode]");
            }

            SysUser subject = ShiroUtil.getSubject();

            // 名称重复校验：同用户下，排除自己
            long nameCount = inspectTemplateService.lambdaQuery()
                    .eq(InspectTemplate::getUserId, subject.getId())
                    .ne(InspectTemplate::getTemplateCode, updateTemplate.getTemplateCode())
                    .eq(InspectTemplate::getTemplateName, updateTemplate.getTemplateName())
                    .count();
            if (nameCount > 0) {
                return ResultVoUtil.error("模板名称不能重复");
            }

            inspectTemplateService.removeInspectTemplate(updateTemplate);
            return ResultVoUtil.success();
        } catch (Exception e) {
            logger.error("更新模板失败, templateCode: {}", updateTemplate.getTemplateCode(), e);
            return ResultVoUtil.error("模板修改失败");
        }
    }

    @DeleteMapping("/remove/{templateCode}")
    @ApiOperation("删除智能巡检模板")
    public ResultVo<Object> delTemplate(@PathVariable("templateCode") String templateCode) {
        try {
            if (null == templateCode || templateCode.isEmpty()) {
                return ResultVoUtil.error("参数不允许为空");
            }
            inspectTemplateService.remove(new LambdaQueryWrapper<InspectTemplate>().eq(InspectTemplate::getTemplateCode, templateCode));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVoUtil.error("模板删除失败");
        }
        return ResultVoUtil.success();
    }

    @GetMapping("/query")
    @ApiOperation("获取智能巡检模板")
    public ResultVo<Object> queryTemplate() {
        try {
            SysUser subject = ShiroUtil.getSubject();
            return ResultVoUtil.success(inspectTemplateService.queryInspectTemplate(subject.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVoUtil.error("获取模板失败");
        }
    }

    /**
     * 模板参数校验
     */
    public ResultVo<Object> verificationParam(InspectTemplateDto templateDto) {
        if (null == templateDto.getTemplateName() || templateDto.getTemplateName().isEmpty()) {
            return ResultVoUtil.error("模板名称不允许为空");
        }

        if (null == templateDto.getAssetData() || templateDto.getAssetData().isEmpty()) {
            return ResultVoUtil.error("设备不允许为空");
        }

        if (templateDto.getTemplateName().length() > 32) {
            return ResultVoUtil.error("模板名称长度超过长度限制");
        }
        return ResultVoUtil.success();
    }
}

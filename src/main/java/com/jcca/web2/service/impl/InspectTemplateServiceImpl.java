package com.jcca.web2.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web2.dao.InspectTemplateMapper;
import com.jcca.web2.dto.xunjian.InspectTemplateDto;
import com.jcca.web2.entity.InspectTemplate;
import com.jcca.web2.service.InspectTemplateService;
import com.jcca.web2.vo.InspectTemplateVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检模板
 * @date 2025-10-09 星期四 10:45:39
 */
@Service
public class InspectTemplateServiceImpl extends ServiceImpl<InspectTemplateMapper, InspectTemplate> implements InspectTemplateService {

    @Resource
    private InspectTemplateMapper inspectTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeInspectTemplate(InspectTemplateDto updateTemplate) {
        LambdaQueryWrapper<InspectTemplate> query = new LambdaQueryWrapper<>();
        query.eq(InspectTemplate::getTemplateCode, updateTemplate.getTemplateCode());
        this.remove(query);

        List<InspectTemplate> list = new ArrayList<>();
        SysUser subject = ShiroUtil.getSubject();
        for (String assetId : updateTemplate.getAssetData()) {
            InspectTemplate template = new InspectTemplate();
            template.setTemplateName(updateTemplate.getTemplateName());
            template.setAssetId(assetId);
            template.setUserId(subject.getId());
            template.setCreator(subject.getUsername());
            template.setCreateTime(new Date());
            template.setTemplateCode(updateTemplate.getTemplateCode());
            list.add(template);
        }

        this.saveBatch(list);
    }

    @Override
    public List<InspectTemplateVo> queryInspectTemplate(String userId) {
        return inspectTemplateMapper.queryInspectTemplateData(userId);
    }
}

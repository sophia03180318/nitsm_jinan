package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.xunjian.InspectTemplateDto;
import com.jcca.web2.entity.InspectTemplate;
import com.jcca.web2.vo.InspectTemplateVo;

import java.util.List;

public interface InspectTemplateService extends IService<InspectTemplate> {

    /**
     * 删除模板
     *
     * @param inspectTemplate
     */
    void removeInspectTemplate(InspectTemplateDto inspectTemplate);

    /**
     * 查询模板
     *
     * @param userId
     * @return
     */
    List<InspectTemplateVo> queryInspectTemplate(String userId);
}

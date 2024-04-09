package com.jcca.admin.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.biz.entity.AlarmTemplate;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-23 15:52:15
 **/
public interface AlarmTemplateService extends IService<AlarmTemplate> {


    /**
     * 更新状态
     *
     * @param param
     * @param ids
     * @return
     */
    boolean updateStatus(String param, List<String> ids);

    /**
     * 查询是否存在相同类别
     *
     * @param category
     * @return
     */
    AlarmTemplate findByCategory(String category);
}

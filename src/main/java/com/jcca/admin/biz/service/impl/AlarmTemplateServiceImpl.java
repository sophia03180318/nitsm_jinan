package com.jcca.admin.biz.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.dao.AlarmTemplateMapper;
import com.jcca.admin.biz.entity.AlarmTemplate;
import com.jcca.admin.biz.service.AlarmTemplateService;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.StatusUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author hanwone
 * @date 2020-04-23 15:52
 **/
@Service
public class AlarmTemplateServiceImpl extends ServiceImpl<AlarmTemplateMapper, AlarmTemplate> implements AlarmTemplateService {

    @Resource
    private AlarmTemplateMapper templateMapper;

    @Resource
    private RedisService redisService;

    /**
     * 更新状态
     *
     * @param param
     * @param ids
     * @return
     */
    @Override
    public boolean updateStatus(String param, List<String> ids) {
        StatusEnum statusEnum = StatusUtil.getStatusEnum(param);
        if (ids != null) {
            ids.forEach(id -> {
                AlarmTemplate template = new AlarmTemplate();
                template.setId(id);
                template.setStatus(statusEnum.getCode());
                templateMapper.updateById(template);
            });
            return ids.size() > 0;
        }

        return false;
    }

    /**
     * 查询是否存在相同类别
     *
     * @param category
     * @return
     */
    @Override
    public AlarmTemplate findByCategory(String category) {
        Object o = redisService.get(RedisCacheConst.ALARM_CATEGORY_PRE + category);
        if (Objects.nonNull(o)) {
            return JSONUtil.toBean(o.toString(), AlarmTemplate.class);
        }
        AlarmTemplate alarmTemplate = templateMapper.selectByCategory(category);
        redisService.set(RedisCacheConst.ALARM_CATEGORY_PRE + category, JSONUtil.toJsonStr(alarmTemplate));
        return alarmTemplate;
    }
}
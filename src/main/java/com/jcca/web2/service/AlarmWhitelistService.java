package com.jcca.web2.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.WhitePageQueryDto;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.vo.WhitePageQueryVo;

/**
 * @description: 告警白名单业务
 * @author: Lvyp
 * @create: 2023/11/30 11:00
 */
public interface AlarmWhitelistService extends IService<AlarmWhitelist> {


    /**
     * 添加白名单
     * 如果有相关告警将告警更新为恢复状态
     *
     * @param copy
     */
    void addWhite(AlarmWhitelist copy);

    /**
     * 删除白名单，并清楚当前缓存状态
     *
     * @param id
     */
    void removeByIdV2(String id);

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    IPage<WhitePageQueryVo> pageV2(WhitePageQueryDto query);
}

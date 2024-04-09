package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.web2.dto.WhitePageQueryDto;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.vo.WhitePageQueryVo;
import org.apache.ibatis.annotations.Param;

/**
 * @description: 告警白名单
 * @author: Lvyp
 * @create: 2023/11/30 10:59
 */
public interface AlarmWhitelistMapper extends BaseMapper<AlarmWhitelist> {

    /**
     * 分页查询
     *
     * @param page
     * @param query
     * @return
     */
    IPage<WhitePageQueryVo> pageV2(Page page, @Param("query") WhitePageQueryDto query);
}

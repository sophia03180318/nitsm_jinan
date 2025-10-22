package com.jcca.web2.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.AlarmWhitelistAddDto;
import com.jcca.web2.dto.WhitePageQueryDto;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.vo.CommonAssetInfo;
import com.jcca.web2.vo.WhitePageQueryVo;

import java.util.List;

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
    void addWhite(AlarmWhitelistAddDto copy);

    /**
     * 删除白名单，并清楚当前缓存状态
     *
     * @param id
     * @param typeMark
     */
    void removeByIdV2(String id, int typeMark);

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    IPage<WhitePageQueryVo> pageV2(WhitePageQueryDto query);

    /**
     * 查看黑名单详情
     *
     * @param whiteId
     */
    List<CommonAssetInfo> queryDetailById(String whiteId);

    /**
     * 查看黑名单详情
     *
     * @param flag
     * @param alarmCode
     */
    int queryWhiteCount(String flag, String alarmCode, String assetId);
}

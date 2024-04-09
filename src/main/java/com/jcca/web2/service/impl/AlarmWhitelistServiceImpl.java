package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;

import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web2.dao.AlarmWhitelistMapper;
import com.jcca.web2.dto.WhitePageQueryDto;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.service.AlarmWhitelistService;
import com.jcca.web2.vo.WhitePageQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.util.Date;


/**
 * @description: 告警白名单实现
 * @author: Lvyp
 * @create: 2023/11/30 11:02
 */
@Service
public class AlarmWhitelistServiceImpl extends ServiceImpl<AlarmWhitelistMapper, AlarmWhitelist> implements AlarmWhitelistService {

    @Resource
    private AlarmInfoService alarmInfoServ;
    @Resource
    private AlarmWhitelistMapper whiteMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addWhite(AlarmWhitelist copy) {
        //白名单覆盖范围内的告警需要转为恢复状态
        copy.setId(MyIdUtil.getId());
        String alarmCode = copy.getAlarmCode();
        String assetId = copy.getAssetId();
        String flag = copy.getFlag();
        copy.setCreator(ShiroUtil.getSubject().getUsername());
        copy.setCreateTime(new Date());

        alarmInfoServ.recoverAlarmV2(alarmCode, assetId, flag, "【新增白名单规则，自动恢复】");
        save(copy);
    }

    @Override
    public void removeByIdV2(String id) {
        removeById(id);
    }

    @Override
    public IPage<WhitePageQueryVo> pageV2(WhitePageQueryDto query) {
        Page page = new Page();
        page.setCurrent(query.getPage());
        page.setSize(query.getSize());
        return whiteMapper.pageV2(page, query);
    }


}

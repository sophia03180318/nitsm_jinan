package com.jcca.web2.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;

import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.AlarmWhiteAssetMapper;
import com.jcca.web2.dao.AlarmWhitelistMapper;
import com.jcca.web2.dto.AlarmWhitelistAddDto;
import com.jcca.web2.dto.WhitePageQueryDto;
import com.jcca.web2.entity.AlarmWhiteAsset;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.service.AlarmWhitelistService;
import com.jcca.web2.vo.CommonAssetInfo;
import com.jcca.web2.vo.WhitePageQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;


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

    @Resource
    private AlarmWhiteAssetMapper alarmWhiteAssetMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addWhite(AlarmWhitelistAddDto req) {
        AlarmWhitelist copy = EntityBeanUtil.copy(req, AlarmWhitelist.class);
        //白名单覆盖范围内的告警需要转为恢复状态
        copy.setId(MyIdUtil.getId());
        String alarmCode = copy.getAlarmCode();
        String flag = copy.getFlag();
        copy.setCreator(ShiroUtil.getSubject().getUsername());
        copy.setCreateTime(new Date());

        if (ObjectUtil.isNotEmpty(flag)) {
            String[] split = flag.split("_");

            if (split.length == 2) {
                copy.setFlag("");
            } else {
                copy.setFlag(split[2]);
            }
        }

        if (req.getTypeMark() == Web2Const.WHITE_NORMAL) {
            String assetId = req.getAssetId().get(0);
            flag = copy.getFlag();
            alarmInfoServ.recoverWhiteAlarmV2(alarmCode, assetId, flag, "【新增屏蔽清单规则，自动恢复】");
            copy.setAssetId(assetId);
            save(copy);
            return;
        }

        if (req.getTypeMark() == Web2Const.WHITE_BATCH) {
            List<String> assetIds = req.getAssetId();
            copy.setAssetId("--");
            save(copy);
            for (String assetId : assetIds) {
                flag = copy.getFlag();
                alarmInfoServ.recoverWhiteAlarmV2(alarmCode, assetId, flag, "【新增屏蔽清单规则，自动恢复】");

                AlarmWhiteAsset alarmWhiteAsset = new AlarmWhiteAsset();
                alarmWhiteAsset.setId(MyIdUtil.getId());
                alarmWhiteAsset.setWhiteId(copy.getId());
                alarmWhiteAsset.setAssetId(assetId);
                alarmWhiteAsset.setUserId(ShiroUtil.getSubject().getId());
                alarmWhiteAsset.setCreator(ShiroUtil.getSubject().getUsername());
                alarmWhiteAsset.setCreateTime(new Date());
                alarmWhiteAssetMapper.insert(alarmWhiteAsset);
            }
        }
    }

    @Override
    public void removeByIdV2(String id, int typeMark) {
        removeById(id);
        // 删除黑名单关联资产
        if (typeMark == Web2Const.WHITE_BATCH) {
            alarmWhiteAssetMapper.delete(new LambdaQueryWrapper<AlarmWhiteAsset>().eq(AlarmWhiteAsset::getWhiteId, id));
        }
    }

    @Override
    public IPage<WhitePageQueryVo> pageV2(WhitePageQueryDto query) {
        Page page = new Page();
        page.setCurrent(query.getPage());
        page.setSize(query.getSize());
        return whiteMapper.pageV2(page, query);
    }

    @Override
    public List<CommonAssetInfo> queryDetailById(String whiteId) {
        AlarmWhitelist alarmWhite = this.lambdaQuery().eq(AlarmWhitelist::getId, whiteId).one();
        if (alarmWhite == null) {
            throw new RuntimeException("无此黑名单数据");
        }
        if (alarmWhite.getTypeMark() == Web2Const.WHITE_NORMAL) {
            return whiteMapper.queryAssetList(whiteId);
        }

        if (alarmWhite.getTypeMark() == Web2Const.WHITE_BATCH) {
            return whiteMapper.queryAssetListBatch(whiteId);
        }
        return Collections.emptyList();
    }

    @Override
    public int queryWhiteCount(String flag, String alarmCode, String assetId) {
        return whiteMapper.queryWhiteCount(flag, alarmCode, assetId);
    }

}

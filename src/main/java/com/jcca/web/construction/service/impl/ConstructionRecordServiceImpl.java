package com.jcca.web.construction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web.construction.dao.ConstructionRecordMapper;
import com.jcca.web.construction.entity.ConstructionRecord;
import com.jcca.web.construction.service.ConstructionRecordService;
import com.jcca.web.cycles.service.CyclesInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 施工记录
 *
 * @author lyp
 */
@Service
public class ConstructionRecordServiceImpl extends ServiceImpl<ConstructionRecordMapper, ConstructionRecord>
        implements ConstructionRecordService {

    @Resource
    private ConstructionRecordMapper constructMapper;
    @Resource
    private BizManageService bizService;
    @Resource
    private AlarmInfoService infoService;
    @Resource
    private CyclesInfoService cyclesInfoService;
    @Resource
    private AssetService assetService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(ConstructionRecord req) {
        String id = MyIdUtil.getId();
        req.setId(id);
        constructMapper.insert(req);
        bizService.save(id, BizManageConstant.CONSTRUCTION);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeConstruction(String id) {
        constructMapper.deleteById(id);
        bizService.remove(id, BizManageConstant.CONSTRUCTION);
    }

    @Override
    public Boolean isBlank(String assetId, Date occurTime) {
        QueryWrapper<ConstructionRecord> query = new QueryWrapper<ConstructionRecord>();
        query.like("INFLUENCE", assetId);
        query.ge("END_TIME", occurTime);
        query.le("START_TIME", occurTime);
        List<ConstructionRecord> constructionRecords = constructMapper.selectList(query);
        if (constructionRecords.isEmpty()) {
            Asset asset = assetService.getById(assetId);
            return cyclesInfoService.verifyIsBlank(asset, occurTime);
        } else {
            return true;
        }
    }

    @Override
    public void createV2(ConstructionRecord req) {
        String id = MyIdUtil.getId();
        req.setId(id);
        constructMapper.insert(req);
        bizService.save(id, BizManageConstant.CONSTRUCTION);
        infoService.blankAlarm(req);
    }

    @Override
    public ConstructionRecord getConstructionRecord(String alarmId) {
        List<ConstructionRecord> list = constructMapper.getConstructionRecord(alarmId);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}

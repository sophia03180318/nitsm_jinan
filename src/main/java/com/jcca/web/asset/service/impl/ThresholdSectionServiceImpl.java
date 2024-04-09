package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.ThresholdSectionMapper;
import com.jcca.web.asset.entity.ThresholdSection;
import com.jcca.web.asset.service.ThresholdSectionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * 区间阈值
 *
 * @author lyp
 */
@Service
public class ThresholdSectionServiceImpl extends ServiceImpl<ThresholdSectionMapper, ThresholdSection> implements ThresholdSectionService {

    @Resource
    private ThresholdSectionMapper sectionMapper;

    @Override
    public ThresholdSection selectSectionConf(String assetId, String type, String flag) {

        return sectionMapper.selectSectionConf(assetId, type, flag);
    }

    @Override
    public List<ThresholdSection> selectSectionConf(String assetId) {

        return sectionMapper.selectSectionConfList(assetId);
    }


}

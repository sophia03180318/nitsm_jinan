package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.InspectAssetMapper;
import com.jcca.web2.dto.InspectTargetDetailInfo;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectAssetServiceImpl
 * @date 2025/5/19 17:32
 * @since 2.1.6.0
 */
@Service
public class InspectAssetServiceImpl extends ServiceImpl<InspectAssetMapper, InspectAsset> implements InspectAssetService {

    @Resource
    private InspectAssetMapper inspectAssetMapper;

    @Override
    public List<InspectAsset> getInspectAssets(List<String> assetIds) {
        return inspectAssetMapper.getInspectAssets(assetIds);
    }

    @Override
    public void removeByJobId(String jobId) {
        QueryWrapper<InspectAsset> query1 = Wrappers.query();
        query1.eq("JOB_ID", jobId);
        this.remove(query1);
    }

    @Override
    public InspectAssetAndTarget getCheckedAssetTarget(String jobId) {
        InspectAssetAndTarget result = new InspectAssetAndTarget();
        result.setAssetList(this.getAllCheckedAsset(jobId));
        result.setTargetList(this.getAllCheckedTarget(jobId));
        return result;
    }

    @Override
    public List<ItemVo> getAllCheckedAsset(String jobId) {
        return inspectAssetMapper.getAllCheckedAsset(jobId);
    }

    @Override
    public List<ItemVo> getAllCheckedTarget(String jobId) {
        List<ItemVo> resultList = new ArrayList<>();
        List<ItemVo> assetDesks = inspectAssetMapper.getDesksByJobId(jobId);
        List<InspectAsset> list = inspectAssetMapper.getAllCheckedTarget(jobId);
        Map<Integer, List<InspectAsset>> collect = list.stream().collect(Collectors.groupingBy(InspectAsset::getAssetDesk));
        for (ItemVo desk : assetDesks) {
            ItemVo vo = new ItemVo();
            vo.setId(desk.getId() + ",");
            vo.setName(desk.getName());
            List<InspectAsset> inspectAssets = collect.get(Integer.parseInt(desk.getId()));
            List<ItemVo> children = new ArrayList<>();
            for (InspectAsset asset : inspectAssets) {
                ItemVo vo1 = new ItemVo();
                vo1.setId(asset.getEventTypeId());
                vo1.setName(asset.getEventTypeName());
                children.add(vo1);
            }
            vo.setChildren(children);
            resultList.add(vo);
        }


        return resultList;
    }

    @Override
    public List<InspectAsset> getAllByJobId(String jobId) {
        return inspectAssetMapper.getAllByJobId(jobId);
    }

    @Override
    public List<ItemVo> getTargetStatus(String jobId) {
        return inspectAssetMapper.getTargetStatus(jobId);
    }

    @Override
    public List<InspectTargetDetailInfo> getTargetAssetInfo(String jobId, String targetItem) {
        return inspectAssetMapper.getTargetAssetInfo(jobId, targetItem);
    }

    @Override
    public List<InspectTargetDetailInfo> getAssetTargetInfo(String jobId, String assetId) {
        return inspectAssetMapper.getAssetTargetInfo(jobId, assetId);
    }

    /**
     * 巡检实时采集
     *
     * @param assetId        资产ID
     * @param thresholdValue 设置的阈值，为空表示是非阈值指标
     * @return
     */
    @Override
    public InspectDetail xunjianCollect(String assetId, String thresholdValue) {
        InspectDetail detail = new InspectDetail();
        detail.setAssetId(assetId);
        detail.setInspectState(Web2Const.INSPECTED);
        detail.setResultMsg("正常");
        if (StringUtils.isEmpty(thresholdValue)) {
            detail.setInspectValue(thresholdValue);
        }
        long l = Long.parseLong(assetId);
        if (l % 2 == 0) {
            detail.setInspectState(Web2Const.INSPECTED);
        } else {
            detail.setInspectState(Web2Const.INSPECT_ERROR);
        }
        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (InterruptedException e) {
            detail.setInspectState(Web2Const.INSPECT_ERROR);
            detail.setInspectValue("--");
            detail.setResultMsg("巡检异常");
        }
        return detail;
    }
}

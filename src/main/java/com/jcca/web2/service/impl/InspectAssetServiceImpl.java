package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.InspectAssetMapper;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
}

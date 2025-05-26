package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.InspectTargetDetailInfo;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectAssetService
 * @date 2025/5/19 17:31
 * @since 2.1.6.0
 */
public interface InspectAssetService extends IService<InspectAsset> {

    List<InspectAsset> getInspectAssets(List<String> assetIds);

    void removeByJobId(String jobId);

    InspectAssetAndTarget getCheckedAssetTarget(String jobId);

    List<ItemVo> getAllCheckedAsset(String jobId);

    List<ItemVo> getAllCheckedTarget(String jobId);

    List<InspectAsset> getAllByJobId(String jobId);

    List<ItemVo> getTargetStatus(String jobId);

    List<InspectTargetDetailInfo> getTargetAssetInfo(String jobId, String targetItem);

    List<InspectTargetDetailInfo> getAssetTargetInfo(String jobId, String assetId);

    InspectDetail xunjianCollect(String assetId, String thresholdValue);
}

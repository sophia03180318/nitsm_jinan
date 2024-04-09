package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.web.asset.entity.ThresholdAsset;
import com.jcca.web.asset.service.bean.AssetThresholdQueryV2;
import com.jcca.web.asset.service.bean.VerifyThresholdReq;
import com.jcca.web.asset.service.bean.VerifyThresholdResp;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.asset.vo.ThresholdAssetVo;

import java.util.Date;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-05-20 10:28:42
 **/
public interface ThresholdAssetService extends IService<ThresholdAsset> {


    /**
     * 查询设备的阈值配置信息
     *
     * @param query
     * @return
     */
    ThresholdBaseEntity queryAssetThresholdV2(AssetThresholdQueryV2 query);

    /**
     * 校验阶段阈值
     * 校验阶段阈值
     *
     * @param collectValue    采集到的值
     * @param eventUniqueCode EventUniqueCode
     * @return
     */
    VerifyThresholdSectionResp verifySectionThreshold(Double collectValue, String eventUniqueCode);

    /**
     * 处理结果
     *
     * @return
     */
    List<CreateEventReq> disposeVerifyThresholdSectionResp(VerifyThresholdSectionResp thresholdSectionResp, String eventGroupConstant, Date collectDate, String assetId, String collectValue, String flag);

    /**
     * 校验阈值
     * 阈值规则权重：区间阈值》设备阈值》设备类型统一阈值
     *
     * @param req
     * @return
     * @author Lvyp
     */
    VerifyThresholdResp verifyThreshold(VerifyThresholdReq req);

    /**
     * 根据资产ID获取阈值
     *
     * @param assetId
     * @return
     */
    ThresholdAssetVo findAssetThreshold(String assetId);

    /**
     * 查询
     *
     * @param id
     * @return
     */
    ThresholdAsset findByAssetId(String id);

    /**
     * 查询是否有手动阈值配置
     *
     * @param id
     * @return
     */
    ThresholdAsset querySpecialConf(String id);

    /**
     * 查询给定设备列表的默认阈值
     *
     * @param assetIds
     * @param assetMode
     * @return
     */
    ThresholdAssetVo queryDefaultConf(List<String> assetIds, Integer assetMode);

    /**
     * 删除手动阈值恢复默认阈值
     *
     * @param assetId
     * @param assetIds
     */
    void removeSpecial(String assetId, List<String> assetIds);

    /***
     * 判定是否恢复告警
     * 区间阈值和的默认阈值上同样的告警
     * 阶段阈值是单独的告警
     */
    void recoverAlarm(String assetId) throws Exception;
}

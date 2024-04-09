package com.jcca.component.quartz.asset;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppListUtils;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.bean.CollectorCenterAssetMsg;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetStatusEnum;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 更新中心资产状态
 *
 * @author lyp
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzUpdateCenterAssetStatusJob extends QuartzJobBean {

    public static boolean once = true;

    @Resource
    private CollectAgent collectAgency;
    @Resource
    private AssetService assetServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (once) {
            once = false;
            return;
        }
        List<CollectorCenterAssetMsg> centerAssetList = null;
        try {
            centerAssetList = collectAgency.getCenterAssetList();
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CRON_DATA, "获取中心资产列表异常", e);
            return;
        }

        if (CollectionUtils.isEmpty(centerAssetList)) {
            AppLogUtils.buildLogError(LogFunctionEnum.CRON_DATA, "获取中心资产列表失败", null);
            return;
        }

        List<Asset> assetList = new ArrayList<>();
        for (CollectorCenterAssetMsg nodeAsset : centerAssetList) {
            String id = nodeAsset.getId();
            Asset asset = assetServ.getById(id);
            if (Objects.isNull(asset)) {
                continue;
            }
            String pingStatus = nodeAsset.getPingStatus();

            if ("0".equals(pingStatus)) {
                asset.setStatus(AssetStatusEnum.ASSET_STATUS_OFFLINE.getCode());
            } else if ("1".equals(pingStatus)) {
                asset.setStatus(AssetStatusEnum.ASSET_STATUS_ONLINE.getCode());
            } else {
                // 查询当前资产是不是监控的，非监控的设置为不监控
                if (AssetWatchStatusEnum.WATCH_STATUS_NO.getCode() == asset.getWatch()) {
                    asset.setStatus(AssetStatusEnum.ASSET_STATUS_NO_WATCH.getCode());
                } else {
                    //asset.setStatus(AssetStatusEnum.UNKONW.getCode());
                }
            }

            assetList.add(asset);
        }

        if (assetList.isEmpty()) {
            return;
        }

        List<List<Asset>> inSplitAsset = AppListUtils.inSplitAsset(assetList, 900);
        for (List<Asset> list : inSplitAsset) {
            assetServ.updateBatchById(list);
        }

    }

}

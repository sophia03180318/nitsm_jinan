package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.vo.ThresholdAssetReq;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HanHW
 * @description 单个阈值设置 运维日志记录
 * @className DevLogThresholdSingleImpl
 * @date 2024/4/8 14:55
 * @since 2.1.0.0
 */
@Service
public class DevLogThresholdSingleImpl implements DevLogService {

    @Resource
    private AssetService assetService;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.THRESHOLD_SINGLE;
    }

    /**
     * 设置运维日志内容
     *
     * @param actionLog 日志
     * @param args      参数
     */
    @Override
    public void setDevLog(SysActionLog actionLog, Object[] args) {
        Object arg = args[0];
        if (!(arg instanceof ThresholdAssetReq)) {
            return;
        }

        ThresholdAssetReq req = (ThresholdAssetReq) arg;
        String assetId = req.getAssetId();
        Asset asset = assetService.getById(assetId);
        ThresholdAssetVo thresholdAsset = thresholdAssetService.findAssetThreshold(assetId);

        actionLog.setLogName("配置【" + asset.getName() + "】阈值");

        Integer assetMode = asset.getAssetMode();

        Double ocpu = thresholdAsset.getCpu();
        Double omemory = thresholdAsset.getMemory();
        Double odisk = thresholdAsset.getDisk();
        Integer otimeDeviation = thresholdAsset.getTimeDeviation();
        Integer orunningTimeDeviation = thresholdAsset.getRunningTimeDeviation();

        Double ncpu = req.getCpu();
        Double nmemory = req.getMemory();
        Double ndisk = req.getDisk();
        Integer ntimeDeviation = req.getTimeDeviation();
        Integer nrunningTimeDeviationLinux = req.getRunningTimeDeviationLinux();
        Integer nrunningTimeDeviationWindows = req.getRunningTimeDeviationWindows();
        Integer nrunningTimeDeviationAix = req.getRunningTimeDeviationAix();

        List<String> list = new ArrayList<>();
        if (assetMode == 183) {
            if (!Objects.equals(ocpu, ncpu)) {
                String description = "CPU使用率由【" + ocpu + "】变更为【" + ncpu + "】";
                list.add(description);
            }
            if (!Objects.equals(omemory, nmemory)) {
                String description = "内存使用率由【" + omemory + "】变更为【" + nmemory + "】";
                list.add(description);
            }
            if (!Objects.equals(odisk, ndisk)) {
                String description = "磁盘使用率由【" + odisk + "】变更为【" + ndisk + "】";
                list.add(description);
            }
            if (!Objects.equals(otimeDeviation, ntimeDeviation)) {
                String description = "服务器偏差时间由【" + otimeDeviation + "】变更为【" + ntimeDeviation + "】";
                list.add(description);
            }

            if (!Objects.equals(orunningTimeDeviation, nrunningTimeDeviationLinux)
                    && Objects.nonNull(nrunningTimeDeviationLinux)
                    && (asset.getCollectionType() == 0 || asset.getCollectionType() == 3)) {
                String description = "LINUX运行时长由【" + orunningTimeDeviation + "】变更为【" + nrunningTimeDeviationLinux + "】";
                list.add(description);
            }
            if (!Objects.equals(orunningTimeDeviation, nrunningTimeDeviationWindows)
                    && Objects.nonNull(nrunningTimeDeviationWindows)
                    && asset.getCollectionType() == 1) {
                String description = "WINDOWS运行时长由【" + orunningTimeDeviation + "】变更为【" + nrunningTimeDeviationWindows + "】";
                list.add(description);
            }
            if (!Objects.equals(orunningTimeDeviation, nrunningTimeDeviationAix)
                    && Objects.nonNull(nrunningTimeDeviationAix)
                    && (asset.getCollectionType() == 2 || asset.getCollectionType() == 4)) {
                String description = "AIX运行时长由【" + orunningTimeDeviation + "】变更为【" + nrunningTimeDeviationAix + "】";
                list.add(description);
            }

            for (String description : list) {
                description = description.replace("null", "无");
                SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                        assetId, assetId, LogDetailItemIdType.ASSET, description);
                sysActionLogDetailService.save(detail);
            }
            return;
        }

        Double oportRateIn = thresholdAsset.getPortRateIn();
        Double oportRateOut = thresholdAsset.getPortRateOut();
        Double opacketLossIn = thresholdAsset.getPacketLossIn();
        Double opacketLossOut = thresholdAsset.getPacketLossOut();
        Double ocodeErrorIn = thresholdAsset.getCodeErrorIn();
        Double ocodeErrorOut = thresholdAsset.getCodeErrorOut();

        Double nportRateIn = req.getPortRateIn();
        Double nportRateOut = req.getPortRateOut();
        Double npacketLossIn = req.getPacketLossIn();
        Double npacketLossOut = req.getPacketLossOut();
        Double ncodeErrorIn = req.getCodeErrorIn();
        Double ncodeErrorOut = req.getCodeErrorOut();

        Integer nrunningTimeDeviation = req.getRunningTimeDeviation();

        if (assetMode == 201 || assetMode == 42) {
            if (!Objects.equals(ocpu, ncpu)) {
                String description = "CPU使用率由【" + ocpu + "】变更为【" + ncpu + "】";
                list.add(description);
            }
            if (!Objects.equals(omemory, nmemory)) {
                String description = "内存使用率由【" + omemory + "】变更为【" + nmemory + "】";
                list.add(description);
            }
            if (!Objects.equals(oportRateIn, nportRateIn)) {
                String description = "端口流入率由【" + oportRateIn + "】变更为【" + nportRateIn + "】";
                list.add(description);
            }
            if (!Objects.equals(oportRateOut, nportRateOut)) {
                String description = "端口流出率由【" + oportRateOut + "】变更为【" + nportRateOut + "】";
                list.add(description);
            }
            if (!Objects.equals(opacketLossIn, npacketLossIn)) {
                String description = "接收丢包率由【" + opacketLossIn + "】变更为【" + npacketLossIn + "】";
                list.add(description);
            }
            if (!Objects.equals(opacketLossOut, npacketLossOut)) {
                String description = "发送丢包率由【" + opacketLossOut + "】变更为【" + npacketLossOut + "】";
                list.add(description);
            }
            if (!Objects.equals(ocodeErrorIn, ncodeErrorIn)) {
                String description = "接收误码率由【" + ocodeErrorIn + "】变更为【" + ncodeErrorIn + "】";
                list.add(description);
            }
            if (!Objects.equals(ocodeErrorOut, ncodeErrorOut)) {
                String description = "发送误码率由【" + ocodeErrorOut + "】变更为【" + ncodeErrorOut + "】";
                list.add(description);
            }
            if (!Objects.equals(orunningTimeDeviation, nrunningTimeDeviation)) {
                String description = "运行时长由【" + orunningTimeDeviation + "】变更为【" + nrunningTimeDeviation + "】";
                list.add(description);
            }
            for (String description : list) {
                description = description.replace("null", "无");
                SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                        "", "201/42", LogDetailItemIdType.DEFAULT_THRESHOLD, description);
                sysActionLogDetailService.save(detail);
            }
        }

    }
}

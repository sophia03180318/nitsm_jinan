package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web2.dto.ThresholdManageQuery;
import com.jcca.web2.entity.AssetMode;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.enums.ThresholdCategoryEnum;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.ThresholdManageService;
import com.jcca.web2.vo.ThresholdManageVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author HanHW
 * @description 默认阈值设置 运维日志记录
 * @className DevLogThresholdDefaultImpl
 * @date 2024/4/8 11:51
 * @since 2.1.0.0
 */
@Service
public class DevLogThresholdDefaultImpl implements DevLogService {

    @Resource
    private SysOrgService sysOrgService;
    @Resource
    private AssetService assetService;
    @Resource
    private ThresholdManageService thresholdManageService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;
    @Resource
    private AssetModeService assetModeService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.THRESHOLD_DEFAULT;
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
        if (!(arg instanceof List)) {
            return;
        }

        List<ThresholdManage> reqList = new ArrayList<>();
        List list = (List) arg;
        for (Object o : list) {
            if (o instanceof ThresholdManage) {
                reqList.add((ThresholdManage) o);
            }
        }

        Collection<SysOrg> sysOrgs = sysOrgService.listByIds(reqList.get(0).getOrgIds());
        Object[] objects = sysOrgs.stream().map(SysOrg::getTitle).distinct().toArray();
        StringBuilder sb = new StringBuilder("组织【");
        for (int i = 0; i < objects.length; i++) {
            if (i != objects.length - 1) {
                sb.append(objects[i]).append(",");
            } else {
                sb.append(objects[i]);
            }
        }
        sb.append("】");

        String desk = "0";
        String assetId = "";
        List<String> descrList = new ArrayList<>();
        for (ThresholdManage req : reqList) {
            desk = req.getAssetDesk() + "";
            ThresholdManageQuery query = new ThresholdManageQuery();
            query.setAssetDesk(req.getAssetDesk());
            if (CollectionUtils.isEmpty(req.getAssetIds())) {
                query.setOrgIds(req.getOrgIds());
            } else {
                assetId = req.getAssetIds().get(0);
                query.setAssetId(assetId);
            }
            query.setCategory(req.getCategory());
            List<ThresholdManageVo> oldList = thresholdManageService.getList(query);
            ThresholdManageVo oldVo = null;
            if (!CollectionUtils.isEmpty(oldList)) {
                oldVo = oldList.get(0);
            }
            Integer autoFlag = req.getAutoFlag();
            AssetMode modeAo = assetModeService.getByCode(req.getAssetDesk());
            String description = sb.toString() + "所有" +(Objects.isNull(modeAo)?"未知类型设备"+req.getAssetDesk(): modeAo.getName()) + "【" + ThresholdCategoryEnum.getTitle(req.getCategory()) + "】";
            if (autoFlag == 2) {
                Asset one = assetService.getById(req.getAssetIds().get(0));
                description = sb.toString() + "的资产【" + one.getName() + "】【" + ThresholdCategoryEnum.getTitle(req.getCategory()) + "】";
            }
            description = this.pinDescr(req, oldVo, description);
            if (description.contains("变更为")) {
                int last = description.lastIndexOf("，");
                String substr = description.substring(0, last) + "。";
                descrList.add(substr);
            }
        }

        for (String description : descrList) {
            description = description.replace("null", "无");
            SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                    assetId, desk, LogDetailItemIdType.DEFAULT_THRESHOLD, description);
            sysActionLogDetailService.save(detail);
        }
    }

    private String pinDescr(ThresholdManage req, ThresholdManageVo oldVo, String description) {
        if (Objects.isNull(oldVo)) {
            if (req.getGeneral() != null) {
                description += "普通阈值由【无】变更为【" + req.getGeneral() + "】，";
            }
            if (req.getRangeMax() != null) {
                description += "范围阈值上限由【无】变更为【" + req.getRangeMax() + "】，";
            }
            if (req.getRangeMin() != null) {
                description += "范围阈值下限由【无】变更为【" + req.getRangeMin() + "】，";
            }
            if (req.getStepHighest() != null) {
                description += "阶段阈值阶梯一由【无】变更为【" + req.getStepHighest() + "】，";
            }
            if (req.getStepHigher() != null) {
                description += "阶段阈值阶梯二由【无】变更为【" + req.getStepHigher() + "】，";
            }
            if (req.getStepHigh() != null) {
                description += "阶段阈值阶梯三由【无】变更为【" + req.getStepHigh() + "】，";
            }
            description = description.replaceAll("null", "无");
            return description;
        }
        if (req.getGeneral() != null && !Objects.equals(req.getGeneral(), oldVo.getGeneral())) {
            description += "普通阈值由【" + oldVo.getGeneral() + "】变更为【" + req.getGeneral() + "】，";
        }
        if (req.getRangeMax() != null && !Objects.equals(req.getRangeMax(), oldVo.getRangeMax())) {
            description += "范围阈值上限由【" + oldVo.getRangeMax() + "】变更为【" + req.getRangeMax() + "】，";
        }
        if (req.getRangeMin() != null && !Objects.equals(req.getRangeMin(), oldVo.getRangeMin())) {
            description += "范围阈值下限由【" + oldVo.getRangeMin() + "】变更为【" + req.getRangeMin() + "】，";
        }
        if (req.getStepHighest() != null && !Objects.equals(req.getStepHighest(), oldVo.getStepHighest())) {
            description += "阶段阈值阶梯一由【" + oldVo.getStepHighest() + "】变更为【" + req.getStepHighest() + "】，";
        }
        if (req.getStepHigher() != null && !Objects.equals(req.getStepHigher(), oldVo.getStepHigher())) {
            description += "阶段阈值阶梯二由【" + oldVo.getStepHigher() + "】变更为【" + req.getStepHigher() + "】，";
        }
        if (req.getStepHigh() != null && !Objects.equals(req.getStepHigh(), oldVo.getStepHigh())) {
            description += "阶段阈值阶梯三由【" + oldVo.getStepHigh() + "】变更为【" + req.getStepHigh() + "】，";
        }
        description = description.replaceAll("null", "无");
        return description;
    }
}

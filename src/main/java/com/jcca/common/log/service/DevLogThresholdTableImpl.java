package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdAsset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.db.controller.bean.DbThresholdReq;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 表空间阈值设置 运维日志记录
 * @className DevLogThresholdTableImpl
 * @date 2024/4/8 15:17
 * @since 2.1.0.0
 */
@Service
public class DevLogThresholdTableImpl implements DevLogService {

    @Resource
    private ManageDbService manageDbService;
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
        return DevLogConstant.THRESHOLD_TABLESPACE;
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
        if (!(arg instanceof DbThresholdReq)) {
            return;
        }

        DbThresholdReq req = (DbThresholdReq) arg;
        String id = req.getId();
        Integer tablespaceThreshold = req.getTablespaceThreshold();

        ManageDb db = manageDbService.getById(id);
        Asset asset = assetService.getById(db.getAssetId());
        ThresholdAsset thresholdAsset = thresholdAssetService.getById(db.getId());

        actionLog.setLogName("配置【" + asset.getName() + "】数据库【" + db.getDbName() + "】表空间阈值");

        String description = "表空间阈值由【" + thresholdAsset.getTablespace() + "】变更为【" + tablespaceThreshold + "】";
        description = description.replace("null", "无");
        SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                asset.getId(), id, LogDetailItemIdType.DB, description);
        sysActionLogDetailService.save(detail);
    }
}

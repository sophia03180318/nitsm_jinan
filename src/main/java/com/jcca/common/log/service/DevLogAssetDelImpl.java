package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 删除资产 运维日志记录
 * @className DevLogAssetDelImpl
 * @date 2024/4/9 10:23
 * @since 2.1.0.0
 */
@Service
public class DevLogAssetDelImpl implements DevLogService {

    @Resource
    private AssetService assetService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.ASSET_DEL;
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
        Asset one = assetService.getById(arg.toString());

        String description = "删除【" + one.getName() + "】数据";
        actionLog.setLogName(description);

        SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                one.getId(), one.getId(), LogDetailItemIdType.ASSET, description);

        sysActionLogDetailService.save(detail);

    }
}

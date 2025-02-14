package com.jcca.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysActionLogDetailMapper;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.utils.MyIdUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HanHW
 * @description 行为日志明细
 * @className SysActionLogDetailServiceImpl
 * @date 2024/4/10 11:16
 * @since 2.1.0.0
 */
@Service
public class SysActionLogDetailServiceImpl extends ServiceImpl<SysActionLogDetailMapper, SysActionLogDetail>
        implements SysActionLogDetailService {

    @Resource
    private SysActionLogDetailMapper sysActionLogDetailMapper;

    @Override
    public SysActionLogDetail setDetail(String actionLogId, String assetId, String itemId, String itemIdType, String decription) {
        SysActionLogDetail detail = new SysActionLogDetail();
        detail.setId(MyIdUtil.getId());
        detail.setActionLogId(actionLogId);
        detail.setAssetId(assetId);
        detail.setItemId(itemId);
        detail.setItemIdType(itemIdType);
        detail.setDescription(decription);
        return detail;
    }

    @Override
    public List<SysActionLogDetail> listByActionLogId(String actionLogId) {
        return sysActionLogDetailMapper.listByActionLogId(actionLogId);
    }
}

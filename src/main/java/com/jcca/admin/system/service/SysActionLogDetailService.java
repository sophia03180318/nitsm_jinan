package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysActionLogDetail;

import java.util.List;

/**
 * @author HanHW
 * @description 行为日志明细
 * @className SysActionLogDetailService
 * @date 2024/4/10 11:15
 * @since 2.1.0.0
 */
public interface SysActionLogDetailService extends IService<SysActionLogDetail> {

    SysActionLogDetail setDetail(String actionLogId, String assetId, String itemId, String itemIdType, String decription);

    List<SysActionLogDetail> listByActionLogId(String actionLogId);
}

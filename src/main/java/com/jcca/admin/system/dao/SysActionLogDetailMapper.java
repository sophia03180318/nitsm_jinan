package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysActionLogDetail;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author HanHW
 * @description 行为日志明细
 * @className SysActionLogDetailMapper
 * @date 2024/4/10 11:17
 * @since 2.1.0.0
 */
public interface SysActionLogDetailMapper extends BaseMapper<SysActionLogDetail> {

    List<SysActionLogDetail> listByActionLogId(String actionLogId);
}

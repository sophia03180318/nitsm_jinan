package com.jcca.web2.service;

import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.web2.dto.CollectConfigDto;
import com.jcca.web2.vo.CollectConfigVo;
import com.jcca.web2.vo.TargetVerifyVo;

import java.util.List;
import java.util.Map;

/**
 * @author HanHW
 * @description 采集配置
 * @className CollectConfigService
 * @date 2024/2/28 14:51
 * @since 2.1.0.0
 */
public interface CollectConfigService {

    /**
     * 查找采集指标和指标字典
     *
     * @param dto 查询参数
     * @return 指标列表和指标字典列表
     */
    List<PerformanceTarget> list(CollectConfigDto dto);

    /**
     * 按ID删除指标字典
     *
     * @param specDictId 指标字典数据ID
     */
    void delSpecDict(String specDictId);

    /**
     * 保存采集配置
     *
     * @param vo CollectConfigVo
     */
    void save(CollectConfigVo vo);

    /**
     * 指标验证
     *
     * @param vo 参数
     * @return 验证结果
     */
    Map<String, Object> verifyTarget(TargetVerifyVo vo);

    /**
     * 修改指标采集时间
     *
     * @param targetId 指标ID
     * @param unit     时间单位
     * @param internal 时间间隔
     */
    void modifyCron(String targetId, String unit, Integer internal);

    /**
     * 启停采集指标
     *
     * @param targetId    指标ID
     * @param isAvaliable 0不可用，1可用
     */
    void modifyStatus(String targetId, Integer isAvaliable);
}

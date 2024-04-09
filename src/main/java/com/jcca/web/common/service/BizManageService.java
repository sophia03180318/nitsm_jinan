package com.jcca.web.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.common.entity.BizManage;

import java.util.List;

/**
 * 业务模块公共服务
 *
 * @author hanwone
 * @date 2020-05-29 10:11:58
 **/
public interface BizManageService extends IService<BizManage> {

    /**
     * 保存数据
     *
     * @param bizId   业务数据ID
     * @param bizType 业务类型
     *                BizManageConstant
     * @return 是否保存成功
     */
    boolean save(String bizId, int bizType);

    /**
     * 根据业务ID和业务类型删除数据
     *
     * @param bizId   业务数据ID
     * @param bizType 业务类型
     * @return 是否删除成功
     */
    boolean remove(String bizId, int bizType);

    /**
     * 根据业务类型查询数据
     *
     * @param bizType 业务类型
     *                BizManageConstant
     * @return 业务ID列表
     */
    List<String> list(int bizType);

    /**
     * 根据业务类型和业务ID查询数据
     *
     * @param id
     * @param net
     * @return
     */
    List<String> listByBizId(String bizId, Integer bizType);

    /**
     * 根据业务类型和业务ID查询数据
     *
     * @param id
     * @param net
     * @return
     */
    List<BizManage> listObjByBizId(String bizId, Integer bizType);

    /**
     * 保存组织-业务参数
     *
     * @param bizId
     * @param bizType
     * @return
     */
    Boolean saveByBizAndOrg(String bizId, Integer bizType);

    /**
     * 通过组织关联
     *
     * @param thresholdProcess
     * @return
     */
    List<String> listByBizAndOrg(Integer thresholdProcess);

    /**
     * 保存组织-业务数据关系
     *
     * @param orgId
     * @param bizId
     * @param bizType
     * @return
     */
    Boolean saveByBizAndOrg(String orgId, String bizId, Integer bizType);
}

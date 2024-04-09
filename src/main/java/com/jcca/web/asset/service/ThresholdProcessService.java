package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.asset.controller.bean.ChangeProcessModeReq;
import com.jcca.web.asset.entity.ThresholdProcess;

import java.util.List;

/**
 * 进程阈值
 *
 * @author Lvyp
 */
public interface ThresholdProcessService extends IService<ThresholdProcess> {

    /**
     * 创建
     *
     * @param entityList
     * @throws Exception
     */
    void createAll(List<ThresholdProcess> entityList);

    /**
     * 删除
     *
     * @param process
     */
    void remove(ThresholdProcess process);

    /**
     * 通过资产IP和进程名字查找
     *
     * @param assetId
     * @param processName
     * @return
     */
    ThresholdProcess getOneByAssetIpAndName(String assetId, String processName);

    /**
     * 改变进程的配置模式(双机双活,双击单活,普通)
     *
     * @return: com.jcca.common.vo.ResultVo<?>
     * @Author: syt
     * @Date: 2021/8/10/010 16:40
     */
    ResultVo<?> modeChange(ChangeProcessModeReq req);

    /***
     * 分别更新单个进程的状态,用于进程管理中的显示 syt
     *
     * @param processName:
     * @param id:
     * @return: void
     * @Author: syt
     * @Date: 2021/8/24/024 10:26
     */
    void updateByAssetIdAndProcessName(String processName, String id, Boolean collectStatus,String processId);

    /**
     * 根据资产ID删除进程
     *
     * @param assetId: 资产ID
     * @return: void
     * @Author: syt
     * @Date: 2021/8/30/030 13:41
     */
    void removeByAssetId(String assetId);

    /**
     * 更新采集模式
     *
     * @param assetCode
     * @param hostMode  模式:1双机单活,2双机双活,3普通
     */
    void updateMode(String assetCode, Integer hostMode);
}

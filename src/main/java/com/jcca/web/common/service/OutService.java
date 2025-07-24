package com.jcca.web.common.service;

import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.common.bean.RestBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.vo.AssetOutVo;
import com.jcca.web.asset.vo.AssetProcessVo;
import com.jcca.web.common.controller.req.AssetOutReq;
import com.jcca.web.common.controller.req.ProcessOutReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web.common.vo.AssetCollectTestVo;
import com.jcca.web.common.vo.ProcessOnChangeVo;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

/**
 * @ClassName OutService
 * @Description ITSM调用外部应用接口
 * @Date 2020/7/31 11:30
 * @Author hanwone
 */
public interface OutService {

    /**
     * 外部接口获取资产
     *
     * @param req
     * @return
     */
    List<AssetOutVo> findForOutRequest(AssetOutReq req);

    /**
     * 资产变动通知外部应用
     *
     * @param optFlag 0:增,1:删,2:改
     * @param asset   变动的资产
     */
    void notifyOnChange(Integer optFlag, Asset asset);

    /**
     * 资产进程变动通知
     *
     * @param changeVo
     * @throws Exception
     */
    void processOnChange(ProcessOnChangeVo changeVo) throws Exception;

    /**
     * 测试资产采集指标
     *
     * @param assetOutVo
     * type:getValue\test
     * @return
     */
    AssetCollectTestVo collectTest(AssetOutVo assetOutVo);

    /**
     * 获取资产所有进程信息
     *
     * @param assetId
     * @return
     * @throws Exception
     */
    List<AssetProcessVo> getAllProcess(String assetId) throws Exception;

    /**
     * 获取中心或者车站的所有进程
     *
     * @return: java.util.List<com.jcca.web.asset.entity.ThresholdProcess>
     * @Author: syt
     */
    List<ThresholdProcess> getProcessByType(ProcessOutReq req);

    /**
     * 分组获取车站资产信息
     *
     * @param req
     * @return
     */
    List<AssetOutVo> findStationAssetByGroup(AssetOutReq req);

    /**
     * 查询
     *
     * @param req
     * @return
     * @throws Exception
     */
    BusinessGetSnmpResultResp getSnmpResult(BusinessGetSnmpResultReq req) throws Exception;

    /**
     * 获取SHH采集结果
     * @param asset
     * @param command
     * @return
     * @throws Exception
     */
    String getSSHResult(Asset asset,String command)throws Exception;

    /**
     * 远程telnet 获取结果
     * @param asset
     * @param commandList
     * @return
     * @throws Exception
     */
    List<String> getTelnetResult(Asset asset, Collection<String> commandList);

    /**
     * 查找主备机标识
     *
     * @param assetId
     * @param tabName
     * @return
     */
    String findMasterOrSlaveFlag(String assetId, String tabName);

    /**
     * 获取车站的TOPO
     *
     * @param stationIp
     * @return
     */
    ResultVo queryStationTopo(String stationIp) throws UnsupportedEncodingException;

    /**
     * 采集指标变动
     */
    void targetOnChange();

    ResultVo testPerformanceTarget(Asset asset, PerformanceTarget performanceTarget);
}

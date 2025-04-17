package com.jcca.web.ip.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.asset.service.bean.AddAssetException;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.enums.IpPingStatusEnum;
import com.jcca.web.ip.vo.DetectionVo;
import com.jcca.web2.vo.IpVo;

import java.util.List;

/**
 * ip信息
 *
 * @author lyp
 */
public interface IpInfoService extends IService<IpInfo> {

    /**
     * ping 检测
     *
     * @param ids
     * @return
     */
    List<DetectionVo> detectionIps(List<String> ids);


    /**
     * 检测ip是否在库存
     *
     * @param ip
     * @return
     */
    Boolean examineIp(String ip);

    /**
     * 分配ip
     *
     * @param ip
     * @return
     */
    ResultVo<String> allocationIp(String ip);

    /**
     * 释放ip
     *
     * @param ip
     * @return 成功后返回被释放的ip
     * @throws Exception
     */
    void liberateIp(String ip) throws AddAssetException;

    /**
     * 检测MAC地址
     *
     * @param ip
     */
    String getMacAddress(IpInfo ip);

    /**
     * 更新mac地址
     *
     * @param ip
     * @param mac
     */
    void updateMacAddress(String ip, String mac);

    /**
     * 更新ping状态
     *
     * @param ip
     * @param pingStatus
     */
    void updatePingStatus(String ip, IpPingStatusEnum pingStatus);

    /**
     * 查询所有
     *
     * @param ip
     * @return
     */
    List<IpInfo> listByIp(String ip);


    /**
     * 发起异步ping任务
     *
     * @param netId
     * @param ipList
     */
    void asyncPing(String netId, List<String> ipList);

    /**
     * 查询IP表单信息
     * @param netWorkId
     * @return
     */
    List<IpVo> selectIPVoV2(String netWorkId);

}

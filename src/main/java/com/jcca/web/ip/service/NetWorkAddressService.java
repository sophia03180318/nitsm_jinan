package com.jcca.web.ip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.ip.controller.bean.SaveNetWorkReq;
import com.jcca.web.ip.entity.NetWorkAddress;
import com.jcca.web2.vo.LineNetWorkVo;
import com.jcca.web.ip.vo.NetWorkAddressVo;
import com.jcca.web.ip.vo.SysOrgVo;
import com.jcca.web2.vo.StationNetObj;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络地址
 *
 * @author lyp
 */
public interface NetWorkAddressService extends IService<NetWorkAddress> {

    /**
     * 创建网络
     *
     * @param req
     * @return network id
     */
    ResultVo<String> createNetWork(SaveNetWorkReq req) throws Exception;


    ResultVo<String> verificationNet(String gateway, String mask, String id);

    /**
     * 删除网络
     *
     * @param net
     */
    void removeNet(NetWorkAddress net);


    /**
     * 查询指定组织下的所有IP段
     *
     * @param org
     */
    List<SysOrgVo> selectIps(SysOrg org);

    /**
     * 查询未分配组织的所有IP段
     */
    List<SysOrgVo> selectOldIp();


    /**
     * 查询所有IP段
     */
    List<NetWorkAddress> selectIpById(SysOrg org);

    /**
     * 查询中心IP段
     */
    List<NetWorkAddressVo> selectCenterIpByIdV2(SysOrg org);


    /**
     * 查询线路IP段
     */
    ArrayList<LineNetWorkVo> selectLineIpByIdV2(SysOrg org);

    /**
     * 根据orgid返回orgname//没有则为未知组织
     */

    String getNameByOrgId(String id);

    SysOrgVo getSysOrgVo(SysOrg sysOrg);

    /**
     * 获取线路下面的网络信息列表
     * @param lineOnrId
     * @return
     */
    List<StationNetObj> getLineNetMsgV2(String lineOnrId);
}

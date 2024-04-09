package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.PortModelTemp;
import com.jcca.web2.dto.PortTempDto;
import com.jcca.web2.entity.PortTemp;

import java.util.List;

/**
 * @description: 端口模板
 * @author: sophia
 * @create: 2023/11/02 14:38
 **/

public interface PortTempService extends IService<PortTemp> {

   /**
    * 获取指定模板信息
    * port1 光口
    * port2 电口
    * */
   PortTempDto getPortTempMsg(String id);

   /**
    * 获取网络设备型号列表
    * */
   List<PortModelTemp> getNetworkDevices();

    /**
     * 通过资产ID获取可使用的模板列表
     * */
    List<PortTemp> getTemplateByAssetId(String assetId);

}
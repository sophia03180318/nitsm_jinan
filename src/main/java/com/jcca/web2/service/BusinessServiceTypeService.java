package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web2.entity.BusinessServiceType;
import org.openxmlformats.schemas.drawingml.x2006.main.STAdjCoordinate;

import java.util.List;


/**
 * @description: 业务类型
 * @author: sophia
 * @create: 2023/11/01 10:38
 **/
public interface BusinessServiceTypeService extends IService<BusinessServiceType> {


    List<Asset> getAssetByType(String orgId, String type);

    Integer getMaxSort();

    String getTypeByName(String name);

}
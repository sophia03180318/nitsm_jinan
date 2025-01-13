package com.jcca.web2.vo;

import com.jcca.web.collect.entity.CollectInterfaces;
import lombok.Data;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;

import java.util.List;

/**
 * @description: Topo 资产
 * @author: Lvyp
 * @create: 2024/04/23 10:26
 */
@Data
public class TopoAsset {

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产型号
     */
    private Integer assetMode;
    /**
     * 端口名称
     */
    private List<CollectInterfaces> portNameList;

}

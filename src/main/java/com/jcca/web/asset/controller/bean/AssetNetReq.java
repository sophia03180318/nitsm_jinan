package com.jcca.web.asset.controller.bean;

import com.jcca.web.asset.entity.AssetTelnet;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author syt
 * @date 2021/05/19  12:17
 * @classname nitsmcom.jcca.web.asset.controller.beanAssetTelnetReq
 */
@Data
@NoArgsConstructor
public class AssetNetReq implements Serializable {

    private static final long serialVersionUID = 1L;

    // 无端口信息
    public static final String PORT_NULL = "无";


    private List<AssetTelnet> telnet;

}
package com.jcca.web.asset.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author syt
 * @date 2021/05/19  14:28
 * @classname nitsmcom.jcca.web.asset.controller.beanAssetPingReq
 */
@Data
public class AssetPingReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "ip不可为空")
    private List<String> ips;

    @NotNull
    private String id;
}

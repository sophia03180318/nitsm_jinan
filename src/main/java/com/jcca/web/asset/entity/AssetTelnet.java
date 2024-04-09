package com.jcca.web.asset.entity;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author syt
 * @date 2021/05/19  14:52
 * @classname nitsmcom.jcca.web.asset.entityAssetTelnet
 */
@Data
public class AssetTelnet implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "ip不可为空")
    private String ip;
    @NotNull(message = "端口不可为空")
    private String port;

    private String id;


}

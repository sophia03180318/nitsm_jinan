package com.jcca.web.asset.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author syt
 * @date 2021/05/19  15:07
 * @classname nitsmcom.jcca.web.asset.voAssetPingVo
 */
@Data
public class AssetPingVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String ip;
    /**
     * ping结果
     * true通, false断
     */
    private String res;

    /**
     * ping结果
     * true通, false断
     */
//    private Byte res;
}

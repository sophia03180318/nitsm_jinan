package com.jcca.web.asset.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author syt
 * @date 2021/05/19  15:09
 * @classname nitsmcom.jcca.web.asset.voAssetTelnetVo
 */
@Data
public class AssetTelnetVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String ip;
    private String port;
    /**
     * telnet结果
     * true通, false断
     */
    private String res;
}

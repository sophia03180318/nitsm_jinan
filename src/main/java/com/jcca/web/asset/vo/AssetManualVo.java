package com.jcca.web.asset.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author hanhw
 * @description 手动配置对端设备信息
 * @className AssetManualVo
 * @date 2023/4/21 16:42
 * @since 2.0.3.0
 */
@Data
public class AssetManualVo {

    // 连接信息数据ID
    private String id;

    private String assetIp;
    private String portName;
    private String log;

    // 对端信息
    @Length(max = 180, message = "设备名字不能超过60个字符")
    private String linkAssetName; // 名称

    private String linkAssetIp; // 对端设备ip
    private String linkPort; // 接口名称

}

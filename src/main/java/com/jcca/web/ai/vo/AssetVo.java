package com.jcca.web.ai.vo;

import com.jcca.common.config.thymeleaf.utility.DictUtil;
import lombok.Data;

/**
 * 资产表
 *
 * @author hanwone
 * @date 2020-04-20 15:37:48
 **/
@Data
public class AssetVo {


    private String id;

    private String name;

    private String assetCode;

    private Integer desk;

    private String ip;

    private Integer assetMode;

    private Byte status;

    private String orgId;

    private String assetImage;


    @Override
    public String toString() {
        String deskS = DictUtil.getValue("ASSET_MODE", desk.toString());
        return "设备{" +
                "  设备ID='" + id + '\'' +
                ", 设备名称='" + name + '\'' +
                ", 设备编号='" + assetCode + '\'' +
                ", 设备类型=" + deskS +
                ", 设备型号='" + assetImage + '\'' +
                ", 设备IP='" + ip + '\'' +
                '}';
    }
}

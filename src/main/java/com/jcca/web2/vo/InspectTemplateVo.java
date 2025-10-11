package com.jcca.web2.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检模板
 * @date 2025-10-09 星期四 11:25:28`
 */
@Data
public class InspectTemplateVo {
    private String templateName;
    private String id;
    private String templateCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private List<AssetData> orgData;


    /**
     * 资产数据
     */
    @Data
    static class AssetData {
        // 资产(设备)ID
        private String assetId;
        // 资产(设备)名称
        private String assetName;
        // 资产类型
        private String modeType;
    }
}

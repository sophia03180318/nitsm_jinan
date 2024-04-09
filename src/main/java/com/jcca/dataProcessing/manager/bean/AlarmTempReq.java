package com.jcca.dataProcessing.manager.bean;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 告警信息参数
 * @description: 告警信息参数
 * @author: Lvyp
 * @create: 2024/01/29 11:12
 */
@Data
public class AlarmTempReq {

    /**
     * 资产IP
     */
    private String assetIp;

    /**
     * 资产名称(可能为空)
     */
    private String assetName;

    /**
     * 原始信息
     */
    private String orgMsg;
    /**
     * 阈值
     * 阶段阈值的话填写 xxx单位 - xxx单位
     * 普通阈值 填写数值
     * 分阶阈值 填写数值
     */
    private String  thresholdValue;

    /**
     * 采集值
     */
   private String collectValue;
    /**
     * 标识
     */
    private String flag;
    /**
     * 对端设备IP
     */
    private String linkAssetIp;
    /**
     * 对端设备名称
     */
    private String linkAssetName;
    /**
     * 资产组号
     */
    private String assetGroupCode;

    /**
     * 格式化告警信息
     * @return
     */
    public String formatting(String template){
        if(StrUtil.isEmpty(template)){
            return this.orgMsg;
        }
        if(StrUtil.isEmpty(this.assetIp)){
            this.assetIp = "";
        }
        if(StrUtil.isEmpty(this.assetName)){
            this.assetName = "";
        }
        if(StrUtil.isEmpty(this.orgMsg)){
            this.orgMsg = "";
        }
        if(StrUtil.isEmpty(this.thresholdValue)){
            this.thresholdValue = "";
        }
        if(StrUtil.isEmpty(this.collectValue)){
            this.collectValue = "";
        }
        if(StrUtil.isEmpty(this.flag)){
            this.flag = "";
        }
        return template.replace("assetIp",this.assetIp).replace("assetName",this.assetName).replace("orgMsg",this.orgMsg)
                .replace("thresholdValue",this.thresholdValue).replace("collectValue",this.collectValue).replace("flag",this.flag);
    }

}

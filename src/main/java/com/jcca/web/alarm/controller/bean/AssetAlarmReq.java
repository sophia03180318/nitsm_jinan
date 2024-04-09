package com.jcca.web.alarm.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName AssetAlarmReq
 * @Description 资产告警
 * @Date 2020/8/11 11:01
 * @Author hanwone
 */
@Data
public class AssetAlarmReq {

    @NotNull(message = "资产ID不能为空")
    private String assetId;

    private String category;
}

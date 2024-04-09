package com.jcca.web2.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author HanHW
 * @description 巡检采集结果
 * @className InspectResultVo
 * @date 2023/11/15 11:25
 * @since 2.1.0.0
 */
@Data
public class InspectResultVo {
    // 设备ID
    private String assetId;
    // 指标
    private String targetItem;
    // 和 targetItem 确定唯一
    private String modeType;
    // 采集指标名称
    private String targetName;
    // 采集结果
    private String resultMsg;
    // 结果状态，有无异常，1待巡检，2正在巡检，3巡检正常，4巡检异常
    private String inspectState;
    //如果有阈值存储阈值
    private String thresholdValue;
    //巡检过后的值
    private String inspectValue;
    //时间
    private Date createTime;
    //参考命令
    private String referCommand;

}

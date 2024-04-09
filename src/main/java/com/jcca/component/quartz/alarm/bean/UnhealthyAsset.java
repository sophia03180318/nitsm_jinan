package com.jcca.component.quartz.alarm.bean;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 15:51 2022/11/30
 * @ Description:
 */
@Data
public class UnhealthyAsset {

    private String assetId;
    /**
     * 次数
     */
    private int num;
}

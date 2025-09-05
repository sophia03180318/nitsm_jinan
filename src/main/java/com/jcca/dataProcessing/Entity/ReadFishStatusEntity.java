package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * 电源状态
 * - Health：健康状态（OK 表示正常，Warning 警告，Critical 故障）；
 * - State：启用状态（Enabled 表示设备已启用并正常工作，Disabled 表示禁用）。
 */
@Data
public class ReadFishStatusEntity {

    /**
     * 健康状态
     *
     */
    private String health;
    /**
     * 是否启用
     * Enabled 启用
     */
    private String state;

}

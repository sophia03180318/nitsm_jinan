package com.jcca.web.common.service.bean;

import lombok.Data;

/**
 * 采集ping结果
 *
 * @author Lvyp
 */
@Data
public class CollectPingResult {

    private String collectIp;

    private Boolean result;

    private String msg;
}

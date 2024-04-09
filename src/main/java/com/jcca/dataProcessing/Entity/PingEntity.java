package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: Ping状态
 * @author: Lvyp
 * @create: 2023/11/18 19:41
 */
@Data
public class PingEntity implements Serializable {

    private String assetName;

    private String assetIp;

    private Boolean status;

    private String assetId;

}

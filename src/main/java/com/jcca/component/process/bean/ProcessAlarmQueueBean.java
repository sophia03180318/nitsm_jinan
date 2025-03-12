package com.jcca.component.process.bean;

import com.jcca.web.asset.entity.Asset;
import lombok.Data;

import java.io.Serializable;

/**
 * 进程告警BEAN
 *
 * @author lyp
 */
@Data
public class ProcessAlarmQueueBean implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 进程状态
     * true 正常 false 丢失、异常
     */
    private Boolean processStatus;
    /**
     * 进程名称
     */
    private String processName;
    /**
     * 进程号
     */
    private String processId;

    private Asset asset;

    private Integer hostMode;
}

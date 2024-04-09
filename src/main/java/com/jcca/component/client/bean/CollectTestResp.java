package com.jcca.component.client.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 测试采集项是否正常
 *
 * @author Lvyp
 */
@Data
public class CollectTestResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 结果码
     * 0正常1失败
     */
    private Boolean isOk;
    /**
     * 信息
     */
    private String msg;

}

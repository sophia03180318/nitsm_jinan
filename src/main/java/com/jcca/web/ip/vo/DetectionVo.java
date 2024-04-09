package com.jcca.web.ip.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 检测结果
 *
 * @author lyp
 */
@Data
public class DetectionVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private String id;
    /**
     * ip
     */
    private String ip;
    /**
     * ping状态
     */
    private String pingStatus;
    /**
     * 描述信息
     */
    private String msg;
}

package com.jcca.web.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 进程阈值
 *
 * @author Lvyp
 */
@Data
public class ThresholdProcessVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产编号
     */
    private String assetCode;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 进程阈值配置主键
     */
    private String id;
    /**
     * 进程名称
     */
    private String processName;
    /**
     * 进程ID
     */
    private String processId;
    /**
     * 进程cpu阈值
     */
    private String thresholdCpu;
    /**
     * 进程内存阈值
     */
    private String thresholdMemory;
    /**
     * 采集状态
     */
    private Byte collectStatus;
    /**
     * 采集状态翻译后的文字
     */
    private String collectStatusStr;
    /**
     * 配置时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 模式 1双机单活 2双机双活 3普通
     */
    private Integer hostMode;

    /**
     * 进程备注
     */
    private String remark;

}

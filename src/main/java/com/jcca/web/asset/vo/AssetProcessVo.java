package com.jcca.web.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 资产进程
 *
 * @author Lvyp
 */
@Data
public class AssetProcessVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    /**
     * cpu使用率
     */
    private String cpuRate;
    /**
     * 内存使用率
     */
    private String memoryRate;
    /**
     * 进程名称
     */
    private String name;
    /**
     * 进程ID
     */
    private String processId;
    /**
     * 资产ID
     */
    private String assetId;

    private Long sortFlag;

}

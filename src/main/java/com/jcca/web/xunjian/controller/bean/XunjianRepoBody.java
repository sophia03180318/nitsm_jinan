package com.jcca.web.xunjian.controller.bean;

import lombok.Data;

/**
 * 巡检报告详情
 *
 * @author Lvyp
 */
@Data
public class XunjianRepoBody {

    private String assetId = "";

    private String assetName = "";
    /**
     * cpu 描述信息
     */
    private String cpuResultMsg = "";

    private Integer cpuNormalFlag;
    /**
     * 内存 描述信息
     */
    private String memoryResultMsg = "";

    private Integer memoryNormalFlag;
    /**
     * 网卡描述信息
     */
    private String netCardResultMsg = "";

    private Integer netCardNormalFlag;
    /**
     * 磁盘 描述信息
     */
    private String diskResultMsg = "";

    private Integer diskNormalFlag;
    /**
     * 端口流入
     */
    private String portInResultMsg = "";

    private Integer portInNormalFlag;
    /**
     * 端口流出
     */
    private String portOutResultMsg = "";

    private Integer portOutNormalFlag;
    /**
     * 软件状态
     */
    private String softwareResultMsg = "";

    private Integer softwareNormalFlag;
    /**
     * 告警信息
     */
    private String alarmResultMsg = "";

    private Integer alarmNormalFlag;
    /**
     * 设备运行时长
     */
    private String runTimeMag = "";
    private Integer runTimelog;

    /**
     * oracle
     */
    private String oracleMag = "";
    private Integer oracleFlag;

}

package com.jcca.web.asset.detail.bean;

import lombok.Data;

/**
 * @ClassName DetailProcess
 * @Description 资产详情----进程信息
 * @Date 2020/6/29 17:38
 * @Author hanwone
 */
@Data
public class DetailProcess {
    /**
     * 进程名称
     */
    private String name;
    /**
     * 进程CPU使用率
     */
    private String cpuUsedRate;
    /**
     * 进程内存使用率
     */
    private String memUsedRate;
    /**
     * 状态
     */
    private Byte status;
    /**
     * 备注信息
     */
    private String remark;
}

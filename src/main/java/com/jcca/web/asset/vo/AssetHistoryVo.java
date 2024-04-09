package com.jcca.web.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName AssetHistoryVo
 * @Description 资产详情查看历史
 * @Date 2020/7/7 11:15
 * @Author hanwone
 */
@Data
public class AssetHistoryVo {
    /**
     * cpu使用率
     */
    private String cpuUsedRate;
    /**
     * 内存使用率
     */
    private String memUsedRate;
    /**
     * 交换空间使用率
     */
    private String swapUsedRate;
    /**
     * 数据产生时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    /**
     * 端口流入量
     */
    private String portIn;
    /**
     * 端口流出量
     */
    private String portOut;
}

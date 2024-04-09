package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 通用型类型机柜设备详情返回值
 * @author: Lvyp
 * @create: 2023/10/24 16:36
 */
@Data
public class CabinetAssetInfoPubVo extends CabinetAssetInfoVo {

    /**
     * 最后一次采集时间
     * CPU采集时间为主
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    /**
     * cpu使用率
     */
    private Double cpuRate;
    /**
     * 内存使用率
     */
    private Double memoryRate;

    public CabinetAssetInfoPubVo(CabinetAssetInfoVo vo, Date collectTime, Double cpuRate, Double memoryRate) {
        super(vo);
        this.collectTime = collectTime;
        this.cpuRate = cpuRate;
        this.memoryRate = memoryRate;
    }

    public CabinetAssetInfoPubVo() {
    }

    ;

}

package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 磁盘阵列详情
 * @author: Lvyp
 * @create: 2023/10/24 16:40
 */
@Data
public class CabinetAssetInfoRaidVo extends CabinetAssetInfoVo {

    /**
     * 最后一次采集时间
     * 容量指标为主
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    /**
     * 总容量
     * 单位Byte
     */
    private Long capacity;
    /**
     * 使用容量
     * 单位Byte
     */
    private Long usedCapacity;

    /**
     * 构造 CabinetAssetInfoRaidVo
     *
     * @param baseInfo
     * @param collectTime
     * @param capacity
     * @param usedCapacity
     */
    public CabinetAssetInfoRaidVo(CabinetAssetInfoVo baseInfo, Date collectTime, Long capacity, Long usedCapacity) {
        super(baseInfo);
        this.collectTime = collectTime;
        this.capacity = capacity;
        this.usedCapacity = usedCapacity;
    }

    /**
     * 构造
     */
    public CabinetAssetInfoRaidVo() {

    }
}

package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO
 * @className AixIoCardEntity
 * @date 2023/12/5 9:18
 * @since 2.1.0.0
 */
@Data
public class AixIoCardEntity {
    private static final long serialVersionUID = -8015583955701543025L;
    private String id;

    private String assetId;

    private String collectCode;


    private Date collectTime;

    /**
     * IO卡名称
     */
    private String adapterName;
    /**
     * 1网口，2光口，3串口，4PCI物理槽位
     */
    private String adapterType;
    /**
     * IO卡状态
     */
    private String adapterStat;
    /**
     * 链路状态
     */
    private String attentionType;
    /**
     * IO口槽位
     */
    private String adapterSlot;
    /**
     * 描述
     */
    private String description;
    /**
     * 光口wwn号
     */
    private String fcWwn;

}

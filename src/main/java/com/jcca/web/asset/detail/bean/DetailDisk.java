package com.jcca.web.asset.detail.bean;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName DetailDisk
 * @Description 资产详情---磁盘信息
 * @Date 2020/6/22 17:14
 * @Author hanwone
 */
@Data
public class DetailDisk {
    /**
     * 磁盘挂载点
     */
    private String mountPoint;
    /**
     * 磁盘可使用量
     */
    private String freeStr;
    /**
     * 磁盘使用量
     */
    private String usedStr;
    /**
     * 磁盘使用率
     */
    private String usedRate;
    /**
     * 磁盘总容量
     */
    private String totalStr;
    /**
     * 创建时间
     */
    private Date collectTime;
}

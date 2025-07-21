package com.jcca.web.ai.vo;

import lombok.Data;

import java.util.Date;

/**
 * 磁盘信息
 *
 * @author sophia
 */
@Data
public class DiskVo2 {
    /**
     * 总量
     */
    private String totalStr;
    /**
     * 已使用
     */
    private String usedStr;
    /**
     * 剩余可用
     */
    private String freeStr;
    /**
     * 创建时间
     */
    private Date collectTime;


}

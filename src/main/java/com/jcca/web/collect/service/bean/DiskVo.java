package com.jcca.web.collect.service.bean;

import com.jcca.web.collect.entity.CollectDisk;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 磁盘信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DiskVo extends CollectDisk {

    private static final long serialVersionUID = 1L;
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

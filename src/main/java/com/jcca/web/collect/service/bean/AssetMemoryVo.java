package com.jcca.web.collect.service.bean;

import com.jcca.web.collect.entity.CollectMemory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内存
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetMemoryVo extends CollectMemory {

    private static final long serialVersionUID = 1L;

    /**
     * 总量
     */
    private String memTotalStr;
    /**
     * 已使用
     */
    private String memUsedStr;
    /**
     * 置换空间总量
     */
    private String swapTotalStr;
    /**
     * 置换空间使用量
     */
    private String swapUsedStr;

}

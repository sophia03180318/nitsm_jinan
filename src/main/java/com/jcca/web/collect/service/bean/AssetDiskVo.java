package com.jcca.web.collect.service.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资产磁盘信息
 *
 * @author Lvyp
 */
@Data
public class AssetDiskVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产磁盘总量
     */
    private String diskTotalStr;

    /**
     * 所有的磁盘
     */
    private List<DiskVo> diskList;

}

package com.jcca.web.alarm.dao.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通过类型查询
 *
 * @author lyp
 */
@Data
public class QueryExportByTypeReq implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 类型
     * OrgTypeEnum
     */
    private byte type;
    /**
     * 开始时间
     */
    private String startDate;
    /**
     * 结束时间
     */
    private String endDate;

    private List<String> orgIds;

    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    private Integer assetMode;


    private Integer showJcca;

}

package com.jcca.web.handbook.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件夹查询
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FolderQueryReq extends PageQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 上层文件夹的Id
     */
    private String pid;

    private String remark;
    /**
     * 组织ID syt
     */
    private String orgId;
    private byte type;

}

package com.jcca.web.handbook.controller.bean;


import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件查询
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileQueryReq extends PageQuery {

    private static final long serialVersionUID = 1L;
    /**
     * 文件夹ID
     */
    private String folderId;

}

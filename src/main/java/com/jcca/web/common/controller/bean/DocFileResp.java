package com.jcca.web.common.controller.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件相应
 *
 * @author lyp
 */
@Data
public class DocFileResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件ID
     */
    private String fileId;
    /**
     * 机柜ID
     */
    private String cabinetId;


}

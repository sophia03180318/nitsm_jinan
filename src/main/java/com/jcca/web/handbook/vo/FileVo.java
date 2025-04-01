package com.jcca.web.handbook.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件
 *
 * @author lyp
 */
@Data
public class FileVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 文件ID
     */
    private String id;
    /**
     * 文件所在文件夹id
     */
    private String folderId;
    /**
     * 文件名称
     */
    private String orignName;
    /**
     * 文件mime类型
     */
    private String mime;
    /**
     * 下载地址
     */
    private String downloadUrl;
    /**
     * 文件在网盘中的路径
     */
    private String filePath;
    /**
     * 备注
     */
    private String remark;
    /**
     * 时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 元素名称
     */
    private Integer itemName;

    private String viewUrl;
}

package com.jcca.web.handbook.controller.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 增加文件
 *
 * @author hanwone
 * @date 2020-04-06 12:14:22
 **/
@Data
public class AddFileReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 文件ID
     */
    @ApiModelProperty(required = false, hidden = true)
    private String id;
    /**
     * 文件原名
     */
    @NotEmpty(message = "文件原名称不能空")
    private String orignName;
    /**
     * 文件名称
     */
    @NotEmpty(message = "文件名称不能空")
    private String fileName;
    /**
     * 文件路径
     */
    @NotEmpty(message = "文件路径不能空")
    private String filePath;
    /**
     * 文件类型
     */
    @NotEmpty(message = "文件类型不能空")
    private String mime;
    /**
     * 文件大小
     */
    @NotNull(message = "文件大小不能空")
    private Long fileSize;
    /**
     * 文件夹ID
     */
    @NotEmpty(message = "所属文件夹id不可空")
    private String folderId;
    /**
     * MD5
     */
    @NotEmpty(message = "文件MD5值不可空")
    private String md5;
    /**
     * SHA1
     */
    @NotEmpty(message = "文件sha1不能空")
    private String sha1;
    /**
     * 备注
     */
    private String remark;

}
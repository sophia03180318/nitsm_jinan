package com.jcca.admin.system.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 版本管理添加请求
 *
 * @author lyp
 */
@Data
public class VersionManagerSaveReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "版本号不可空")
    private String versionNum;
    @NotEmpty(message = "文件ID不存在")
    private String sysFileId;
    @NotEmpty(message = "文件路径不存在")
    private String filePath;

    private String descStr;

    private String remark;


}

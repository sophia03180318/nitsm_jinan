package com.jcca.web.handbook.controller.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 新增文件夹
 *
 * @author lyp
 */
@Data
public class AddFolderReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = false, hidden = true)
    private String id;
    /**
     * 名称
     */
    @NotEmpty(message = "请输入文件夹名称")
    @Length(max = 64, message = "文件夹名字不能超过60个字符")
    private String title;

    /**
     * 父级ID
     */
    @NotEmpty(message = "请选择新文件夹位置")
    private String pid;

    /**
     * 备注信息
     * 关联机柜的话此处填入机柜ID
     */
    private String remark;
}

package com.jcca.web.handbook.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件夹
 *
 * @author lyp
 */
@Data
public class FolderVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件夹id
     */
    private String id;
    /**
     * 文件夹父级id
     */
    private String pid;
    /**
     * 文件夹标题
     */
    private String title;

    /**
     * 文件夹位置
     */
    private String index;
    private String remark;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;

}

package com.jcca.web.broken.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 故障记录删除附件
 *
 * @author lyp
 */

@Data
public class BrokenRecordRemoveFileReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 文件Id
     */
    @NotEmpty(message = "附件ID不能空")
    private String ids;
    /**
     * 故障记录ID
     */
    @NotEmpty(message = "故障记录ID不能空")
    private String brokenRecordId;


}

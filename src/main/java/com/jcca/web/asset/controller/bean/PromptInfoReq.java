package com.jcca.web.asset.controller.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 新增
 *
 * @author syt
 */
@Data
public class PromptInfoReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 软件类型ID
     */
    private String softwareTypeId;
    /**
     * 软件类型名称
     */
    private String softwareTypeName;
    /**
     * 进程名称,备注
     */
    private List<Map<String, String>> processes;
}

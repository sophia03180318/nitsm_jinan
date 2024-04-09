package com.jcca.web2.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author HanHW
 * @description 进程模板
 * @className ProcessPlateVo
 * @date 2023/11/10 15:04
 * @since 2.1.0.0
 */
@Data
public class ProcessPlateVo {

    // 模板名称
    @NotEmpty(message = "模板名称不能为空")
    private String plateName;

    // 模板ID
    private String plateId;

    // 进程名称列表
    private List<String> processList;

    private String remark;
    /**
     * 所属软件类型ID，BUSINESSSERVICE_SERVICE_TYPE 表ID
     */
    private String softwareTypeId;
}

package com.jcca.admin.system.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 开始更新车站请求参数
 *
 * @author lyp
 */
@Data
public class BeginUpdateReq {

    @NotEmpty(message = "请选择使用的版本")
    private String versionId;

    @NotNull(message = "请输入上传限速大小，必须为整数")
    private Integer rateLimi;

    @NotNull(message = "请输入上传间隔时长，必须为整数")
    private Integer intervalTime;

    private String stationIdListStr;

    private List<String> stationIdList;
}

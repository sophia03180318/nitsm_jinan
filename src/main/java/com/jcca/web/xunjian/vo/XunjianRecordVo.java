package com.jcca.web.xunjian.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName XunjianRecordVo
 * @Description 巡检参数接收
 * @Author wone
 * @Date 2021/2/25 16:36
 * @Version ITSM2.0
 **/
@Data
public class XunjianRecordVo {

    private String operator;

    private String xunjianShift;

    @NotEmpty(message = "至少需要一个巡检指标")
    private String xunjianTarget;
}

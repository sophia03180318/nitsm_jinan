package com.jcca.web.common.vo;

import lombok.Data;

/**
 * @ClassName AssetTestResult
 * @Description 采集指标测试结束
 * @Date 2020/7/31 13:56
 * @Author hanwone
 */
@Data
public class AssetTestResult {

    public static Integer SUCCES_CODE = 0;
    public static Integer ERRO_CODE = 1;
    /**
     * 测试指标结果 0-正常，1-异常
     */
    private Integer state;
    /**
     * 测试指标描述
     */
    private String targetDescription;
    /**
     * 错误消息
     */
    private String errorMsg;
}

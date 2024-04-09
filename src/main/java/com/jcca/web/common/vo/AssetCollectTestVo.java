package com.jcca.web.common.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AssetCollectTestVo
 * @Description 添加资产前进行资产采集测试后的响应数据
 * @Date 2020/7/31 13:52
 * @Author hanwone
 */
@Data
public class AssetCollectTestVo {

    public static String SUCCES_CODE = "0";
    public static String ERRO_CODE = "1";

    /**
     * 响应码 0-成功，1-失败
     */
    private String code;
    /**
     * 错误信息
     */
    private String msg;
    /**
     * 指标测试列表
     */
    private List<AssetTestResult> testResultList;

}

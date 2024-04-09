package com.jcca.web.asset.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>Description: </p>
 *
 * @author 苏一拓
 * @date 2021/5/12/012  17:58
 * @classname nitsmcom.jcca.web.asset.voCabinetUsedVo
 */
@Data
public class CabinetUsedVo {
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
    private List<?> resultList;
}

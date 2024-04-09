package com.jcca.web.asset.service.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssetTelnetException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String FORMAT_ERROR = "FORMAT_ERROR";
    public static final String SCOPE_ERROR = "SCOPE_ERROR";
    public static final String ASSET_ERROR = "ASSET_ERROR";


    private String telnetErrorCode;

    private String telnetErrorMsg;

    /**
     * 资产测试结果
     */
//	private List<AssetTestResult> testResultList;
    public AssetTelnetException(String telnetErrorCode, String telnetErrorMsg) {
        super();
        this.telnetErrorCode = telnetErrorCode;
        this.telnetErrorMsg = telnetErrorMsg;
    }


}

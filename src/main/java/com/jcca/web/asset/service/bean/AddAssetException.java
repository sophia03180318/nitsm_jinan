package com.jcca.web.asset.service.bean;

import com.jcca.web.common.vo.AssetTestResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddAssetException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String VERIFY_ERROR = "VERIFY_ERROR";
    public static final String COLLECT_ERROR = "COLLECT_ERROR";


    private String addErrorcode;

    private String addErrorMsg;
    /**
     * 资产测试结果
     */
    private List<AssetTestResult> testResultList;

    public AddAssetException(String addErrorcode, String addErrorMsg, List<AssetTestResult> testResultList) {
        super();
        this.addErrorcode = addErrorcode;
        this.addErrorMsg = addErrorMsg;
        this.testResultList = testResultList;
    }

    @Override
    public String toString() {
        return "AddAssetException: " +
                "addErrorcode='" + addErrorcode+", addErrorMsg='" + addErrorMsg;
    }
}

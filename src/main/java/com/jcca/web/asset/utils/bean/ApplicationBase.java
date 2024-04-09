package com.jcca.web.asset.utils.bean;

import lombok.Data;

import java.util.List;

/**
 * 应用基础信息
 *
 * @author Lvyp
 */
@Data
public class ApplicationBase {

    /**
     * 应用名称
     */
    private String name;
    /**
     * 应用版本记录
     */
    private List<ApplicationVersionBase> versionList;

}

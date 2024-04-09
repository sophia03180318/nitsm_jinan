package com.jcca.web.common.controller.bean;

import lombok.Data;

import java.util.List;

/**
 * 车站需要更新的jar的信息
 *
 * @author lyp
 */
@Data
public class StationJarQuertResp {

    /**
     * jar所在车站存储地址
     */
    private List<String> jarPathList;

}

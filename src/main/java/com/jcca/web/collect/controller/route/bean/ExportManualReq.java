package com.jcca.web.collect.controller.route.bean;

import lombok.Data;

import java.util.List;

/**
 * @author HW
 * @description ExportManualReq
 * @className ExportManualReq
 * @date 2024/12/17 15:45
 * @since 2.1.1.0
 */
@Data
public class ExportManualReq {

    private String orgId;
    private List<String> idList;
}

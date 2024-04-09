package com.jcca.web.common.controller.req;

import lombok.Data;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 19:54 2023/3/15
 * @ Description:
 */
@Data
public class DsErrorLog {
    private static final long serialVersionUID = 1;
    private String assetId;
    private List<EvenLog> logs;
    private String logStr;
}

package com.jcca.web.common.controller.req;

import lombok.Data;

/**
 * @ClassName ProcessOutReq
 * @Description 外部请求资产列表
 * @Date 2021/7/30 16:54
 * @Author syt
 */
@Data
public class ProcessOutReq {

    /**
     * 需求类型 2-获取中心资产，4-获取车站资产
     */
    private Integer type;
}

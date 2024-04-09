package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检数据
 * @className InspectVo
 * @date 2023/11/14 17:25
 * @since 2.1.0.0
 */
@Data
public class InspectVo {


    private String id;
    private String name;
    //指标code码
    private String code;
    private Integer total;
    private Integer count;
    /**
     * 指标选中状态
     * 0未选中，1选中
     */
    private String targetStatus;
    /**
     * 资产选中状态
     * 0未选中，1选中
     */
    private String assetStatus;

    /**
     * 1组织， 2机柜
     */
    private String type;

    /**
     * 1待巡检，2正在巡检，3巡检正常，4巡检异常
     */
    private String state;

    private String inspectCode;

    List<InspectVo> list;
}

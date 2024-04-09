package com.jcca.admin.system.controller.bean;

import lombok.Data;

/**
 * 更新明细
 *
 * @author lyp
 */
@Data
public class StationUpdateDetail {

    private Integer rank;

    private String name;

    private String remark;

    private Boolean showButton;

    private String stationId;

    private String buttonName;

}

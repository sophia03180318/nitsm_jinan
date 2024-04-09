package com.jcca.component.quartz.station.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 车站通知
 *
 * @author lyp
 */
@Data
public class StationNotifyBean implements Serializable {

    private String assetId;

    private Boolean status;

    private String url;

    private String orgId;

    private String flag;


}

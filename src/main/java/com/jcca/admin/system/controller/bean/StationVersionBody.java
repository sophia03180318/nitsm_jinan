package com.jcca.admin.system.controller.bean;

import com.jcca.admin.biz.entity.Station;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 车站升级展示
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StationVersionBody extends Station {

    private static final long serialVersionUID = 1L;

    /**
     * 当前的版本
     */
    private String version;
    /**
     * 正在更新的版本
     */
    private String futureVersion;
    /**
     * 更新状态
     */
    private String status;

    private String statusStr;

    private String filePath;
    /**
     * 更新进度
     */
    private String updateRate;
    /**
     * 开始时间
     */
    private Date startDate;
    /**
     * 结束时间
     */
    private Date endDate;
    /**
     * 更新备注
     */
    private String remark;
    /**
     * 车站采集器访问URL
     */
    private String stationUrl;

}

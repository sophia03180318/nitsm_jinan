package com.jcca.web.xunjian.entity.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 巡检服务详情
 */
@Data
public class XunjianServerDetailBean implements Serializable {

    /**
     * 应用名称
     */
    private String appName;
    /**
     * 设备IP
     */
    private String ip;
    /**
     * 设备型号
     */
    private String assetImage;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 系统版本 缺
     */
    private String sysVersion;
    /**
     * 设备版本号 缺
     */
    private String assetVersion;
    /**
     * 设备安装位置
     */
    private String assetPosition;
    /**
     * 上架时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date onlineTime;
    /**
     * 设备供货商
     */
    private String assetSupplier;
    /**
     * 备注
     */
    private String remark;
    /**
     * 巡检详情
     */
    private List<XunjianDetailV2> detailList;
}

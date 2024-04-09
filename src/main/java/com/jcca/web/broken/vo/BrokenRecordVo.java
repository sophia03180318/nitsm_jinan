package com.jcca.web.broken.vo;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 故障记录
 *
 * @author lyp
 */
@Data
public class BrokenRecordVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 故障记录id
     */
    private String id;
    /**
     * 故障资产id
     */
    private String assetId;
    /**
     * orgId
     */
    private String orgId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产ip
     */
    private String assetIp;
    /**
     * 故障资产的信息
     */
    private List<JSONObject> assetList;
    /**
     * 故障时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;

    /**
     * 故障时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 故障现象
     */
    private String description;
    /**
     * 故障原因
     */
    private String reason;
    /**
     * 状态
     * BrokenRecordConst
     */
    private Byte status;
    /**
     * 状态
     * BrokenRecordConst
     */
    private String statusStr;

    private String alarmTitle;

    private String alarmId;

}

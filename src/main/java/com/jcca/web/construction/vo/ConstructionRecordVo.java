package com.jcca.web.construction.vo;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 项目施工记录
 *
 * @author lyp
 */
@Data
public class ConstructionRecordVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * 施工记录名称
     */
    private String name;
    /**
     * 影响范围
     * 资产id,链接
     */
    private String influence;
    /**
     * 选中资产列表
     */
    private List<JSONObject> influenceMsg;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
     * 结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    /**
     * 施工地点
     */
    private String place;
    /**
     * 施工人
     */
    private String operator;
    /**
     * 配合人
     */
    private String cooperator;
    /**
     * 项目
     */
    private String project;

}

package com.jcca.web.xunjian.entity.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 巡检历史
 */
@Data
public class XunjianLogBean implements Serializable {

    private List<XunjianDetailV2> asset;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dateTime;
    /**
     * 巡检报告ID
     */
    private String xunjianRecordId;
    /**
     * 原始报告下载地址
     */
    private String orgFilePath;

    private String remark;

}

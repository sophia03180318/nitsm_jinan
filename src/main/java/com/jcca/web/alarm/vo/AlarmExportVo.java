package com.jcca.web.alarm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName AlarmExportVo
 * @Description 导出告警字段
 * @Date 2020/6/16 15:20
 * @Author hanwone
 */
@Data
public class AlarmExportVo {
    private String alarmLevel;
    private String title;
    private String assetIp;
    private String assetName;
    private String alarmType;
    private String description;
    private String alarmStatus;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;
    private String status;
    private String confirmor;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmTime;

    private String remark;
}

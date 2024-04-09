package com.jcca.web.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName AssetGeneralInfo
 * @Description 资产简要详情
 * @Date 2020/6/30 16:18
 * @Author hanwone
 */
@Data
public class AssetGeneralInfo {

    private String id;
    private String status;
    private String name;
    private String ip;
    private String assetMode;
    private String assetImage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    private Double cpuUsed;
    private Double diskUsed;
    private Double memUsed;

    private List<AlarmUnconfirmVo> alarmList;
}

package com.jcca.web.asset.vo;

import lombok.Data;

/**
 * @ClassName DetailCabinetVo
 * @Description 机柜详情
 * @Date 2020/6/18 17:12
 * @Author hanwone
 */
@Data
public class DetailCabinetVo {
    private String cabinetId;
    private String cabinetName;
    private String assetId;
    private String assetName;
    private String assetMode;
    private String assetImage;
    private String ip;
    private String ip2;
    private String alarmLevel;
    private String startPosition;
    private String endPosition;
    // 巡检管理用  1待巡检，2正在巡检，3巡检正常，4巡检异常
    private String state;

}

package com.jcca.web.asset.vo;

import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import lombok.Data;

import java.util.List;

/**
 * @ClassName CabinetGeneralVo
 * @Description 机柜简要详情
 * @Date 2020/7/8 17:17
 * @Author hanwone
 */
@Data
public class CabinetGeneralVo {

    /**
     * 机柜ID
     */
    private String cabinetId;
    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 行标
     */
    private Integer rowIndex;
    /**
     * 列标
     */
    private Integer columnIndex;
    /**
     * 机柜内设备告警列表
     */
    private List<AlarmUnconfirmVo> alarmList;
}

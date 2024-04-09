package com.jcca.web.xunjian.controller.util.bean;

import com.jcca.web.xunjian.controller.bean.XunjianRepoBody;
import com.jcca.web.xunjian.entity.XunjianAlarmMsg;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 巡检报告模板
 *
 * @author Lvyp
 */
@Data
public class XunjianReportTemp {

    /**
     * 巡检人
     */
    private String operator = "";
    /**
     * 班次
     */
    private String xunjianShift = "";
    /**
     * 巡检时间
     */
    private Date xunjianTime;
    /**
     * 异常设备数量
     */
    private Integer exceptionNum;
    /**
     * 正常设备数量
     */
    private Integer normalNum;
    /**
     * 巡视内容
     */
    private String xunjianTarget = "";
    /**
     * 服务器数量
     */
    private Integer serverNum;
    /**
     * 调度台总数
     */
    private Integer displayerNum;
    /**
     * 网络设备
     */
    private Integer netServerNum;
    /**
     * 报告明细详情
     */
    private List<XunjianRepoBody> reportList;
    /**
     * 告警列表
     */
    private List<XunjianAlarmMsg> alarmList;
}

package com.jcca.component.event.bean;

import com.jcca.web.asset.entity.Asset;
import lombok.Data;

import java.util.Date;

/**
 * 创建事件请求
 *
 * @author lyp
 */
@Data
public class CreateEventReq {

    /**
     * 事件匹配码
     */
    private String uniqueCode;
    /**
     * 发生事件的资产ID
     */
    private String assetId;
    /**
     * 事件状态
     * EventLevelEnum
     */
    private Integer eventLevel;
    /**
     * 阈值类 设定基础参数
     */
    private String baseValue;
    /**
     * 阈值类 采集到的参数
     */
    private String collectValue;
    /**
     * 原本的信息
     */
    private String originalMsg;
    /**
     * 翻译后的信息
     */
    private String repoMsg;
    /**
     * 特殊标记
     */
    private String flag;
    /**
     * 产生时间
     */
    private Date createTime;
    /**
     * 事件备注信息
     */
    private String remark;

    /**
     * 匹配标识 如果事件之间的flag一样则告警需要验证alarmCode，否则的话不需要
     */
    private String groupFlag;

    private Asset asset;

    /**
     * 产生告警的厂家
     * 业务种类，0中航，1卡斯柯，2通号，3铁科，4北羊，5信达环宇，6从兴
     * BusinessTypeEnums
     */
    private Integer businessType;


    /**
     * 这边记录的是端口别名
     */
    private String processTop5Mem;

}

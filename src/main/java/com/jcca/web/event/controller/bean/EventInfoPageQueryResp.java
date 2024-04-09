package com.jcca.web.event.controller.bean;

import com.jcca.web.event.entity.AlarmEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 事件响应信息
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventInfoPageQueryResp extends AlarmEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 资产名称
     */
    private String assetName;

    private String assetImage;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 事件类型
     */
    private String eventTypeName;
    /**
     * 设备类型
     */
    private Integer assetMode;
    /**
     * 事件性质
     */
    private String eventLevelStr;
    /**
     * 是否显示转为知识库的按钮
     * false 修改
     * true  新增
     */
    private Boolean showSyslogButton;

    /**
     * 收到的事件标识
     */
    private String uniqueCode;

}

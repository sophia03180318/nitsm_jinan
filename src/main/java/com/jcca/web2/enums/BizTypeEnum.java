package com.jcca.web2.enums;

import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import lombok.Getter;

/**
 * @author HanHW
 * @description 综合类别
 * @className BizTypeEnum
 * @date 2023/11/9 10:27
 * @since 2.1.0.0
 */
@Getter
public enum BizTypeEnum {


    CTC(StatusInfoChangeTypeEnum.event_CTC.getCode(), "CTC业务"),

    NETWROK_SECURITY(StatusInfoChangeTypeEnum.event_xdhy.getCode(), "网络安全"),

    COMMUNICATION(StatusInfoChangeTypeEnum.event_linkQuality.getCode(), "通信质量"),

    POWER(StatusInfoChangeTypeEnum.event_power.getCode(), "电源状态"),

    SURROUNDING(StatusInfoChangeTypeEnum.event_environment.getCode(), "动环状态"),
    /**
     * 中航
     */
    JCCA("JCCA", "综合运维");

    public String code;
    public String description;

    BizTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}

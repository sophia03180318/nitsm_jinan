package com.jcca.web2.enums;

import lombok.Getter;

/**
 * @author HanHW
 * @description 业务服务枚举
 * <p>
 * 在 BROKER_TOPO_BUSINESS 表中类型 SERVICE_TYPE 用数字表示，其中车站数据 TYPE 用字母表示
 * @className ServiceTypeEnum
 * @date 2023/10/27 15:50
 * @since 2.1.0.0
 */
@Getter
public enum ServiceTypeEnum {

    COMM_SERVER("0", "COMMServer"),
    DIVIDING_SERVER("1", "分界通讯接口服务器"),
    APP_SERVER("2", "应用服务器"),
    TSRS_SERVER("3", "TSRS接口服务器"),
    TDMS_SERVER("4", "TDMS接口服务器"),
    TZ_SERVER("5", "铁路总公司"),
    RBC_SERVER("6", "RBC接口服务器"),
    MESSAGE_SERVER("7", "通信前置机"),
    STATION("8", "车站"),
    DISPATCH_SERVER("9", "调度命令"),
    OPERATION_SERVER("10", "运行图"),
    STATION_YARD("11", "站场图"),
    GSMR_SERVER("12", "GSMR接口服务器"),
    DIAODU_TAI("13", "调度台"),
    BAOBU_SERVER("14", "报部接口服务器"),
    TDI_SERVER("15", "TD接口服务器"),

    // 以下为车站细分类型
    ZD("ZD", "车务终端"),
    ZL("ZL", "自律机"),
    ZBY("ZBY", "值班员"),
    XHY("XHY", "信号员"),
    WHJ("WHJ", "维护机"),
    DW("DW", "电务终端"),
    DEFAULT("DEFAULT", "终端"),

    ;

    String serviceTypeCode;

    String serviceTypeName;

    ServiceTypeEnum(String serviceTypeCode, String serviceTypeName) {
        this.serviceTypeCode = serviceTypeCode;
        this.serviceTypeName = serviceTypeName;
    }
}

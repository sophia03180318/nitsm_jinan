package com.jcca.common.enums;

import com.jcca.common.bean.constant.OrgTypeConst;
import lombok.Getter;

/**
 * @ClassName OrgTypeEnum
 * @Description 组织类型
 * @Date 2020/5/27 11:02
 * @Author hanwone
 */
@Getter
public enum OrgTypeEnum {
    ALL((byte) 5, "全部"),
    PARENT(OrgTypeConst.PARENT, "铁路总公司"),
    GROUP(OrgTypeConst.GROUP, "铁路局"),
    CENTER(OrgTypeConst.CENTER, "中心"),
    LINE(OrgTypeConst.LINE, "线路"),
    STATION(OrgTypeConst.STATION, "车站");

    private byte code;

    private String title;

    OrgTypeEnum(Byte code, String title) {
        this.code = code;
        this.title = title;
    }

    public static String getTitleByCode(byte code) {
        OrgTypeEnum[] values = OrgTypeEnum.values();
        for (OrgTypeEnum item : values) {
            if (item.getCode() == code) {
                return item.getTitle();
            }
        }
        return code + "";
    }
}

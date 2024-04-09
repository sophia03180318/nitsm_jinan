package com.jcca.common.enums;

import com.jcca.common.bean.constant.BrokenRecordConst;
import lombok.Getter;

/**
 * @ClassName BrokenRecordEnum
 * @Description 故障记录状态
 * @Date 2020/6/29 15:39
 * @Author hanwone
 */
@Getter
public enum BrokenRecordEnum {

    UNPROCESSED(BrokenRecordConst.UNPROCESSED, "未处理"),
    PROCESSED(BrokenRecordConst.PROCESSED, "已处理");

    byte code;
    String name;

    BrokenRecordEnum(Byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(Byte code) {
        BrokenRecordEnum[] recordEnums = BrokenRecordEnum.values();
        for (BrokenRecordEnum recordEnum : recordEnums) {
            if (code == recordEnum.getCode()) {
                return recordEnum.getName();
            }
        }
        return "UNKNOW";
    }
}

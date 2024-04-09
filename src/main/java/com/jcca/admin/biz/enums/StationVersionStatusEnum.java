package com.jcca.admin.biz.enums;

import lombok.Getter;

/**
 * 版本升级过程中的状态
 *
 * @author lyp
 */
@Getter
public enum StationVersionStatusEnum {

    /**
     * 排队等待上传JAR
     */
    AWAIT_UPLOADING("排队等待上传"),
    /**
     * 上传JAR中
     */
    UPLOADING("上传JAR中"),
    /**
     * 上传完成
     */
    UPLOAD_OK("上传完成"),
    /**
     * 上传失败
     */
    UPLOAD_FAIL("上传失败"),
    /**
     * 更新中
     */
    UPDATEING("更新中"),
    /**
     * 更新失败
     */
    UPDATE_FAIL("更新失败"),
    /**
     * 更新完成
     */
    UPDATE_SUCCESS("更新完成");

    private String msg;

    private StationVersionStatusEnum(String msg) {
        this.msg = msg;
    }

    public static String getMsgByName(String name) {
        StationVersionStatusEnum[] values = StationVersionStatusEnum.values();
        for (StationVersionStatusEnum item : values) {
            if (item.name().equals(name)) {
                return item.getMsg();
            }
        }

        return name;
    }

}

package com.jcca.common.enums;

import lombok.Getter;

/**
 * 文件夹类别
 *
 * @author lyp
 */
@Getter
public enum FolderCategoryEnum {

    CLOUD_DISK((byte) 2, "共享云盘"),
    MAINTAIN_HAND_BOOK((byte) 1, "维护手册");

    private Byte code;

    private String message;

    FolderCategoryEnum(Byte code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String findMsgByCode(String code) {
        FolderCategoryEnum[] values = FolderCategoryEnum.values();
        for (FolderCategoryEnum categoryEnum : values) {
            if (categoryEnum.getCode().equals(code)) {
                return categoryEnum.getMessage();
            }
        }
        return code;
    }


}

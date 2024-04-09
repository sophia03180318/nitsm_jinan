package com.jcca.web.collect.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 网卡状态
 *
 * @author Lvyp
 */
@Getter
public enum CollectNetCardStatus {

    /**
     * 未知
     */
    UNKONW((byte) 0,"未知"),
    /**
     * 通
     */
    UP((byte) 1,"通"),
    /**
     * 断
     */
    DOWN((byte) 2,"断");

    private Byte code;
    private String msg;

    private CollectNetCardStatus(Byte code,String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(Byte status) {
        if(Objects.isNull(status)){
            return "";
        }
        CollectNetCardStatus[] values = CollectNetCardStatus.values();
        for (CollectNetCardStatus value : values) {
            if(value.getCode()==status){
                return value.getMsg();
            }
        }

        return status.toString();
    }
}

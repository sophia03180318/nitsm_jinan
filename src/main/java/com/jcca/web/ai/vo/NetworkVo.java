package com.jcca.web.ai.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 内存信息
 */
@Data
public class NetworkVo implements Serializable {


    private Date collectDate;

    private String name;

    private Byte status;

    private String statusStr;

    private String macAddress;

    private String ip;


    @Override
    public String toString() {
        if (status == 1) {
            statusStr = "通";
        } else {
            statusStr = "断";
        }
        return "{网卡名称=" + name +
                ",网卡状态'" + statusStr +
                ",IP地址'" + ip +
                ",MAC地址'" + macAddress +
                ",网卡采集时间'" + collectDate +
                '}';
    }
}

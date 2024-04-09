package com.jcca.web.alarm.controller.bean;

import com.jcca.web.collect.entity.CollectPort;
import lombok.Data;

@Data
public class CollectPortResp extends CollectPort {

    private String tcp;
    private String udp;
    private Double portNumDouble;

}

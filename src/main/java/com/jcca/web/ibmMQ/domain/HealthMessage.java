package com.jcca.web.ibmMQ.domain;


import com.jcca.web.ibmMQ.vo.HealthState;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class HealthMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;

    private String connection;


    private String monitor;

    private HealthState level;

    private String rule;


    private String result;
    private Date time;

    private Boolean flag;//true为正常，false为异常

}
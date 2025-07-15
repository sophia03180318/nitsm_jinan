package com.jcca.web.ai.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * CPU数据
 */
@Data
public class ResultResp implements Serializable {


    private String value;
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private Date collectTime;


}

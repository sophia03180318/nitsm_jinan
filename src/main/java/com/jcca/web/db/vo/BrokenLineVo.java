package com.jcca.web.db.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 折线数据
 *
 * @author Lvyp
 */
@Data
public class BrokenLineVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * x轴
     */
    private String x;
    /**
     * y轴
     */
    private Object y;

}

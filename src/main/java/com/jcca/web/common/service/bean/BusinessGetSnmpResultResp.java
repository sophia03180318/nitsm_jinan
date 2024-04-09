package com.jcca.web.common.service.bean;


import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.Data;

import java.util.List;

/**
 * 获取进程相应信息
 *
 * @author lyp
 */
@Data
public class BusinessGetSnmpResultResp {

    public static final String SUNNCESS = "0";
    public static final String ERROR = "-1";

    /**
     * 响应码
     */
    private String code;
    /**
     * 响应消息
     */
    private String msg;
    /**
     * 相应结果
     */
    private List<SnmpExecuteResult> resultList;

}

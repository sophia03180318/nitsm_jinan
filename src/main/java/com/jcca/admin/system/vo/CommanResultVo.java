package com.jcca.admin.system.vo;

import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 13:58 2021/8/25
 * @ Description:
 */
@Data
public class CommanResultVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String comman;
    private List<SnmpExecuteResult> result;
    private String msg;
}

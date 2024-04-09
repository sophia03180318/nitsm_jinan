package com.jcca.web.topo.service.bean;

import lombok.Data;

/**
 * snmp执行结果
 *
 * @author Lvyp
 */
@Data
public class SnmpExecuteResult {

    private String oid = "";

    private String value = "";

}

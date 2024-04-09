package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.MQException;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("PING_QMGR")
public class PingQMgrCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(PingQMgrCommand.class);
    private PCFMessage inquireQMgrRequest;


    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        Assert.notNull(connection);
        if (this.inquireQMgrRequest == null) {
            this.inquireQMgrRequest = PCFMessageFactory.createInquireQMgr();
        }
        try {
            sendRequestWithoutCheck(connection, this.inquireQMgrRequest, -1);
            return Integer.valueOf(0);
        } catch (Exception e) {
            if (e instanceof MQException) {
                MQException mqe = (MQException) e;
                if (((MQException) e).getReason() == 2067) {
                    this.inquireQMgrRequest = PCFMessageFactory.createInquireQMgrV7();
                    return execute(context);
                }
                log.warn("Reason Code: {}({}), QMGR is probably not running or network unavailable!", Integer.valueOf(mqe.getReason()),
                        MQConstants.lookupReasonCode(mqe.getReason()));
                return Integer.valueOf(mqe.getReason());
            }
            throw e;
        }
    }
}
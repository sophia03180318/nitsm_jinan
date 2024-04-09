package com.jcca.web.ibmMQ.command.impl;

import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;

import java.util.Map;


@Command("INQUIRE_QMGR_NAME")
public class InquireQMgrNameCommand
        extends InquireDataCommand {
    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        return this.messageService.getQueueManagerName(connection, false);
    }
}

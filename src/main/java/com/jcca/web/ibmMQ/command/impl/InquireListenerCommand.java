package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.ListenerData;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*    */
/*    */
/*    */
/*    */
/*    */
/*    */


@Command("INQUIRE_LISTENER")
public class InquireListenerCommand extends InquireDataCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireListenerCommand.class);
    private PCFMessage listenerStatusRequest;
    private PCFMessage listenerRequest;
    private int timeout;

    public Object execute(Map<String, Object> context) throws Exception {
        Monitor monitor = getMonitor(context);
        if (this.listenerStatusRequest == null) {
            this.listenerStatusRequest = PCFMessageFactory.createInquireListenerStatus(monitor.getObjectName());
            this.listenerRequest = PCFMessageFactory.createInquireListener(monitor.getObjectName());
            this.timeout = getTimeout(monitor);
        }
        List<StatisticalData> dataList = new ArrayList<StatisticalData>();
        try {
            PCFMessage[] responses = sendRequest(monitor.getConnection(), this.listenerStatusRequest, this.timeout,
                    isAutoConnect(context));
            if (log.isDebugEnabled()) {
                log.debug("Inquire listener status responses size: {}", Integer.valueOf(responses.length));
            }
            for (PCFMessage response : responses) {
                StatisticalData data = handleResponse(monitor, response, context);
                dataList.add(data);
            }
        } catch (PCFException e) {
            if (e.getReason() == 3250 || e.getReason() == 2085) {
                log.warn("Run MQCMD_INQUIRE_LISTENER_STATUS failed with '{}', probably means the listener is stopped, trying MQCMD_INQUIRE_LISTENER to get data!",
                        MQConstants.lookupReasonCode(e.getReason()));
                PCFMessage[] responses = sendRequest(monitor.getConnection(), this.listenerRequest, this.timeout,
                        isAutoConnect(context));
                if (log.isDebugEnabled()) {
                    log.debug("Inquire listener responses size: {}", Integer.valueOf(responses.length));
                }
                final ListenerData.ListenerStatus listenerStatus = ListenerData.ListenerStatus.Stopped;
                this.setHealthEvaluationContext(context, new HashMap<String, Object>(1) {
                    {
                        this.put("listenerStatus", listenerStatus);
                    }
                });
                StatisticalData data = handleResponse(monitor, responses[0], context);
                ListenerData listenerData = data.adapt(ListenerData.class);
                listenerData.setListenerStatus(ListenerData.ListenerStatus.Stopped);
                dataList.add(data);
            } else {
                throw e;
            }
        }
        return dataList;
    }
}



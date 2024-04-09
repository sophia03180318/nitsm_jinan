package com.jcca.web.ibmMQ.common;


import com.google.common.collect.Lists;

import java.util.List;
import java.util.Locale;


public class LocalizedMonitoringException extends MonitoringException {
    private static final long serialVersionUID = 1L;
    private String messageId;
    private Object[] bindings;


    public LocalizedMonitoringException(Throwable cause) {
        super(cause);
    }

    public LocalizedMonitoringException(String messageId, Object... bindings) {
        this(0, messageId, bindings);
    }

    public LocalizedMonitoringException(int errorCode, String messageId) {
        this(errorCode, messageId, null, null);
    }

    public LocalizedMonitoringException(String messageId, Throwable cause) {
        this(0, messageId, cause);
    }

    public LocalizedMonitoringException(int errorCode, Throwable cause) {
        this(errorCode, null, cause);
    }

    public LocalizedMonitoringException(int errorCode, String messageId, Throwable cause) {
        this(errorCode, messageId, null, cause);
    }

    public LocalizedMonitoringException(int errorCode, String messageId, Object[] bindings) {
        this(errorCode, messageId, bindings, null);
    }

    public LocalizedMonitoringException(String messageId, Object[] bindings, Throwable cause) {
        this(0, messageId, bindings, cause);
    }

    public LocalizedMonitoringException(int errorCode, String messageId, Object[] bindings, Throwable cause) {
        super(errorCode, (messageId != null) ? ErrorConstants.Message.binds(messageId, (bindings == null) ? new Object[0] : bindings) : null, cause);
        this.messageId = messageId;
        this.bindings = bindings;
    }

    public String getLocalizedMessage(Locale locale) {
        List<Object> finalBindings = Lists.newArrayList();
        if (this.bindings != null) {
            for (Object binding : this.bindings) {
                if (binding instanceof LocalizedMonitoringException) {
                    finalBindings.add(((LocalizedMonitoringException) binding).getLocalizedMessage(locale));
                } else if (binding instanceof Throwable) {
                    finalBindings.add(((Throwable) binding).getLocalizedMessage());
                } else {
                    finalBindings.add(binding);
                }
            }
        }
        if (this.messageId != null) {
            return ErrorConstants.Message.binds(locale, this.messageId, finalBindings.toArray());
        }
        Throwable ex = getCause();
        if (ex instanceof LocalizedMonitoringException) {
            return ((LocalizedMonitoringException) ex).getLocalizedMessage(locale);
        }
        return ex.getLocalizedMessage();
    }
}


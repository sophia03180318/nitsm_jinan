package com.jcca.web2.service.xunjian;


import com.jcca.web2.dto.xunjian.InspectSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author lifp
 * @version 1.0
 * @description: 会话管理器
 * @date 2025-12-04 星期四 13:57:36
 */
@Component
public class InspectSessionManager {

    private final Map<String, InspectSession> sessions = new ConcurrentHashMap<>();

    public InspectSession getOrCreateSession(String inspectRecordId, Supplier<InspectSession> creator) {
        return sessions.computeIfAbsent(inspectRecordId, k -> creator.get());
    }

    public void removeSession(String inspectRecordId) {
        sessions.remove(inspectRecordId);
    }

    public InspectSession getSession(String inspectRecordId) {
        return sessions.get(inspectRecordId);
    }
}

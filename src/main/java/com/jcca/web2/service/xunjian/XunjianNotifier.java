package com.jcca.web2.service.xunjian;


import com.jcca.web2.dto.xunjian.InspectBaseDataWsVo;
import com.jcca.web2.service.impl.XunJianSocketService;
import org.springframework.stereotype.Component;

/**
 * @author lifp
 * @version 1.0
 * @description: WebSocket 推送器
 * @date 2025-12-04 星期四 13:59:42
 */
@Component
public class XunjianNotifier {

    private final XunJianSocketService socketService;

    public XunjianNotifier(XunJianSocketService socketService) {
        this.socketService = socketService;
    }

    /**
     * 统一推送完整快照
     */
    public void sendSnapshot(InspectBaseDataWsVo snapshot, String operator) {
        socketService.sendWsMsgV2(snapshot, operator);
    }
}
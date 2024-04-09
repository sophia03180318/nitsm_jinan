package com.jcca.component.event;

import com.jcca.component.event.bean.AddEventQueueBean;
import com.jcca.component.event.bean.CreateEventReq;

/**
 * 事件逻辑处理
 *
 * @author lyp
 */
public interface EventLogicService {

    /**
     * 将事件分发到队列中添加
     *
     * @param req
     * @throws Exception
     */
    void addEvent(CreateEventReq req) throws Exception;

    /**
     * 添加事件列表--处理队列中的待添加事件
     *
     * @param Eventreq
     */
    void addEventQueue(AddEventQueueBean Eventreq);

    /**
     * v2重构版
     * 直接发送告警或恢复信息
     * 调用这个方法则直接触发告警或恢复 不受知识库配置的级别影响
     * 经过这段时间的积累分析，事件应该分为两种：一种是系统产生的，明确
     * 知道正常异常的，应该遵循系统的判定，不应该被修改。一种是判定不了的
     * 则需要依赖知识库二次判定
     * @return
     */
    boolean sendAlarmOrRecoverEventV2(CreateEventReq req);


}

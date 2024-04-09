package com.jcca.web.event.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.component.event.bean.AddEventItem;
import com.jcca.web.alarm.controller.bean.AddAlarmRepoReq;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web2.dto.EventPageDto;
import com.jcca.web2.vo.EventPageVo;

import java.util.List;

/**
 * 告警事件
 *
 * @author lyp
 */
public interface AlarmEventService extends IService<AlarmEvent> {

    /**
     * 查询资产该类型明细的最后一次事件信息
     *
     * @param assetId
     * @param uniqueCode
     * @param flag       特殊标记
     * @return
     */
    AlarmEvent getLastLog(AddEventItem req, String assetId, String flag, String uniqueCode);

    /**
     * 查询资产该类型明细的最后一次事件信息
     *
     * @param req
     * @param assetId
     * @param flag       特殊标记
     * @return
     */
    AlarmEvent getTypeLastLog(AddEventItem req, String assetId, String flag);


    /**
     * 查询该类型 最后一次在事件表中存入的事件状态
     * <p>
     * 恢复事件\空  --true -- 事件正常
     * 异常事件\通知  --false -- 事件异常
     *
     * @param assetId
     * @param typeId
     * @return
     */
    Boolean getEventStatus(String assetId, String typeId, String flag, AlarmEvent orgEvent);

    /**
     * 删除相关事件
     *
     * @param assetId
     */
    void removeByAssetId(String assetId);

    /**
     * 判定上来的事件是否是同一类事件
     *
     * @param asList
     * @return
     */
    Boolean groupMatchFlagByCode(List<String> asList);

    /**
     * 获取缓存KEY
     *
     * @param assetid
     * @param typeId
     * @param flag
     * @param repositoryId
     * @return
     */
    String getCacheKey(String assetid, String typeId, String flag, String uniqueCode, String repositoryId);

    /**
     * 清除已经转为已知类型的未知事件
     *
     * @param knowledge
     */
    void removeUnkonwEvent(AlarmRepository knowledge) throws Exception;

    /**
     * 查询最后一次PING告警信息
     *
     * @param assetId
     * @return
     */
    AlarmEvent getLastPingLog(String assetId);

    /**
     * 编辑知识库处理事件
     *
     * @param req
     * @throws Exception
     */
    void updateKnowledge(AddAlarmRepoReq req) throws Exception;

    /**
     * 更新位置的告警
     *
     * @param knowledge
     * @throws Exception
     */
    void updateUnkonwEvent(AlarmRepository knowledge) throws Exception;

    /**
     * 查询事件列表通过告警ID
     *
     * @param id
     * @return
     */
    List<AlarmEvent> selectAllEventByAlarmId(String id);

    /**
     * 查看syslog日志
     *
     * @param assetId
     * @param start
     * @param end
     * @return
     */
    List<AlarmEvent> listSyslog(String assetId, Integer start, Integer end);

    /**
     * 分页查询事件
     *
     * @param query
     * @return
     */
    IPage<EventPageVo> pageEventListV2(EventPageDto query);
}

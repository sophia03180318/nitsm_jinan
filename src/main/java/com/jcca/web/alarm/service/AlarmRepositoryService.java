package com.jcca.web.alarm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.exception.common.VerifyException;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web2.dto.EventRpoPageDto;

import java.util.List;

/**
 * 告警知识库
 *
 * @author Lvyp
 */
public interface AlarmRepositoryService extends IService<AlarmRepository> {

    /**
     * 告警知识库新增记录
     *
     * @param copy
     * @return
     */
    ResultVo<AlarmRepository> create(AlarmRepository copy);

    /**
     * 查询所有关联该类型的知识库
     *
     * @param eventTypeId
     * @return
     */
    List<AlarmRepository> getAllByEventId(String eventTypeId);

    /**
     * 通过知识库匹配码查询
     *
     * @param alarmCode
     * @return
     */
    List<AlarmRepository> getAllByAlarmCode(String alarmCode);

    /**
     * 判定此告警是否可恢复
     * 如果配置有恢复规则则可以恢复，
     * 如果未配置恢复规则则不可恢复
     *
     * @param alarmCode
     * @return false 不可恢复  true可恢复
     */
    boolean canRecoverV2(String alarmCode);

    /**
     * 保存新的规则并把符合条件的事件从未知移出来
     *
     * @param copy
     */
    String saveV2(AlarmRepository copy);

    /**
     * 分页查询告警规则配置
     *
     * @param query
     */
    IPage<AlarmRepository> pageListV2(EventRpoPageDto query);

    /**
     * 删除规则库
     * 会恢复规则下面的产生的告警 删除规则所属的白名单
     *
     * @param repoId
     * @throws VerifyException
     */
    void removeByIdV2(String repoId) throws VerifyException;

    /**
     * 更新
     *
     * @param copy
     */
    void updateByIdV2(AlarmRepository copy) throws VerifyException;
}

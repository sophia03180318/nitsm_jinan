package com.jcca.web.event.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web2.dto.EventPageDto;
import com.jcca.web2.vo.EventPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 事件
 *
 * @author lyp
 */
@Mapper
public interface AlarmEventMapper extends BaseMapper<AlarmEvent> {

    /**
     * 查询该事件明细最后一次告警
     *
     * @param assetId
     * @param typeId
     * @param flag
     * @param uniqueCode
     * @return
     */
    AlarmEvent selectLastLog(@Param("assetId") String assetId, @Param("typeId") String typeId,
                             @Param("flag") String flag, @Param("uniqueCode") String uniqueCode);

    /**
     * 查询该事件最后一次告警
     *
     * @param assetId
     * @param typeId
     * @param flag
     * @return
     */
    AlarmEvent selectTypeLastLog(@Param("assetId") String assetId, @Param("typeId") String typeId,
                                 @Param("flag") String flag);

    /**
     * 查询该事件明细最后一次告警
     *
     * @param assetId
     * @param typeId
     * @param uniqueCode
     * @return
     */
    AlarmEvent selectLastLogExceptFlag(@Param("assetId") String assetId, @Param("typeId") String typeId,
                                       @Param("uniqueCode") String uniqueCode);

    /**
     * 查询该事件最后一次告警
     *
     * @param assetId
     * @param typeId
     * @return
     */
    AlarmEvent selectTypeLastLogExceptFlag(@Param("assetId") String assetId, @Param("typeId") String typeId);

    List<String> groupMatchFlagByCode(@Param("codeList") List<String> codeList);

    AlarmEvent getLastPingLog(@Param("assetId") String assetId);

    /**
     * 批量更新时间类型ID
     *
     * @return
     */
    int batchUpdateTypeById(@Param("eventTypeId") String eventTypeId, @Param("alarmRepoId") String alarmRepoId);

    /**
     * 通过告警ID查询下属所有的事件信息
     *
     * @param alarmId
     * @return
     */
    List<AlarmEvent> selectAllEventByAlarmId(@Param("alarmId") String alarmId);

    List<AlarmEvent> listSyslog(String assetId, Integer start, Integer end);

    /**
     * 分页查询事件列表
     *
     * @param page
     * @param query
     * @return
     */
    IPage<EventPageVo> pageEventListV2(Page page, @Param("query") EventPageDto query);

    /**
     * 查询记录在表中白名单数量
     *
     * @param eventId
     * @return
     */
    Integer getWhiteSize(String eventId);

    /**
     * 更新
     *
     * @param eventTypeId
     * @param repositoryId
     * @param uniqueCode
     */
    void updateInfoByUniqueCode(@Param("eventTypeId") String eventTypeId, @Param("repositoryId") String repositoryId, @Param("uniqueCode") String uniqueCode);
}

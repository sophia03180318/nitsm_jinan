package com.jcca.web.alarm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web2.dto.EventRpoPageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 告警知识库
 *
 * @author Lvyp
 */
@Mapper
public interface AlarmRepositoryMapper extends BaseMapper<AlarmRepository> {

    /**
     * 查询知识库
     *
     * @param uniqueCode
     * @return
     */
    @Select("SELECT * FROM ALARM_REPOSITORY WHERE ALARM_CODE = #{uniqueCode}")
    List<AlarmRepository> selectListByAlarmCode(@Param("uniqueCode") String uniqueCode);

    /**
     * 查询原始告警信息命中的所有知识库
     *
     * @param uniqueCode
     * @param originalMsg
     * @return
     */
    @Select("select * from ALARM_REPOSITORY where ALARM_CODE = #{uniqueCode} and  regexp_like(#{originalMsg},STATUS_FLAG)")
    List<AlarmRepository> selectOrgMsgContainStatusFlg(@Param("uniqueCode") String uniqueCode, @Param("originalMsg") String originalMsg);

    /**
     * 分页查询规则配置
     *
     * @param page
     * @param query
     * @return
     */
    IPage<AlarmRepository> pageListV2(Page page, @Param("query") EventRpoPageDto query);


    /**
     * 查询告警对应的知识库名称
     * @param alarmId
     * @return
     */
    List<String> selectNameByAlarmId(@Param("alarmId") String alarmId);
}

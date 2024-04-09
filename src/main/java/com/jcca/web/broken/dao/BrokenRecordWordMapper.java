package com.jcca.web.broken.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.broken.controller.bean.BrokenRecordWord;
import com.jcca.web2.vo.DialogsAlarmListVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hanwone
 * @date 2021-03-16 09:37:52
 **/
public interface BrokenRecordWordMapper extends BaseMapper<BrokenRecordWord> {

    List<BrokenRecordWord> getBrokenRecordWords(AlarmInfoPageQuery query);

    /**
     * 查询
     *
     * @param query
     * @return
     */
    List<BrokenRecordWord> getBrokenRecordWordsV2(AlarmInfoPageQuery query);

    List<DialogsAlarmListVo> listByAlarmId(@Param("alarmId") String alarmId);
}

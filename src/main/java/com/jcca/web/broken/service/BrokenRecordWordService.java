package com.jcca.web.broken.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.broken.controller.bean.BrokenRecordWord;

import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2021-03-16 09:37:52
 **/
public interface BrokenRecordWordService extends IService<BrokenRecordWord> {

    List<BrokenRecordWord> getBrokenRecordWords(AlarmInfoPageQuery query);

    /**
     * 获取结果集
     *
     * @param query
     * @param list
     * @return
     * @author lvyp
     */
    Map<String, Object> getExportWordMapV2(AlarmInfoPageQuery query, List<BrokenRecordWord> list);

    /**
     * 查询符合条件的记录V2
     *
     * @param query
     * @return
     */
    List<BrokenRecordWord> getBrokenRecordWordsV2(AlarmInfoPageQuery query);
}

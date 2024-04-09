package com.jcca.web.broken.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.alarm.entity.Option;
import com.jcca.web.alarm.entity.Record;
import com.jcca.web.broken.controller.bean.BrokenRecordWord;
import com.jcca.web.broken.dao.BrokenRecordWordMapper;
import com.jcca.web.broken.entity.BrokenRecordOpinion;
import com.jcca.web.broken.service.BrokenRecordWordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author yu_chen
 * @date 2021-03-16 09:37
 **/
@Service
public class BrokenRecordWordServiceImpl extends ServiceImpl<BrokenRecordWordMapper, BrokenRecordWord> implements BrokenRecordWordService {
    @Resource
    private BrokenRecordWordMapper brokenRecordWordMapper;

    @Override
    public List<BrokenRecordWord> getBrokenRecordWords(AlarmInfoPageQuery query) {
        return brokenRecordWordMapper.getBrokenRecordWords(query);
    }

    @Override
    public Map<String, Object> getExportWordMapV2(AlarmInfoPageQuery query, List<BrokenRecordWord> list) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        // 未确认
        List<Record> unconfirmList = new ArrayList<>();
        // 已解决
        List<Record> solveList = new ArrayList<>();
        // 未解决
        List<Record> unSolveList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            BrokenRecordWord brokenRecordWord = list.get(i);
            if (Objects.isNull(brokenRecordWord)) {
                continue;
            }
            Record record = new Record();

            record.setContent(brokenRecordWord.getContent().replace("<", "*").replace(">", "*"));
            // 告警级别syt
            record.setLevel(list.get(i).getAlarmLevel() + "级告警");
            record.setAlarmCategory(list.get(i).getTitle());
            List<Option> options = new ArrayList<>();
            if (brokenRecordWord.getBrokenRecordOpinions() != null
                    && brokenRecordWord.getBrokenRecordOpinions().size() > 0) {
                for (int j = 0; j < brokenRecordWord.getBrokenRecordOpinions().size(); j++) {
                    Option option = new Option();
                    BrokenRecordOpinion brokenRecordOpinion = brokenRecordWord.getBrokenRecordOpinions().get(j);
                    option.setContent("时间：" + sdf.format(brokenRecordOpinion.getCreateTime()) + ";处理情况："
                            + brokenRecordOpinion.getOpinion());
                    options.add(option);
                }
            }
            if (options.size() == 0) {
                // 如果处理列表为空，给他附一个空的内容，word可以展示一个空的表格
                Option option = new Option();
                option.setContent("");
                options.add(option);
            }
            record.setOptions(options);
            if (brokenRecordWord.getSolveStatus() != null && brokenRecordWord.getSolveStatus() == 1) {
                // 未解决故障
                record.setIndex(unSolveList.size() + 1);
                unSolveList.add(record);
            } else if (brokenRecordWord.getSolveStatus() != null && brokenRecordWord.getSolveStatus() == 2) {
                // 已解决故障
                record.setIndex(solveList.size() + 1);
                solveList.add(record);
            } else if (brokenRecordWord.getAlarmStatus() != null && brokenRecordWord.getAlarmStatus() == 1) {
                // 未确认告警
                record.setIndex(unconfirmList.size() + 1);
                unconfirmList.add(record);
            }
        }
        Map<String, Object> dataMap = new HashMap<String, Object>();
        if (query.getStartDate() != null && query.getEndDate() != null) {
            String timeScope = "在" + sdf.format(query.getStartDate()) + "到" + sdf.format(query.getEndDate()) + "时间范围内，";
            dataMap.put("timeScope", timeScope);
        } else {
            dataMap.put("timeScope", "");
        }
        // 告警总数
        dataMap.put("alarmTotal", list.size());
        // 未确认数
        dataMap.put("unCofirm", unconfirmList.size());
        // 未确认数据
        dataMap.put("unconfirmList", unconfirmList);
        // 故障条数
        dataMap.put("faultTotal", solveList.size() + unSolveList.size());
        // 解决故障条数
        dataMap.put("solveFault", solveList.size());
        // 未解决故障条数
        dataMap.put("unSolveFault", unSolveList.size());
        // 已解决数据
        dataMap.put("solveList", solveList);
        // 未解决数据
        dataMap.put("unSolveList", unSolveList);
        // 已确认数 syt
        dataMap.put("cofirm", list.size() - unconfirmList.size());

        return dataMap;
    }

    @Override
    public List<BrokenRecordWord> getBrokenRecordWordsV2(AlarmInfoPageQuery query) {
        return brokenRecordWordMapper.getBrokenRecordWordsV2(query);
    }
}
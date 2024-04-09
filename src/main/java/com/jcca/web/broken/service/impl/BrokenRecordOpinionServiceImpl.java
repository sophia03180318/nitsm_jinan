package com.jcca.web.broken.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.broken.dao.BrokenRecordMapper;
import com.jcca.web.broken.dao.BrokenRecordOpinionMapper;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web.broken.entity.BrokenRecordOpinion;
import com.jcca.web.broken.service.BrokenRecordOpinionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName BrokenRecordOpinionServiceImpl
 * @Description 故障记录处理意见
 * @Date 2020/6/28 17:45
 * @Author hanwone
 */
@Service
public class BrokenRecordOpinionServiceImpl extends ServiceImpl<BrokenRecordOpinionMapper, BrokenRecordOpinion> implements BrokenRecordOpinionService {

    @Resource
    private BrokenRecordMapper brokenRecordMapper;

    @Override
    public String getMsgByAlarmInfoId(String id) {
        QueryWrapper<BrokenRecord> queryWrapper = new QueryWrapper<BrokenRecord>();
        queryWrapper.eq("ALARM_ID", id);
        BrokenRecord alarmRecord = brokenRecordMapper.selectOne(queryWrapper);
        if (Objects.isNull(alarmRecord)) {
            return "";
        }
        QueryWrapper<BrokenRecordOpinion> brokenRecordOpinion = new QueryWrapper<BrokenRecordOpinion>();
        brokenRecordOpinion.eq("BROKEN_RECORD_ID", alarmRecord.getId());
        brokenRecordOpinion.orderByDesc("CREATE_TIME");
        List<BrokenRecordOpinion> list = list(brokenRecordOpinion);

        if (list.isEmpty()) {
            return "";
        }

        BrokenRecordOpinion brokenRecordOpinion2 = list.get(0);


        return brokenRecordOpinion2.getOpinion();
    }
}

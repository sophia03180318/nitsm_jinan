package com.jcca.web.test;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.enums.EventTypeStatusEnum;
import com.jcca.web.event.service.AlarmEventTypeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 告警初始化测试类
 * @author: Lvyp
 * @create: 2023/11/24 08:57
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AlarmRepoTest {

    @Resource
    private AlarmRepositoryService alarmRepoServ;
    @Resource
    private AlarmEventTypeService eventTypeServ;


    @Test
    public void init() {
        List<AlarmRepository> list = alarmRepoServ.list();
        if (list.isEmpty()) {
            eventTypeServ.remove(new QueryWrapper<AlarmEventType>());

            StatusInfoChangeTypeEnum[] values = StatusInfoChangeTypeEnum.values();

            Map<String, String> typeMap = new HashMap<>();
            for (StatusInfoChangeTypeEnum value : values) {
                String code = value.getCode();
                if (!code.startsWith("event:")) {
                    continue;
                }
                String[] split = code.split(":");
                if (split.length == 2) {
                    //类型
                    String id = MyIdUtil.getId();
                    AlarmEventType type = new AlarmEventType();
                    type.setId(id);
                    type.setStatus(EventTypeStatusEnum.USED.getCode());
                    type.setName(value.getName().replace("状态", "类型事件"));
                    type.setCreateTime(new Date());
                    type.setEventCategory(code);
                    eventTypeServ.save(type);
                    typeMap.put(code, id);
                }
            }

            for (StatusInfoChangeTypeEnum value : values) {
                String code = value.getCode();
                if (!code.startsWith("event:")) {
                    continue;
                }
                String[] split = code.split(":");
                if (split.length == 3) {
                    //规则
                    extracted(typeMap.get(split[0] + ":" + split[1]), value, code, "异常", EventLevelEnum.ABNORMAL);
                    extracted(typeMap.get(split[0] + ":" + split[1]), value, code, "正常", EventLevelEnum.NORMAL);
                }
            }
        }
    }

    private void extracted(String eventTypeId, StatusInfoChangeTypeEnum value, String code, String flag, EventLevelEnum flagType) {
        AlarmRepository repo = new AlarmRepository();
        repo.setId(MyIdUtil.getId());
        repo.setName(value.getName());
        repo.setAlarmLevel(2);
        repo.setAlarmCode(code);
        repo.setStatusFlag(flag);
        repo.setFlagType(flagType.getCode());
        repo.setEventTypeId(eventTypeId);
        repo.setDescStr(value.getName());
        repo.setPlanStr("参考手册解决");
        repo.setCreator("root");
        repo.setCreateTime(new Date());
        repo.setModifier("root");
        repo.setModifyTime(new Date());
        alarmRepoServ.save(repo);
    }


}

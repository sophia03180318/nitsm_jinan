package com.jcca.web.test.process;

import cn.hutool.json.JSONUtil;
import com.jcca.common.redis.service.RedisService;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.process.bean.ProcessAlarmQueueBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: hhw
 * @description: ProcessChangeTest主要是用来
 * @date: 2025-03-12  14:36
 * @since: 2.0.11.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProcessChangeTest {

    @Resource
    private RedisService redisService;

    @Test
    public void processChange() {

        List<ProcessAlarmQueueBean> queueList = new ArrayList<>();

        ProcessAlarmQueueBean bean1 = new ProcessAlarmQueueBean();
        bean1.setProcessName("Notepad.exe");
        bean1.setHostMode(1);
        bean1.setProcessId("12345");
        bean1.setAssetIp("192.168.1.188");
        bean1.setProcessStatus(true);

        ProcessAlarmQueueBean bean2 = new ProcessAlarmQueueBean();
        bean2.setProcessName("Notepad.exe");
        bean2.setHostMode(1);
        bean2.setProcessId("54321");
        bean2.setAssetIp("192.168.20.130");
        bean2.setProcessStatus(false);


        ReceiveAlarmDto dto1 = new ReceiveAlarmDto();
        dto1.setOccurTime(System.currentTimeMillis() + "");
        dto1.setCategory("27");
        dto1.setAssetIp("once");
        dto1.setProcessChange("once"); // 因json处理时不能有相同名称字段，因此增加此字段
        dto1.setContent(JSONUtil.parseArray(queueList).toString());
        String queueMsg1 = JSONUtil.toJsonStr(dto1);
        redisService.convertAndSend(RedisQueueConst.ALARM_QUEUE, queueMsg1);
    }
}

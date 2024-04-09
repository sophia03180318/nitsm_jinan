package com.jcca.web.test.event;

import com.jcca.component.thresholds.bean.EventLogBean;
import com.jcca.web.common.controller.ApiCollectSyslogController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 测试资产
 *
 * @author lyp
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomeventTest {

    @Resource
    private ApiCollectSyslogController apiCollectSyslogController;


    @Test
    public void testCustomEvent() throws InterruptedException {
        EventLogBean req = new EventLogBean();
        req.setAssetId("1731586401314807808");
        req.setDescription("测试告警信息");
        req.setEventId("112");
        req.setLastTime(new Date().getTime() + "");
        apiCollectSyslogController.raidSyslog(req);

        while (true) {
            Thread.sleep(5000);
        }


    }


}

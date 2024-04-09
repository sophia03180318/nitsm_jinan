package com.jcca.web.test.xunjianSupport;

import com.jcca.web2.service.XunjianSupportService;
import com.jcca.web2.vo.InspectResultVo;
import com.jcca.web2.vo.InspectVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试资产
 *
 * @author lyp
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class XunjianSupportTest {
    @Resource
    private XunjianSupportService xunjianSupportService;

    @Test
    public void testInspectItem() {
        xunjianSupportService.inspectItem();
    }


    @Test
    public void testInspect() {
        List<InspectVo> list = xunjianSupportService.inspectItem();
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            InspectVo inspectVo = list.get(i);
            for (InspectVo inspectVoSon : inspectVo.getList()) {
                codes.add(inspectVoSon.getId());
            }

        }

        List<InspectResultVo> inspect = xunjianSupportService.inspect("1743549372479840256", "192.168.80.1", codes);

        System.out.println(inspect.toString());


    }


}

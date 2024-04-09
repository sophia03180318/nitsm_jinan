package com.jcca.web.environment.controller;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.environment.vo.EnvironmentVo;
import com.jcca.web.environment.vo.KV;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName EnvironmentController
 * @Description TODO 此处需要有硬件设备监测环境后才能获取真实数据
 * @Date 2020/5/18 18:03
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/environment")
@Api(tags = "环境监测")
public class EnvironmentController {

    /**
     * 获取监测数据
     *
     * @return
     */
    @GetMapping("/monitor")
    @ApiOperation(value = "获取数据")
    @RequiresPermissions("api:environment:monitor")
    public ResultVo index() {
        List<EnvironmentVo> envList = new ArrayList<>();

        EnvironmentVo leak = new EnvironmentVo();
        leak.setId("1");
        leak.setStatus(StatusEnum.OK.getCode());
        leak.setName("漏水监测");
        leak.setDescript("漏水监测正常");
        leak.setDate(new Date());

        EnvironmentVo smoke = new EnvironmentVo();
        smoke.setId("2");
        smoke.setStatus(StatusEnum.OK.getCode());
        smoke.setName("烟感监测");
        smoke.setDescript("烟感监测正常");
        smoke.setDate(new Date());

        EnvironmentVo temprature = new EnvironmentVo();
        temprature.setId("3");
        temprature.setStatus(StatusEnum.OK.getCode());
        temprature.setName("温度监测");
        temprature.setDescript("温度监测值为24℃,正常");
        temprature.setDate(new Date());

        EnvironmentVo moisture = new EnvironmentVo();
        moisture.setId("4");
        moisture.setStatus(StatusEnum.NO.getCode());
        moisture.setName("湿度监测");
        moisture.setDescript("湿度监测不正常");
        moisture.setDate(new Date());

        envList.add(leak);
        envList.add(smoke);
        envList.add(temprature);
        envList.add(moisture);

        Map<String, Object> map = new HashMap<>(16);
        List<KV> wenduList = new ArrayList<>();
        wenduList.add(new KV("14:00", "19"));
        wenduList.add(new KV("14:05", "19.5"));
        wenduList.add(new KV("14:10", "20.2"));
        wenduList.add(new KV("14:15", "24"));
        wenduList.add(new KV("14:20", "21.2"));
        wenduList.add(new KV("14:25", "22.3"));
        wenduList.add(new KV("14:30", "20.5"));
        wenduList.add(new KV("14:35", "18.9"));
        wenduList.add(new KV("14:40", "17"));
        wenduList.add(new KV("14:45", "18"));

        List<KV> shiduList = new ArrayList<>();
        shiduList.add(new KV("14:00", "40.4"));
        shiduList.add(new KV("14:05", "44.5"));
        shiduList.add(new KV("14:10", "50.8"));
        shiduList.add(new KV("14:15", "55.3"));
        shiduList.add(new KV("14:20", "44.6"));
        shiduList.add(new KV("14:25", "53.2"));
        shiduList.add(new KV("14:30", "53"));
        shiduList.add(new KV("14:35", "55"));
        shiduList.add(new KV("14:40", "54.4"));
        shiduList.add(new KV("14:45", "60"));

        map.put("envList", envList);
        map.put("wenduList", wenduList);
        map.put("shiduList", shiduList);

        return ResultVoUtil.success(map);
    }
}

package com.jcca.component.quartz.threeD;

import ch.qos.logback.classic.Level;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.common.constants.ThreeDConst;
import com.jcca.web.common.service.ThreeDService;
import com.jcca.web.common.service.bean.ThreeDResult;
import com.jcca.web.common.util.MQUtil;
import lombok.extern.log4j.Log4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 推送3D机房现存告警
 *
 * @author sophia
 */
@Service
@Log4j
@DisallowConcurrentExecution
public class QuartzThreeDPushAlarmJob extends QuartzJobBean {

    @Resource
    private SysModuleConfigService configService;
    @Resource
    private ThreeDService threeDService;
    @Resource
    private RedisService redisService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        //查看是否开启3D机房
        SysModuleConfig config = configService.getSysModuleConfig("config:threeDJob");
        if (Objects.isNull(config)) {
            SysModuleConfig config1 = new SysModuleConfig();
            config1.setId(MyIdUtil.getId());
            config1.setName("config:threeDJob");
            config1.setValue("close");
            config1.setDescription("open：打开3D机房告警推送  close：关闭3D机房告警推送");
            config1.setOrgId("0");
            config1.setServiceType(3);
            configService.save(config1);
            config = config1;
        }
        if ("open".equals(config.getValue())) {
            redisService.set(ThreeDConst.KEY_STATUS, "1");
            ThreeDResult threeDResult = threeDService.pushAlarm();
            if (!threeDResult.isStatus()) {
               log.error(threeDResult.getLog());
            }
            ThreeDResult threeDResult2 = threeDService.pushProperty();
            if (!threeDResult2.isStatus()) {
                log.error(threeDResult2.getLog());
            }
            ThreeDResult threeDResult3 = threeDService.pushLink();
            if (!threeDResult3.isStatus()) {
                log.error(threeDResult3.getLog());
            }
        } else {
            MQUtil.closeChannelAndConnection();
            redisService.set(ThreeDConst.KEY_STATUS, "0");
        }
    }
}




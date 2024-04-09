package com.jcca.common.init;

import com.jcca.common.init.constant.CacheConstant;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.ip.controller.IpController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 初始化需要清空的缓存
 *
 * @author lyp
 */
@Order(2)
@Slf4j
@Component
public class InitCleanCacheRunner implements ApplicationRunner {

    @Resource
    private RedisService redisService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        redisService.remove(CacheConstant.BUSSESS_CACHE_UNKNOW_EVENT_TYPE_OBJ);
        redisService.removePattern(IpController.BATCH_PING_FLG + "*");
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "已完成未知事件、批量ping 缓存清理"));
        }
    }

}

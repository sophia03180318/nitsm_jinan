package com.jcca.dataProcessing;

import cn.hutool.json.JSONUtil;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppRedisUtils;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class NoThresholdInfoReceiver{

    @Resource
    private RedisService redisService;
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;


    @Async
    public void run() {
        RedisConnection connection = redisService.getReceiverRedisConnection();
        while (true) {
            try {
                //connection.bLPop,阻塞获取数据，如果缓存中不存在采集信息则将会停在此处
                List<byte[]> thresholdList = connection.bLPop(0, RedisQueueConst.ALARM_QUEUE.getBytes());

                String bodyJson =  redisService.stringRedisTemplateDeserialize(thresholdList.get(1));
                ReceiveAlarmDto alarmDto = JSONUtil.toBean(bodyJson, ReceiveAlarmDto.class);
                IAdapter adapter = null;
                if (alarmDto.getCategory().equals("19")) {
                    //数据库状态，因数据不统一做的转换
                    adapter = dataProcessManager.getAdapter(CollectConst.DB_ALARM);
                } else if (alarmDto.getCategory().equals("1")) {
                    //ping，因数据不统一做的转换
                    adapter = dataProcessManager.getAdapter(CollectConst.ping);
                } else if (alarmDto.getCategory().equals("27")) {
                    //进程组，因数据不统一做的转换
                    adapter = dataProcessManager.getAdapter(CollectConst.PROCESS_GROUP);
                } else {
                    adapter = dataProcessManager.getAdapter(alarmDto.getCategory());
                }
                adapter.dispose(alarmDto);
                //获取当前处理数量
                adapter.dataProcess();
            } catch (JedisConnectionException | RedisConnectionFailureException e1) {
                AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER, "redis 网络断线……", e1);
                try {
                    AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_DATA_PARSER, "已关闭原有连接","");
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER, "redis 网络断线关闭原有链接异常……", e);
                }
            }catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER,"非阈值处理调度被中断",e);
            }finally {
                //检查连接有效性
                if(!AppRedisUtils.verifyRedisConn(connection)){
                    AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_DATA_PARSER, "非阈值处理Redis连接已经失效，重新建立连接","");
                    connection  = redisService.getReceiverRedisConnection();
                }
            }
        }
    }

}

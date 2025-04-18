package com.jcca.dataProcessing;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;

import com.jcca.common.log.enums.LogFunctionEnum;
;
import com.jcca.common.utils.AppLogUtils;

import com.jcca.common.utils.AppRedisUtils;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveCollectDto;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.Resource;
import java.util.List;


@Component
@Slf4j
public class ThresholdInfoReceiver {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;


    @Async
    public void run() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        while (true) {
            try {
                //connection.bLPop,阻塞获取数据，如果缓存中不存在采集信息则将会停在此处
                List<byte[]> thresholdList = connection.bLPop(0, RedisQueueConst.THRESHOLD_QUEUE.getBytes());
                String bodyJson = redisTemplate.getStringSerializer().deserialize(thresholdList.get(1));
                ReceiveCollectDto dto = JSONUtil.toBean(bodyJson, ReceiveCollectDto.class);
                String content = dto.getContent();
                JSONArray result = JSONUtil.parseArray(content);
                IAdapter adapter = dataProcessManager.getAdapter(dto.getCategory());
                adapter.dispose(result);
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
                AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER,"阈值处理调度被中断",e);
            }finally {
                //检查连接有效性
                if(!AppRedisUtils.verifyRedisConn(connection)){
                    AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_DATA_PARSER, "阈值处理Redis连接已经失效，重新建立连接","");
                    connection = redisTemplate.getConnectionFactory().getConnection();
                }
            }
        }
    }

}

package com.jcca.dataProcessing;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveCollectDto;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ThresholdInfoReceiver implements Runnable {

    private RedisQueueTemplate queueTemplate;
    private DataProcessManager dataProcessManager;


    @Override
    public void run() {
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        dataProcessManager = SpringContextUtil.getBean(DataProcessManager.class);
        queueTemplate = new RedisQueueTemplate(stringRedisTemplate);
        int i = 0;
        RedisConnection connection = null;

        while (true) {
            //建立redis连接
            if(Objects.isNull(connection)){
                try {
                    connection = queueTemplate.getRedisTemplate().getConnectionFactory().getConnection();
                }catch (Exception e){
                    AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER, "redis建立链接失败……", e);
                    try {
                        Thread.sleep(1000*2);
                    } catch (InterruptedException ex) {
                        log.error(ex.getMessage(),ex);
                    }
                    continue;
                }
            }

            try {
                //connection.bLPop,阻塞获取数据，如果缓存中不存在采集信息则将会停在此处
                List<byte[]> thresholdList = connection.bLPop(0, RedisQueueConst.THRESHOLD_QUEUE.getBytes());
                String bodyJson = queueTemplate.getRedisTemplate().getStringSerializer().deserialize(thresholdList.get(1));
                AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_DATA_PARSER, "从 _threshold_queue 队列中读取数据", bodyJson);
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
                    connection.close();
                    AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_DATA_PARSER, "已关闭原有连接","");
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER, "redis 网络断线关闭原有链接异常……", e);
                }
                //至为空
                connection = null;
            }catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER,"阈值处理调度被中断",e);
            }
        }
    }

    public static void init() {
        Thread thresholdDispatchThread = new Thread(new ThresholdInfoReceiver());
        thresholdDispatchThread.setName("thresholdDispatchThread-" + DateUtil.now());
        thresholdDispatchThread.start();

        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "阈值队列处理调度初始化完成"));
        }
    }

}

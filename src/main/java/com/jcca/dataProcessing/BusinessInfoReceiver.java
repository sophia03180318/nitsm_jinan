package com.jcca.dataProcessing;

import cn.hutool.json.JSONUtil;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppRedisUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
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
import java.util.Objects;

/**
 * @author HanHW
 * @description 处理业务告警信息
 * @className BrokerAlarmRunnable
 * @date 2023/7/3 15:47
 * @since 2.0.5.0
 */
@Component
@Slf4j
public class BusinessInfoReceiver{

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Async
    public void run() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        while (true) {
            try {
                List<byte[]> list = connection.bLPop(0, RedisQueueConst.BROKER_QUEUE_KEY.getBytes());
                assert list != null;
                String str = redisTemplate.getStringSerializer().deserialize(list.get(1));
                ItsmQueueEntity itsmQueueReq = JSONUtil.toBean(str, ItsmQueueEntity.class);
                IAdapter adapter = dataProcessManager.getAdapter(itsmQueueReq.getCascoAlarmType());
                adapter.dispose(itsmQueueReq);
                //获取当前处理数量
                adapter.dataProcess();
            }catch (JedisConnectionException | RedisConnectionFailureException e1) {
                AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER, "redis 网络断线……", e1);
                try {
                    AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_DATA_PARSER, "已关闭原有连接","");
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER, "redis 网络断线关闭原有链接异常……", e);
                }
            }catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER,"业务告警处理调度被中断",e);
            }finally {
                //检查连接有效性
                if(!AppRedisUtils.verifyRedisConn(connection)){
                    AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_DATA_PARSER, "","");
                    connection = redisTemplate.getConnectionFactory().getConnection();
                }
            }
        }

    }
}

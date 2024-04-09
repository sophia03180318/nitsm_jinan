package com.jcca.dataProcessing;

import cn.hutool.json.JSONUtil;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Objects;

/**
 * @author HanHW
 * @description 处理业务告警信息
 * @className BrokerAlarmRunnable
 * @date 2023/7/3 15:47
 * @since 2.0.5.0
 */
@Slf4j
public class BusinessInfoReceiver implements Runnable {

    private DataProcessManager dataProcessManager;
    @Override
    public void run() {
        dataProcessManager = SpringContextUtil.getBean(DataProcessManager.class);
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        RedisQueueTemplate queueTemplate = new RedisQueueTemplate(stringRedisTemplate);
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
                List<byte[]> list = connection.bLPop(0, RedisQueueConst.BROKER_QUEUE_KEY.getBytes());
                assert list != null;
                String str = queueTemplate.getRedisTemplate().getStringSerializer().deserialize(list.get(1));
                ItsmQueueEntity itsmQueueReq = JSONUtil.toBean(str, ItsmQueueEntity.class);
                IAdapter adapter = dataProcessManager.getAdapter(itsmQueueReq.getCascoAlarmType());
                adapter.dispose(itsmQueueReq);
                //获取当前处理数量
                adapter.dataProcess();
            }catch (JedisConnectionException | RedisConnectionFailureException e1) {
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
                AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_DATA_PARSER,"业务告警处理调度被中断",e);
            }
        }

    }

    public static void init() {
        Thread thread = new Thread(new BusinessInfoReceiver());
        thread.setName("brokerAlarmThread");
        thread.start();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, "处理业务告警数据时发生异常", "");
    }
}

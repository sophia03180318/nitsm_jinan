package com.jcca.admin.system.controller;

import cn.hutool.json.JSONObject;
import com.jcca.admin.system.entity.ThreadPoolMonitorNode;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyMonitor;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.web.common.constants.OutConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author:
 * @Date: 2020/1/6
 * @Describe: 首页
 */
@Controller
@RequestMapping("/system/itsmmonitor")
@Slf4j
public class ItsmMonitorController {
    @Autowired
    private RedisService redisService;

    /**
     * 获取itsm系统性能
     *
     * @param model
     * @return
     */
    @GetMapping("/index")
    @ActionLog(name = "查看线程池监控", title = "本地监控", key = LogTypeConstant.QUERY)
    public String getCollectSystem(Model model) {

        // 获取redis信息
        try {
            Long size1 = redisService.stringRedisTemplateOpsForListSize(RedisQueueConst.THRESHOLD_QUEUE);
            Long size2 = redisService.stringRedisTemplateOpsForListSize(RedisQueueConst.ALARM_QUEUE);
            Long size3 = redisService.stringRedisTemplateOpsForListSize(RedisQueueConst.ALARM_PUSH_FRONT_QUEUE);
            Long size4 = redisService.stringRedisTemplateOpsForListSize(RedisQueueConst.BROKER_QUEUE_KEY);

            //事件告警分发队列
            Long size5 = redisService.stringRedisTemplateOpsForListSize(RedisQueueConst.EVENT_GROUP_ALARM);
            // 事件添加队列
            Long size7 = redisService.stringRedisTemplateOpsForListSize(RedisQueueConst.EVENT_GROUP_ALARM_ADD);
            // 告警处理队列
            Long size8 = redisService.stringRedisTemplateOpsForListSize(RedisQueueConst.EVENT_GROUP_ALARM_EXE);


            String url = "";
            Object collectUrl = redisService.get(OutConst.COLLECT_MASTER_URL);
            if (Objects.nonNull(collectUrl)) {
                url = collectUrl.toString();
            }
            //阈值队列
            model.addAttribute(RedisQueueConst.THRESHOLD_QUEUE, size1);
            //非阈值队列
            model.addAttribute(RedisQueueConst.ALARM_QUEUE, size2);
            model.addAttribute(RedisQueueConst.ALARM_PUSH_FRONT_QUEUE, size3);
            model.addAttribute(RedisQueueConst.BROKER_QUEUE_KEY, size4);

            // 事件告警分发队列
            model.addAttribute(RedisQueueConst.EVENT_GROUP_ALARM, size5);
            // 告警处理队列
            model.addAttribute("EVENT_ALARM_EXE_TOTAL", size8);
            // 事件添加队列
            model.addAttribute("EVENT_ALARM_ADD_TOTAL", size7.longValue());

            model.addAttribute(OutConst.COLLECT_MASTER_URL, url);
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_MONITOR)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_MONITOR, ErrorCodeEnum.SYSTEM_MONITOR_REDIS, "", "本地监控获取redis信息异常:" + e.getMessage()));
            }
        }
        return "/system/monitor/itsm_system";
    }


    @GetMapping("/getThreadPoolMonitorNode")
    @ResponseBody
    @ActionLog(name = "查看节点线程池监控", title = "本地监控", key = LogTypeConstant.QUERY)
    public JSONObject getThreadPoolMonitorNode() {
        List<ThreadPoolMonitorNode> list = new ArrayList<>();
        list.add(MyMonitor.getThreadPoolMonitorNode(ThreadPoolEnum.thresholdDataDisposePool));
        list.add(MyMonitor.getThreadPoolMonitorNode(ThreadPoolEnum.linksNumExecutor));
        list.add(MyMonitor.getThreadPoolMonitorNode(ThreadPoolEnum.transferDataExecutor));
        list.add(MyMonitor.getSpringThreadPoolMonitorNode(ThreadPoolEnum.taskExecutor));
//        list.add(MyMonitor.getThreadPoolMonitorNode(ThreadPoolEnum.ipmiPingJob));
        list.add(MyMonitor.getThreadPoolMonitorNode(ThreadPoolEnum.xunjianAsync));
//        list.add(MyMonitor.getThreadPoolMonitorNode(ThreadPoolEnum.cascoAlarmDispose));
        list.add(MyMonitor.getThreadPoolMonitorNode(ThreadPoolEnum.xunjianExecutor));

        ThreadPoolMonitorNode node = new ThreadPoolMonitorNode();
        //线程池名称
        node.setPoolName("redis连接数");
        //当前线程数
        node.setPoolSize(redisService.stringRedisTemplateClientSize());

        list.add(node);

        JSONObject resp = new JSONObject();
        resp.put("code", 0);
        resp.put("data", list);
        resp.put("count", list.size());
        return resp;
    }


    @GetMapping("/getThread")
    @ResponseBody
    @ActionLog(name = "查看线程监控", title = "本地监控", key = LogTypeConstant.QUERY)
    public JSONObject getThread() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();
        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        List<JSONObject> respList = new ArrayList<JSONObject>();
        for (Thread t : threads) {
            JSONObject body = new JSONObject();
            body.put("id", t.getId());
            body.put("name", t.getName());
            body.put("state", t.getState());

            respList.add(body);
        }

        JSONObject resp = new JSONObject();
        resp.put("code", 0);
        resp.put("data", respList);
        resp.put("count", respList.size());
        return resp;
    }

}

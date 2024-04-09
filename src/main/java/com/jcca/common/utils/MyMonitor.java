package com.jcca.common.utils;


import cn.hutool.extra.spring.SpringUtil;
import com.jcca.admin.system.entity.ThreadPoolMonitorNode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 返回监控信息
 */
@Slf4j
public class MyMonitor {
    /**
     * 返回线程池监控信息
     *
     * @param poolName 线程池名
     * @return
     */
    public static String getThreadPoolMonitor(String poolName) {
        StringBuilder sb = new StringBuilder();
        try {
            ThreadPoolExecutor tpl = (ThreadPoolExecutor) SpringUtil.getBean(poolName);
            if (tpl != null) {

                sb.append("[线程池监控:").append(poolName).append("=>当前线程数:").append(tpl.getPoolSize())
                        .append(",核心线程数:").append(tpl.getCorePoolSize())
                        .append(",正在执行的任务数量:").append(tpl.getActiveCount())
                        .append(",已完成任务数量:").append(tpl.getCompletedTaskCount())
                        .append(",任务总数:").append(tpl.getTaskCount())
                        .append(",队列里缓存的任务数量:").append(tpl.getQueue().size())
                        .append(",池中存在的最大线程数:").append(tpl.getLargestPoolSize())
                        .append(",最大允许的线程数:").append(tpl.getMaximumPoolSize())
                        .append(",线程空闲时间:").append(tpl.getKeepAliveTime(TimeUnit.MILLISECONDS))
                        .append(",线程池是否关闭:").append(tpl.isShutdown())
                        .append(",线程池是否终止:").append(tpl.isTerminated()).append("]");
            }
        } catch (Exception e) {
            log.error("获取线程池监控异常:", e);
        }
        String logstr = sb.toString();
        log.info(logstr);
        return logstr;
    }


    /**
     * 返回线程池监控信对象信息
     *
     * @param poolName 线程池名
     * @return
     */
    public static ThreadPoolMonitorNode getThreadPoolMonitorNode(String poolName) {
        ThreadPoolMonitorNode node = new ThreadPoolMonitorNode();
        try {
            ThreadPoolExecutor tpl = (ThreadPoolExecutor) SpringContextUtil.getBean(poolName);
            if (tpl != null) {
                node.setPoolSize(tpl.getPoolSize());
                node.setCorePoolSize(tpl.getCorePoolSize());
                node.setActiveCount(tpl.getActiveCount());
                node.setCompletedTaskCount(tpl.getCompletedTaskCount());
                node.setTaskCount(tpl.getTaskCount());
                node.setQueueSize(tpl.getQueue().size());
                node.setLargestPoolSize(tpl.getLargestPoolSize());
                node.setMaximumPoolSize(tpl.getMaximumPoolSize());
                node.setKeepAliveTime(tpl.getKeepAliveTime(TimeUnit.MILLISECONDS));
                node.setShutdown(tpl.isShutdown());
                node.setTerminated(tpl.isTerminated());
                node.setPoolName(poolName);
            }
        } catch (Exception e) {
            log.error("获取线程池监控异常:", e);
        }
        return node;
    }

    /**
     * 返回spring线程池监控信对象信息
     *
     * @param poolName
     * @return
     */
    public static ThreadPoolMonitorNode getSpringThreadPoolMonitorNode(String poolName) {
        ThreadPoolMonitorNode node = new ThreadPoolMonitorNode();
        try {
            org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor tpl = (org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) SpringContextUtil.getBean(poolName);
            if (tpl != null) {
                node.setPoolSize(tpl.getPoolSize());
                node.setCorePoolSize(tpl.getCorePoolSize());
                node.setActiveCount(tpl.getActiveCount());
                node.setCompletedTaskCount(tpl.getThreadPoolExecutor().getCompletedTaskCount());
                node.setTaskCount(tpl.getThreadPoolExecutor().getTaskCount());
                node.setQueueSize(tpl.getThreadPoolExecutor().getQueue().size());
                node.setLargestPoolSize(tpl.getThreadPoolExecutor().getLargestPoolSize());
                node.setMaximumPoolSize(tpl.getThreadPoolExecutor().getMaximumPoolSize());
                node.setKeepAliveTime(tpl.getThreadPoolExecutor().getKeepAliveTime(TimeUnit.MILLISECONDS));
                node.setShutdown(tpl.getThreadPoolExecutor().isShutdown());
                node.setTerminated(tpl.getThreadPoolExecutor().isTerminated());
                node.setPoolName(poolName);
            }
        } catch (Exception e) {
            log.error("获取线程池监控异常:", e);
        }
        return node;
    }
}

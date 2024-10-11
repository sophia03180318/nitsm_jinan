package com.jcca.common.config.async;

import com.jcca.component.enums.ThreadPoolEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName AsyncConfig
 * @Description 异步配置
 * @Date 2020/4/17 17:34
 * @Author hanwone
 */
@Configuration
public class AsyncThreadConfig {

    /**
     * WARNNING 新的线程池需要另起名字创建，不建议使用相同名字的线程池处理不同的业务
     */
    @Bean(name = ThreadPoolEnum.taskExecutor)
    public Executor taskExecutor() {
        return this.getExecutor("task");
    }

    @Bean(name = ThreadPoolEnum.linksNumExecutor)
    public Executor linksNumExecutor() {
        return this.getExecutor("links-num");
    }

    @Bean(name = ThreadPoolEnum.transferDataExecutor)
    public Executor transferDataExecutor() {
        return this.getExecutor("transfer-data");
    }

    @Bean(name = ThreadPoolEnum.ipmiPingJob)
    public Executor ipmiPingExecutor() {
        return this.getExecutor("IMPI-PING-JOB");
    }


    /**
     * 巡检耗时较大，系统只允许一个线程运行
     * 系统只允许一个线程巡检
     *
     * @return
     */
    @Bean(name = ThreadPoolEnum.xunjianAsync)
    public Executor xunjianAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 当线程数量小于corePoolSize时，创建线程，不管线程是不是闲置的
        executor.setCorePoolSize(1);
        // 当线程数量大于等于corePoolSize时，把任务放到queueCapacity队列
        // 当queueCapacity满了，就创建新的线程来执行
        executor.setQueueCapacity(20000);
        // 当线程数量大于等于maxPoolSize时，根据RejectedExecutionHandler设置的策略来处理新加入的任务
        executor.setMaxPoolSize(2);
        // (maxPoolSize-corePoolSize)部分线程空闲最大存活时间
        executor.setKeepAliveSeconds(10);

        // 允许核心线程空闲时，过了一定的时间自动销毁
        executor.setAllowCoreThreadTimeOut(true);

        // 拒绝策略，由调用方线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }


    private Executor getExecutor(String preName) {
        int core = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 当线程数量小于corePoolSize时，创建线程，不管线程是不是闲置的
        executor.setCorePoolSize(core * 2);
        // 当线程数量大于等于corePoolSize时，把任务放到queueCapacity队列
        // 当queueCapacity满了，就创建新的线程来执行
        executor.setQueueCapacity(20000);
        // 当线程数量大于等于maxPoolSize时，根据RejectedExecutionHandler设置的策略来处理新加入的任务
        executor.setMaxPoolSize(core * 4 + 1);
        // (maxPoolSize-corePoolSize)部分线程空闲最大存活时间
        executor.setKeepAliveSeconds(10);

        executor.setThreadNamePrefix(preName);

        // 允许核心线程空闲时，过了一定的时间自动销毁
        executor.setAllowCoreThreadTimeOut(true);

        // 拒绝策略，由调用方线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }


    // 巡检
    @Bean(name = ThreadPoolEnum.xunjianExecutor)
    public ThreadPoolExecutor getXunjianDisposePool() {
        int coreSize = Runtime.getRuntime().availableProcessors() + 2;
        int size = Runtime.getRuntime().availableProcessors() * 2 + 2;
        return new ThreadPoolExecutor(coreSize, size, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<>(20000),
                new MyThreadFactory(ThreadPoolEnum.xunjianExecutor));
    }

    // 阈值采集数据处理
    @Bean(name = ThreadPoolEnum.thresholdDataDisposePool)
    public ThreadPoolExecutor getThresholdDisposePool() {
        int size = Runtime.getRuntime().availableProcessors() * 2 + 2;
        return new ThreadPoolExecutor(size, size, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<>(20000),
                new MyThreadFactory(ThreadPoolEnum.thresholdDataDisposePool));
    }
}

class MyThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public MyThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix + "-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
        if (t.isDaemon())
            t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}
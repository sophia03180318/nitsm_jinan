package com.jcca.web.ibmMQ.util.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;

@ThreadSafe
public final class Executors {
    public static ITrackableScheduledExecutorService newTrackableScheduledThreadPool(int corePoolSize) {
        return new TrackableScheduledThreadPoolExecutor(corePoolSize);
    }

    public static ITrackableScheduledExecutorService newTrackableScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return new TrackableScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    public static ITrackableScheduledExecutorService newTrackableScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new TrackableScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
    }
}


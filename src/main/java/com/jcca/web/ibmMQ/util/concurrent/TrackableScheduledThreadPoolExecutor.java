package com.jcca.web.ibmMQ.util.concurrent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;


@ThreadSafe
public class TrackableScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements ITrackableScheduledExecutorService {
    private static final Logger log = LoggerFactory.getLogger(TrackableScheduledThreadPoolExecutor.class);


    public TrackableScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }


    public TrackableScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }


    public TrackableScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }


    public TrackableScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }


    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable r, RunnableScheduledFuture<V> task) {
        return new TrackableScheduledFutureTask<V>(r, task);
    }


    public ExecutorStats stats() {
        return new ExecutorStats(getTaskCount(), getCompletedTaskCount(), getActiveCount(), getQueue().size());
    }


    protected void beforeExecute(Thread t, Runnable r) {
        if (log.isDebugEnabled()) {
            log.debug("beforeExecute {} with task {}", t, r);
        }
        super.beforeExecute(t, r);
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (log.isDebugEnabled()) {
            log.debug(String.format("afterExecute task %s", new Object[]{r}), t);
        }
    }


    private static class TrackableScheduledFutureTask<V>
            implements RunnableScheduledFuture<V> {
        private final RunnableScheduledFuture<V> task;

        private final Runnable runnable;


        public TrackableScheduledFutureTask(Runnable runnable, RunnableScheduledFuture<V> task) {
            this.runnable = runnable;
            this.task = task;
        }


        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.task.cancel(mayInterruptIfRunning);
        }


        public boolean isCancelled() {
            return this.task.isCancelled();
        }


        public boolean isDone() {
            return this.task.isDone();
        }

        public V get() throws InterruptedException, ExecutionException {
            return this.task.get();
        }


        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.task.get(timeout, unit);
        }


        public long getDelay(TimeUnit unit) {
            return this.task.getDelay(unit);
        }


        public int compareTo(Delayed o) {
            return this.task.compareTo(o);
        }


        public boolean isPeriodic() {
            return this.task.isPeriodic();
        }

        public void run() {
            this.task.run();
        }


        public String toString() {
            return this.runnable.toString();
        }

    }

    public String toString() {
        return stats().toString();
    }

}

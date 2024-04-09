package com.jcca.web.ibmMQ.common;


import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/*    */
/*    */


@ThreadSafe
public final class ThreadFactories {

    public static ThreadFactory newDaemonThreadFactory() {
        return new DaemonThreadFactory(null);
    }


    public static ThreadFactory newDaemonThreadFactory(String poolNamePrefix) {
        return new DaemonThreadFactory(poolNamePrefix);
    }

    static class DaemonThreadFactory implements ThreadFactory {
        private static final String DEFAULT_POOL_NAME_PREFIX = "mq-monitoring-daemon-pool";

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public DaemonThreadFactory(String poolNamePrefix) {
            if (poolNamePrefix == null || poolNamePrefix.trim().equals("")) {
                poolNamePrefix = "mq-monitoring-daemon-pool";
            }

            this.namePrefix = String.format("%s-%s-thread-", new Object[]{poolNamePrefix, Integer.valueOf(poolNumber.getAndIncrement())});

        }


        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(this.namePrefix + this.threadNumber.getAndIncrement());
            return t;

        }

    }

}


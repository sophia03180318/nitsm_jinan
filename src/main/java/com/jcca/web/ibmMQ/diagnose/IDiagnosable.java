package com.jcca.web.ibmMQ.diagnose;

import java.util.concurrent.atomic.AtomicInteger;

public interface IDiagnosable {
    Indicator getIndicator();

    <A> A adapt(Class<A> paramClass);

    public static class Indicator {
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger timeoutCount = new AtomicInteger(0);
        private final AtomicInteger consecutiveFailureCount = new AtomicInteger(0);


        public int getSuccessCount() {
            return this.successCount.get();
        }


        public void increaseSuccessCount() {
            this.successCount.incrementAndGet();

            this.consecutiveFailureCount.getAndSet(0);
        }


        public int getFailureCount() {
            return this.failureCount.get();
        }


        public void increaseFailureCount() {
            this.failureCount.incrementAndGet();
            this.consecutiveFailureCount.incrementAndGet();
        }


        public int getTimeoutCount() {
            return this.timeoutCount.get();
        }


        public void increaseTimeoutCount() {
            this.timeoutCount.incrementAndGet();
            this.consecutiveFailureCount.incrementAndGet();
        }


        public int getConsecutiveFailureCount() {
            return this.consecutiveFailureCount.get();
        }


        public String toString() {
            return Indicator.class.getSimpleName() + "[successCount=" + this.successCount +
                    "," + "failureCount=" + this.failureCount + "," + "timeoutCount=" +
                    this.timeoutCount + "," + "consecutiveFailureCount=" + this.consecutiveFailureCount +
                    "]";
        }
    }
}



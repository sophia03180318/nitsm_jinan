package com.jcca.web.ibmMQ.util.concurrent;


import javax.annotation.concurrent.Immutable;


@Immutable
public final class ExecutorStats {
    private final long totalTaskCount;
    private final long completedTaskCount;
    private final int activeTaskCount;
    private final int pendingTaskCount;

    public ExecutorStats(long totalTaskCount, long completedTaskCount, int activeTaskCount, int pendingTaskCount) {
        checkArguments((totalTaskCount >= 0L));
        checkArguments((completedTaskCount >= 0L));
        checkArguments((activeTaskCount >= 0));
        checkArguments((pendingTaskCount >= 0));
        this.totalTaskCount = totalTaskCount;
        this.completedTaskCount = completedTaskCount;
        this.activeTaskCount = activeTaskCount;
        this.pendingTaskCount = pendingTaskCount;
    }

    public long getTotalTaskCount() {
        return this.totalTaskCount;
    }

    public long getCompletedTaskCount() {
        return this.completedTaskCount;
    }


    public int getActiveTaskCount() {
        return this.activeTaskCount;
    }


    public int getPendingTaskCount() {
        return this.pendingTaskCount;
    }

    private static void checkArguments(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException("Condition is not satisfied!");
        }
    }

    public String toString() {
        return getClass().getSimpleName() + " [" + "totalTaskCount=" + this.totalTaskCount +
                "," + "activeTaskCount=" + this.activeTaskCount + "," + "pendingTaskCount=" +
                this.pendingTaskCount + "," + "completedTaskCount=" + this.completedTaskCount +
                "]";
    }
}

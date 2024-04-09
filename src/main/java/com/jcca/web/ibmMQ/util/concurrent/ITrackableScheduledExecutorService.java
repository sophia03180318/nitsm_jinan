package com.jcca.web.ibmMQ.util.concurrent;

import java.util.concurrent.ScheduledExecutorService;

public interface ITrackableScheduledExecutorService extends ScheduledExecutorService {
    ExecutorStats stats();
}

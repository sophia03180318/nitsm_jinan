package com.jcca.web2.dto.xunjian;

import com.jcca.web2.service.XunjianScheduleService;
import lombok.Data;

import static com.jcca.web2.controller.XunjianFinalController.INSPECT_THREAD_MAP;

/**
 * @author: hhw
 * @description: XunjianTask 主要是用来
 * @date: 2025-06-27  21:25
 * @since: 2.1.6.0
 */
@Data
public class XunjianTask implements Runnable {
    private final String jobId;
    private final XunjianJobDto dto;
    private final XunjianScheduleService xunjianScheduleService;

    public XunjianTask(String jobId, XunjianScheduleService xunjianScheduleService, XunjianJobDto dto) {
        this.jobId = jobId;
        this.dto = dto;
        this.xunjianScheduleService = xunjianScheduleService;
    }

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        thread.setName(jobId);
        INSPECT_THREAD_MAP.put(jobId, thread);
        xunjianScheduleService.beginXunjian(dto);
    }
}

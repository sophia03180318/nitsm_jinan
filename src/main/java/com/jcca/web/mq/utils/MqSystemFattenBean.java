package com.jcca.web.mq.utils;

import com.jcca.web.mq.entity.MqMonitor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 19:48 2022/6/5
 * @ Description:
 */
@Data
public class MqSystemFattenBean {
    /**
     * 采集时间
     */
    private String collectTime;

    /**
     * 采集码
     */
    private String collectCode;


    // 1. 队列管理器运行状态
    private String qmgrStatus; // RUNNING / STARTING / STOPPING / UNKNOWN ...
    // 2. 队列管理器连接数
    private int connectionCount;

    // 3. 所有队列名称(本地/传输/远程)
    private List<String> localQueues = new ArrayList<>();
    private List<String> transmissionQueues = new ArrayList<>();
    private List<String> remoteQueues = new ArrayList<>();

    // 4. 所有通道名称(接收/发送)
    private List<String> senderChannels = new ArrayList<>();   // SDR / CLUSSDR
    private List<String> receiverChannels = new ArrayList<>(); // RCVR / CLUSRCVR

    // 5. 监控队列
    private List<MqMonitor> queues = new ArrayList<>();

    // 6. 监控通道
    private List<MqMonitor> channels = new ArrayList<>();


}

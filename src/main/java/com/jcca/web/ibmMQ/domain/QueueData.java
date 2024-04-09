/*     */
package com.jcca.web.ibmMQ.domain;


import com.jcca.web.ibmMQ.annotation.PCFParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QueueData extends StatisticalData {
    private static final long serialVersionUID = 1L;

    public QueueData(Monitor monitor) {
        super(monitor);
    }

    @PCFParam(2016)
    private String queueName;

    @PCFParam(20)
    private QueueType queueType;

    @PCFParam(value = 3, supportType = {1, 6}, supportTypeClass = QueueType.class, option = 11)
    private int currentQDepth;//当前队列深度

    @PCFParam(value = 15, supportType = {1, 6}, supportTypeClass = QueueType.class, option = 11)
    //@PCFParam(value = 15)
    private int maxQDepth;//最大队列深度

    @PCFParam(supportType = {1, 6}, supportTypeClass = QueueType.class, option = 12)
    //@PCFParam(16)
    private float occupiedPercent;

    @PCFParam(value = 17, supportType = {1, 6}, supportTypeClass = QueueType.class, option = 11)
    //@PCFParam(value = 17)
    private int openInputCount;//输入计数

    @PCFParam(value = 18, supportType = {1, 6}, supportTypeClass = QueueType.class, option = 11)
    //@PCFParam(value = 18)
    private int openOutputCount;//输出计数

    @PCFParam(9)
    private boolean inhibitGet;

    @PCFParam(10)
    private boolean inhibitPut;

    @PCFParam(12)
    private QueueUsage usage;

    private List<String> connectedChannels = new ArrayList<>();

    public enum QueueType {
        //本地队列、模板队列、别名队列、远程队列、集群队列
        Local(1), Model(2), Alias(3), Remote(6), Cluster(7);
        private final int value;

        QueueType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    public enum QueueTypeQueue {
        //本地队列 //远程队列
        Local(1), Remote(6);
        private final int value;

        QueueTypeQueue(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    public enum QueueUsage {
        //普通队列  传输队列
        Normal(0), Transmission(1);
        private final int value;

        QueueUsage(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}



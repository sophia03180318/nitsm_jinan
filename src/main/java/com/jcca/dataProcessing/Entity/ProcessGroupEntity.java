package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 中心采集到的进程处理
 * @author: Lvyp
 * @create: 2023/11/21 18:00
 */
@Data
public class ProcessGroupEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ProcessAlarmQueueEntity> queueObj;


}
